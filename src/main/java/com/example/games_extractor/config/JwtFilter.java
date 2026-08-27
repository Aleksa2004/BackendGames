package com.example.games_extractor.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.games_extractor.dto.UserAuthenticationDetails;
import com.example.games_extractor.model.Tenant;
import com.example.games_extractor.repository.TenantRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
//JWT filter provjerava token iz Authorization headera i postavlja korisnika u Spring Security context.
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TenantRepository tenantRepositoty;

    public JwtFilter(JwtUtil jwtUtil,TenantRepository tenantRepositoty) {
        this.jwtUtil = jwtUtil;
        this.tenantRepositoty=tenantRepositoty;
    }
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
    	System.out.println("JWT FILTER POZVAN");

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);
            
            System.out.println("TOKEN PRIMLJEN");
            System.out.println("TOKEN VALIDAN: " + jwtUtil.validateToken(token));

            if (jwtUtil.validateToken(token)) {

                String username = jwtUtil.extractUsername(token);
                Long userId = jwtUtil.extractUserId(token);
                Long tenantId = jwtUtil.extractTenantId(token);
                
                Tenant tenant = tenantRepositoty
                		.findById(tenantId)
                		.orElseThrow(() -> 
                				new IllegalStateException("Tenant nije pronadjen: " + tenantId));
                
                String tenantName = tenant.getName();
                
                System.out.println("JWT TENANT NAME: " + tenantName);
                		

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of()
                        );

                authentication.setDetails(
                        new UserAuthenticationDetails(userId, tenantId,tenantName)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
                
                System.out.println("JWT USER: " + username);
                System.out.println("JWT USER ID: " + userId);
                System.out.println("JWT TENANT ID: " + tenantId);
                System.out.println("AUTHENTICATED: " + authentication.isAuthenticated());
            }
        }

        filterChain.doFilter(request, response);
    }
}