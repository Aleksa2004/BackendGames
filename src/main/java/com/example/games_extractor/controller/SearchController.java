package com.example.games_extractor.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.games_extractor.dto.SearchResult;
import com.example.games_extractor.service.QdrantService;

@RestController
public class SearchController {

    private final QdrantService qdrantService;

    public SearchController(QdrantService qdrantService) {
        this.qdrantService = qdrantService;
    }

    @GetMapping("/search")
    public List<SearchResult> search(
            @RequestParam String query) throws Exception {

        return qdrantService.search(query);
    }
}