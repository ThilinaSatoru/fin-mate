package lk.londontec.fin_mate.component;

import lk.londontec.fin_mate.entity.User;
import lk.londontec.fin_mate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2) // runs after DataSeeder (categories) if that's @Order(1), doesn't matter much either way here
public class AdminUserSeeder implements CommandLineRunner {

    private static final String TEST_EMAIL = "satoru@finmate.lk";
    private static final String TEST_PASSWORD = "admin123"; // dev/demo only — never a real credential
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(TEST_EMAIL)) {
            log.info("Test admin user already exists: {}", TEST_EMAIL);
            return;
        }

        User admin = User.builder()
                .email(TEST_EMAIL)
                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                .phoneNumber("0770000000")
                .build();

        userRepository.save(admin);
        log.info("Seeded test admin user -> email: {} / password: {}", TEST_EMAIL, TEST_PASSWORD);
    }
}