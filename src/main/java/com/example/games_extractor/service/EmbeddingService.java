package com.example.games_extractor.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class EmbeddingService {
	//HttpClient,HttpRequest,HttpResponse ,ugradjene klase u Javi preko koji komuniciramo sa API-em
	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final ObjectMapper mapper = new ObjectMapper();
	
	
	@Value("${ollama.api.url}")
	private String apiUrl;
	
	@Value("${ollama.model}")
	private String model;
	
	
	public double[] getEmbedding(String text) throws Exception{
		//saljemo u requestu model jer ollama mora znati koji joj saljemo,nije dovoljno input chunka
		Map<String,String> body = Map.of(
			"model",model,
			"input",text
			);
		
		String jsonBody = mapper.writeValueAsString(body);
		
		HttpRequest request = HttpRequest.newBuilder()
		        .uri(URI.create(apiUrl))
		        .header("Content-Type", "application/json")
		        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
		        .build();
	
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		
		if(response.statusCode() !=200) {
			throw new RuntimeException("Greska : " + response.statusCode() + " - " + response.body());
		}
		//JSON koji smo dobili od API
		JsonNode responseJson = mapper.readTree(response.body());
		
		JsonNode embeddingNode = responseJson.get("embeddings").get(0);

		return mapper.treeToValue(embeddingNode, double[].class);

	}
	
}
