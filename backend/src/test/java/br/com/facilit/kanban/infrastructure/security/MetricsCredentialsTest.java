package br.com.facilit.kanban.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

class MetricsCredentialsTest {

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Test
    void blankPasswordKeepsMetricsClosed() {
        UserDetailsService service = MetricsCredentials.userDetailsService("prometheus", "  ", passwordEncoder);

        assertThatThrownBy(() -> service.loadUserByUsername("prometheus"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shortPasswordFailsStartup() {
        assertThatThrownBy(() -> MetricsCredentials.userDetailsService("prometheus", "curta-demais", passwordEncoder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 16");
    }

    @Test
    void passwordAboveBcryptLimitFailsStartup() {
        assertThatThrownBy(() -> MetricsCredentials.userDetailsService("prometheus", "x".repeat(73), passwordEncoder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most 72 bytes");
    }

    @Test
    void blankUsernameWithPasswordFailsStartup() {
        assertThatThrownBy(() -> MetricsCredentials.userDetailsService(" ", "metrics-password-long", passwordEncoder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("username");
    }

    @Test
    void configuredPasswordCreatesOnlyEncodedMetricsUser() {
        UserDetailsService service = MetricsCredentials.userDetailsService(
                "prometheus", "metrics-password-long", passwordEncoder);

        UserDetails user = service.loadUserByUsername("prometheus");

        assertThat(user.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_METRICS");
        assertThat(user.getPassword()).startsWith("{bcrypt}").doesNotContain("metrics-password-long");
        assertThat(passwordEncoder.matches("metrics-password-long", user.getPassword())).isTrue();
    }
}
