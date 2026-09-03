package com.example.games_extractor.controller;

import java.util.List;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.games_extractor.model.Game;
import com.example.games_extractor.service.GameService;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;
    
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public List<Game> getGames() {
        return gameService.getGamesForCurrentTenant();
    }

}