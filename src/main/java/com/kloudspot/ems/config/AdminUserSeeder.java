package com.kloudspot.ems.config;

import com.kloudspot.ems.model.AppUser;
import com.kloudspot.ems.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the first administrator when the users table is empty.
 * Credentials come from APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD (see .env.example).
 * If the admin already exists, nothing is changed - so changing the env values later
 * does not overwrite an existing password.
 */
@Component
public class AdminUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminUserSeeder(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.username}") String username,
                           @Value("${app.admin.password}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        if (username == null || username.isBlank() || password == null || password.length() < 6) {
            throw new IllegalStateException(
                    "APP_ADMIN_USERNAME must be set and APP_ADMIN_PASSWORD must be at least 6 characters.");
        }
        userRepository.save(new AppUser(username.trim(), passwordEncoder.encode(password), "Administrator"));
        log.info("Created initial administrator account '{}'", username.trim());
    }
}
