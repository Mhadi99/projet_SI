package com.example.backend.services;

import com.example.backend.dto.RegisterRequest;
import com.example.backend.models.TokenBlacklist;
import com.example.backend.models.User;
import com.example.backend.repositories.TokenBlacklistRepository;
import com.example.backend.repositories.UserRepository;
import com.example.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }


    // Check if a username already exists
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // Check if a student number already exists
    public boolean studentExists(Long studentNumber) {
        return userRepository.findByStudentNumber(studentNumber).isPresent();
    }

    // Login: Validate credentials and generate a JWT token
    public Optional<String> login(Long studentNumber, String password) {
        Optional<User> userOpt = userRepository.findByStudentNumber(studentNumber);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                return Optional.of(jwtUtil.generateToken(user.getUsername()));
            }
        }
        return Optional.empty();
    }

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public Map<String, Object> register(RegisterRequest request) {
        logger.info("Registering user: {}", request.getUsername());

        // Check if the username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            logger.warn("User already exists: {}", request.getUsername());
           // throw new RuntimeException("Username already exists");
        }

        // Generate a unique student number
        Long studentNumber = generateUniqueStudentNumber();

        // Hash the password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create a new user with personal information
        User user = new User(
                request.getUsername(),
                hashedPassword,
                studentNumber,
                request.getFullName(),
                request.getDateOfBirth(),
                request.getAddress()
        );

        // Save the user
        userRepository.save(user);
        logger.info("User saved: {}, {}", user.getUsername(), user.getStudentNumber());

        // Generate a JWT token
        // Generate a JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        // Return both the token and the student number
        return Map.of(
                "token", token,
                "studentNumber", studentNumber
        );
    }

    private Long generateUniqueStudentNumber() {
        // Example: Use timestamp as a temporary solution
        return System.currentTimeMillis();
    }



    public String resetPassword(Long studentNumber, String newPassword) {
        // Find the user by student number
        Optional<User> userOpt = userRepository.findByStudentNumber(studentNumber);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Hash the new password
            String hashedPassword = passwordEncoder.encode(newPassword);
            logger.info("New password: {}", newPassword);
            logger.info("Hashed password: {}", hashedPassword);

            // Update the user's password hash
            user.setPasswordHash(hashedPassword);
            userRepository.save(user);

            // Return the updated password hash
            return user.getPasswordHash();
        }
        return null; // User not found
    }

    // Validate Token: Check if the token is valid and extract the username
    public Optional<String> validateToken(String token) {
        if (jwtUtil.validateToken(token)) {
            return Optional.of(jwtUtil.extractUsername(token));
        }
        return Optional.empty(); // Token is invalid
    }

    /*private User mapToUser(RegisterRequest request) {
        return new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()), // Hash password
                request.getStudent() // Ensure correct mapping to studentNumber
        );
    }
*/
    // Add a token to the blacklist
    public void addToBlacklist(String token, Date expiration) {
        TokenBlacklist blacklistedToken = new TokenBlacklist(token, expiration);
        tokenBlacklistRepository.save(blacklistedToken);
    }

    public boolean unregister(String token) {
        // Extract the username from the token
        String username = jwtUtil.extractUsername(token);

        // Delete the user from the database
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            userRepository.delete(userOpt.get());
            return true; // User deleted successfully
        }
        return false; // User not found
    }


}