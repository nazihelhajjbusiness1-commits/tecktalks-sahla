package com.farmmanagement.backend.auth;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks JWT ids (jti) that have been explicitly invalidated via logout. Since
 * JWTs are stateless, this gives us server-side revocation for the token's
 * remaining lifetime.
 *
 * In-memory and per-instance — adequate for the single-instance MVP. A
 * horizontally scaled deployment should back this with a shared store (Redis)
 * so a logout holds across instances. Entries self-expire when the token would
 * have expired anyway, so the map stays bounded.
 */
@Component
public class TokenDenylist {

    private final Map<String, Instant> revoked = new ConcurrentHashMap<>();

    public void revoke(String jti, long ttlMillis) {
        if (jti == null) {
            return;
        }
        purgeExpired();
        revoked.put(jti, Instant.now().plusMillis(Math.max(ttlMillis, 0)));
    }

    public boolean isRevoked(String jti) {
        if (jti == null) {
            return false;
        }
        Instant until = revoked.get(jti);
        if (until == null) {
            return false;
        }
        if (Instant.now().isAfter(until)) {
            revoked.remove(jti);
            return false;
        }
        return true;
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        revoked.entrySet().removeIf(e -> now.isAfter(e.getValue()));
    }
}
