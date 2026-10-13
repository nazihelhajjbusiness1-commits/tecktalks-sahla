
package com.farmmanagement.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates JWTs. Short-lived access tokens carry the role and are
 * used on every request; longer-lived refresh tokens (type "refresh") can only
 * be exchanged for a new access token at /api/auth/refresh. Every token has a
 * unique id (jti) so individual tokens can be invalidated on logout.
 */
@Service
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private static final int MIN_SECRET_LENGTH = 32;

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${JWT_SECRET}") String secret,
            // Falls back to the legacy JWT_EXPIRATION, then to 15 minutes.
            @Value("${JWT_ACCESS_EXPIRATION:${JWT_EXPIRATION:900000}}") long accessExpiration,
            @Value("${JWT_REFRESH_EXPIRATION:604800000}") long refreshExpiration
    ) {
        if (secret == null || secret.strip().length() < MIN_SECRET_LENGTH
                || "change-me".equals(secret)) {
            throw new IllegalStateException(
                    "JWT_SECRET must be set to a strong value of at least " + MIN_SECRET_LENGTH
                            + " characters (generate one with: openssl rand -base64 48). "
                            + "The placeholder 'change-me' is not allowed.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /** Backward-compatible alias — issues an access token. */
    public String generateToken(String email, Role role) {
        return generateAccessToken(email, role);
    }

    public String generateAccessToken(String email, Role role) {
        return build(email, role, TYPE_ACCESS, accessExpiration);
    }

    public String generateRefreshToken(String email, Role role) {
        return build(email, role, TYPE_REFRESH, refreshExpiration);
    }

    private String build(String email, Role role, String type, long ttlMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ttlMillis);
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim("role", role.name())
                .claim("type", type)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public String extractJti(String token) {
        return extractAllClaims(token).getId();
    }

    /** Remaining lifetime in millis — used to bound how long a denylist entry is kept. */
    public long millisUntilExpiry(String token) {
        Date exp = extractAllClaims(token).getExpiration();
        return exp == null ? 0 : Math.max(0, exp.getTime() - System.currentTimeMillis());
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
