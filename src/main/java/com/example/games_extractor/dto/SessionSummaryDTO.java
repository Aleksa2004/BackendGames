package com.example.games_extractor.dto;

public class SessionSummaryDTO {

    private Long id;
    private String title;

    public SessionSummaryDTO(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}