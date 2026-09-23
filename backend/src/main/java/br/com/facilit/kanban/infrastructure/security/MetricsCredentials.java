package br.com.facilit.kanban.infrastructure.security;

import java.nio.charset.StandardCharsets;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Credencial técnica usada apenas pelo coletor de métricas (Prometheus).
 *
 * <p>Sem senha configurada, nenhum usuário é criado e o endpoint de métricas
 * permanece fechado (fail closed). Não compartilha usuários com o login da aplicação.
 */
final class MetricsCredentials {

    static final String ROLE = "METRICS";
    static final int MIN_PASSWORD_LENGTH = 16;
    static final int MAX_PASSWORD_BYTES = 72;

    private MetricsCredentials() {
    }

    static UserDetailsService userDetailsService(
            String username,
            String password,
            PasswordEncoder passwordEncoder) {
        if (password == null || password.isBlank()) {
            return new InMemoryUserDetailsManager();
        }
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("app.metrics.username must not be blank");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                    "app.metrics.password must have at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new IllegalStateException(
                    "app.metrics.password must have at most " + MAX_PASSWORD_BYTES + " bytes");
        }
        return new InMemoryUserDetailsManager(User.withUsername(username.strip())
                .password(passwordEncoder.encode(password))
                .roles(ROLE)
                .build());
    }
}
