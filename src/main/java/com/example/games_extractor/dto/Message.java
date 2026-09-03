package com.example.games_extractor.dto;
//DTO klasa koja predstavlja poruku koja se salje LLM-u 
public class Message {
	private String role;
	private String content;

	public Message(String role, String content) {
		this.role = role;
		this.content = content;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
