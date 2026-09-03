package com.example.games_extractor.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.games_extractor.dto.UserAuthenticationDetails;

public class SecurityUtil {

    // Vraca podatke o trenutno prijavljenom korisniku
    public static UserAuthenticationDetails getCurrentUserDetails() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return (UserAuthenticationDetails) authentication.getDetails();
    }
    
    public static String getCurrentTenant() {

        return getCurrentUserDetails().getTenant();
    }

    // Vraca ID trenutno prijavljenog tenanta
    public static Long getCurrentTenantId() {

        return getCurrentUserDetails().getTenantId();
    }

    // Vraca ID trenutno prijavljenog korisnika
    public static Long getCurrentUserId() {

        return getCurrentUserDetails().getUserId();
    }
    
    
}