package com.healthflow.auth.service;

import com.healthflow.auth.dto.RegisterRequest;
import com.healthflow.auth.dto.UserResponse;
import com.healthflow.auth.entity.AppUser;
import com.healthflow.auth.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An account with this email already exists"
            );
        }

        AppUser user = new AppUser(
            email,
            passwordEncoder.encode(request.password()),
            request.fullName().trim(),
            "PATIENT"
        );

        AppUser savedUser = userRepository.save(user);

        return new UserResponse(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFullName(),
            savedUser.getRole()
        );
    }
}