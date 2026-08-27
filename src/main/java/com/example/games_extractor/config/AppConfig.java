package com.example.games_extractor.config;

import java.net.http.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//Ova klasa kreira HttpClient koji se koristi za komunikaciju sa spoljnim API-em
@Configuration
public class AppConfig {
	
	
	@Bean 
	public HttpClient httpClient() {
		return HttpClient.newHttpClient();
	}
}
