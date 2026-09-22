package br.com.facilit.kanban.infrastructure.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SecurityAdminBootstrap implements ApplicationRunner {

    private final SecurityUserJpaRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final String email;
    private final String password;

    public SecurityAdminBootstrap(
            SecurityUserJpaRepository repository,
            PasswordEncoder passwordEncoder,
            Clock clock,
            @Value("${app.security.bootstrap-admin-email:}") String email,
            @Value("${app.security.bootstrap-admin-password:}") String password) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        boolean hasEmail = !email.isBlank();
        boolean hasPassword = !password.isBlank();
        if (!hasEmail && !hasPassword) {
            return;
        }
        if (!hasEmail || !hasPassword) {
            throw new IllegalStateException(
                    "Bootstrap administrator email and password must be configured together");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (repository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            return;
        }

        Instant now = clock.instant();
        repository.save(new SecurityUserJpaEntity(
                UUID.randomUUID(),
                normalizedEmail,
                passwordEncoder.encode(password),
                "ADMIN",
                true,
                now,
                now));
    }
}
