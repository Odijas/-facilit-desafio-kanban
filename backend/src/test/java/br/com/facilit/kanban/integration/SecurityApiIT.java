package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "DB_PASSWORD=test-only",
            "app.security.bootstrap-admin-email=security-admin@example.invalid",
            "app.security.bootstrap-admin-password=security-test-password"
        })
class SecurityApiIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine3.24"));

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void protectsBusinessEndpointsAndStoresOnlyEncodedPassword() {
        ResponseEntity<JsonNode> anonymous = restTemplate.getForEntity(
                url("/api/v1/projects?page=0&size=20"), JsonNode.class);
        assertThat(anonymous.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(anonymous.getBody()).isNotNull();
        assertThat(anonymous.getBody().path("code").asText()).isEqualTo("UNAUTHORIZED");

        String storedPassword = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM app_users WHERE email = ?",
                String.class,
                "security-admin@example.invalid");
        assertThat(storedPassword)
                .isNotNull()
                .isNotEqualTo("security-test-password")
                .startsWith("{bcrypt}");
    }

    @Test
    void authenticatesWithSessionAndRequiresCsrfForStateChanges() {
        AuthenticatedSession session = login("security-admin@example.invalid", "security-test-password", "ROLE_ADMIN");

        HttpHeaders meHeaders = new HttpHeaders();
        meHeaders.set(HttpHeaders.COOKIE, session.sessionCookie());
        ResponseEntity<JsonNode> me = restTemplate.exchange(
                url("/api/v1/auth/me"),
                HttpMethod.GET,
                new HttpEntity<>(meHeaders),
                JsonNode.class);
        JsonNode meBody = requireBody(me, HttpStatus.OK);
        assertThat(meBody.path("email").asText()).isEqualTo("security-admin@example.invalid");
        assertThat(meBody.path("authorities").toString()).contains("ROLE_ADMIN");

        HttpHeaders noCsrfHeaders = jsonHeaders();
        noCsrfHeaders.set(HttpHeaders.COOKIE, session.sessionCookie());
        ResponseEntity<JsonNode> denied = restTemplate.exchange(
                url("/api/v1/responsibles"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of(
                        "name", "CSRF blocked",
                        "email", "csrf-blocked@example.invalid",
                        "position", "Analista"), noCsrfHeaders),
                JsonNode.class);
        assertThat(denied.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(denied.getBody()).isNotNull();
        assertThat(denied.getBody().path("code").asText()).isEqualTo("FORBIDDEN");

        ResponseEntity<JsonNode> refreshedCsrf = csrf(session.sessionCookie());
        requireBody(refreshedCsrf, HttpStatus.OK);
        String csrfCookie = cookie(refreshedCsrf.getHeaders(), "XSRF-TOKEN");
        String authenticatedCookies = session.sessionCookie() + "; " + csrfCookie;

        HttpHeaders createHeaders = jsonHeaders();
        createHeaders.set(HttpHeaders.COOKIE, authenticatedCookies);
        createHeaders.set("X-XSRF-TOKEN", cookieValue(csrfCookie));
        ResponseEntity<JsonNode> created = restTemplate.exchange(
                url("/api/v1/responsibles"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of(
                        "name", "CSRF allowed",
                        "email", "csrf-allowed@example.invalid",
                        "position", "Analista"), createHeaders),
                JsonNode.class);
        String responsibleId = requireBody(created, HttpStatus.CREATED).path("id").asText();

        ResponseEntity<Void> deleted = restTemplate.exchange(
                url("/api/v1/responsibles/" + responsibleId),
                HttpMethod.DELETE,
                new HttpEntity<>(createHeaders),
                Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void rejectsInvalidCredentialsAndLogoutInvalidatesSession() {
        ResponseEntity<JsonNode> badLogin = loginRequest(
                "security-admin@example.invalid", "wrong-password");
        assertThat(badLogin.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(badLogin.getBody()).isNotNull();
        assertThat(badLogin.getBody().path("code").asText()).isEqualTo("UNAUTHORIZED");
        assertThat(badLogin.getBody().path("detail").asText()).isEqualTo("Invalid email or password");

        AuthenticatedSession session = login("security-admin@example.invalid", "security-test-password", "ROLE_ADMIN");
        ResponseEntity<JsonNode> refreshedCsrf = csrf(session.sessionCookie());
        requireBody(refreshedCsrf, HttpStatus.OK);
        String csrfCookie = cookie(refreshedCsrf.getHeaders(), "XSRF-TOKEN");

        HttpHeaders logoutHeaders = new HttpHeaders();
        logoutHeaders.set(HttpHeaders.COOKIE, session.sessionCookie() + "; " + csrfCookie);
        logoutHeaders.set("X-XSRF-TOKEN", cookieValue(csrfCookie));
        ResponseEntity<Void> logout = restTemplate.exchange(
                url("/api/v1/auth/logout"),
                HttpMethod.POST,
                new HttpEntity<>(logoutHeaders),
                Void.class);
        assertThat(logout.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        HttpHeaders staleSession = new HttpHeaders();
        staleSession.set(HttpHeaders.COOKIE, session.sessionCookie());
        ResponseEntity<JsonNode> afterLogout = restTemplate.exchange(
                url("/api/v1/auth/me"),
                HttpMethod.GET,
                new HttpEntity<>(staleSession),
                JsonNode.class);
        assertThat(afterLogout.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void responsibleAuthenticatesAndManagesOnlyOwnProjects() {
        AuthenticatedSession admin = login("security-admin@example.invalid", "security-test-password", "ROLE_ADMIN");
        String ownerId = requireBody(send(admin, HttpMethod.POST, "/api/v1/responsibles", Map.of(
                "name", "Responsável dono",
                "email", "owner-responsible@example.invalid",
                "position", "Analista")), HttpStatus.CREATED).path("id").asText();
        String otherId = requireBody(send(admin, HttpMethod.POST, "/api/v1/responsibles", Map.of(
                "name", "Outro responsável",
                "email", "other-responsible@example.invalid",
                "position", "Gestor")), HttpStatus.CREATED).path("id").asText();

        ResponseEntity<JsonNode> weakPassword = send(admin, HttpMethod.PUT,
                "/api/v1/responsibles/" + ownerId + "/credentials", Map.of("password", "curta"));
        assertThat(requireBody(weakPassword, HttpStatus.BAD_REQUEST).path("code").asText())
                .isEqualTo("INVALID_REQUEST");
        assertThat(send(admin, HttpMethod.PUT, "/api/v1/responsibles/" + ownerId + "/credentials",
                Map.of("password", "responsible-test-password")).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        String storedPassword = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM app_users WHERE responsible_id = ?::uuid",
                String.class,
                ownerId);
        assertThat(storedPassword).startsWith("{bcrypt}").doesNotContain("responsible-test-password");

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        String otherProjectId = requireBody(send(admin, HttpMethod.POST, "/api/v1/projects", Map.of(
                "name", "Projeto de outro responsável",
                "responsibleIds", List.of(otherId),
                "plannedStart", today.toString(),
                "plannedEnd", today.plusDays(10).toString())), HttpStatus.CREATED).path("id").asText();

        AuthenticatedSession owner = login(
                "owner-responsible@example.invalid", "responsible-test-password", "ROLE_RESPONSIBLE");
        HttpHeaders meHeaders = new HttpHeaders();
        meHeaders.set(HttpHeaders.COOKIE, owner.sessionCookie());
        JsonNode me = requireBody(restTemplate.exchange(
                url("/api/v1/auth/me"), HttpMethod.GET, new HttpEntity<>(meHeaders), JsonNode.class),
                HttpStatus.OK);
        assertThat(me.path("responsibleId").asText()).isEqualTo(ownerId);

        String ownProjectId = requireBody(send(owner, HttpMethod.POST, "/api/v1/projects", Map.of(
                "name", "Projeto próprio",
                "responsibleIds", List.of(ownerId),
                "plannedStart", today.toString(),
                "plannedEnd", today.plusDays(10).toString())), HttpStatus.CREATED).path("id").asText();
        JsonNode transitioned = requireBody(send(owner, HttpMethod.PATCH,
                "/api/v1/projects/" + ownProjectId + "/status", Map.of("status", "IN_PROGRESS")), HttpStatus.OK);
        assertThat(transitioned.path("status").asText()).isEqualTo("IN_PROGRESS");

        assertForbidden(send(owner, HttpMethod.PATCH,
                "/api/v1/projects/" + otherProjectId + "/status", Map.of("status", "IN_PROGRESS")));
        assertForbidden(send(owner, HttpMethod.PUT, "/api/v1/projects/" + otherProjectId, Map.of(
                "name", "Tomado",
                "responsibleIds", List.of(ownerId))));
        assertForbidden(send(owner, HttpMethod.DELETE, "/api/v1/projects/" + otherProjectId, null));
        assertForbidden(send(owner, HttpMethod.POST, "/api/v1/projects", Map.of(
                "name", "Sem o dono",
                "responsibleIds", List.of(otherId))));
        assertForbidden(send(owner, HttpMethod.POST, "/api/v1/responsibles", Map.of(
                "name", "Indevido",
                "email", "indevido@example.invalid",
                "position", "Analista")));
        assertForbidden(send(owner, HttpMethod.PUT, "/api/v1/responsibles/" + otherId, Map.of(
                "name", "Outro responsável",
                "email", "takeover@example.invalid",
                "position", "Gestor")));
        assertForbidden(send(owner, HttpMethod.POST, "/api/v1/secretariats", Map.of("name", "Indevida")));
        assertForbidden(send(owner, HttpMethod.PUT, "/api/v1/responsibles/" + otherId + "/credentials",
                Map.of("password", "responsible-test-password")));

        JsonNode graphqlDenied = requireBody(send(owner, HttpMethod.POST, "/graphql", Map.of(
                "query", "mutation { createSecretariat(input: {name: \"Indevida\"}) { id } }")), HttpStatus.OK);
        assertThat(graphqlDenied.at("/errors/0/extensions/code").asText()).isEqualTo("FORBIDDEN");

        HttpHeaders ownerRead = new HttpHeaders();
        ownerRead.set(HttpHeaders.COOKIE, owner.sessionCookie());
        JsonNode board = requireBody(restTemplate.exchange(
                url("/api/v1/projects?page=0&size=100"), HttpMethod.GET, new HttpEntity<>(ownerRead), JsonNode.class),
                HttpStatus.OK);
        assertThat(board.path("content").toString()).contains(otherProjectId).contains(ownProjectId);

        assertThat(send(owner, HttpMethod.DELETE, "/api/v1/projects/" + ownProjectId, null).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        requireBody(send(admin, HttpMethod.PUT, "/api/v1/responsibles/" + ownerId, Map.of(
                "name", "Responsável dono",
                "email", "owner-renamed@example.invalid",
                "position", "Analista")), HttpStatus.OK);
        String syncedEmail = jdbcTemplate.queryForObject(
                "SELECT email FROM app_users WHERE responsible_id = ?::uuid",
                String.class,
                ownerId);
        assertThat(syncedEmail).isEqualTo("owner-renamed@example.invalid");
        ResponseEntity<JsonNode> adminEmailCollision = send(admin, HttpMethod.PUT, "/api/v1/responsibles/" + ownerId,
                Map.of(
                        "name", "Responsável dono",
                        "email", "security-admin@example.invalid",
                        "position", "Analista"));
        assertThat(requireBody(adminEmailCollision, HttpStatus.CONFLICT).path("code").asText()).isEqualTo("CONFLICT");

        assertThat(send(admin, HttpMethod.DELETE, "/api/v1/projects/" + otherProjectId, null).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(send(admin, HttpMethod.DELETE, "/api/v1/responsibles/" + ownerId + "/credentials", null)
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Integer remainingCredentials = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM app_users WHERE responsible_id = ?::uuid",
                Integer.class,
                ownerId);
        assertThat(remainingCredentials).isZero();
        assertThat(send(admin, HttpMethod.DELETE, "/api/v1/responsibles/" + ownerId, null).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(send(admin, HttpMethod.DELETE, "/api/v1/responsibles/" + otherId, null).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void unknownAuthenticatedRouteIsNotMaskedAsForbidden() {
        AuthenticatedSession admin = login("security-admin@example.invalid", "security-test-password", "ROLE_ADMIN");
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, admin.sessionCookie());

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url("/api/v1/unknown-resource"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<JsonNode> send(
            AuthenticatedSession session,
            HttpMethod method,
            String path,
            Object body) {
        ResponseEntity<JsonNode> refreshedCsrf = csrf(session.sessionCookie());
        requireBody(refreshedCsrf, HttpStatus.OK);
        String csrfCookie = cookie(refreshedCsrf.getHeaders(), "XSRF-TOKEN");
        HttpHeaders headers = jsonHeaders();
        headers.set(HttpHeaders.COOKIE, session.sessionCookie() + "; " + csrfCookie);
        headers.set("X-XSRF-TOKEN", cookieValue(csrfCookie));
        return restTemplate.exchange(url(path), method, new HttpEntity<>(body, headers), JsonNode.class);
    }

    private static void assertForbidden(ResponseEntity<JsonNode> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().path("code").asText()).isEqualTo("FORBIDDEN");
    }

    private AuthenticatedSession login(String email, String password, String expectedAuthority) {
        ResponseEntity<JsonNode> response = loginRequest(email, password);
        JsonNode body = requireBody(response, HttpStatus.OK);
        assertThat(body.path("authorities").toString()).contains(expectedAuthority);
        String sessionCookie = cookie(response.getHeaders(), "JSESSIONID");
        String setCookie = response.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE).stream()
                .filter(value -> value.startsWith("JSESSIONID="))
                .findFirst()
                .orElseThrow();
        assertThat(setCookie).contains("HttpOnly").contains("SameSite=Lax");
        return new AuthenticatedSession(sessionCookie);
    }

    private ResponseEntity<JsonNode> loginRequest(String email, String password) {
        ResponseEntity<JsonNode> csrf = csrf(null);
        requireBody(csrf, HttpStatus.OK);
        String csrfCookie = cookie(csrf.getHeaders(), "XSRF-TOKEN");

        HttpHeaders headers = jsonHeaders();
        headers.set(HttpHeaders.COOKIE, csrfCookie);
        headers.set("X-XSRF-TOKEN", cookieValue(csrfCookie));
        return restTemplate.exchange(
                url("/api/v1/auth/login"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("email", email, "password", password), headers),
                JsonNode.class);
    }

    private ResponseEntity<JsonNode> csrf(String sessionCookie) {
        HttpHeaders headers = new HttpHeaders();
        if (sessionCookie != null) {
            headers.set(HttpHeaders.COOKIE, sessionCookie);
        }
        return restTemplate.exchange(
                url("/api/v1/auth/csrf"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class);
    }

    private static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private static String cookie(HttpHeaders headers, String name) {
        String prefix = name + "=";
        return headers.getOrEmpty(HttpHeaders.SET_COOKIE).stream()
                .filter(value -> value.startsWith(prefix))
                .map(value -> value.substring(0, value.indexOf(';')))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing cookie: " + name));
    }

    private static String cookieValue(String cookie) {
        int separator = cookie.indexOf('=');
        if (separator < 0 || separator == cookie.length() - 1) {
            throw new IllegalArgumentException("Invalid cookie");
        }
        return cookie.substring(separator + 1);
    }

    private static JsonNode requireBody(ResponseEntity<JsonNode> response, HttpStatus expectedStatus) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private record AuthenticatedSession(String sessionCookie) {
    }
}
