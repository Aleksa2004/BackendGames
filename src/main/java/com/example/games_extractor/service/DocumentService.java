package com.example.games_extractor.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.games_extractor.config.SecurityUtil;
import com.example.games_extractor.model.EmbeddingDoc;
import com.example.games_extractor.model.EmbeddingStatus;
import com.example.games_extractor.model.Game;
import com.example.games_extractor.model.Tenant;
import com.example.games_extractor.repository.EmbeddingDocRepository;
import com.example.games_extractor.repository.GameRepository;
import com.example.games_extractor.repository.TenantRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Servis za ucitavanje i obradu dokumenata.
 *
 * Dokumente organizovane po tenantima ucitava iz dataset foldera, provjerava da
 * li su već obradjeni, dijeli ih na chunkove i prosledjuje ih na embedding i
 * cuvanje u vektorsku bazu.
 *
 * Za game dataset dodatno cuva podatke o igrama u SQL bazi.
 */
@Service
public class DocumentService {

	private final ChunkingService chunkingService;
	private final QdrantService qdrantService;
	private final EmbeddingDocRepository embeddingDocRepository;
	private final GameRepository gameRepository;
	private final TenantRepository tenantRepository;
	private final SteamService steamService;

	private final ObjectMapper objectMapper;

	private static final String DATASETS_DIR = "E:/Backend/datasets";

	@Value("${ollama.model}")
	private String embeddingModel;

	public DocumentService(ChunkingService chunkingService, QdrantService qdrantService,
			EmbeddingDocRepository embeddingDocRepository, GameRepository gameRepository,
			TenantRepository tenantRepository, ObjectMapper objectMapper, SteamService steamService) {
		this.chunkingService = chunkingService;
		this.qdrantService = qdrantService;
		this.embeddingDocRepository = embeddingDocRepository;
		this.gameRepository = gameRepository;
		this.tenantRepository = tenantRepository;
		this.objectMapper = objectMapper;
		this.steamService = steamService;
	}

	private String readPdf(Path filePath) throws Exception {

		try (PDDocument document = Loader.loadPDF(filePath.toFile())) {

			PDFTextStripper stripper = new PDFTextStripper();

			return stripper.getText(document);
		}
	}

	// Glavna metoda,ucitava dokumente svih tenanta i pokrece njihovu obradu
	public void loadAndChunkDocuments() throws Exception {

		Path root = Paths.get(DATASETS_DIR);

		if (!Files.exists(root) || !Files.isDirectory(root)) {
			throw new IllegalStateException("Datasets folder ne postoji: " + DATASETS_DIR);
		}

		try (Stream<Path> tenants = Files.list(root)) {

			tenants.filter(Files::isDirectory).forEach(tenantPath -> {

				String tenant = tenantPath.getFileName().toString();

				processTenant(tenantPath, tenant);
			});
		}

		System.out.println("\nUkupno napravljeno chunk-ova: " + chunkingService.getTotalnChunck());
	}

	// Obraduje sve datasete jednog tenanta
	private void processTenant(Path tenantPath, String tenant) {

		Tenant tenantEntity = tenantRepository.findByName(tenant)
				.orElseThrow(() -> new IllegalStateException("Tenant ne postoji u bazi: " + tenant));

		Long tenantId = tenantEntity.getId();

		String collectionName = tenant.toLowerCase();

		try {

			qdrantService.createCollectionIfNotExists(collectionName);

		} catch (Exception e) {

			throw new RuntimeException("Greska pri kreiranju Qdrant kolekcije: " + collectionName, e);
		}

		System.out.println("\n==============================");

		System.out.println("TENANT: " + tenant);

		System.out.println("TENANT ID: " + tenantId);

		System.out.println("==============================");

		try (Stream<Path> datasets = Files.list(tenantPath)) {

			datasets.filter(Files::isDirectory)
					.forEach(datasetPath -> processDataset(datasetPath, tenant, tenantId, collectionName));

		} catch (Exception e) {

			throw new RuntimeException(e);
		}
	}

	// Obradjuje podrzane dokumente unutar dataseta
	private void processDataset(Path datasetPath, String tenant, Long tenantId, String collectionName) {

		String dataset = datasetPath.getFileName().toString();

		System.out.println("\nDATASET: " + dataset);

		try (Stream<Path> files = Files.list(datasetPath)) {

			files.filter(Files::isRegularFile).filter(this::isSupportedFile)
					.forEach(filePath -> processFile(filePath, tenant, tenantId, dataset, collectionName));

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// Podrzavamo fajlove sa nastavcima .txt i .pdf
	private boolean isSupportedFile(Path path) {

		String fileName = path.getFileName().toString().toLowerCase();

		return fileName.endsWith(".txt") || fileName.endsWith(".pdf");
	}

	private String readDocument(Path filePath) throws Exception {

		String path = filePath.toString().toLowerCase();

		if (path.endsWith(".pdf")) {

			return readPdf(filePath);

		} else {

			return Files.readString(filePath, StandardCharsets.UTF_8);
		}
	}

	// Provjera da li je dokument vec uspjesno obradjen
	private boolean isAlreadyProcessed(String fileName, String tenant, String dataset) {

		return embeddingDocRepository.existsByFileNameAndEmbeddingModelAndTenantAndDatasetAndStatus(fileName,
				embeddingModel, tenant, dataset, EmbeddingStatus.COMPLETED);
	}

	// Obradjuje jedan dokument:cita sadrzaj,kreira chunkove,cuva podatke o igri i
	// opisuje chunkove
	// u vektorsku bazu
	private void processFile(Path filePath, String tenant, Long tenantId, String dataset, String collectionName) {

		String fileName = filePath.getFileName().toString();

		System.out.println("\nObrada fajla: " + fileName + " | Tenant: " + tenant + " | Dataset: " + dataset);

		try {

			if (isAlreadyProcessed(fileName, tenant, dataset)) {

				System.out.println("Fajl je vec obradjen, preskacem: " + fileName);

				return;
			}

			EmbeddingDoc embeddingDoc = createEmbeddingDoc(filePath, fileName, tenant, dataset);

			String text = readDocument(filePath);

			Long appId = null;

			if (dataset.toLowerCase().startsWith("games")) {
				Path metadataPath = filePath.resolveSibling(fileName.replace(".txt", ".metadata.json"));

				String metadataJson = Files.readString(metadataPath, StandardCharsets.UTF_8);

				JsonNode metadata = objectMapper.readTree(metadataJson);

				appId = metadata.get("app_id").asLong();

				saveGame(text, metadataPath, tenantId);
			}

			embeddingDoc.setCharacterCount(text.length());

			List<String> chunks = createChunks(text, dataset);

			saveChunksToQdrant(chunks, fileName, tenant, dataset, collectionName, appId);

			completeEmbeddingDoc(embeddingDoc, chunks.size());

			System.out.println("Fajl uspjesno obradjen: " + fileName + " | chunkova: " + chunks.size() + " | tenant: "
					+ tenant + " | dataset: " + dataset);

		} catch (Exception e) {

			System.err.println("Greska pri obradi fajla: " + fileName);

			e.printStackTrace();
		}
	}

	private EmbeddingDoc createEmbeddingDoc(Path filePath, String fileName, String tenant, String dataset)
			throws Exception {

		EmbeddingDoc embeddingDoc = new EmbeddingDoc();

		embeddingDoc.setFileName(fileName);

		embeddingDoc.setFilePath(filePath.toAbsolutePath().toString());

		embeddingDoc.setFileSize(Files.size(filePath));

		embeddingDoc.setChunkSize(600);

		embeddingDoc.setChunkCount(0);

		embeddingDoc.setEmbeddingModel(embeddingModel);

		embeddingDoc.setStatus(EmbeddingStatus.PROCESSING);

		embeddingDoc.setTenant(tenant);

		embeddingDoc.setDataset(dataset);

		return embeddingDocRepository.save(embeddingDoc);
	}

	private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
			DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
			DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
			DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH),
			DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.ENGLISH),
			DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
			DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH));

	private LocalDate parseDate(String dateText) {

		for (DateTimeFormatter formatter : DATE_FORMATS) {
			try {
				return LocalDate.parse(dateText, formatter);
			} catch (DateTimeParseException e) {
				// probaj sljedeci format
			}
		}

		throw new IllegalArgumentException("Nepoznat format datuma: " + dateText);
	}

	// Izvlaci vrijednost zadatog polja iz dokumenta
	private String extractField(String text, String field) {

		return text.lines().filter(line -> line.startsWith(field)).map(line -> line.substring(field.length()).trim())
				.findFirst().orElse("");
	}

	private void saveGame(String text, Path metadataPath, Long tenantId) throws Exception {

		String title = extractField(text, "Title:");

		String metadataJson = Files.readString(metadataPath, StandardCharsets.UTF_8);

		JsonNode metadata = objectMapper.readTree(metadataJson);

		Long appId = metadata.get("app_id").asLong();

		Optional<Game> existingGame = gameRepository.findByAppIdAndTenantId(appId, tenantId);

		if (existingGame.isPresent()) {
			System.out.println("Igra vec postoji, preskacem: " + title + " | APP ID: " + appId);
			return;
		}

		Game game = new Game();

		game.setTitle(title);

		game.setAppId(appId);

		game.setImageUrl(steamService.getGameImageUrl(appId));

		game.setPrice(metadata.get("price").decimalValue());

		game.setReleaseDate(parseDate(metadata.get("release_date").asString()));

		game.setDescription(extractField(text, "Description:"));

		JsonNode genres = metadata.get("genres");

		game.setCategory(genres != null && genres.isArray() ? StreamSupport.stream(genres.spliterator(), false)
				.map(JsonNode::asString).collect(Collectors.joining(", ")) : "");

		game.setTenantId(tenantId);

		gameRepository.save(game);
	}
	private List<String> createChunks(String text, String dataset) {

		List<String> chunks = chunkingService.chunkTxt(text);

		if (!dataset.toLowerCase().startsWith("games")) {
			return chunks;
		}

		String gameTitle = text.lines().findFirst().orElse("").replace("Title: ", "").trim();

		return chunks.stream()
				.map(chunk -> chunk.startsWith("Title:") ? chunk : "Game Title: " + gameTitle + "\n\n" + chunk)
				.toList();
	}


	private void saveChunksToQdrant(List<String> chunks, String fileName, String tenant, String dataset,
			String collectionName, Long appId) throws Exception {

		for (int i = 0; i < chunks.size(); i++) {

			qdrantService.saveChunk(fileName, i + 1, chunks.get(i), tenant, dataset, collectionName, appId);
		}
	}

	private void completeEmbeddingDoc(EmbeddingDoc embeddingDoc, int chunkCount) {

		embeddingDoc.setChunkCount(chunkCount);

		embeddingDoc.setStatus(EmbeddingStatus.COMPLETED);

		embeddingDocRepository.save(embeddingDoc);
	}

	public void clearCurrentTenantData() throws Exception {

		String tenant = SecurityUtil.getCurrentTenant();

		qdrantService.clearCurrentTenantPoints();

		embeddingDocRepository.deleteByTenant(tenant);

		System.out.println("Podaci tenanta " + tenant + " su uspjesno obrisani");
	}

}
