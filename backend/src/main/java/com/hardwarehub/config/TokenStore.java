package com.hardwarehub.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Opaque in-memory tokens — a deliberate shortcut over JWT, documented in
 * the README. Tokens map to a user id only, so the filter (Block G4) always
 * re-reads the User from the database rather than trusting a cached copy,
 * meaning a role change takes effect on the very next request.
 *
 * Lost on restart, and doesn't scale past one instance — fine for a
 * single-instance demo, not for production.
 */
@Component
public class TokenStore {

    private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();

    public String issueToken(Long userId) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, userId);
        return token;
    }

    public Optional<Long> resolve(String token) {
        return Optional.ofNullable(tokenToUserId.get(token));
    }
}
