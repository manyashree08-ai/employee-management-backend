package com.kloudspot.ems.service;

import com.kloudspot.ems.dto.LoginRequest;
import com.kloudspot.ems.dto.LoginResponse;
import com.kloudspot.ems.dto.UserInfoResponse;
import com.kloudspot.ems.model.AppUser;
import com.kloudspot.ems.repository.UserRepository;
import com.kloudspot.ems.security.IssuedToken;
import com.kloudspot.ems.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    static final String INVALID_CREDENTIALS = "Invalid username or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // The same message is used for "unknown user" and "wrong password" so the API
        // does not reveal which usernames exist.
        AppUser user = userRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new BadCredentialsException(INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }

        IssuedToken issued = jwtService.issue(user.getUsername());
        return new LoginResponse(issued.token(), "Bearer", issued.expiresAt(),
                user.getUsername(), user.getFullName());
    }

    @Transactional(readOnly = true)
    public UserInfoResponse currentUser(String username) {
        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new BadCredentialsException("User no longer exists"));
        return new UserInfoResponse(user.getUsername(), user.getFullName());
    }
}
