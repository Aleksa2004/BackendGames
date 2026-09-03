package com.example.games_extractor.dto;
//DTO klasa koja predstavlja podatke potrebne za prijavu korisnika
public class LoginRequest {
	private String username;
	private String password;

	public LoginRequest() {
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}