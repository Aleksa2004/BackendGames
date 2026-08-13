package com.example.games_extractor.service;

import java.net.http.HttpClient;

import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

@Service
public class EmbeddingService {
	
	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final ObjectMapper mapper = new ObjectMapper();
	
	
	
	
	
}
