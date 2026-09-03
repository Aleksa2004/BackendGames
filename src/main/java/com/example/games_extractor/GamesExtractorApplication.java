package com.example.games_extractor;

import java.io.BufferedWriter;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.games_extractor.dto.EvaluationResult;
import com.example.games_extractor.dto.GeneratedQuestion;
import com.example.games_extractor.dto.RetrievalEvaluationReport;
import com.example.games_extractor.dto.RetrievalEvaluationResult;

import com.example.games_extractor.model.Tenant;
import com.example.games_extractor.model.User;
import com.example.games_extractor.service.ChunkingService;
import com.example.games_extractor.service.DocumentService;
import com.example.games_extractor.service.EvaluationService;
import com.example.games_extractor.service.GameExtractorService;
import com.example.games_extractor.service.RagService;
import com.example.games_extractor.service.TenantService;
import com.example.games_extractor.service.UserService;

import tools.jackson.databind.ObjectMapper;
//Glavna klasa Spring Boot aplikacije.
//Pokrece aplikaciju i omogucava izvrsavanje definisanih operacija
@SpringBootApplication
public class GamesExtractorApplication implements CommandLineRunner {

	private final GameExtractorService gameExtractorService;
	private final DocumentService documentService;
	private final RagService ragService;
	private final EvaluationService evaluationService;
	private final ChunkingService chunkingService;
	private final TenantService tenantService;
	private final UserService userService;


	private final ObjectMapper objectMapper;

	public GamesExtractorApplication(ObjectMapper objectMapper, GameExtractorService gameExtractorService,
			DocumentService documentService,RagService ragService, EvaluationService evaluationService, ChunkingService chunkingService,
			TenantService tenantService,UserService userService) {
		this.gameExtractorService = gameExtractorService;
		this.documentService = documentService;
		this.ragService = ragService;
		this.evaluationService = evaluationService;
		this.objectMapper = objectMapper;
		this.chunkingService = chunkingService;
		this.tenantService=tenantService;
		this.userService=userService;
	}
	@Override
	public void run(String... args) throws Exception {
		if (args.length == 0) {

			return;
		}

		String command = args[0];
		switch (command) {
		case "extract" -> gameExtractorService.extractAndSaveGames();
		case "ask" -> {

			String question = "Which game features monsters that become stronger by fighting each other, while the player must manage different character stats to survive?";

			String answer = ragService.ask(question);

			System.out.println(answer);

		}
		
		case "ingest" -> documentService.loadAndChunkDocuments();
		case "evaluation" -> {
			Path folder = Paths.get("E:/Backend/game-extractor-output/documents");
			Path evaluationFolder = Paths.get("E:/Backend/evaluation");

			Files.createDirectories(evaluationFolder);
			Path outputFile = evaluationFolder.resolve("evaluation_5.jsonl");
			try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {

				try (Stream<Path> paths = Files.list(folder)) {

					

					List<Path> files = paths.filter(path -> path.toString().endsWith(".txt"))
							.filter(path -> path.toString().endsWith(".txt")).limit(5).toList();
					
					long nextId = 1;
					for (Path file : files) {

						System.out.println("\n==============================");
						System.out.println("FAJL: " + file.getFileName());
						System.out.println("==============================");

						String gameText = Files.readString(file, StandardCharsets.UTF_8);

						List<String> chunks = chunkingService.chunkTxt(gameText);

						List<GeneratedQuestion> questions = evaluationService
								.generateQuestion(file.getFileName().toString(), chunks);

						try {
							Thread.sleep(12000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							throw new RuntimeException(e);
						}

						for (GeneratedQuestion question : questions) {

							question.setId(nextId++);

							// automatski napravi liniju json-a
							String jsonLine = objectMapper.writeValueAsString(question);
							writer.write(jsonLine);
							writer.newLine();
						}

					}

				}

			}
		}
		case "evaluation_result" -> {

		    Path evaluationFile = Paths.get(
		            "E:/Backend/evaluation/evaluation_5.jsonl"
		    );

		    Path outputFile = Paths.get(
		            "E:/Backend/evaluation/evaluation_result_5.jsonl"
		    );

		    try (
		        Stream<String> lines = Files.lines(
		                evaluationFile,
		                StandardCharsets.UTF_8
		        );
		        BufferedWriter writer = Files.newBufferedWriter(
		                outputFile,
		                StandardCharsets.UTF_8
		        )
		    ) {

		        lines
		            .filter(line -> !line.isBlank())
		            .forEach(line -> {

		                try {

		                    // 1. Učitamo evaluation pitanje
		                    GeneratedQuestion question =
		                            objectMapper.readValue(
		                                    line,
		                                    GeneratedQuestion.class
		                            );

		                    // 2. Pošaljemo query kroz naš RAG
		                    String ragAnswer =
		                            ragService.askEvaluation(
		                                    question.getQuery()
		                            );

		                    // Pauza nakon RAG poziva
		                    Thread.sleep(15000);

		                    // 3. Evaluiramo RAG odgovor
		                    EvaluationResult result =
		                            evaluationService.evaluate(
		                                    question,
		                                    ragAnswer
		                            );

		                    // 4. Pretvorimo rezultat u JSON
		                    String jsonLine =
		                            objectMapper.writeValueAsString(result);

		                    // 5. Jedan rezultat = jedan JSONL red
		                    writer.write(jsonLine);
		                    writer.newLine();

		                    // Pauza nakon evaluation poziva
		                    Thread.sleep(15000);

		                } catch (InterruptedException e) {

		                    Thread.currentThread().interrupt();
		                    throw new RuntimeException(e);

		                } catch (Exception e) {

		                    throw new RuntimeException(e);
		                }
		            });
		    }
		}
		case "retrieval" -> {

		    Path evaluationFile = Paths.get(
		            "E:/Backend/evaluation/evaluation_5.jsonl"
		    );

		    List<RetrievalEvaluationResult> results = new ArrayList<>();

		    try (Stream<String> lines = Files.lines(
		            evaluationFile,
		            StandardCharsets.UTF_8
		    )) {

		        lines
		            .filter(line -> !line.isBlank())
		            .limit(15)
		            .forEach(line -> {

		                try {

		                    GeneratedQuestion question =
		                            objectMapper.readValue(
		                                    line,
		                                    GeneratedQuestion.class
		                            );

		                    RetrievalEvaluationResult result =
		                            evaluationService.evaluateRetrieval(
		                                    question
		                            );

		                    results.add(result);

		                } catch (Exception e) {
		                    throw new RuntimeException(e);
		                }
		            });
		    }

		    RetrievalEvaluationReport report =
		            evaluationService.createRetrievalReport(results);

		    Path outputFile = Paths.get(
		            "E:/Backend/evaluation/retrieval_result_5.json"
		    );

		    String json =
		            objectMapper
		                    .writerWithDefaultPrettyPrinter()
		                    .writeValueAsString(report);

		    Files.writeString(
		            outputFile,
		            json,
		            StandardCharsets.UTF_8
		    );
		}
		case "clear" -> documentService.clearCurrentTenantData();
		case "tenant"->{
			
			Tenant tenant = tenantService.createTenant("Capcom");
			
		    System.out.println("Tenant created:");
		    System.out.println("ID: " + tenant.getId());
		    System.out.println("Name: " + tenant.getName());
		
		}
		
		case "user"->{
			
			
			User user = userService.createUser("Vuk", "987654321", 6L);
			
		    System.out.println("User created:");
		    System.out.println("ID: " + user.getId());
		    System.out.println("Username: " + user.getUsername());
		    System.out.println("Tenant ID: " + user.getTenant().getId());
			
		}

		default -> System.out.println("Nepoznata komanda: " + command);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(GamesExtractorApplication.class, args);
	}
}