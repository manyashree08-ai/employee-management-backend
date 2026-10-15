package com.kloudspot.ems.dto;

import java.time.Instant;

public record LoginResponse(
        String token,
        String tokenType,
        Instant expiresAt,
        String username,
        String fullName
) {
}
