package com.example.games_extractor.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Service;


import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;



@Service
public class SteamService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    //Vraca URL slike igre
    public String getGameImageUrl(Long appId) throws Exception {

        String defaultUrl =
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/"
                + appId
                + "/header.jpg";

        HttpRequest imageRequest = HttpRequest.newBuilder()
                .uri(URI.create(defaultUrl))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<Void> imageResponse =
                httpClient.send(
                        imageRequest,
                        HttpResponse.BodyHandlers.discarding()
                );

        if (imageResponse.statusCode() == 200) {
            return defaultUrl;
        }

        return getGameImageUrlFromApi(appId);
    }
    
    //Dohvata URL slike direktno iz Stream API-ja
    private String getGameImageUrlFromApi(Long appId) throws Exception {

        String url =
                "https://store.steampowered.com/api/appdetails?appids="
                + appId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {
            return null;
        }

        JsonNode root =
                objectMapper.readTree(response.body());

        JsonNode app =
                root.get(String.valueOf(appId));

        if (app == null ||
                !app.get("success").asBoolean()) {
            return null;
        }

        return app
                .get("data")
                .get("header_image")
                .asString();
    }

}