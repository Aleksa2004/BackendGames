package com.example.games_extractor.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.games_extractor.model.Game;
import com.example.games_extractor.repository.GameRepository;
import com.example.games_extractor.config.SecurityUtil;

@Service
public class GameService {
	
	private final GameRepository gameRepository;
	private final SteamService steamService;

	public GameService(
	        GameRepository gameRepository,SteamService steamService) {

	    this.gameRepository = gameRepository;
	    this.steamService=steamService;
	}
	// Vraca igre za trenutno prijavljenog tenanta
    public List<Game> getGamesForCurrentTenant() {

        Long tenantId =
                SecurityUtil.getCurrentTenantId();

        return gameRepository.findByTenantId(tenantId);
    }
    
    // Dopunjava URL slike za igre koje ga nemaju koristeci Steam API
  	public void updateMissingImageUrls(Long tenantId) throws Exception {

  	    List<Game> games =
  	            gameRepository.findByImageUrlIsNullAndTenantId(tenantId);

  	    for (Game game : games) {

  	        String imageUrl =
  	                steamService.getGameImageUrl(game.getAppId());

  	        if (imageUrl != null) {
  	            game.setImageUrl(imageUrl);
  	            gameRepository.save(game);

  	            System.out.println(
  	                    "Slika dodata: "
  	                    + game.getTitle()
  	                    + " | APP ID: "
  	                    + game.getAppId()
  	            );
  	        }
  	    }
  	}
    
}