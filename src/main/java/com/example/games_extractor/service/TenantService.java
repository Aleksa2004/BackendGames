package com.example.games_extractor.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.games_extractor.model.Tenant;
import com.example.games_extractor.repository.TenantRepository;
//Service klasa koja pravi tenant-e
@Service
public class TenantService {

	private final TenantRepository tenantRepository;

	public TenantService(TenantRepository tenantRepository) {
		super();
		this.tenantRepository = tenantRepository;
	}

	public Tenant createTenant(String name) {

		Tenant tenant = new Tenant();

		tenant.setName(name);
		tenant.setActive(true);
		tenant.setCreatedAt(LocalDateTime.now());

		return tenantRepository.save(tenant);

	}
}
