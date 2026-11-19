package com.example.games_extractor.dto;

import java.util.List;

public class ChatResponse {

    private String answer;

    private List<GameResponse> games;

    public ChatResponse(
            String answer,
            List<GameResponse> games
    ) {
        this.answer = answer;
        this.games = games;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<GameResponse> getGames() {
        return games;
    }

    public void setGames(List<GameResponse> games) {
        this.games = games;
    }
}