package com.zeynep.librarymanagementsystem.repository;

import com.zeynep.librarymanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // For login/auth
    boolean existsByEmail(String email); // To check if already registered
}