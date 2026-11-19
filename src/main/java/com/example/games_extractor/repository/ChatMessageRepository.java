package com.example.games_extractor.repository;

import com.example.games_extractor.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByTimestampAsc(Long sessionId);
}