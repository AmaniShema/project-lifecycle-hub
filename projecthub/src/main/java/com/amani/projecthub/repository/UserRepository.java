package com.amani.projecthub.repository;

import com.amani.projecthub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used during login to find user by email
    Optional<User> findByEmail(String email);

    // Used during registration to check if email is already taken
    boolean existsByEmail(String email);
}