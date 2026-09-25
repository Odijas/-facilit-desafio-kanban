package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Tabela de transição do desafio pela API, com o PostgreSQL real: as 12 linhas pela REST
 * ({@code PATCH /api/v1/projects/{id}/status}) e 3 pela GraphQL ({@code transitionProject}), incluindo bloqueio
 * com orientação (422 {@code TRANSITION_BLOCKED}) e confirmação obrigatória (422 {@code CONFIRMATION_REQUIRED}).
 * Cada projeto nasce pela API com as datas que colocam o status de origem no dia de hoje.
 */
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "DB_PASSWORD=test-only",
            "app.security.bootstrap-admin-email=transition-admin@example.invalid",
            "app.security.bootstrap-admin-password=transition-test-password"
        })
class StatusTransitionApiIT {

    private static final String TRANSITION_MUTATION = """
            mutation($id: ID!, $status: ProjectStatus!, $confirm: Boolean) {
              transitionProject(id: $id, status: $status, confirm: $confirm) { id status actualStart actualEnd }
            }
            """;

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

    // Mesmo relógio da aplicação: "hoje" no fuso de negócio, não em UTC.
    @Autowired
    private Clock clock;

    private ApiSession admin;
    private UUID responsibleId;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        admin = ApiSession.login(restTemplate, port, "transition-admin@example.invalid", "transition-test-password");
        responsibleId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO responsibles (id, name, email, position, created_at, updated_at)
                VALUES (?, 'Responsável transição', ?, 'Analista', now(), now())
                """,
                responsibleId,
                "transition-" + responsibleId + "@example.invalid");
        today = LocalDate.now(clock);
    }

    // Linha 1 — A iniciar → Em andamento: início realizado = hoje.
    @Test
    void line01NotStartedToInProgressSetsActualStart() {
        String id = createProject("NOT_STARTED", today, today.plusDays(10), null, null);

        JsonNode project = transition(id, "IN_PROGRESS", null, HttpStatus.OK);

        assertThat(project.path("status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(project.path("actualStart").asText()).isEqualTo(today.toString());
    }

    // Linha 2 — A iniciar → Atrasado: erro se hoje < início previsto.
    @Test
    void line02NotStartedToOverdueIsBlockedBeforePlannedStart() {
        String id = createProject("NOT_STARTED", today.plusDays(1), today.plusDays(10), null, null);

        JsonNode problem = transition(id, "OVERDUE", null, HttpStatus.UNPROCESSABLE_ENTITY);

        assertBlocked(problem, "NOT_STARTED", "OVERDUE",
                "A iniciar → Atrasado bloqueado: não é possível marcar Atrasado antes do início previsto");
        assertUnchanged(id, "NOT_STARTED");
    }

    // Linha 3 — A iniciar → Concluído: término realizado = hoje.
    @Test
    void line03NotStartedToCompletedSetsActualEnd() {
        String id = createProject("NOT_STARTED", today, today.plusDays(10), null, null);

        JsonNode project = transition(id, "COMPLETED", null, HttpStatus.OK);

        assertThat(project.path("status").asText()).isEqualTo("COMPLETED");
        assertThat(project.path("actualEnd").asText()).isEqualTo(today.toString());
    }

    // Linha 4 — Em andamento → A iniciar: início realizado = null, com confirmação.
    @Test
    void line04InProgressToNotStartedRequiresConfirmationThenClearsActualStart() {
        String id = createProject("IN_PROGRESS", today, today.plusDays(10), today, null);

        JsonNode problem = transition(id, "NOT_STARTED", null, HttpStatus.UNPROCESSABLE_ENTITY);
        assertConfirmationRequired(problem, "IN_PROGRESS", "NOT_STARTED", "actualStart");
        assertThat(get(id).path("actualStart").asText()).isEqualTo(today.toString());

        JsonNode project = transition(id, "NOT_STARTED", true, HttpStatus.OK);
        assertThat(project.path("status").asText()).isEqualTo("NOT_STARTED");
        assertThat(isAbsent(project.path("actualStart"))).isTrue();
    }

    // Linha 5 — Em andamento → Atrasado: erro pedindo remover início realizado ou ajustar datas previstas.
    @Test
    void line05InProgressToOverdueIsBlockedWithGuidance() {
        String id = createProject("IN_PROGRESS", today, today.plusDays(10), today, null);

        JsonNode problem = transition(id, "OVERDUE", null, HttpStatus.UNPROCESSABLE_ENTITY);

        assertBlocked(problem, "IN_PROGRESS", "OVERDUE", "Em andamento → Atrasado bloqueado:");
        assertThat(problem.path("detail").asText())
                .contains("Remova o início realizado (actualStart)")
                .contains("plannedStart/plannedEnd");
        assertUnchanged(id, "IN_PROGRESS");
    }

    // Linha 6 — Em andamento → Concluído: término realizado = hoje.
    @Test
    void line06InProgressToCompletedSetsActualEnd() {
        String id = createProject("IN_PROGRESS", today, today.plusDays(10), today, null);

        JsonNode project = transition(id, "COMPLETED", null, HttpStatus.OK);

        assertThat(project.path("status").asText()).isEqualTo("COMPLETED");
        assertThat(project.path("actualEnd").asText()).isEqualTo(today.toString());
    }

    // Linha 7 — Atrasado → A iniciar: erro pedindo remover início realizado e ajustar datas previstas > hoje.
    @Test
    void line07OverdueToNotStartedIsBlockedWithGuidance() {
        String id = createProject("OVERDUE", today.minusDays(2), today.plusDays(10), null, null);

        JsonNode problem = transition(id, "NOT_STARTED", null, HttpStatus.UNPROCESSABLE_ENTITY);

        assertBlocked(problem, "OVERDUE", "NOT_STARTED", "Atrasado → A iniciar bloqueado:");
        assertUnchanged(id, "OVERDUE");
    }

    // Linha 8 — Atrasado → Em andamento: erro pedindo ajustar datas previstas > hoje.
    @Test
    void line08OverdueToInProgressIsBlockedWithGuidance() {
        String id = createProject("OVERDUE", today.minusDays(5), today.minusDays(1), today.minusDays(5), null);

        JsonNode problem = transition(id, "IN_PROGRESS", null, HttpStatus.UNPROCESSABLE_ENTITY);

        assertBlocked(problem, "OVERDUE", "IN_PROGRESS", "Atrasado → Em andamento bloqueado:");
        assertUnchanged(id, "OVERDUE");
    }

    // Linha 9 — Atrasado → Concluído: término realizado = hoje.
    @Test
    void line09OverdueToCompletedSetsActualEnd() {
        String id = createProject("OVERDUE", today.minusDays(5), today.minusDays(1), today.minusDays(5), null);

        JsonNode project = transition(id, "COMPLETED", null, HttpStatus.OK);

        assertThat(project.path("status").asText()).isEqualTo("COMPLETED");
        assertThat(project.path("actualEnd").asText()).isEqualTo(today.toString());
    }

    // Linha 10 — Concluído → A iniciar: erro pedindo remover término realizado e ajustar datas previstas > hoje.
    @Test
    void line10CompletedToNotStartedIsBlockedEvenWhenConfirmed() {
        String id = createProject("COMPLETED", today.minusDays(1), today.plusDays(10), today.minusDays(1), today);

        JsonNode problem = transition(id, "NOT_STARTED", true, HttpStatus.UNPROCESSABLE_ENTITY);

        assertBlocked(problem, "COMPLETED", "NOT_STARTED", "Concluído → A iniciar bloqueado:");
        assertUnchanged(id, "COMPLETED");
    }

    // Linha 11 — Concluído → Em andamento: término realizado = null, com confirmação.
    @Test
    void line11CompletedToInProgressRequiresConfirmationThenClearsActualEnd() {
        String id = createProject("COMPLETED", today.minusDays(1), today.plusDays(10), today.minusDays(1), today);

        JsonNode problem = transition(id, "IN_PROGRESS", false, HttpStatus.UNPROCESSABLE_ENTITY);
        assertConfirmationRequired(problem, "COMPLETED", "IN_PROGRESS", "actualEnd");
        assertThat(get(id).path("actualEnd").asText()).isEqualTo(today.toString());

        JsonNode project = transition(id, "IN_PROGRESS", true, HttpStatus.OK);
        assertThat(project.path("status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(isAbsent(project.path("actualEnd"))).isTrue();
    }

    // Linha 12 — Concluído → Atrasado: término realizado = null, com confirmação e só se as regras classificarem Atrasado.
    @Test
    void line12CompletedToOverdueRequiresConfirmationThenClearsActualEnd() {
        String id = createProject("COMPLETED", today.minusDays(5), today.minusDays(1), today.minusDays(5), today);

        JsonNode problem = transition(id, "OVERDUE", null, HttpStatus.UNPROCESSABLE_ENTITY);
        assertConfirmationRequired(problem, "COMPLETED", "OVERDUE", "actualEnd");

        JsonNode project = transition(id, "OVERDUE", true, HttpStatus.OK);
        assertThat(project.path("status").asText()).isEqualTo("OVERDUE");
        assertThat(project.path("delayDays").asLong()).isEqualTo(1);
        assertThat(isAbsent(project.path("actualEnd"))).isTrue();
    }

    @Test
    void line12IsBlockedWhenClearingActualEndDoesNotClassifyAsOverdue() {
        String id = createProject("COMPLETED", today.minusDays(1), today.plusDays(10), today.minusDays(1), today);

        JsonNode problem = transition(id, "OVERDUE", true, HttpStatus.UNPROCESSABLE_ENTITY);

        assertBlocked(problem, "COMPLETED", "OVERDUE", "Concluído → Atrasado bloqueado:");
        assertUnchanged(id, "COMPLETED");
    }

    // GraphQL — linha 1 (sucesso).
    @Test
    void graphqlLine01NotStartedToInProgress() {
        String id = createProject("NOT_STARTED", today, today.plusDays(10), null, null);

        JsonNode response = graphqlTransition(id, "IN_PROGRESS", null);

        assertThat(response.path("errors").isMissingNode()).isTrue();
        assertThat(response.at("/data/transitionProject/status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(response.at("/data/transitionProject/actualStart").asText()).isEqualTo(today.toString());
    }

    // GraphQL — linha 2 (bloqueio com orientação).
    @Test
    void graphqlLine02IsBlocked() {
        String id = createProject("NOT_STARTED", today.plusDays(1), today.plusDays(10), null, null);

        JsonNode error = graphqlTransition(id, "OVERDUE", null).at("/errors/0");

        assertThat(error.at("/extensions/code").asText()).isEqualTo("TRANSITION_BLOCKED");
        assertThat(error.at("/extensions/currentStatus").asText()).isEqualTo("NOT_STARTED");
        assertThat(error.path("message").asText()).startsWith("A iniciar → Atrasado bloqueado:");
        assertUnchanged(id, "NOT_STARTED");
    }

    // GraphQL — linha 11 (confirmação obrigatória e depois confirmada).
    @Test
    void graphqlLine11RequiresConfirmation() {
        String id = createProject("COMPLETED", today.minusDays(1), today.plusDays(10), today.minusDays(1), today);

        JsonNode error = graphqlTransition(id, "IN_PROGRESS", null).at("/errors/0");
        assertThat(error.at("/extensions/code").asText()).isEqualTo("CONFIRMATION_REQUIRED");
        assertThat(error.at("/extensions/clearedField").asText()).isEqualTo("actualEnd");
        assertUnchanged(id, "COMPLETED");

        JsonNode confirmed = graphqlTransition(id, "IN_PROGRESS", true);
        assertThat(confirmed.path("errors").isMissingNode()).isTrue();
        assertThat(confirmed.at("/data/transitionProject/status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(isAbsent(confirmed.at("/data/transitionProject/actualEnd"))).isTrue();
    }

    private String createProject(
            String expectedStatus,
            LocalDate plannedStart,
            LocalDate plannedEnd,
            LocalDate actualStart,
            LocalDate actualEnd) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Transição " + expectedStatus + " " + UUID.randomUUID());
        body.put("responsibleIds", List.of(responsibleId.toString()));
        putDate(body, "plannedStart", plannedStart);
        putDate(body, "plannedEnd", plannedEnd);
        putDate(body, "actualStart", actualStart);
        putDate(body, "actualEnd", actualEnd);
        JsonNode created = admin.send(HttpMethod.POST, "/api/v1/projects", body, HttpStatus.CREATED);
        assertThat(created.path("status").asText())
                .as("as datas do cenário devem classificar o projeto como %s hoje", expectedStatus)
                .isEqualTo(expectedStatus);
        return created.path("id").asText();
    }

    private JsonNode transition(String id, String status, Boolean confirm, HttpStatus expectedStatus) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        if (confirm != null) {
            body.put("confirm", confirm);
        }
        return admin.send(HttpMethod.PATCH, "/api/v1/projects/" + id + "/status", body, expectedStatus);
    }

    private JsonNode graphqlTransition(String id, String status, Boolean confirm) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("id", id);
        variables.put("status", status);
        if (confirm != null) {
            variables.put("confirm", confirm);
        }
        return admin.graphql(TRANSITION_MUTATION, variables);
    }

    private JsonNode get(String id) {
        return admin.send(HttpMethod.GET, "/api/v1/projects/" + id, null, HttpStatus.OK);
    }

    private void assertUnchanged(String id, String expectedStatus) {
        assertThat(get(id).path("status").asText()).isEqualTo(expectedStatus);
    }

    private static void assertBlocked(JsonNode problem, String currentStatus, String requestedStatus, String detailPrefix) {
        assertThat(problem.path("code").asText()).isEqualTo("TRANSITION_BLOCKED");
        assertThat(problem.path("currentStatus").asText()).isEqualTo(currentStatus);
        assertThat(problem.path("requestedStatus").asText()).isEqualTo(requestedStatus);
        assertThat(problem.path("detail").asText()).startsWith(detailPrefix);
    }

    private static void assertConfirmationRequired(
            JsonNode problem, String currentStatus, String requestedStatus, String clearedField) {
        assertThat(problem.path("code").asText()).isEqualTo("CONFIRMATION_REQUIRED");
        assertThat(problem.path("currentStatus").asText()).isEqualTo(currentStatus);
        assertThat(problem.path("requestedStatus").asText()).isEqualTo(requestedStatus);
        assertThat(problem.path("clearedField").asText()).isEqualTo(clearedField);
        assertThat(problem.path("detail").asText()).contains("Reenvie com confirm = true.");
    }

    private static void putDate(Map<String, Object> body, String field, LocalDate value) {
        if (value != null) {
            body.put(field, value.toString());
        }
    }

    private static boolean isAbsent(JsonNode node) {
        return node.isMissingNode() || node.isNull();
    }
}
