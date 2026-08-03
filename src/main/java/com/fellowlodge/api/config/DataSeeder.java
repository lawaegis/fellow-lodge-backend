package com.fellowlodge.api.config;

import com.fellowlodge.api.entity.User;
import com.fellowlodge.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Creates the default admin/receptionist/accountant accounts on first run.
 * These are the only accounts seeded into a fresh database; every other piece
 * of data (rooms, menu, events, content, settings) is added by the staff
 * through the application. Accounts mirror the legacy desktop seeds so
 * credentials remain unchanged: Admin / Admin@123. Seeded accounts are flagged
 * to require a password change on first login (except under the test profile).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    public static final UUID ROLE_ADMIN = UUID.fromString("a1000000-0000-4000-8000-000000000001");
    public static final UUID ROLE_RECEPTIONIST = UUID.fromString("a1000000-0000-4000-8000-000000000002");
    public static final UUID ROLE_ACCOUNTANT = UUID.fromString("a1000000-0000-4000-8000-000000000003");
    public static final UUID ROLE_GUEST = UUID.fromString("a1000000-0000-4000-8000-000000000004");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        log.info("Seeding default user accounts...");
        createUser("b1000000-0000-4000-8000-000000000001", "Admin", "admin@fellowlodge.com",
                "System Administrator", ROLE_ADMIN, "Admin@123");
        createUser("b1000000-0000-4000-8000-000000000002", "Reception", "reception@fellowlodge.com",
                "Front Desk Officer", ROLE_RECEPTIONIST, "Reception@123");
        createUser("b1000000-0000-4000-8000-000000000003", "Accountant", "accounts@fellowlodge.com",
                "Chief Accountant", ROLE_ACCOUNTANT, "Account@123");
    }

    private void createUser(String id, String username, String email, String fullName, UUID roleId, String rawPassword) {
        User user = new User();
        user.setId(UUID.fromString(id));
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRoleId(roleId);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.setMustChangePassword(!environment.acceptsProfiles(Profiles.of("test")));
        userRepository.save(user);
    }
}
