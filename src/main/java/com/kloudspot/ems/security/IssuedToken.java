package com.kloudspot.ems.security;

import java.time.Instant;

/** A freshly issued JWT together with its expiry time. */
public record IssuedToken(String token, Instant expiresAt) {
}
