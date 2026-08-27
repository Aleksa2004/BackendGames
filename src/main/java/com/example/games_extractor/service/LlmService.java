package com.example.games_extractor.service;

import java.net.http.HttpClient;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.games_extractor.dto.GroqRequest;
import com.example.games_extractor.dto.Message;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
/**
 * Servis za komunikaciju sa LLM modelom preko Groq API-ja.
 *
 * Ucitava sistemski prompt, dodaje korisnicko pitanje i relevantni
 * kontekst, salje zahtev modelu i vraca generisani odgovor.
 */
@Service
public class LlmService {
	
	
	private final HttpClient httpClient;
	private final ObjectMapper objMapper;
	private final PromptService promptService;
	
	
	@Value("${groq.api.key}")
	private String apiKey;
	
	@Value("${groq.api.url}")
	private String apiUrl;
	
	public LlmService(HttpClient httpClient,ObjectMapper objMapper,PromptService promptService) {
		this.httpClient=httpClient;
		this.objMapper=objMapper;
		this.promptService=promptService;
	}

	//Salje korisnicko pitanje i relevantni kontekst LLM modelu
	//i vraca generisani odgovor.
	public String userQuestion(String userQuestion,List<String> chunks) throws Exception {
		String systemPrompt = promptService.loadPrompt("rag-answer.txt");
		
		Message systemMessage = new Message("system", systemPrompt);
		
		
		String context = String.join("\n\n", chunks);
		
		
		Message userMessage  = new Message(
				"user",
				"Question:\n" + userQuestion +
				"\n\nContext:\n" + context);
		
		
		GroqRequest request = new GroqRequest(
				"openai/gpt-oss-120b",
				List.of(systemMessage,userMessage),
				0.3
				
		);
		
		String jsonBody = objMapper.writeValueAsString(request);
		
		
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(URI.create(apiUrl))
				.header("Authorization","Bearer " + apiKey)
				.header("Content-Type","application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.build();
		
		HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		
		JsonNode root = objMapper.readTree(response.body());
		
		String answer = root
				.path("choices")
				.path(0)
				.path("message")
				.path("content")
				.asString();
		
		
		return answer;
	}
	
}
