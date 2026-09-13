package com.farmmanagement.backend.auth;

import com.farmmanagement.backend.auth.dto.AuthResponse;
import com.farmmanagement.backend.auth.dto.LoginRequest;
import com.farmmanagement.backend.auth.dto.RegisterRequest;
import com.farmmanagement.backend.common.exception.AuthenticationException;
import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.users.User;
import com.farmmanagement.backend.users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;


import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();

        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.WAREHOUSE_EMPLOYEE);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new AuthenticationException("Invalid email or password")
                );

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new AuthenticationException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return new AuthResponse(token);
    }

    public User getCurrentUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );
    }


}