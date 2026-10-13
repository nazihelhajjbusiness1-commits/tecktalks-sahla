package com.farmmanagement.backend.auth;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Brute-force protection for login (DT market-readiness). Tracks recent failed
 * attempts per account key and temporarily locks after too many failures.
 *
 * In-memory and per-instance — adequate for the MVP / single backend instance.
 * For a horizontally scaled deployment this should move to a shared store
 * (e.g. Redis) so limits hold across instances.
 */
@Component
public class LoginRateLimiter {

    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);
    static final Duration LOCKOUT = Duration.ofMinutes(15);

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    private static final class Attempts {
        int count;
        Instant windowStart = Instant.now();
        Instant lockedUntil;
    }

    public boolean isLocked(String key) {
        Attempts a = attempts.get(normalize(key));
        if (a == null) {
            return false;
        }
        synchronized (a) {
            return a.lockedUntil != null && Instant.now().isBefore(a.lockedUntil);
        }
    }

    /** Seconds until the lock expires, for the 429 Retry-After header (0 if unlocked). */
    public long secondsUntilUnlock(String key) {
        Attempts a = attempts.get(normalize(key));
        if (a == null) {
            return 0;
        }
        synchronized (a) {
            if (a.lockedUntil == null) {
                return 0;
            }
            long seconds = Duration.between(Instant.now(), a.lockedUntil).getSeconds();
            return Math.max(seconds, 0);
        }
    }

    public void recordFailure(String key) {
        Attempts a = attempts.computeIfAbsent(normalize(key), k -> new Attempts());
        synchronized (a) {
            Instant now = Instant.now();
            if (now.isAfter(a.windowStart.plus(WINDOW))) {
                a.count = 0;
                a.windowStart = now;
                a.lockedUntil = null;
            }
            a.count++;
            if (a.count >= MAX_FAILURES) {
                a.lockedUntil = now.plus(LOCKOUT);
            }
        }
    }

    public void reset(String key) {
        attempts.remove(normalize(key));
    }

    private static String normalize(String key) {
        return key == null ? "" : key.trim().toLowerCase();
    }
}
