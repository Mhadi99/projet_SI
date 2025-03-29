package com.example.backend.repositories;

import com.example.backend.models.TokenBlacklist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.Optional;

public interface TokenBlacklistRepository extends MongoRepository<TokenBlacklist, String> {
    // Check if a token is blacklisted
    Optional<TokenBlacklist> findByToken(String token);
    // Delete tokens with expiration before a specific date
    @Transactional
    long  deleteByExpirationBefore(Date expiration);
}
