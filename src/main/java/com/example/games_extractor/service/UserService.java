package com.example.games_extractor.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.games_extractor.model.Tenant;
import com.example.games_extractor.model.User;
import com.example.games_extractor.repository.TenantRepository;
import com.example.games_extractor.repository.UserRepository;

//Service klasa koja pravi user-e za tenante
@Service
public class UserService {
	
	private final UserRepository userRepository;
	private final TenantRepository tenantRepositoty;
	private final PasswordEncoder passwordEncoder;
	public UserService(UserRepository userRepository, TenantRepository tenantRepositoty,
			PasswordEncoder passwordEncoder) {
		super();
		this.userRepository = userRepository;
		this.tenantRepositoty = tenantRepositoty;
		this.passwordEncoder = passwordEncoder;
	}
	
	public User createUser(String username,String password,Long tenantId) {
		
		Tenant tenant = tenantRepositoty.findById(tenantId).orElseThrow(() -> new RuntimeException("Tenant not found"));
		
		
		User user = new User();
		
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode(password));
		user.setCreatedAt(LocalDateTime.now());
		user.setTenant(tenant);
		
		
		return userRepository.save(user);
		
	}
}
