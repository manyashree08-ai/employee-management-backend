package com.kloudspot.ems.controller;

import com.kloudspot.ems.dto.LoginRequest;
import com.kloudspot.ems.dto.LoginResponse;
import com.kloudspot.ems.dto.UserInfoResponse;
import com.kloudspot.ems.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and current-user APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Log in",
            description = "Returns a JWT. Send it on later requests as 'Authorization: Bearer <token>'.")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Get the currently logged-in user")
    @GetMapping("/me")
    public UserInfoResponse me(Authentication authentication) {
        return authService.currentUser(authentication.getName());
    }
}
