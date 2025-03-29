package com.example.backend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "token_blacklist")
public class TokenBlacklist {
    @Id
    private String id;
    private String token;
    private Date expiration;

    // Constructors, getters, and setters
    public TokenBlacklist() {}

    public TokenBlacklist(String token, Date expiration) {
        this.token = token;
        this.expiration = expiration;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }
}