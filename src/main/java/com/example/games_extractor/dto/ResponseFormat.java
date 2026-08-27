package com.example.games_extractor.dto;

//DTO klasa koja definise format odgovora koji ocekujemo od Groq API-a
public class ResponseFormat {
	private String type;

    public ResponseFormat(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
