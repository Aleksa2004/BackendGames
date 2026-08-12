package com.example.games_extractor.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import org.springframework.stereotype.Service;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;

@Service
public class GameExtractorService {
	
	private static final String JSON_PATH = "E:\\games.json";
	private static final String OUTPUT_DIR = "E:\\Backend/game-extractor-output";
	//output folderi za .txt i metadata
	private static final String TXT_DIR = OUTPUT_DIR  + "\\documents";
	private static final String METADATA_DIR = OUTPUT_DIR + "\\metadata";
	
	
	private static final int GAMES_COUNT = 100;
	
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
		
		Files.createDirectories(Paths.get(TXT_DIR));
		Files.createDirectories(Paths.get(METADATA_DIR));
		
		clearDirectories();
		
		
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
                
                
                if (isValidGame(gameNode,appId)) {
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
	//provjera 4 bitna polja (price,required_age,date,categories)
	private boolean isValidGame(JsonNode game, String appId) {
	    if (!game.has("name") || game.get("name").asString().isBlank()) {
	        System.out.println("Odbaceno [" + appId + "]: nedostaje naziv (name)");
	        return false;
	    }
	    String opis = game.get("detailed_description").asString();
	    
	    if(opis.length()<600 || opis.length() >2500) {
	    	System.out.println("Odbaceno [" + appId + "]: dužina opisa=" + opis.length());
	        return false;
	    }

	    if (!game.has("detailed_description") || game.get("detailed_description").asString().isBlank()) {
	        System.out.println("Odbaceno [" + appId + "]: nedostaje opis (detailed_description)");
	        return false;
	    }
	    if (!hasValidPrice(game)) {
	        System.out.println("Odbaceno [" + appId + "]: nevalidna cena (price)");
	        return false;
	    }
	    if(!hasValidAge(game)) {
	    	System.out.println("Odbaceno [" + appId + "]: nevalidan uzrast (required_age)");
	    	return false;
	    }
	    if(!hasValidDate(game)) {
	    	System.out.println("Odbaceno [" + appId + "]: nevalidan datum izdanja (release_date)");
	    	return false;
	    }
	    if(!hasValidCategories(game)) {
	    	System.out.println("Odbaceno [" + appId + "]: nedostaju kategorije (categories)");
	    	return false;
	    	
	    }
	    if (!isEnglish(game.get("detailed_description").asString())) {
	        System.out.println("Odbaceno [" + appId + "]: opis nije na engleskom");
	        return false;
	    }
	    String name = game.get("name").asString();
	    if (!isEnglishName(name)) {
	        System.out.println("Odbaceno [" + appId + "]: naziv nije engleski");
	        return false;
	    }
	    
	    return true;
	}
	
	private boolean isEnglish(String text) {
		return detector.detectLanguageOf(text) == Language.ENGLISH;
				
	}
	private boolean isEnglishName(String name) {
		return name.matches("[a-zA-Z0-9\\s':,.!?()\\-]+");
	}
	//cisti sav tekst
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
	//cuvamo fajlove za svaku igru po jedan .txt i .metadata.json
	private void saveGame(JsonNode game,String appId) throws Exception{
		String name = game.get("name").asString();
		String cName = cleanName(name);
		String baseFileName = cName + "__" + appId;
		
		String description = cleanText(game.get("detailed_description").asString());
		
		String content = "Title: " + name + "\n\n" + "Description: " + description;
		Path path = Paths.get(TXT_DIR,baseFileName + ".txt");
		Files.writeString(path, content);
		
		Path metaPath = Paths.get(METADATA_DIR,baseFileName + ".metadata.json");
		String metadataJson = buildMetadata(game,appId);
		Files.writeString(metaPath, metadataJson);
		
		
	}
	private void deleteFiles(String dirPath) throws IOException{
		Path dir = Paths.get(dirPath);
		
		if(!Files.exists(dir)) {
			return;
		}
		
		try (DirectoryStream<Path> files = Files.newDirectoryStream(dir)) {
	        for (Path file : files) {
	            Files.deleteIfExists(file);
	        }
	    }
		System.out.println("Ociscen folder: " + dirPath);
	}
	
	private void clearDirectories() throws IOException{
		deleteFiles(TXT_DIR);
		deleteFiles(METADATA_DIR);
	}

	private String cleanName(String name) {
	    String cleaned = name.replaceAll("[\\\\/:*?\"<>|]", "").trim();
	    //zamijenimo razmake sa _
	    cleaned = cleaned.replaceAll("\\s", "_");
	    return cleaned;
	}
	private String buildMetadata(JsonNode game, String appId) {
        var metadata = mapper.createObjectNode();
    
        metadata.put("app_id", appId);
        putDate(metadata,"release_date",game,"release_date",appId);
        putDouble(metadata,"price",game,"price",appId);
        putInt(metadata,"required_age",game,"required_age",appId);
        putBooleanOrNull(metadata,"windows",game,"windows");
        putBooleanOrNull(metadata,"mac",game,"mac");
        putBooleanOrNull(metadata,"linux",game,"linux");
        //putIntOrNull(metadata,"metacritic_score",game,"metacritic_score");
        putStringOrNull(metadata,"estimated_owners",game,"estimated_owners");
        putIntOrNull(metadata,"recommendations",game,"recommendations");
        putIntOrNull(metadata,"average_playtime_forever",game,"average_playtime_forever");
        
        metadata.set("developers", game.has("developers") ? game.get("developers") : mapper.nullNode());
        metadata.set("publishers", game.has("publishers") ? game.get("publishers") : mapper.nullNode());
        metadata.set("genres", game.has("genres") ? game.get("genres") : mapper.nullNode());
        
        putArray(metadata,"categories",game,"categories",appId);
        
        
        
        //objekat u string
        return metadata.toPrettyString();
    }
	
	//Validacija JSON-a kroz svaku vrijednost
	private void putIntOrNull(ObjectNode target, String key, JsonNode source, String sourceField) {
	    if (!source.has(sourceField) || source.get(sourceField).isNull()) {
	        target.putNull(key);
	        return;
	    }
	    JsonNode node = source.get(sourceField);

	    if (node.isNumber()) {
	        target.put(key, node.asInt());
	        return;
	    }

	    if (node.isString()) {
	        try {
	            target.put(key, Integer.parseInt(node.asString().trim()));
	            return;
	        } catch (NumberFormatException e) {
	            
	        }
	    }
	    target.putNull(key);
	}
	private void putStringOrNull(ObjectNode target,String key,JsonNode source,String sourceField) {
		if(source.has(sourceField) && !source.get(sourceField).isNull()) {
			target.put(key , source.get(sourceField).asString());
		}else {
			target.putNull(key);
		}
	}
	
	private void putInt(ObjectNode target, String key, JsonNode source, String sourceField, String appId) {
	    if (!source.has(sourceField) || source.get(sourceField).isNull()) {
	        throw new IllegalStateException("appId " + appId + ": obavezno polje '" + sourceField + "' nedostaje!");
	    }

	    JsonNode node = source.get(sourceField);

	    if (node.isNumber()) {
	        target.put(key, node.asInt());
	        return;
	    }

	    if (node.isString()) {
	        try {
	            target.put(key, Integer.parseInt(node.asString().trim()));
	            return;
	        } catch (NumberFormatException e) {
	            throw new IllegalStateException("appId " + appId + ": polje '" + sourceField + "' ima nevalidan format broja: " + node.asString());
	        }
	    }

	    throw new IllegalStateException("appId " + appId + ": polje '" + sourceField + "' ima neočekivan tip: " + node);
	}
	private void putDouble(ObjectNode target,String key,JsonNode source,String sourceField,String appId) {
		if(!source.has(sourceField) || source.get(sourceField).isNull()) {
			 throw new IllegalStateException("appId " + appId + ": obavezno polje '" + sourceField + "' nedostaje, iako je prošlo validaciju!");
		}
		
		JsonNode node = source.get(sourceField);
		
		if(node.isNumber()) {
			target.put(key, node.asDouble());
			return;
		}
		
		if(node.isString()) {
			try {
				target.put(key, Double.parseDouble(node.asString().trim()));
				return;
			}catch(NumberFormatException e) {
				 throw new IllegalStateException("appId " + appId + ": polje '" + sourceField + "' ima nevalidan format: " + node.asString());
			}
		}
		throw new IllegalStateException("appId " + appId + ": polje '" + sourceField + "' ima neočekivan tip: " + node);
	}
	private void putBooleanOrNull(ObjectNode target, String key, JsonNode source, String sourceField) {
	    if (source.has(sourceField) && !source.get(sourceField).isNull()) {
	        target.put(key, source.get(sourceField).asBoolean());
	    } else {
	        target.putNull(key);
	    }
	}
	
	private static final java.util.List<DateTimeFormatter> DATE_FORMATS = java.util.List.of(
			java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy", java.util.Locale.ENGLISH), 
	        java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.ENGLISH), 
	        java.time.format.DateTimeFormatter.ofPattern("MMM yyyy", java.util.Locale.ENGLISH),      
	        java.time.format.DateTimeFormatter.ofPattern("d MMM, yyyy", java.util.Locale.ENGLISH),    
	        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd", java.util.Locale.ENGLISH),     
	        java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy", java.util.Locale.ENGLISH)      
	);
	
	private String safeValidDate(String rawDate) {
		if(rawDate == null || rawDate.isBlank()) {
			return null;
		}
		
		String trimmed = rawDate.trim();
		
		for(DateTimeFormatter format : DATE_FORMATS) {
			try {
				LocalDate.parse(trimmed,format);
				return trimmed;
			}catch(DateTimeParseException e) {
				
			}
		}
		System.out.println("Nevalidan format datuma, nijedan poznat format ne odgovara: '" + trimmed + "'");
		return null;
	}
	private void putDate(ObjectNode target, String key, JsonNode source, String sourceField, String appId) {
	    if (!source.has(sourceField) || source.get(sourceField).isNull()) {
	        throw new IllegalStateException("appId " + appId + ": obavezno polje '" + sourceField + "' nedostaje!");
	    }

	    String raw = source.get(sourceField).asString(null);
	    String valid = safeValidDate(raw);

	    if (valid != null) {
	        target.put(key, valid);
	    } else {
	        throw new IllegalStateException("appId " + appId + ": polje '" + sourceField + "' ima nevalidan format datuma: " + raw);
	    }
	}
	private boolean isValidNumberField(JsonNode game, String fieldName) {
	    if (!game.has(fieldName) || game.get(fieldName).isNull()) {
	        return false;
	    }

	    JsonNode node = game.get(fieldName);

	    if (node.isNumber()) {
	        return true;
	    }

	    if (node.isString()) {
	        String raw = node.asString().trim();
	        return raw.matches("\\d+(\\.\\d+)?");
	    }

	    return false;
	}
	
	private boolean hasValidPrice(JsonNode game) {
	    if (!isValidNumberField(game, "price")) {
	        return false;
	    }
	    return true;
	}
	private boolean hasValidAge(JsonNode game) {
		if (!isValidNumberField(game, "required_age")) {
	        return false;
	    }
		return true;
	}
	private boolean hasValidDate(JsonNode game) {
		if(!game.has("release_date") || game.get("release_date").isNull()) {
			return false;
		}
		
		String raw = game.get("release_date").asString();
		
		return safeValidDate(raw) !=null;
	}
	
	private boolean hasValidCategories(JsonNode game) {
		if(!game.has("categories") || game.get("categories").isNull()) {
			return false;
		}
		
		return game.get("categories").isArray()
				&& !game.get("categories").isEmpty();
	}
	//provjera za kategoriju
	private void putArray(ObjectNode target, String key, JsonNode source, String sourceField, String appId) {
	    if (!source.has(sourceField) || source.get(sourceField).isNull()) {
	        throw new IllegalStateException("appId " + appId + ": obavezno polje '" + sourceField + "' nedostaje!");
	    }

	    JsonNode node = source.get(sourceField);

	    if (!node.isArray() || node.isEmpty()) {
	        throw new IllegalStateException("appId " + appId + ": polje '" + sourceField + "' je prazan niz ili nije niz.");
	    }

	    target.set(key, node);
	}

	
}
