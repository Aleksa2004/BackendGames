package com.example.games_extractor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//gotova java klasa iz Qdrant biblioteke
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;

//Ova klasa omogucava da iz nase aplikacije pricamo sa Qdrantom
@Configuration
public class QdrantConfig {
	
	@Bean
	public QdrantClient qdrantClient() {
		QdrantGrpcClient grpcClient = QdrantGrpcClient.newBuilder("localhost",6334,false)
				.build();
		
		return new QdrantClient(grpcClient);
	}
	
}
