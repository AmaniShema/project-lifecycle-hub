package com.amani.projecthub.security;

import com.amani.projecthub.entity.User;
import com.amani.projecthub.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    private final UserRepository userRepository;

    public AuthUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Extract the currently authenticated user from the Security context.
    // The email was embedded in the JWT token when it was issued.
    // We use it to look up the full User entity from the database.

    public User getCurrentUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found: " + email));
    }
}