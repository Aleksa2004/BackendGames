package com.example.games_extractor.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.games_extractor.dto.SearchResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
// Servis za generisanje kratkih opisa igara pomocu Groq LLM-a
@Service
public class GroqService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final PromptService promptService;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;
    
    @Value("${groq.model}")
    private String model;
    
    public GroqService(PromptService promptService) {
    	this.promptService=promptService;
    }
    

    public List<String> generateReasons(
            String query,
            List<SearchResult> results) throws Exception {

        StringBuilder prompt = new StringBuilder(promptService.loadPrompt("game-reasons.txt"));

        
        for (int i = 0; i < results.size(); i++) {

            SearchResult result = results.get(i);

            prompt.append("\nGame ")
                  .append(i + 1)
                  .append(":\n");

            prompt.append(result.getChunk())
                  .append("\n");
        }

        String requestBody = mapper.writeValueAsString(
                new GroqRequest(
                        model,
                        List.of(
                                new Message(
                                        "user",
                                        prompt.toString()
                                )
                        )
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Groq greska: "
                    + response.statusCode()
                    + " - "
                    + response.body()
            );
        }

        JsonNode root = mapper.readTree(response.body());

        String content = root
                .get("choices")
                .get(0)
                .get("message")
                .get("content")
                .asString();

        JsonNode reasonsJson = mapper.readTree(content);

        List<String> reasons = new ArrayList<>();

        for (JsonNode reason : reasonsJson) {
            reasons.add(reason.asString());
        }

        return reasons;
    }
    //record sluzi da naparvimo klasu koja uglavnom se korisiti za drzanje podataka
    private record GroqRequest(
            String model,
            List<Message> messages
    ) {}

    private record Message(
            String role,
            String content
    ) {}
}