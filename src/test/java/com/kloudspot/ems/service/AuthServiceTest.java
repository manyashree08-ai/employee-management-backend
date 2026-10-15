package com.kloudspot.ems.service;

import com.kloudspot.ems.dto.LoginRequest;
import com.kloudspot.ems.dto.LoginResponse;
import com.kloudspot.ems.dto.UserInfoResponse;
import com.kloudspot.ems.model.AppUser;
import com.kloudspot.ems.repository.UserRepository;
import com.kloudspot.ems.security.IssuedToken;
import com.kloudspot.ems.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("login: successful authentication returns token and user info")
    void login_success() {
        LoginRequest req = new LoginRequest("admin", "admin123");
        AppUser user = new AppUser("admin", "encodedHash", "Administrator");
        user.setId(1L);

        when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", "encodedHash")).thenReturn(true);
        Instant expires = Instant.now().plusSeconds(3600);
        when(jwtService.issue("admin")).thenReturn(new IssuedToken("jwt.token.here", expires));

        LoginResponse res = authService.login(req);

        assertNotNull(res);
        assertEquals("jwt.token.here", res.token());
        assertEquals("Bearer", res.tokenType());
        assertEquals("admin", res.username());
        assertEquals("Administrator", res.fullName());
        assertEquals(expires, res.expiresAt());
    }

    @Test
    @DisplayName("login: throws BadCredentialsException when username not found")
    void login_userNotFound_throws() {
        LoginRequest req = new LoginRequest("unknown", "secret");
        when(userRepository.findByUsernameIgnoreCase("unknown")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(req));
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).issue(any());
    }

    @Test
    @DisplayName("login: throws BadCredentialsException when password does not match")
    void login_wrongPassword_throws() {
        LoginRequest req = new LoginRequest("admin", "wrongpassword");
        AppUser user = new AppUser("admin", "encodedHash", "Administrator");

        when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedHash")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(req));
        verify(jwtService, never()).issue(any());
    }

    @Test
    @DisplayName("currentUser: returns current user profile when found")
    void currentUser_success() {
        AppUser user = new AppUser("admin", "encodedHash", "Administrator");
        when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));

        UserInfoResponse res = authService.currentUser("admin");

        assertNotNull(res);
        assertEquals("admin", res.username());
        assertEquals("Administrator", res.fullName());
    }

    @Test
    @DisplayName("currentUser: throws BadCredentialsException when user not found")
    void currentUser_notFound_throws() {
        when(userRepository.findByUsernameIgnoreCase("ghost")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.currentUser("ghost"));
    }
}
