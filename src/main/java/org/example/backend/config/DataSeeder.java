package org.example.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.User;
import org.example.backend.enums.Role;
import org.example.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("No users found — seeding default users...");

            User admin = User.builder()
                    .username("admin")
                    .email("admin@mobai.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("System")
                    .lastName("Administrator")
                    .role(Role.ADMIN)
                    .active(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            User supervisor = User.builder()
                    .username("supervisor1")
                    .email("supervisor@mobai.com")
                    .password(passwordEncoder.encode("supervisor123"))
                    .firstName("John")
                    .lastName("Supervisor")
                    .role(Role.SUPERVISOR)
                    .active(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            User employee = User.builder()
                    .username("employee1")
                    .email("employee@mobai.com")
                    .password(passwordEncoder.encode("employee123"))
                    .firstName("Jane")
                    .lastName("Employee")
                    .role(Role.EMPLOYEE)
                    .active(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(admin);
            userRepository.save(supervisor);
            userRepository.save(employee);

            log.info("Default users seeded successfully (admin, supervisor1, employee1)");
        } else {
            log.info("Users already exist — resetting default passwords...");
            resetDefaultUserPassword("admin", "admin123");
            resetDefaultUserPassword("supervisor1", "supervisor123");
            resetDefaultUserPassword("employee1", "employee123");
        }
    }

    private void resetDefaultUserPassword(String username, String rawPassword) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(rawPassword));
                user.setActive(true);
                userRepository.save(user);
                log.info("Password reset for user: {}", username);
            }
        });
    }
}
