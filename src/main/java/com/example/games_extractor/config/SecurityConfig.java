package com.example.games_extractor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;


//Konfigurise Spring Security, JWT autentifikaciju, sesije i CORS pravila aplikacije.
@Configuration
public class SecurityConfig {

	private final JwtFilter jwtFilter;

	public SecurityConfig(JwtFilter jwtFilter) {
		super();
		this.jwtFilter = jwtFilter;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// Glavna konfiguracija za http zahtjeve
	// Konfiguriše Spring Security: dozvoljava login bez autentifikacije,
	// dok svi ostali zahtjevi moraju imati validan JWT token.
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

	    http
	        .csrf(csrf -> csrf.disable())

	        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

	        .sessionManagement(session ->
	            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	        )

	        .authorizeHttpRequests(auth ->
	            auth
	                .requestMatchers("/auth/login").permitAll()
	                .anyRequest().authenticated()
	        )

	        .addFilterBefore(
	            jwtFilter,
	            UsernamePasswordAuthenticationFilter.class
	        );

	    return http.build();
	}
	// Konfigurise CORS pravila koja odredjuju sa kojih adresa frontend moze pristupiti backend-u.
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

	    CorsConfiguration configuration = new CorsConfiguration();

	    configuration.setAllowedOrigins(
	        List.of("http://localhost:5173")
	    );

	    configuration.setAllowedMethods(
	        List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
	    );

	    configuration.setAllowedHeaders(
	        List.of("Authorization", "Content-Type")
	    );

	    configuration.setAllowCredentials(true);

	    UrlBasedCorsConfigurationSource source =
	        new UrlBasedCorsConfigurationSource();

	    source.registerCorsConfiguration(
	        "/**",
	        configuration
	    );

	    return source;
	}
	// AuthenticationManager provjerava username i password korisnika prilikom login-a.
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

		return configuration.getAuthenticationManager();
	}
}
