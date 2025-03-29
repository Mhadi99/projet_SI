package com.example.backend.controllers;


import com.example.backend.dto.RegisterRequest;
import com.example.backend.security.JwtUtil;
import com.example.backend.services.AuthService;
import com.example.backend.services.TokenBlacklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:8080", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    private final JwtUtil jwtUtil; 


    private TokenBlacklistService tokenBlacklistService; 


    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil; 

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            if (!request.containsKey("studentNumber")) {
                return ResponseEntity.badRequest().body("Student number is required");
            }

            Long studentNumber = Long.parseLong(request.get("studentNumber"));

            if (!request.containsKey("password")) {
                return ResponseEntity.badRequest().body("Password is required");
            }

            String password = request.get("password");

            Optional<String> token = authService.login(studentNumber, password);
            if (token.isPresent()) {
                return ResponseEntity.ok(Map.of("token", token.get()));
            } else {
                return ResponseEntity.status(403).body("Accès refusé, numéro étudiant ou mot de passe incorrect");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid student number format");
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            Map<String, Object> response = authService.register(request);
            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "token", response.get("token"),
                    "studentNumber", response.get("studentNumber")
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

    }

    @PutMapping("/reset-password/{studentNumber}")
    public ResponseEntity<?> resetPassword(@PathVariable Long studentNumber, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        if (newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "New password is required"));
        }

        String updatedPasswordHash = authService.resetPassword(studentNumber, newPassword);

        if (updatedPasswordHash != null) {
            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successfully",
                    "passwordHash", updatedPasswordHash
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
    }

    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Date expiration = jwtUtil.extractExpiration(token);

        authService.addToBlacklist(token, expiration);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<?> unregister(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        boolean isUnregistered = authService.unregister(token);

        if (isUnregistered) {
            return ResponseEntity.ok(Map.of("message", "User unregistered successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader;
            if (authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "Token invalide ou expiré"));
            }
            
            String username = jwtUtil.extractUsername(token);
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("studentNumber", user.getStudentNumber());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("dateOfBirth", user.getDateOfBirth());
                userInfo.put("address", user.getAddress());
                
                return ResponseEntity.ok(userInfo);
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "Utilisateur non trouvé"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur serveur: " + e.getMessage()));
        }
    }

}
