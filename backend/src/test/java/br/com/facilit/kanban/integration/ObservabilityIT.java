package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@AutoConfigureObservability
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "DB_PASSWORD=test-only",
            "app.security.bootstrap-admin-email=observability-admin@example.invalid",
            "app.security.bootstrap-admin-password=observability-test-password",
            "app.metrics.username=prometheus",
            "app.metrics.password=observability-metrics-password"
        })
class ObservabilityIT {

    private static final String METRICS_USER = "prometheus";
    private static final String METRICS_PASSWORD = "observability-metrics-password";
    private static final String ADMIN_EMAIL = "observability-admin@example.invalid";
    private static final String ADMIN_PASSWORD = "observability-test-password";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine3.24"));

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthAndProbesArePublicWithoutDetails() {
        ResponseEntity<JsonNode> health = restTemplate.getForEntity(url("/actuator/health"), JsonNode.class);
        assertThat(health.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(health.getBody()).isNotNull();
        assertThat(health.getBody().path("status").asText()).isEqualTo("UP");
        assertThat(health.getBody().has("components")).isFalse();
        assertThat(health.getBody().has("details")).isFalse();

        assertThat(restTemplate.getForEntity(url("/actuator/health/liveness"), JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(restTemplate.getForEntity(url("/actuator/health/readiness"), JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void prometheusRequiresTheMetricsCredential() {
        assertThat(restTemplate.getForEntity(url("/api/v1/health"), String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        ResponseEntity<String> anonymous = restTemplate.getForEntity(url("/actuator/prometheus"), String.class);
        assertThat(anonymous.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(anonymous.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).startsWith("Basic");

        assertThat(restTemplate.withBasicAuth(METRICS_USER, "wrong-metrics-password")
                .getForEntity(url("/actuator/prometheus"), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(restTemplate.withBasicAuth(ADMIN_EMAIL, ADMIN_PASSWORD)
                .getForEntity(url("/actuator/prometheus"), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        HttpHeaders sessionHeaders = new HttpHeaders();
        sessionHeaders.set(HttpHeaders.COOKIE, adminSessionCookie());
        assertThat(restTemplate.exchange(
                url("/actuator/prometheus"),
                HttpMethod.GET,
                new HttpEntity<>(sessionHeaders),
                String.class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> scrape = restTemplate.withBasicAuth(METRICS_USER, METRICS_PASSWORD)
                .getForEntity(url("/actuator/prometheus"), String.class);
        assertThat(scrape.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(scrape.getHeaders().get(HttpHeaders.SET_COOKIE)).isNullOrEmpty();
        assertThat(scrape.getBody())
                .isNotNull()
                .contains("http_server_requests_seconds_bucket")
                .contains("application=\"facilit-kanban\"")
                .contains("jvm_memory_used_bytes")
                .contains("hikaricp_connections_active");
    }

    @Test
    void nonExposedActuatorEndpointsAreUnavailable() {
        for (String path : new String[] {"/actuator", "/actuator/env", "/actuator/heapdump", "/actuator/metrics"}) {
            ResponseEntity<String> response = restTemplate.withBasicAuth(METRICS_USER, METRICS_PASSWORD)
                    .getForEntity(url(path), String.class);
            assertThat(response.getStatusCode().is2xxSuccessful())
                    .as(path)
                    .isFalse();
        }
    }

    private String adminSessionCookie() {
        ResponseEntity<JsonNode> csrf = restTemplate.getForEntity(url("/api/v1/auth/csrf"), JsonNode.class);
        assertThat(csrf.getStatusCode()).isEqualTo(HttpStatus.OK);
        String csrfCookie = cookie(csrf.getHeaders(), "XSRF-TOKEN");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.COOKIE, csrfCookie);
        headers.set("X-XSRF-TOKEN", csrfCookie.substring(csrfCookie.indexOf('=') + 1));
        ResponseEntity<JsonNode> login = restTemplate.exchange(
                url("/api/v1/auth/login"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD), headers),
                JsonNode.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        return cookie(login.getHeaders(), "JSESSIONID");
    }

    private static String cookie(HttpHeaders headers, String name) {
        String prefix = name + "=";
        return headers.getOrEmpty(HttpHeaders.SET_COOKIE).stream()
                .filter(value -> value.startsWith(prefix))
                .map(value -> value.substring(0, value.indexOf(';')))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing cookie: " + name));
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
