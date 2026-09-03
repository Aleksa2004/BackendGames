package com.example.games_extractor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.games_extractor.model.User;
import java.util.Optional;

public interface UserRepository  extends JpaRepository<User,Long>{
	
	Optional<User> findByUsername(String username);
}
