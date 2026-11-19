package com.example.games_extractor.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.games_extractor.dto.SearchFilters;
import com.example.games_extractor.dto.SearchResult;
import com.example.games_extractor.service.FilterService;
import com.example.games_extractor.service.QdrantService;

@RestController
public class SearchController {

    private final QdrantService qdrantService;
    private final FilterService filterService;

    public SearchController(QdrantService qdrantService,FilterService filterService) {
        this.qdrantService = qdrantService;
        this.filterService=filterService;
    }

    @GetMapping("/search")
    public List<SearchResult> search(
            @RequestParam String query) throws Exception {
    	
    	SearchFilters filters = filterService.extractFilters(query);
    	
    	System.out.println("========== FILTERS ==========");
    	System.out.println("Semantic query: " + filters.getSemanticQuery());
    	System.out.println("Price: " + filters.getPrice());
    	System.out.println("Required age: " + filters.getRequiredAge());
    	System.out.println("Windows: " + filters.getWindows());
    	System.out.println("Mac: " + filters.getMac());
    	System.out.println("Linux: " + filters.getLinux());
    	System.out.println("Genres: " + filters.getGenres());
    	System.out.println("Categories: " + filters.getCategories());
    	System.out.println("Developers: " + filters.getDevelopers());
    	System.out.println("Date: " + filters.getReleaseDate());
    	System.out.println("============================");

        return qdrantService.search(filters.getSemanticQuery(),filters);
    }
}