package com.example.games_extractor.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;

@Service
public class GameExtractorService {
	
	private static final String JSON_PATH = "E:\\games.json";
	private static final String OUTPUT_DIR = "E:\\Backend/game-extractor-output";
	private static final int GAMES_COUNT = 50;
	
	private final LanguageDetector detector =  LanguageDetectorBuilder
			.fromAllLanguages()
			.build();
	
	
	//mapiranje JSON u objekte/strukture
	private final ObjectMapper mapper = new ObjectMapper()
			.rebuild()
			.disable(tools.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
			.build();
	
	public void extractAndSaveGames() throws Exception{
		List<JsonNode> validGames = new ArrayList<>();
		
	
		JsonFactory factory = new JsonFactory();
		
		Files.createDirectories(Paths.get(OUTPUT_DIR));
		
		
		//JsonParser cita JSON token po token,da ne ucitavamo sve u memoriju
		try (JsonParser parser = factory.createParser(new File(JSON_PATH))) {

            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalStateException("Očekivan JSON objekat na početku fajla.");
            }

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String appId = parser.currentName();
                parser.nextToken(); // pomeri na vrijednost (objekat igrice)
                
                //uzmi sto parser trenutno cita i napravi Jnode stablo
                JsonNode gameNode = mapper.readTree(parser);
                
                
                if (isValidGame(gameNode)) {
                    validGames.add(gameNode);
                    saveGame(gameNode,appId);
                    
                    System.out.println(
                            "VALIDNA: " + gameNode.get("name").asString()
                        );
                }else {
                	System.out.println(
                	        "NIJE VALIDNA: " + gameNode.get("name").asString()
                	    );
                }

                if (validGames.size() >= GAMES_COUNT) {
                    break;
                }
            }
        }
		
		
		System.out.println("Ukupno sacuvano " + validGames.size() + " igrica.");
		
	}
	
	//JsonNode prima jedan JSON objekat 
	private boolean isValidGame(JsonNode game) {
		if(!game.has("name") || game.get("name").asString().isBlank()) {
			return false;
		}
		String nazivIgre = game.get("name").asString();
		
		
		if(!game.has("detailed_description") || game.get("detailed_description").asString().isBlank()) {
			System.out.println("  -> odbačeno: nema detailed_description [" + nazivIgre + "]");
			return false;
		}
		String opis = game.get("detailed_description").asString();
		
		if(opis.length()<200 || opis.length()>4000) {
			System.out.println("  -> odbačeno: dužina opisa=" + opis.length() + " [" + nazivIgre + "]");
			return false;
		}
		if(!game.has("price")) {
			System.out.println("  -> odbačeno: nema price [" + nazivIgre + "]");
			return false;
		}
		String cleanedOpis = cleanText(opis);
	    boolean engOpis = isEnglish(cleanedOpis);
	    boolean engName = isEnglishName(nazivIgre);
		
	    if(!engOpis) {
	        System.out.println("  -> odbačeno: opis nije prepoznat kao engleski [" + nazivIgre + "]");
	    }
	    if(!engName) {
	        System.out.println("  -> odbačeno: ime nije englesko [" + nazivIgre + "]");
	    }

	    return engOpis && engName;
	}
	
	private boolean isEnglish(String text) {
		return detector.detectLanguageOf(text) == Language.ENGLISH;
				
	}
	private boolean isEnglishName(String name) {
		return name.matches("[a-zA-Z0-9\\s':,.!?()\\-]+");
	}
	
	private String cleanText(String text) {
		if(text ==null) {
			return "";
		}
		text = text.replaceAll("<[^>]*>", " ");	
		text = text.replaceAll("&\\w+;", " ");
		text = text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
		text = text.replaceAll("\\s+", " ");
		
		return text.trim();
	}
	
	private void saveGame(JsonNode game,String appId) throws Exception{
		String name = game.get("name").asString();
		String cName = cleanName(name);
		String baseFileName = cName + "__" + appId;
		
		String description = cleanText(game.get("detailed_description").asString());
		
		String content = name + "\n\n" + description;
		Path path = Paths.get(OUTPUT_DIR,baseFileName + ".txt");
		Files.writeString(path, content);
		
		Path metaPath = Paths.get(OUTPUT_DIR,baseFileName + ".metadata.json");
		String metadataJson = buildMetadata(game,appId);
		Files.writeString(metaPath, metadataJson);
		
		
	}

	private String cleanName(String name) {
	    return name.replaceAll("[\\\\/:*?\"<>|]", "").trim();
	}
	private String buildMetadata(JsonNode game, String appId) {
        var metadata = mapper.createObjectNode();

        metadata.put("app_id", appId);
        metadata.put("release_date", game.path("release_date").asString(""));
        metadata.put("price", game.path("price").asDouble(0));
        metadata.put("required_age", game.path("required_age").asInt(0));
        metadata.put("windows", game.path("windows").asBoolean(false));
        metadata.put("mac", game.path("mac").asBoolean(false));
        metadata.put("linux", game.path("linux").asBoolean(false));
        metadata.put("metacritic_score", game.path("metacritic_score").asInt(0));
        metadata.put("estimated_owners", game.path("estimated_owners").asString(""));
        metadata.put("recommendations", game.path("recommendations").asInt(0));
        metadata.put("average_playtime_forever", game.path("average_playtime_forever").asInt(0));
        metadata.set("developers", game.path("developers"));
        metadata.set("publishers", game.path("publishers"));
        metadata.set("genres", game.path("genres"));
        metadata.set("categories", game.path("categories"));

        return metadata.toPrettyString();
    }
	
}
