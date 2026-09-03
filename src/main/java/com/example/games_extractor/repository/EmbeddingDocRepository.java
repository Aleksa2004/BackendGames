package com.example.games_extractor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.games_extractor.model.EmbeddingDoc;
import com.example.games_extractor.model.EmbeddingStatus;

public interface EmbeddingDocRepository extends JpaRepository<EmbeddingDoc, Long> {
	// Spring Data Jpa zna da generise sam na osnovu imena metode,omogucava rad sa
	// sql-om bez pisanja sql-a rucno
	
	
	//Provjerava da li je fajl vec uspjesno obradjen za dati tenant,dataset i embedding model
	boolean existsByFileNameAndEmbeddingModelAndTenantAndDatasetAndStatus(String fileName, String embeddingModel,
			String tenant, String dataset, EmbeddingStatus status);

	void deleteByTenant(String tenant);
}
