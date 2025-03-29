package com.example.backend.models;


import lombok.Data;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "users")
@Data // This automatically generates getters, setters, equals, hashCode, and toString

public class User {
    @Id
    private String id;
    private String username;
    private String passwordHash; // Store password hash
    private Long studentNumber; // Automatically generated student number
    private String fullName; // New field for personal information
    private LocalDate dateOfBirth; // New field for personal information
    private String address; // New field for personal information

    // Constructors
    public User() {}

    public User(String username, String passwordHash, Long studentNumber, String fullName, LocalDate dateOfBirth, String address) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.studentNumber = studentNumber;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    // Getters et Setters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Long getStudentNumber() {
        return studentNumber;
    }

}
