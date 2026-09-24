package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Contrato do OpenAPI: toda operação REST documenta suas respostas de erro no formato real
 * ({@code application/problem+json}, código estável e exemplo).
 */
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "DB_PASSWORD=test-only")
class OpenApiContractIT {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "patch", "delete");
    private static final Set<String> PUBLIC_PATHS = Set.of("/api/v1/health", "/api/v1/auth/csrf", "/api/v1/auth/login");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine3.24"));

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void everyRestOperationDocumentsItsErrorsAsProblemJsonWithExamples() {
        JsonNode docs = apiDocs();
        List<String> problems = new ArrayList<>();
        int operations = 0;

        for (Map.Entry<String, JsonNode> path : docs.path("paths").properties()) {
            if (!path.getKey().startsWith("/api/v1/")) {
                continue;
            }
            for (Map.Entry<String, JsonNode> method : path.getValue().properties()) {
                if (!HTTP_METHODS.contains(method.getKey())) {
                    continue;
                }
                operations++;
                String operation = method.getKey().toUpperCase(Locale.ROOT) + " " + path.getKey();
                JsonNode responses = method.getValue().path("responses");
                List<String> required = new ArrayList<>(List.of("500"));
                if (!PUBLIC_PATHS.contains(path.getKey())) {
                    required.addAll(List.of("401", "403"));
                    if (!"get".equals(method.getKey())) {
                        required.add("409");
                    }
                    if (path.getKey().contains("{id}")) {
                        required.add("404");
                    }
                }
                for (String code : required) {
                    JsonNode problemContent = responses.path(code).path("content").path(PROBLEM_JSON);
                    if (problemContent.isMissingNode()) {
                        problems.add(operation + " sem resposta " + code + " em " + PROBLEM_JSON);
                    } else if (!problemContent.has("examples") && !problemContent.has("example")) {
                        problems.add(operation + " resposta " + code + " sem exemplo");
                    }
                }
            }
        }

        assertThat(operations).isGreaterThanOrEqualTo(20);
        assertThat(problems).isEmpty();
    }

    @Test
    void transitionDocumentsBlockedConfirmationAndBusinessRuleErrors() {
        JsonNode docs = apiDocs();
        JsonNode examples = docs.at("/paths/~1api~1v1~1projects~1{id}~1status/patch/responses/422/content")
                .path(PROBLEM_JSON)
                .path("examples");

        assertThat(examples.has("TRANSITION_BLOCKED")).isTrue();
        assertThat(examples.has("CONFIRMATION_REQUIRED")).isTrue();
        assertThat(examples.has("BUSINESS_RULE_VIOLATION")).isTrue();
        assertThat(examples.at("/CONFIRMATION_REQUIRED/value/clearedField").asText()).isEqualTo("actualStart");
        assertThat(docs.at("/components/schemas/ProjectStatusRequest/properties/confirm").isObject()).isTrue();
        assertThat(docs.at("/components/schemas/Problem/properties/code").isObject()).isTrue();
    }

    @Test
    void projectWritesDocumentBusinessRuleViolations() {
        JsonNode docs = apiDocs();

        for (String pointer : List.of(
                "/paths/~1api~1v1~1projects/post/responses/422",
                "/paths/~1api~1v1~1projects~1{id}/put/responses/422")) {
            assertThat(docs.at(pointer + "/content").path(PROBLEM_JSON).path("examples").has("BUSINESS_RULE_VIOLATION"))
                    .as(pointer)
                    .isTrue();
        }
    }

    private JsonNode apiDocs() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api-docs", JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }
}
