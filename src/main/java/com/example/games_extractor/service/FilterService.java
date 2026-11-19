package com.example.games_extractor.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.games_extractor.dto.GroqRequest;
import com.example.games_extractor.dto.Message;
import com.example.games_extractor.dto.ResponseFormat;
import com.example.games_extractor.dto.SearchFilters;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class FilterService {

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final PromptService promptService;

	@Value("${groq.api.key}")
	private String apiKey;

	@Value("${groq.api.url}")
	private String apiUrl;

	@Value("${groq.model}")
	private String model;

	public FilterService(HttpClient httpClient, ObjectMapper objectMapper, PromptService promptService) {

		this.httpClient = httpClient;
		this.objectMapper = objectMapper;
		this.promptService = promptService;
	}

	public SearchFilters extractFilters(String query) throws Exception {

		String systemPrompt = promptService.loadPrompt("filter-extraction.txt");

		Message systemMessage = new Message("system", systemPrompt);

		Message userMessage = new Message("user", query);

		GroqRequest request = new GroqRequest(model, List.of(systemMessage, userMessage), 0.0,new ResponseFormat("json_object"));

		String jsonBody = objectMapper.writeValueAsString(request);

		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(apiUrl))
				.header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
		
		System.out.println("========== GROQ REQUEST ==========");
		System.out.println(jsonBody);
		System.out.println("==================================");

		HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Groq greska: " + response.statusCode() + " - " + response.body());
		}

		JsonNode root = objectMapper.readTree(response.body());

		String content = root.path("choices").path(0).path("message").path("content").asString();
		
		System.out.println("CONTENT: [" + content + "]");
		
		System.out.println("========== GROQ FILTER RESPONSE ==========");
		System.out.println(content);
		System.out.println("==========================================");
		
		//Groq vrati JSON ,a objectMapper ga direktno pretvori u SearchFilters
		return objectMapper.readValue(content, SearchFilters.class);
	}
}