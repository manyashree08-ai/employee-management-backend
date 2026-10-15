package com.kloudspot.ems.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String TEST_SECRET = "01234567890123456789012345678901"; // 32 characters

    @Test
    @DisplayName("issues and validates a token correctly")
    void issueAndValidateToken() {
        JwtService service = new JwtService(TEST_SECRET, 60);
        IssuedToken issued = service.issue("admin");

        assertNotNull(issued);
        assertNotNull(issued.token());
        assertNotNull(issued.expiresAt());

        Optional<String> username = service.validateAndGetUsername(issued.token());
        assertTrue(username.isPresent());
        assertEquals("admin", username.get());
    }

    @Test
    @DisplayName("rejects invalid or tampered tokens")
    void rejectInvalidToken() {
        JwtService service = new JwtService(TEST_SECRET, 60);

        Optional<String> result1 = service.validateAndGetUsername("not-a-valid-token");
        assertTrue(result1.isEmpty());

        Optional<String> result2 = service.validateAndGetUsername("");
        assertTrue(result2.isEmpty());
    }

    @Test
    @DisplayName("rejects secret keys that are shorter than 32 bytes")
    void rejectShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtService("short-secret", 60));
    }
}
