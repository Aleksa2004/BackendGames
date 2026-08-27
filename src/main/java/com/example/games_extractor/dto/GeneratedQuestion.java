package com.example.games_extractor.dto;

import java.util.List;

//DTO klasa koja predstavlja pitanja generisano pomocu LLM-a za evaluaciju RAG sistema
public class GeneratedQuestion {

	private Long id;
	private String query;
	private Difficulty difficulty;
	private String expectedFile;
	private List<String> expectedChunks;
	private List<String> expectedSources;

	public GeneratedQuestion(Long id, String query, Difficulty difficulty, String expectedFile,
			List<String> expectedChunks, List<String> expectedSources) {
		super();
		this.id = id;
		this.query = query;
		this.difficulty = difficulty;
		this.expectedFile = expectedFile;
		this.expectedChunks = expectedChunks;
		this.expectedSources = expectedSources;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	public String getExpectedFile() {
		return expectedFile;
	}

	public void setExpectedFile(String expectedFile) {
		this.expectedFile = expectedFile;
	}

	public List<String> getExpectedChunks() {
		return expectedChunks;
	}

	public void setExpectedChunks(List<String> expectedChunks) {
		this.expectedChunks = expectedChunks;
	}

	public List<String> getExpectedSources() {
		return expectedSources;
	}

	public void setExpectedSources(List<String> expectedSources) {
		this.expectedSources = expectedSources;
	}

}
