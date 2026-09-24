package br.com.facilit.kanban.infrastructure.security;

import br.com.facilit.kanban.delivery.common.ApiErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        return repository;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper,
            SecurityContextRepository securityContextRepository,
            CsrfTokenRepository csrfTokenRepository) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                .securityContext(context ->
                        context.securityContextRepository(securityContextRepository))
                .requestCache(cache -> cache.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/health", "/api/v1/auth/csrf")
                        .permitAll()
                        .requestMatchers("/api/v1/auth/login")
                        .permitAll()
                        .requestMatchers(
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/graphiql",
                                "/graphiql/**")
                        .permitAll()
                        .requestMatchers("/api/v1/**", "/graphql")
                        .hasAnyRole("ADMIN", "RESPONSIBLE")
                        .anyRequest()
                        .denyAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeProblem(
                                        response,
                                        objectMapper,
                                        HttpStatus.UNAUTHORIZED,
                                        ApiErrorCode.UNAUTHORIZED,
                                        "Autenticação obrigatória."))
                        .accessDeniedHandler((request, response, exception) ->
                                writeProblem(
                                        response,
                                        objectMapper,
                                        HttpStatus.FORBIDDEN,
                                        ApiErrorCode.FORBIDDEN,
                                        "Acesso negado.")))
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT)));

        return http.build();
    }

    private static void writeProblem(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            HttpStatus status,
            ApiErrorCode code,
            String detail) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "title", code.name(),
                "status", status.value(),
                "detail", detail,
                "code", code.name()));
    }
}
