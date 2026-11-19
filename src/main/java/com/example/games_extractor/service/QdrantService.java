package com.example.games_extractor.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.games_extractor.config.SecurityUtil;
import com.example.games_extractor.dto.SearchFilters;
import com.example.games_extractor.dto.SearchResult;
import com.example.games_extractor.model.Game;
import com.example.games_extractor.repository.GameRepository;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.Common.PointId;
import io.qdrant.client.grpc.JsonWithInt.ListValue;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.WithPayloadSelector;
import tools.jackson.databind.JsonNode;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;

import static io.qdrant.client.VectorsFactory.vectors;

import io.qdrant.client.grpc.Common.Range;

import static io.qdrant.client.ConditionFactory.match;
import static io.qdrant.client.ConditionFactory.matchKeywords;
import static io.qdrant.client.ConditionFactory.range;

/**
 * Servis za rad sa Qdrant vektorskom bazom.
 *
 * Kreira kolekcije i upisuje embeddinge dokumenata
 * zajedno sa pripadajucim metapodacima.
 */
@Service
public class QdrantService {
	//Klasa Qdrnat Jave biblioteke koja nam omogucuje da komuniciramo sa Qdrant vektoskom bazom
	private final QdrantClient qdrantClient;
	
	private final EmbeddingService embeddingService;
	private final GroqService groqService;
	private final GameRepository gameRepository;
	
	public QdrantService(QdrantClient qdrantClient, EmbeddingService embeddingService,GroqService groqService,GameRepository gameRepository) {
		this.qdrantClient=qdrantClient;
		this.embeddingService=embeddingService;
		this.groqService=groqService;
		this.gameRepository=gameRepository;
	}
	
	public void createCollectionIfNotExists(String collectionName)  throws Exception {

	    boolean exists =
	            qdrantClient
	                    .collectionExistsAsync(collectionName)
	                    .get();

	    if (exists) {
	        System.out.println(
	                "Qdrant kolekcija vec postoji: "
	                + collectionName
	        );
	        return;
	    }

	    VectorParams vectorParams =
	            VectorParams.newBuilder()
	                    .setSize(768)
	                    .setDistance(Distance.Cosine)
	                    .build();

	    qdrantClient
	            .createCollectionAsync(
	                    collectionName,
	                    vectorParams
	            )
	            .get();

	    System.out.println(
	            "Qdrant kolekcija uspjesno napravljena: "
	            + collectionName
	    );
	}
	
	public void saveChunk(String fileName,int chunkNumber,String chunk,String tenant,String dataset,String collectionName,Long appId,JsonNode metadata) throws Exception{
		double[] embedding = embeddingService.getEmbedding(chunk);
		
		List<Float> vector = new ArrayList<>();
		
		for(double value : embedding) {
			vector.add((float) value);
		}
		//Point-ov id
		String pointKey =
		        tenant + "|" +
		        dataset + "|" +
		        fileName + "|" +
		        chunkNumber;

		UUID uuid = UUID.nameUUIDFromBytes(
		        pointKey.getBytes(StandardCharsets.UTF_8)
		);

		PointId pointId = PointId.newBuilder()
		        .setUuid(uuid.toString())
		        .build();
		
		
		//payload = naziv za dodatne podatke u Qdrantu
		Map<String,Value> payload = new HashMap<>();
		
		payload.put("fileName",
					Value.newBuilder()
						 .setStringValue(fileName)
						 .build());
		
		payload.put("chunk",
					Value.newBuilder()
						 .setStringValue(chunk)
						 .build());
		
		payload.put("chunkNumber", 
				Value.newBuilder()
					  .setIntegerValue(chunkNumber)
					  .build());
		
		payload.put("tenant",
				Value.newBuilder()
					   .setStringValue(tenant)
					   .build());
		
		payload.put("dataset",
		        Value.newBuilder()
		                .setStringValue(dataset)
		                .build());
		if(appId!=null) {
			payload.put("appId",
			        Value.newBuilder()
			                .setIntegerValue(appId)
			                .build());
		}
		
		addMetadataPayload(payload, metadata);
		
		
		
		//5.Spojimo sve u jedan point (Qdrantov oblik)
		PointStruct point = PointStruct.newBuilder()
		        .setId(pointId)
		        .setVectors(vectors(vector))
		        .putAllPayload(payload)
		        .build();
		
		//Upisivanje Pointa u Qdrant
		qdrantClient.upsertAsync(
				collectionName,
				List.of(point)
		).get();
	}
	private void addMetadataPayload(
	        Map<String, Value> payload,
	        JsonNode metadata) {

	    if (metadata == null) {
	        return;
	    }

	    payload.put(
	            "price",
	            Value.newBuilder()
	                    .setDoubleValue(
	                            metadata.get("price").asDouble()
	                    )
	                    .build()
	    );

	    payload.put(
	            "required_age",
	            Value.newBuilder()
	                    .setIntegerValue(
	                            metadata.get("required_age").asLong()
	                    )
	                    .build()
	    );

	    payload.put(
	            "windows",
	            Value.newBuilder()
	                    .setBoolValue(
	                            metadata.get("windows").asBoolean()
	                    )
	                    .build()
	    );

	    payload.put(
	            "mac",
	            Value.newBuilder()
	                    .setBoolValue(
	                            metadata.get("mac").asBoolean()
	                    )
	                    .build()
	    );

	    payload.put(
	            "linux",
	            Value.newBuilder()
	                    .setBoolValue(
	                            metadata.get("linux").asBoolean()
	                    )
	                    .build()
	    );

	    String releaseDateString = metadata.get("release_date").asString();

	    LocalDate releaseDate = parseDate(releaseDateString);

	    if (releaseDate != null) {
	        long releaseDateValue =
	                Long.parseLong(releaseDate.format(
	                        DateTimeFormatter.BASIC_ISO_DATE
	                ));

	        payload.put(
	                "release_date",
	                Value.newBuilder()
	                        .setIntegerValue(releaseDateValue)
	                        .build()
	        );
	    }

	    addArrayPayload(
	            payload,
	            "genres",
	            metadata.get("genres")
	    );

	    addArrayPayload(
	            payload,
	            "categories",
	            metadata.get("categories")
	    );

	    addArrayPayload(
	            payload,
	            "developers",
	            metadata.get("developers")
	    );
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

	
	//Pomocna metoda za nizove
	private void addArrayPayload(
	        Map<String, Value> payload,
	        String field,
	        JsonNode array) {

	    if (array == null || !array.isArray()) {
	        return;
	    }

	    List<Value> values = new ArrayList<>();

	    for (JsonNode item : array) {
	        values.add(
	                Value.newBuilder()
	                        .setStringValue(item.asString())
	                        .build()
	        );
	    }

	    payload.put(
	            field,
	            Value.newBuilder()
	                    .setListValue(
	                            ListValue.newBuilder()
	                                    .addAllValues(values)
	                                    .build()
	                    )
	                    .build()
	    );
	}
	
	public void clearCurrentTenantPoints() throws Exception{
		
		String tenant = SecurityUtil.getCurrentTenant();
		
		String collectionName = tenant.toLowerCase();
		
		Filter filter = Filter.newBuilder()
				.build();
		
		qdrantClient.deleteAsync(
				collectionName,
				filter
		).get();
		
		System.out.println("Svi pointovi tenanta " + tenant + " su obrisani iz Qdranta");
	}
	
	// Pitanje korisnika
	public List<SearchResult> search(String query,SearchFilters filters) throws Exception {
		    System.out.println("========== SEARCH FILTERS ==========");
		    System.out.println("FILTERS OBJECT: " + filters);
		    System.out.println("WINDOWS: " + filters.getWindows());
		    System.out.println("MAC: " + filters.getMac());
		    System.out.println("LINUX: " + filters.getLinux());
		    System.out.println("GENRES: " + filters.getGenres());
		    System.out.println("CATEGORIES: " + filters.getCategories());
		    System.out.println("====================================");

		
		
	    String tenant = SecurityUtil.getCurrentTenant();
	    Long tenantId = SecurityUtil.getCurrentTenantId();


	    double[] embedding = embeddingService.getEmbedding(query);
	    
	    System.out.println("========== EMBEDDING ==========");
	    System.out.println("QUERY: " + query);
	    System.out.println("EMBEDDING NULL: " + (embedding == null));
	    System.out.println("EMBEDDING SIZE: " + (embedding != null ? embedding.length : 0));
	    System.out.println("===============================");

	    List<Float> vector = new ArrayList<>();

	    for (double value : embedding) {
	        vector.add((float) value);
	    }
	    System.out.println("WINDOWS FILTER: " + filters.getWindows());
	    System.out.println("BUILT FILTER: " + buildFilter(filters));
	    

	    List<ScoredPoint> points = qdrantClient.searchAsync(
	            SearchPoints.newBuilder()
	                    .setCollectionName(tenant.toLowerCase())
	                    .addAllVector(vector)
	                    .setLimit(5)
	                    .setFilter(buildFilter(filters))
	                    .setWithPayload(
	                            WithPayloadSelector.newBuilder()
	                                    .setEnable(true)
	                                    .build()
	                    )
	                    .build()
	    ).get();
	    
	    System.out.println("========== RAW QDRANT RESULTS ==========");

	    System.out.println("POINTS SIZE: " + points.size());

	    for (ScoredPoint point : points) {

	        Long appId =
	                point.getPayloadOrThrow("appId")
	                        .getIntegerValue();

	        System.out.println(
	                "APP ID: " + appId +
	                " | SCORE: " + point.getScore()
	        );
	        
	        System.out.println(
	        	    "GENRES: " + point.getPayloadOrThrow("genres")
	        	);

	        	System.out.println(
	        	    "CATEGORIES: " + point.getPayloadOrThrow("categories")
	        	);
	    }

	    System.out.println("========================================");

	    Map<Long, SearchResult> bestGames = new LinkedHashMap<>();

	    for (ScoredPoint result : points) {

	        if (result.getScore() <= 0.60f) {
	            continue;
	        }

	        String fileName =
	                result.getPayloadOrThrow("fileName")
	                        .getStringValue();

	        int chunkNumber =
	                (int) result.getPayloadOrThrow("chunkNumber")
	                        .getIntegerValue();

	        float score = result.getScore();

	        Long appId =
	                result.getPayloadOrThrow("appId")
	                        .getIntegerValue();

	        String chunk =
	                result.getPayloadOrThrow("chunk")
	                        .getStringValue();

	        Game game =
	                gameRepository.findByAppIdAndTenantId(
	                        appId,
	                        tenantId
	                ).orElse(null);

	        if (game == null) {
	            continue;
	        }

	        SearchResult searchResult =
	                new SearchResult(
	                        fileName,
	                        chunkNumber,
	                        chunk,
	                        score,
	                        null,
	                        game
	                );

	        SearchResult existing =
	                bestGames.get(appId);

	        if (existing == null ||
	                score > existing.getScore()) {

	            bestGames.put(appId, searchResult);
	        }
	    }

	    List<SearchResult> results =
	            new ArrayList<>(bestGames.values());
	    
	    if(results.isEmpty()) {
	    	return results;
	    }
	    
	    System.out.println("========== QDRANT RESULTS ==========");
	    System.out.println("RESULTS SIZE: " + results.size());
	    System.out.println("=====================================");

	    List<String> reasons =
	            groqService.generateReasons(
	                    query,
	                    results
	            );
	    
	    System.out.println("========== REASONS ==========");
	    System.out.println(reasons);
	    System.out.println("=============================");

	    for (int i = 0; i < results.size(); i++) {

	        results.get(i)
	                .setReason(reasons.get(i));
	    }

	    return results;
	}
	private Filter buildFilter(SearchFilters filters) {

	    Filter.Builder filter = Filter.newBuilder();

	    if (filters.getPrice() != null &&
	            (filters.getPrice().getMin() != null ||
	             filters.getPrice().getMax() != null)) {

	        Range.Builder rangeBuilder = Range.newBuilder();

	        if (filters.getPrice().getMin() != null) {
	            rangeBuilder.setGte(
	                    filters.getPrice().getMin().doubleValue()
	            );
	        }

	        if (filters.getPrice().getMax() != null) {
	            rangeBuilder.setLte(
	                    filters.getPrice().getMax().doubleValue()
	            );
	        }

	        filter.addMust(
	                range("price", rangeBuilder.build())
	        );
	    }

	    if (filters.getRequiredAge() != null &&
	            (filters.getRequiredAge().getMin() != null ||
	             filters.getRequiredAge().getMax() != null)) {

	        Range.Builder rangeBuilder = Range.newBuilder();

	        if (filters.getRequiredAge().getMin() != null) {
	            rangeBuilder.setGte(
	                    filters.getRequiredAge().getMin().doubleValue()
	            );
	        }

	        if (filters.getRequiredAge().getMax() != null) {
	            rangeBuilder.setLte(
	                    filters.getRequiredAge().getMax().doubleValue()
	            );
	        }

	        filter.addMust(
	                range("required_age", rangeBuilder.build())
	        );
	    }
	    if (filters.getReleaseDate() != null &&
	            (filters.getReleaseDate().getMin() != null ||
	             filters.getReleaseDate().getMax() != null)) {

	        Range.Builder rangeBuilder = Range.newBuilder();

	        if (filters.getReleaseDate().getMin() != null) {
	            rangeBuilder.setGte(
	                Integer.parseInt(
	                    filters.getReleaseDate()
	                           .getMin()
	                           .format(DateTimeFormatter.BASIC_ISO_DATE)
	                )
	            );
	        }

	        if (filters.getReleaseDate().getMax() != null) {
	            rangeBuilder.setLte(
	                Integer.parseInt(
	                    filters.getReleaseDate()
	                           .getMax()
	                           .format(DateTimeFormatter.BASIC_ISO_DATE)
	                )
	            );
	        }

	        filter.addMust(
	            range("release_date", rangeBuilder.build())
	        );
	    }
	    

	    if (filters.getWindows() != null) {
	        filter.addMust(
	                match("windows", filters.getWindows())
	        );
	    }

	    if (filters.getMac() != null) {
	        filter.addMust(
	                match("mac", filters.getMac())
	        );
	    }

	    if (filters.getLinux() != null) {
	        filter.addMust(
	                match("linux", filters.getLinux())
	        );
	    }

	    if (filters.getGenres() != null &&
	            !filters.getGenres().isEmpty()) {

	        filter.addMust(
	                matchKeywords(
	                        "genres",
	                        normalizeGenres(filters.getGenres())
	                )
	        );
	    }

	    if (filters.getCategories() != null &&
	            !filters.getCategories().isEmpty()) {

	        filter.addMust(
	                matchKeywords(
	                        "categories",
	                        filters.getCategories()
	                )
	        );
	    }

	    if (filters.getDevelopers() != null &&
	            !filters.getDevelopers().isEmpty()) {

	        filter.addMust(
	                matchKeywords(
	                        "developers",
	                        filters.getDevelopers()
	                )
	        );
	    }

	    return filter.build();
	}
	//Metoda namjenjena za testiranje similarity search-a
	public List<SearchResult> searchEvaluation(String query) throws Exception {

	    List<SearchResult> results = new ArrayList<>();

	    double[] embedding = embeddingService.getEmbedding(query);

	    List<Float> vector = new ArrayList<>();

	    for (double value : embedding) {
	        vector.add((float) value);
	    }

	    List<ScoredPoint> points = qdrantClient.searchAsync(
	            SearchPoints.newBuilder()
	                    .setCollectionName("games")
	                    .addAllVector(vector)
	                    .setLimit(5)
	                    .setWithPayload(
	                            WithPayloadSelector.newBuilder()
	                                    .setEnable(true)
	                                    .build()
	                    )
	                    .build()
	    ).get();

	    for (ScoredPoint result : points) {

	        if (result.getScore() <= 0.60f) {
	            continue;
	        }

	        String fileName =
	                result.getPayloadOrThrow("fileName")
	                        .getStringValue();

	        int chunkNumber =
	                (int) result.getPayloadOrThrow("chunkNumber")
	                        .getIntegerValue();

	        String chunk =
	                result.getPayloadOrThrow("chunk")
	                        .getStringValue();

	        float score = result.getScore();

	        results.add(
	                new SearchResult(
	                        fileName,
	                        chunkNumber,
	                        chunk,
	                        score,
	                        null,
	                        null
	                )
	        );
	    }

	    return results;
	}
	private List<String> normalizeGenres(List<String> genres) {

	    if (genres == null) {
	        return null;
	    }

	    return genres.stream()
	            .map(genre -> {

	                if (genre.equalsIgnoreCase("racing")) {
	                    return "Racing";
	                }

	                return genre;
	            })
	            .toList();
	}
}
