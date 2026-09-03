package com.example.games_extractor.service;

import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
//Servis za ucitavanje promptova iz prompts foldera
@Service
public class PromptService {

    public String loadPrompt(String fileName) throws Exception {

        ClassPathResource resource =
                new ClassPathResource("prompts/" + fileName);

        return new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
    }
}