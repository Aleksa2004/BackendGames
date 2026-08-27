package com.example.games_extractor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.games_extractor.model.Game;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
	List<Game> findByTenantId(Long tenantId);

	Optional<Game> findByAppIdAndTenantId(Long appId, Long tenantId);
	
	////Vraca igre odredjenog tenanta koje nemaju URL slike
	List<Game> findByImageUrlIsNullAndTenantId(Long tenantId);
    
}