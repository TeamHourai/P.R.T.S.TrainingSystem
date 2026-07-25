package com.hourai.prts.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    @Test
    void generatedTokenContainsExpectedIdentityAndRole() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);

        String token = provider.generateToken(12L, "doctor", true);

        assertTrue(provider.validateToken(token));
        assertEquals(12L, provider.getUserIdFromToken(token));
        assertEquals("doctor", provider.getUsernameFromToken(token));
        assertTrue(provider.isAdminFromToken(token));
    }

    @Test
    void modifiedPayloadIsRejectedBecauseSignatureNoLongerMatches() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);
        String token = provider.generateToken(12L, "doctor", false);
        String[] parts = token.split("\\.");
        char replacement = parts[1].charAt(0) == 'A' ? 'B' : 'A';
        parts[1] = replacement + parts[1].substring(1);

        assertFalse(provider.validateToken(String.join(".", parts)));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, -1);

        String token = provider.generateToken(12L, "doctor", false);

        assertFalse(provider.validateToken(token));
    }
}
