package com.example.games_extractor.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.games_extractor.config.SecurityUtil;
import com.example.games_extractor.dto.SearchResult;
import com.example.games_extractor.model.Game;
import com.example.games_extractor.repository.GameRepository;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.Common.PointId;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.WithPayloadSelector;

import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;

import static io.qdrant.client.VectorsFactory.vectors;

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
	
	public void saveChunk(String fileName,int chunkNumber,String chunk,String tenant,String dataset,String collectionName,Long appId) throws Exception{
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
		
		payload.put("appId",
		        Value.newBuilder()
		                .setIntegerValue(appId)
		                .build());
		
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
	public List<SearchResult> search(String query) throws Exception {

	    String tenant = SecurityUtil.getCurrentTenant();
	    Long tenantId = SecurityUtil.getCurrentTenantId();


	    double[] embedding = embeddingService.getEmbedding(query);

	    List<Float> vector = new ArrayList<>();

	    for (double value : embedding) {
	        vector.add((float) value);
	    }

	    List<ScoredPoint> points = qdrantClient.searchAsync(
	            SearchPoints.newBuilder()
	                    .setCollectionName(tenant.toLowerCase())
	                    .addAllVector(vector)
	                    .setLimit(5)
	                    .setWithPayload(
	                            WithPayloadSelector.newBuilder()
	                                    .setEnable(true)
	                                    .build()
	                    )
	                    .build()
	    ).get();

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

	    List<String> reasons =
	            groqService.generateReasons(
	                    query,
	                    results
	            );

	    for (int i = 0; i < results.size(); i++) {

	        results.get(i)
	                .setReason(reasons.get(i));
	    }

	    return results;
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

}
