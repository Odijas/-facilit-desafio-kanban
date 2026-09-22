package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
            "app.security.bootstrap-admin-email=integration-admin@example.invalid",
            "app.security.bootstrap-admin-password=integration-test-password"
        })
class KanbanApiIT {

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

    private AuthenticatedSession authenticatedSession;

    @Test
    void appliesMigrationsAndApiIndexesAgainstRealPostgres() {
        Integer migrations = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success = true",
                Integer.class);
        Integer indexes = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM pg_indexes
                WHERE schemaname = 'public'
                  AND indexname IN (
                    'ix_projects_name_id',
                    'ix_projects_status_name_id',
                    'ix_responsibles_name_id'
                  )
                """,
                Integer.class);

        assertThat(migrations).isNotNull().isGreaterThanOrEqualTo(4);
        assertThat(indexes).isEqualTo(3);
    }

    @Test
    void validatesRestInputAndReturnsStableProblemCodes() {
        ResponseEntity<JsonNode> invalid = post(
                "/api/v1/responsibles",
                Map.of(
                        "name", "Maria Silva",
                        "email", "not-an-email",
                        "position", "Analista"));

        assertThat(invalid.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invalid.getBody()).isNotNull();
        assertThat(invalid.getBody().path("code").asText()).isEqualTo("VALIDATION_ERROR");
        assertThat(invalid.getBody().path("violations").toString()).contains("email");

        String email = "f1l3-" + UUID.randomUUID() + "@example.com";
        JsonNode responsible = requireBody(post(
                "/api/v1/responsibles",
                Map.of(
                        "name", "Maria Silva",
                        "email", email,
                        "position", "Analista")),
                HttpStatus.CREATED);
        String responsibleId = responsible.path("id").asText();

        ResponseEntity<JsonNode> duplicate = post(
                "/api/v1/responsibles",
                Map.of(
                        "name", "Outra Pessoa",
                        "email", email.toUpperCase(),
                        "position", "Gestora"));
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicate.getBody()).isNotNull();
        assertThat(duplicate.getBody().path("code").asText()).isEqualTo("CONFLICT");

        ResponseEntity<JsonNode> missing = authenticatedGet(
                "/api/v1/responsibles/" + UUID.randomUUID(), JsonNode.class);
        assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(missing.getBody()).isNotNull();
        assertThat(missing.getBody().path("code").asText()).isEqualTo("RESOURCE_NOT_FOUND");

        delete("/api/v1/responsibles/" + responsibleId, HttpStatus.NO_CONTENT);
    }

    @Test
    void restAndGraphqlShareApplicationRulesAndGraphqlValidation() {
        String email = "f1l3-gql-" + UUID.randomUUID() + "@example.com";
        JsonNode responsible = requireBody(post(
                "/api/v1/responsibles",
                Map.of(
                        "name", "Responsável GraphQL",
                        "email", email,
                        "position", "Analista")),
                HttpStatus.CREATED);
        String responsibleId = responsible.path("id").asText();

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        JsonNode project = requireBody(post(
                "/api/v1/projects",
                Map.of(
                        "name", "Projeto compartilhado",
                        "responsibleIds", List.of(responsibleId),
                        "plannedStart", today.toString(),
                        "plannedEnd", today.plusDays(10).toString())),
                HttpStatus.CREATED);
        String projectId = project.path("id").asText();
        assertThat(project.path("status").asText()).isEqualTo("NOT_STARTED");

        JsonNode transition = graphql(
                "mutation($id: ID!, $status: ProjectStatus!) { transitionProject(id: $id, status: $status) { id status actualStart } }",
                Map.of("id", projectId, "status", "IN_PROGRESS"));
        assertThat(transition.path("errors").isMissingNode()).isTrue();
        assertThat(transition.at("/data/transitionProject/status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(transition.at("/data/transitionProject/actualStart").asText()).isEqualTo(today.toString());

        ResponseEntity<JsonNode> filtered = authenticatedGet(
                "/api/v1/projects?status=IN_PROGRESS&page=0&size=20", JsonNode.class);
        assertThat(filtered.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(filtered.getBody()).isNotNull();
        assertThat(filtered.getBody().path("content").toString()).contains(projectId);

        JsonNode invalidGraphQl = graphql(
                "mutation($input: ResponsibleInput!) { createResponsible(input: $input) { id } }",
                Map.of("input", Map.of(
                        "name", "Inválido",
                        "email", "invalid-email",
                        "position", "Analista")));
        assertThat(invalidGraphQl.at("/errors/0/extensions/code").asText()).isEqualTo("VALIDATION_ERROR");

        JsonNode invalidDateGraphQl = graphql(
                "mutation($input: ProjectInput!) { createProject(input: $input) { id } }",
                Map.of("input", Map.of(
                        "name", "Data inválida",
                        "responsibleIds", List.of(responsibleId),
                        "plannedStart", "not-a-date")));
        assertThat(invalidDateGraphQl.at("/errors/0/extensions/code").asText()).isEqualTo("INVALID_REQUEST");

        delete("/api/v1/projects/" + projectId, HttpStatus.NO_CONTENT);
        delete("/api/v1/responsibles/" + responsibleId, HttpStatus.NO_CONTENT);
    }

    @Test
    void publishesOpenApiWithSchemasAndExamples() {
        ResponseEntity<JsonNode> docs = restTemplate.getForEntity(url("/api-docs"), JsonNode.class);
        assertThat(docs.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(docs.getBody()).isNotNull();
        assertThat(docs.getBody().path("openapi").asText()).startsWith("3.");
        assertThat(docs.getBody().at("/paths/~1api~1v1~1projects").isObject()).isTrue();
        assertThat(docs.getBody().at("/paths/~1api~1v1~1responsibles").isObject()).isTrue();
        assertThat(docs.getBody().at("/components/schemas/ProjectRequest/properties/name/example").asText())
                .isEqualTo("Implantação do portal");
        assertThat(docs.getBody().at("/components/schemas/ResponsibleRequest/properties/email/example").asText())
                .isEqualTo("maria.silva@example.com");

        ResponseEntity<String> ui = restTemplate.getForEntity(url("/swagger-ui.html"), String.class);
        assertThat(ui.getStatusCode().is2xxSuccessful() || ui.getStatusCode().is3xxRedirection()).isTrue();
    }

    private JsonNode graphql(String query, Map<String, Object> variables) {
        JsonNode body = requireBody(post(
                "/graphql",
                Map.of("query", query, "variables", variables)),
                HttpStatus.OK);
        return body;
    }

    private ResponseEntity<JsonNode> post(String path, Object body) {
        AuthenticatedSession session = authenticatedSession();
        HttpHeaders headers = jsonHeaders();
        headers.set(HttpHeaders.COOKIE, session.cookieHeader());
        headers.set("X-XSRF-TOKEN", session.csrfToken());
        return restTemplate.exchange(
                url(path),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                JsonNode.class);
    }

    private <T> ResponseEntity<T> authenticatedGet(String path, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, authenticatedSession().cookieHeader());
        return restTemplate.exchange(
                url(path),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                responseType);
    }

    private void delete(String path, HttpStatus expectedStatus) {
        AuthenticatedSession session = authenticatedSession();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, session.cookieHeader());
        headers.set("X-XSRF-TOKEN", session.csrfToken());
        ResponseEntity<Void> response = restTemplate.exchange(
                url(path),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class);
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    }

    private AuthenticatedSession authenticatedSession() {
        if (authenticatedSession == null) {
            authenticatedSession = login("integration-admin@example.invalid", "integration-test-password");
        }
        return authenticatedSession;
    }

    private AuthenticatedSession login(String email, String password) {
        ResponseEntity<JsonNode> initialCsrf = restTemplate.getForEntity(
                url("/api/v1/auth/csrf"), JsonNode.class);
        requireBody(initialCsrf, HttpStatus.OK);
        String initialCsrfCookie = cookie(initialCsrf.getHeaders(), "XSRF-TOKEN");

        HttpHeaders loginHeaders = jsonHeaders();
        loginHeaders.set(HttpHeaders.COOKIE, initialCsrfCookie);
        loginHeaders.set("X-XSRF-TOKEN", cookieValue(initialCsrfCookie));
        ResponseEntity<JsonNode> login = restTemplate.exchange(
                url("/api/v1/auth/login"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of("email", email, "password", password), loginHeaders),
                JsonNode.class);
        requireBody(login, HttpStatus.OK);
        String sessionCookie = cookie(login.getHeaders(), "JSESSIONID");

        HttpHeaders csrfHeaders = new HttpHeaders();
        csrfHeaders.set(HttpHeaders.COOKIE, sessionCookie);
        ResponseEntity<JsonNode> refreshedCsrf = restTemplate.exchange(
                url("/api/v1/auth/csrf"),
                HttpMethod.GET,
                new HttpEntity<>(csrfHeaders),
                JsonNode.class);
        requireBody(refreshedCsrf, HttpStatus.OK);
        String refreshedCsrfCookie = cookie(refreshedCsrf.getHeaders(), "XSRF-TOKEN");

        return new AuthenticatedSession(
                sessionCookie + "; " + refreshedCsrfCookie,
                cookieValue(refreshedCsrfCookie));
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

    private record AuthenticatedSession(String cookieHeader, String csrfToken) {
    }
}
