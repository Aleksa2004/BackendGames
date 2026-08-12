package com.example.games_extractor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.games_extractor.service.GameExtractorService;

@SpringBootApplication
public class GamesExtractorApplication implements CommandLineRunner{
	
	private final GameExtractorService gameExtractorService;

    public GamesExtractorApplication(GameExtractorService gameExtractorService) {
        this.gameExtractorService = gameExtractorService;
    }
    //trenutna postavka testiranja bez API-a
    @Override
    public void run(String... args) throws Exception {
        gameExtractorService.extractAndSaveGames();
    }
	
	
	public static void main(String[] args) {
		SpringApplication.run(GamesExtractorApplication.class, args);
	}

}
