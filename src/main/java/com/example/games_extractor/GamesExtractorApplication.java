package com.example.games_extractor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.games_extractor.service.GameExtractorService;

@SpringBootApplication
public class GamesExtractorApplication implements CommandLineRunner {

    private final GameExtractorService gameExtractorService;

    public GamesExtractorApplication(GameExtractorService gameExtractorService) {
        this.gameExtractorService = gameExtractorService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("Nedostaje komanda. Primer upotrebe: extract");
            return;
        }

        String command = args[0];
        //odlucujemo preko terminala koji servis da pokrenemo
        switch (command) {
            case "extract" -> gameExtractorService.extractAndSaveGames();
            default -> System.out.println("Nepoznata komanda: " + command);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(GamesExtractorApplication.class, args);
    }
}