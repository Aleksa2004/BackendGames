package com.example.games_extractor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.games_extractor.model.Tenant;

public interface TenantRepository
        extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByName(String name);
}