package com.example.backend.security;

import com.example.backend.services.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    private TokenBlacklistService tokenBlacklistService; // Service to check blacklisted tokens

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(getSecretKey()) // Use the secure key
                .compact();
    }

    // Validate the token
    public boolean validateToken(String token) {

        if (tokenBlacklistService.isBlacklisted(token)) {
            return false; // Token is blacklisted
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey()) // Verify the token's signature
                    .build()
                    .parseClaimsJws(token); // Parse the token
            return true; // Token is valid
        } catch (Exception e) {
            return false; // Token is invalid or expired
        }
    }

    // Extract the username (subject) from the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract the expiration date from the token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generic method to extract a claim from the token
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey()) // Verify the token's signature
                .build()
                .parseClaimsJws(token) // Parse the token
                .getBody(); // Get the claims
    }
}