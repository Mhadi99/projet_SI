package com.example.backend.repositories;


import com.example.backend.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    // Find a user by student number
    Optional<User> findByStudentNumber(Long studentNumber);


}
