package br.com.facilit.kanban.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

/**
 * Cadeia dedicada aos endpoints expostos do Actuator.
 *
 * <ul>
 *   <li>{@code health} (e grupos liveness/readiness): público, sem detalhes;</li>
 *   <li>{@code prometheus}: HTTP Basic com a credencial técnica {@code ROLE_METRICS};</li>
 *   <li>demais endpoints do Actuator: negados.</li>
 * </ul>
 *
 * <p>A cadeia é stateless, não lê a sessão do usuário da aplicação e usa um
 * {@code AuthenticationManager} próprio, de modo que credenciais de ADMIN ou de
 * responsável não dão acesso às métricas.
 */
@Configuration(proxyBeanMethods = false)
public class ActuatorSecurityConfiguration {

    @Bean
    @Order(1)
    SecurityFilterChain actuatorSecurityFilterChain(
            HttpSecurity http,
            PasswordEncoder passwordEncoder,
            @Value("${app.metrics.username:prometheus}") String username,
            @Value("${app.metrics.password:}") String password) throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(
                MetricsCredentials.userDetailsService(username, password, passwordEncoder));
        provider.setPasswordEncoder(passwordEncoder);

        http
                .securityMatcher(EndpointRequest.toAnyEndpoint())
                .authenticationManager(new ProviderManager(provider))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(EndpointRequest.to("health"))
                        .permitAll()
                        .requestMatchers(EndpointRequest.to("prometheus"))
                        .hasRole(MetricsCredentials.ROLE)
                        .anyRequest()
                        .denyAll())
                .httpBasic(Customizer.withDefaults())
                // Somente leitura, sem cookie nem sessão: não há token CSRF a proteger.
                .csrf(AbstractHttpConfigurer::disable)
                .requestCache(cache -> cache.disable())
                .securityContext(context ->
                        context.securityContextRepository(new RequestAttributeSecurityContextRepository()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
