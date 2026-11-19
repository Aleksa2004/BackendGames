package com.example.games_extractor;




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

import com.example.games_extractor.dto.GeneratedQuestion;
import com.example.games_extractor.dto.RetrievalEvaluationReport;
import com.example.games_extractor.dto.RetrievalEvaluationResult;

import com.example.games_extractor.model.Tenant;
import com.example.games_extractor.model.User;
import com.example.games_extractor.service.DocumentService;
import com.example.games_extractor.service.EvaluationService;
import com.example.games_extractor.service.GameExtractorService;
import com.example.games_extractor.service.TenantService;
import com.example.games_extractor.service.UserService;

import tools.jackson.databind.ObjectMapper;
//Glavna klasa Spring Boot aplikacije.
//Pokrece aplikaciju i omogucava izvrsavanje definisanih operacija
@SpringBootApplication
public class GamesExtractorApplication implements CommandLineRunner {

	private final GameExtractorService gameExtractorService;
	private final DocumentService documentService;
	private final EvaluationService evaluationService;
	private final TenantService tenantService;
	private final UserService userService;


	private final ObjectMapper objectMapper;

	public GamesExtractorApplication(ObjectMapper objectMapper, GameExtractorService gameExtractorService,
			DocumentService documentService, EvaluationService evaluationService,
			TenantService tenantService,UserService userService) {
		this.gameExtractorService = gameExtractorService;
		this.documentService = documentService;
		this.evaluationService = evaluationService;
		this.objectMapper = objectMapper;
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
		case "ingest" -> documentService.loadAndChunkDocuments();
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