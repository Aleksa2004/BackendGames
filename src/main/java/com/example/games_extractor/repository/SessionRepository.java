package com.example.games_extractor.repository;

import com.example.games_extractor.model.Session;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByUserIdAndTenant(Long userId, String tenant);
    
    List<Session> findByUserIdOrderByUpdatedAtDesc(Long userId);
    
    Optional<Session> findByIdAndUserIdAndTenant(
            Long sessionId,
            Long userId,
            String tenant
    );
    
    List<Session> findByUserIdAndTenantOrderByUpdatedAtDesc(
            Long userId,
            String tenant
    );
}