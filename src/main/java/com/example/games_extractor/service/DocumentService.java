package com.example.games_extractor.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
//Ucitavanje .txt fajlova iz foldera,ucitava se sve odjednom,pitati mentora
@Service
public class DocumentService {
	
	private static final String OUTPUT_DIR_PATTERN = "file:E:/Backend/game-extractor-output/documents/*.txt";
	
	public Map<String,String> loadDocuments() throws IOException{
		Map<String,String> documents = new HashMap<>();
		
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources(OUTPUT_DIR_PATTERN);	
		
		for(Resource resource : resources) {
			String fName = resource.getFilename();
			String context = new String(resource.getInputStream().readAllBytes(),StandardCharsets.UTF_8);
			documents.put(fName, context);
			
		}
		
		return documents;
	}
}
