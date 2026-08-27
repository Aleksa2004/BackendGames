package com.example.games_extractor.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.games_extractor.dto.EvaluationGroqRequest;
import com.example.games_extractor.dto.EvaluationResult;
import com.example.games_extractor.dto.GeneratedQuestion;
import com.example.games_extractor.dto.Message;
import com.example.games_extractor.dto.ResponseFormat;
import com.example.games_extractor.dto.RetrievalEvaluationReport;
import com.example.games_extractor.dto.RetrievalEvaluationResult;
import com.example.games_extractor.dto.SearchResult;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
//Servis za generisanje evaluation pitanja i evaluaciju RAG sistema@Service
@Service
public class EvaluationService {

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	
	private final QdrantService qdrantService;
	private final PromptService promptService;

	@Value("${groq.api.key}")
	private String apiKey;

	@Value("${groq.api.url}")
	private String apiUrl;

	public EvaluationService(HttpClient httpClient, ObjectMapper objectMapper,QdrantService qdrantService,PromptService promptService) {
		this.httpClient = httpClient;
		this.objectMapper = objectMapper;
		this.qdrantService=qdrantService;
		this.promptService=promptService;
	}
	//Generisanje evaluation query-a
	public List<GeneratedQuestion> generateQuestion(String fileName, List<String> chunks) throws Exception {

		StringBuilder document = new StringBuilder();

		document.append("Game file: ").append(fileName).append("\n\n");

		for (int i = 0; i < chunks.size(); i++) {
			document.append("CHUNK: ").append(i + 1).append(":\n").append(chunks.get(i)).append("\n\n");
		}
		
		String systemPrompt = promptService.loadPrompt("question-generation.txt");
		
		
		//saljemo modelu kako da razmislja
		Message systemMessage = new Message("system", systemPrompt);
				
		//saljemo mu podatke
		Message userMessage = new Message("user", document.toString());

		EvaluationGroqRequest request = new EvaluationGroqRequest("openai/gpt-oss-120b",
				List.of(systemMessage, userMessage), 0.3, new ResponseFormat("json_object"));

		String jsonBody = objectMapper.writeValueAsString(request);

		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(apiUrl))
				.header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

		HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Groq error " + response.statusCode() + ": " + response.body());
		}
		JsonNode root = objectMapper.readTree(response.body());

		String content = root.path("choices").path(0).path("message").path("content").asString();

		System.out.println("\n========== RAW LLM RESPONSE ==========");
		System.out.println(content);
		System.out.println("======================================\n");
		//vrati u obliku objekta GeneratedQuestion
		return objectMapper.readValue(content,
				objectMapper.getTypeFactory().constructCollectionType(List.class, GeneratedQuestion.class));

	}
	//evaluacija odgovora
	public EvaluationResult evaluate(GeneratedQuestion question, String ragAnswer) throws Exception {
		
		String systemPrompt = promptService.loadPrompt("evaluation.txt");
		
	    Message systemMessage = new Message(
	            "system",
	           systemPrompt
	    );

	    Message userMessage = new Message(
	            "user",
	            "Question:\n"
	            + question.getQuery()

	            + "\n\nExpected source file:\n"
	            + question.getExpectedFile()

	            + "\n\nExpected chunk numbers:\n"
	            + question.getExpectedChunks()

	            + "\n\nExpected source passages:\n"
	            + question.getExpectedSources()

	            + "\n\nRAG answer:\n"
	            + ragAnswer
	    );

	    // 0.0 jer zelimo sto manje varijacije
	    EvaluationGroqRequest request =
	            new EvaluationGroqRequest(
	                    "openai/gpt-oss-120b",
	                    List.of(systemMessage, userMessage),
	                    0.0,
	                    new ResponseFormat("json_object")
	            );

	    String jsonBody =
	            objectMapper.writeValueAsString(request);

	    HttpRequest httpRequest =
	            HttpRequest.newBuilder()
	                    .uri(URI.create(apiUrl))
	                    .header("Authorization", "Bearer " + apiKey)
	                    .header("Content-Type", "application/json")
	                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
	                    .build();

	    HttpResponse<String> response =
	            httpClient.send(
	                    httpRequest,
	                    HttpResponse.BodyHandlers.ofString()
	            );

	    if (response.statusCode() != 200) {
	        throw new RuntimeException(
	                "Groq error "
	                + response.statusCode()
	                + ": "
	                + response.body()
	        );
	    }

	    JsonNode root =
	            objectMapper.readTree(response.body());

	    String content =
	            root.path("choices")
	                    .path(0)
	                    .path("message")
	                    .path("content")
	                    .asString();

	    return objectMapper.readValue(
	            content,
	            EvaluationResult.class
	    );
	}
	// Evaluira da li je ocekivani dokument i chunk pronadjen u rezultatima pretrage
	public RetrievalEvaluationResult evaluateRetrieval(
	        GeneratedQuestion question) throws Exception {

	    List<SearchResult> results =
	            qdrantService.searchEvaluation(question.getQuery());

	    // HIT5
	    boolean hit = results.stream().anyMatch(result ->
	            result.getFileName().equals(question.getExpectedFile())
	            && question.getExpectedChunks().contains(
	                    String.valueOf(result.getChunkNumber())
	            )
	    );

	    // HIT1
	    boolean hit1 = !results.isEmpty()
	            && results.get(0).getFileName().equals(question.getExpectedFile())
	            && question.getExpectedChunks().contains(
	                    String.valueOf(results.get(0).getChunkNumber())
	            );

	    // TOP SCORE
	    float topScore = results.isEmpty()
	            ? 0.0f
	            : results.get(0).getScore();

	    //Ispisivanje u terminalu
	    System.out.println("======================================");
	    System.out.println("QUESTION: " + question.getQuery());
	    System.out.println("EXPECTED FILE: " + question.getExpectedFile());
	    System.out.println(
	            "EXPECTED CHUNKS: " + question.getExpectedChunks()
	    );

	    for (int i = 0; i < results.size(); i++) {

	        SearchResult result = results.get(i);

	        System.out.println(
	                "RANK " + (i + 1)
	                + " | FILE: " + result.getFileName()
	                + " | CHUNK: " + result.getChunkNumber()
	                + " | SCORE: " + result.getScore()
	        );
	    }

	    System.out.println("HIT5: " + hit);
	    System.out.println("HIT1: " + hit1);
	    System.out.println("TOP SCORE: " + topScore);

	    return new RetrievalEvaluationResult(
	            question.getId(),
	            question.getQuery(),
	            hit,
	            hit1,
	            topScore
	    );
	}
	public RetrievalEvaluationReport createRetrievalReport(
	        List<RetrievalEvaluationResult> results) {

	    int totalExamples = results.size();

	    int hits = 0;
	    int top1Hits = 0;

	    double totalScore = 0.0;

	    List<Long> failures = new ArrayList<>();

	    for (RetrievalEvaluationResult result : results) {

	        if (result.isHit()) {
	            hits++;
	        } else {
	            failures.add(result.getId());
	        }

	        if (result.isTop1Hit()) {
	            top1Hits++;
	        }

	        totalScore += result.getTopScore();
	    }

	    double hitRate = totalExamples == 0
	            ? 0
	            : (double) hits / totalExamples;

	    double top1Accuracy = totalExamples == 0
	            ? 0
	            : (double) top1Hits / totalExamples;

	    double avgTopScore = totalExamples == 0
	            ? 0
	            : totalScore / totalExamples;

	    return new RetrievalEvaluationReport(
	            totalExamples,
	            hitRate,
	            top1Accuracy,
	            avgTopScore,
	            "games",
	            failures,
	            results);
	}
}
