package com.storypublisher.security;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    @Value("${app.jwt.secret:mySecretKey}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:86400000}")
    private int jwtExpirationMs;
    
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        // Temporary implementation - we'll fix JWT later
        return "Bearer-" + userPrincipal.getUsername() + "-" + UUID.randomUUID().toString();
    }
    
    public String generateTokenFromUsername(String username) {
        // Temporary implementation - we'll fix JWT later
        return "Bearer-" + username + "-" + UUID.randomUUID().toString();
    }
    
    public String getUsernameFromToken(String token) {
        try {
            // Temporary implementation - extract username from our temporary format
            if (token.startsWith("Bearer-")) {
                String[] parts = token.split("-");
                if (parts.length >= 2) {
                    return parts[1];
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Error parsing token", e);
            return null;
        }
    }
    
    public boolean validateToken(String authToken) {
        try {
            // Temporary implementation - basic validation
            return authToken != null && authToken.startsWith("Bearer-") && authToken.length() > 10;
        } catch (Exception ex) {
            logger.error("Invalid token");
        }
        return false;
    }
}
