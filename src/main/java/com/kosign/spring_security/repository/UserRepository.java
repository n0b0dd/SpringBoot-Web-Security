package com.kosign.spring_security.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kosign.spring_security.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
}
