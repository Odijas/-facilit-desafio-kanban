package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
class ProjectIndicatorsIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine3.24"));

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Clock clock;

    private ApiSession session;

    @BeforeEach
    void login() {
        session = ApiSession.login(
                restTemplate,
                port,
                "integration-admin@example.invalid",
                "integration-test-password");
    }

    @Test
    void exposesGroupedIndicatorsAndDeadlineWindowOverRestAndGraphql() {
        JsonNode secretariatA = createSecretariat("Tecnologia " + UUID.randomUUID());
        JsonNode secretariatB = createSecretariat("Gestão " + UUID.randomUUID());

        JsonNode responsibleA = createResponsible("A", secretariatA.path("id").asText());
        JsonNode responsibleB = createResponsible("B", secretariatA.path("id").asText());
        JsonNode responsibleC = createResponsible("C", secretariatB.path("id").asText());

        LocalDate today = LocalDate.now(clock);
        JsonNode upcoming = createProject(
                "Prazo próximo",
                List.of(responsibleA.path("id").asText(), responsibleB.path("id").asText()),
                today,
                today.plusDays(3));
        createProject(
                "Projeto atrasado",
                List.of(responsibleA.path("id").asText()),
                today.minusDays(10),
                today.minusDays(5));
        createProject(
                "Prazo fora da janela",
                List.of(responsibleC.path("id").asText()),
                today.plusDays(1),
                today.plusDays(8));

        JsonNode bySecretariat = session.send(
                HttpMethod.GET,
                "/api/v1/indicators/projects/by-secretariat",
                null,
                HttpStatus.OK);
        JsonNode secretariatASummary = item(bySecretariat, secretariatA.path("id").asText());
        JsonNode secretariatBSummary = item(bySecretariat, secretariatB.path("id").asText());
        assertThat(secretariatASummary.path("projectCount").asLong()).isEqualTo(2);
        assertThat(secretariatASummary.path("averageDelayDays").asDouble()).isEqualTo(2.5);
        assertThat(secretariatBSummary.path("projectCount").asLong()).isEqualTo(1);

        JsonNode byResponsible = session.send(
                HttpMethod.GET,
                "/api/v1/indicators/projects/by-responsible",
                null,
                HttpStatus.OK);
        JsonNode responsibleASummary = item(byResponsible, responsibleA.path("id").asText());
        JsonNode responsibleBSummary = item(byResponsible, responsibleB.path("id").asText());
        assertThat(responsibleASummary.path("projectCount").asLong()).isEqualTo(2);
        assertThat(responsibleASummary.path("averageDelayDays").asDouble()).isEqualTo(2.5);
        assertThat(responsibleBSummary.path("projectCount").asLong()).isEqualTo(1);

        JsonNode deadlines = session.send(
                HttpMethod.GET,
                "/api/v1/indicators/projects/deadlines?withinDays=7",
                null,
                HttpStatus.OK);
        assertThat(deadlines.path("withinDays").asInt()).isEqualTo(7);
        assertThat(deadlines.path("from").asText()).isEqualTo(today.toString());
        assertThat(deadlines.path("to").asText()).isEqualTo(today.plusDays(7).toString());
        assertThat(deadlines.path("projects").size()).isEqualTo(1);
        assertThat(deadlines.at("/projects/0/projectId").asText()).isEqualTo(upcoming.path("id").asText());
        assertThat(deadlines.at("/projects/0/daysUntilDeadline").asLong()).isEqualTo(3);

        JsonNode invalidRest = session.send(
                HttpMethod.GET,
                "/api/v1/indicators/projects/deadlines?withinDays=0",
                null,
                HttpStatus.BAD_REQUEST);
        assertThat(invalidRest.path("code").asText()).isEqualTo("INVALID_REQUEST");

        JsonNode graphql = session.graphql(
                """
                query {
                  projectIndicatorsBySecretariat { id projectCount averageDelayDays }
                  projectIndicatorsByResponsible { id projectCount averageDelayDays }
                  projectDeadlines(withinDays: 7) {
                    withinDays
                    projects { projectId daysUntilDeadline }
                  }
                }
                """,
                Map.of());
        assertThat(graphql.path("errors").isMissingNode()).isTrue();
        assertThat(item(graphql.at("/data/projectIndicatorsBySecretariat"), secretariatA.path("id").asText())
                .path("projectCount").asLong()).isEqualTo(2);
        assertThat(item(graphql.at("/data/projectIndicatorsByResponsible"), responsibleA.path("id").asText())
                .path("projectCount").asLong()).isEqualTo(2);
        assertThat(graphql.at("/data/projectDeadlines/projects/0/projectId").asText())
                .isEqualTo(upcoming.path("id").asText());

        JsonNode invalidGraphql = session.graphql(
                "query { projectDeadlines(withinDays: 91) { withinDays } }",
                Map.of());
        assertThat(invalidGraphql.at("/errors/0/extensions/code").asText()).isEqualTo("INVALID_REQUEST");
    }

    private JsonNode createSecretariat(String name) {
        return session.send(
                HttpMethod.POST,
                "/api/v1/secretariats",
                Map.of("name", name),
                HttpStatus.CREATED);
    }

    private JsonNode createResponsible(String suffix, String secretariatId) {
        return session.send(
                HttpMethod.POST,
                "/api/v1/responsibles",
                Map.of(
                        "name", "Responsável " + suffix,
                        "email", "f5l4-" + suffix.toLowerCase() + "-" + UUID.randomUUID() + "@example.com",
                        "position", "Analista",
                        "secretariatId", secretariatId),
                HttpStatus.CREATED);
    }

    private JsonNode createProject(
            String name,
            List<String> responsibleIds,
            LocalDate plannedStart,
            LocalDate plannedEnd) {
        return session.send(
                HttpMethod.POST,
                "/api/v1/projects",
                Map.of(
                        "name", name,
                        "responsibleIds", responsibleIds,
                        "plannedStart", plannedStart.toString(),
                        "plannedEnd", plannedEnd.toString()),
                HttpStatus.CREATED);
    }

    private static JsonNode item(JsonNode array, String id) {
        for (JsonNode candidate : array) {
            if (id.equals(candidate.path("id").asText())) {
                return candidate;
            }
        }
        throw new AssertionError("Indicador não encontrado: " + id + " em " + array);
    }
}
