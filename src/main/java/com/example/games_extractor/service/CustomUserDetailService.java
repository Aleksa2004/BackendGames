package com.example.games_extractor.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import com.example.games_extractor.model.User;
import com.example.games_extractor.repository.UserRepository;
//Servis za pronalazenje korisnika koji koristi Spring Security autentifikaciju
@Service
public class CustomUserDetailService implements UserDetailsService{
	
	private final UserRepository userRepository;

	public CustomUserDetailService(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
		
		
		return org.springframework.security.core.userdetails.User
		        .withUsername(user.getUsername())
		        .password(user.getPassword())
		        .roles("USER")
		        .build();
		
	}
	
	
}
