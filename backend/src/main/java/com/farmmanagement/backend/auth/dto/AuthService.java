package com.farmmanagement.backend.auth;

import com.farmmanagement.backend.auth.dto.AuthResponse;
import com.farmmanagement.backend.auth.dto.LoginRequest;
import com.farmmanagement.backend.auth.dto.RegisterRequest;
import com.farmmanagement.backend.common.exception.AuthenticationException;
import com.farmmanagement.backend.common.exception.ConflictException;
import com.farmmanagement.backend.common.exception.RateLimitedException;
import com.farmmanagement.backend.users.User;
import com.farmmanagement.backend.users.UserRepository;
import com.farmmanagement.backend.users.UserStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.farmmanagement.backend.common.exception.ResourceNotFoundException;


import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginRateLimiter loginRateLimiter;
    private final TokenDenylist tokenDenylist;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LoginRateLimiter loginRateLimiter,
            TokenDenylist tokenDenylist
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginRateLimiter = loginRateLimiter;
        this.tokenDenylist = tokenDenylist;
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

        // Role selection is only reachable by an ADMIN (the endpoint is
        // ADMIN-gated); default to the least-privileged role when unspecified.
        user.setRole(request.getRole() != null ? request.getRole() : Role.WAREHOUSE_EMPLOYEE);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        String key = request.getEmail();

        if (loginRateLimiter.isLocked(key)) {
            throw new RateLimitedException(
                    "Too many failed login attempts. Please try again later.",
                    loginRateLimiter.secondsUntilUnlock(key));
        }

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        boolean passwordMatches = user != null && passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            // Record for both unknown-email and wrong-password to avoid enumeration.
            loginRateLimiter.recordFailure(key);
            // A failure may have just crossed the lockout threshold.
            if (loginRateLimiter.isLocked(key)) {
                throw new RateLimitedException(
                        "Too many failed login attempts. Please try again later.",
                        loginRateLimiter.secondsUntilUnlock(key));
            }
            throw new AuthenticationException("Invalid email or password");
        }

        loginRateLimiter.reset(key);

        if (user.getStatus() == UserStatus.DISABLED) {
            throw new AuthenticationException(
                    "Your account has been disabled. Contact an administrator.");
        }

        return issueTokens(user);
    }

    /** Exchange a valid refresh token for a fresh access token (keeps same refresh token). */
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null
                || !jwtService.isTokenValid(refreshToken)
                || !JwtService.TYPE_REFRESH.equals(jwtService.extractType(refreshToken))
                || tokenDenylist.isRevoked(jwtService.extractJti(refreshToken))) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        User user = userRepository
                .findByEmail(jwtService.extractEmail(refreshToken))
                .orElseThrow(() -> new AuthenticationException("Invalid or expired refresh token"));

        if (user.getStatus() == UserStatus.DISABLED) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        String access = jwtService.generateAccessToken(user.getEmail(), user.getRole());
        return new AuthResponse(access, refreshToken);
    }

    /** Invalidate a token (access or refresh) for its remaining lifetime. */
    public void logout(String token) {
        if (token != null && jwtService.isTokenValid(token)) {
            tokenDenylist.revoke(jwtService.extractJti(token), jwtService.millisUntilExpiry(token));
        }
    }

    private AuthResponse issueTokens(User user) {
        String access = jwtService.generateAccessToken(user.getEmail(), user.getRole());
        String refresh = jwtService.generateRefreshToken(user.getEmail(), user.getRole());
        return new AuthResponse(access, refresh);
    }

    public User getCurrentUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );
    }

    /** DT-84/Create-Account: list all users for the admin user-management screen. */
    public java.util.List<User> listUsers() {
        return userRepository.findAll(
                org.springframework.data.domain.Sort.by("createdAt").descending());
    }
}