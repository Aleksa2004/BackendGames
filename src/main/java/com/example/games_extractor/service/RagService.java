package com.example.games_extractor.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.games_extractor.dto.SearchResult;
/**
 * Servis koji povezuje retrieval i generisanje odgovora u RAG procesu.
 *
 * Pronalazi relevantne chunkove u Qdrantu i prosledjuje ih LLM modelu
 * zajedno sa korisnickim pitanjem.
 */
@Service
public class RagService {

	private final QdrantService qdrantService;
	private final LlmService llmService;

	public RagService(QdrantService qdrantService, LlmService llmService) {
		this.qdrantService = qdrantService;
		this.llmService = llmService;
	}
	//Obradjuje korisnicko pitanje kroz RAG proces.
	public String ask(String userQuestion) throws Exception {

		List<SearchResult> results = qdrantService.search(userQuestion);

		List<String> chunks = results.stream()

				.map(SearchResult::getChunk)

				.toList();


		System.out.println("=================== CHUNKS POSLANI LLM-u ================");

		for (String chunk : chunks) {

			System.out.println(chunk);

			System.out.println("----------------------------------");

		}

		return llmService.userQuestion(userQuestion, chunks);

	}
	
	//Obradjuje korisnicko pitanje kroz RAG proces za potrebe evaluacije. 
	public String askEvaluation(String userQuestion) throws Exception {

	    List<SearchResult> results =
	            qdrantService.searchEvaluation(userQuestion);

	    List<String> chunks = results.stream()
	            .map(SearchResult::getChunk)
	            .toList();

	    System.out.println(
	            "=================== CHUNKS POSLANI LLM-u ================="
	    );

	    for (String chunk : chunks) {
	        System.out.println(chunk);
	        System.out.println("----------------------------------");
	    }

	    return llmService.userQuestion(
	            userQuestion,
	            chunks
	    );
	}
}
