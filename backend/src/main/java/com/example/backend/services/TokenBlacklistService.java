package com.example.backend.services;

import com.example.backend.models.TokenBlacklist;
import com.example.backend.repositories.TokenBlacklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenBlacklistService {

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    // Add a token to the blacklist
    public void addToBlacklist(String token, Date expiration) {
        TokenBlacklist blacklistedToken = new TokenBlacklist(token, expiration);
        tokenBlacklistRepository.save(blacklistedToken);
    }

    // Check if a token is blacklisted
    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.findByToken(token).isPresent();
    }

    // Clean up expired tokens
    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 milliseconds)
    public void cleanupExpiredTokens() {
        long deletedCount = tokenBlacklistRepository.deleteByExpirationBefore(new Date());
        System.out.println("Expired tokens cleaned up at: " + new Date() + ". Deleted " + deletedCount + " tokens.");
    }
}