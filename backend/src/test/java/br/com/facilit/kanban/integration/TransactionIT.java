package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.TransactionRunner;
import br.com.facilit.kanban.application.project.ProjectRepository;
import br.com.facilit.kanban.application.project.ProjectService;
import br.com.facilit.kanban.application.project.SaveProjectCommand;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.domain.project.TransitionBlockedException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Transações dos casos de uso com o PostgreSQL real: falha no meio desfaz tudo o que o caso de uso gravou, e
 * duas edições simultâneas do mesmo projeto não se sobrescrevem (a segunda a gravar recebe conflito).
 */
@Testcontainers
@SpringBootTest(properties = "DB_PASSWORD=test-only")
class TransactionIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine3.24"));

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TransactionRunner transactions;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Clock clock;

    private UUID responsibleId;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        responsibleId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO responsibles (id, name, email, position, created_at, updated_at)
                VALUES (?, 'Responsável transação', ?, 'Analista', now(), now())
                """,
                responsibleId,
                "transaction-" + responsibleId + "@example.invalid");
        today = LocalDate.now(clock);
    }

    @Test
    void failureAfterTheUseCaseWroteRollsBackEverything() {
        String name = "Rollback " + UUID.randomUUID();

        assertThatThrownBy(() -> transactions.execute(() -> {
            Project created = projectService.create(command(name, today, today.plusDays(10)), Actor.admin());
            entityManager.flush();
            assertThat(countByName(name)).as("INSERT já enviado ao banco dentro da transação").isEqualTo(1);
            throw new IllegalStateException("falha depois da gravação " + created.id());
        }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("falha depois da gravação");

        assertThat(countByName(name)).isZero();
    }

    @Test
    void blockedTransitionRollsBackTheRecalculationDoneInsideTheUseCase() {
        Project project = projectService.create(
                command("Recalculado e desfeito " + UUID.randomUUID(), today.plusDays(1), today.plusDays(10)),
                Actor.admin());
        LocalDate twoDaysAgo = today.minusDays(2);
        simulateCalculatedOn(project.id(), twoDaysAgo);

        // A leitura dentro do caso de uso recalcula o projeto e grava; o bloqueio desfaz essa gravação junto.
        assertThatThrownBy(() -> projectService.transition(project.id(), ProjectStatus.OVERDUE, false, Actor.admin()))
                .isInstanceOf(TransitionBlockedException.class);

        assertThat(calculatedOn(project.id())).isEqualTo(twoDaysAgo);
        assertThat(version(project.id())).isZero();
    }

    @Test
    void concurrentEditsOfTheSameProjectDoNotOverwriteEachOther() {
        Project project = projectService.create(
                command("Original " + UUID.randomUUID(), today, today.plusDays(10)), Actor.admin());
        TransactionTemplate independentTransaction = new TransactionTemplate(transactionManager);
        independentTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // Edição 1 lê o projeto (versão 0); a edição 2 lê, grava e confirma; só então a edição 1 grava.
        assertThatThrownBy(() -> transactions.execute(() -> {
            assertThat(projectRepository.findById(project.id())).isPresent();
            independentTransaction.executeWithoutResult(status -> projectService.update(
                    project.id(), command("Edição 2", today, today.plusDays(20)), Actor.admin()));
            return projectService.update(project.id(), command("Edição 1", today, today.plusDays(30)), Actor.admin());
        })).isInstanceOf(OptimisticLockingFailureException.class);

        Project stored = projectRepository.findById(project.id()).orElseThrow();
        assertThat(stored.name()).isEqualTo("Edição 2");
        assertThat(stored.dates().plannedEnd()).isEqualTo(today.plusDays(20));
        assertThat(version(project.id())).isEqualTo(1);
    }

    @Test
    void sequentialEditsIncrementTheVersion() {
        Project project = projectService.create(
                command("Sequencial " + UUID.randomUUID(), today, today.plusDays(10)), Actor.admin());

        projectService.update(project.id(), command("Primeira", today, today.plusDays(11)), Actor.admin());
        projectService.transition(project.id(), ProjectStatus.IN_PROGRESS, false, Actor.admin());

        assertThat(version(project.id())).isEqualTo(2);
        assertThat(projectRepository.findById(project.id()).orElseThrow().status()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    private SaveProjectCommand command(String name, LocalDate plannedStart, LocalDate plannedEnd) {
        return new SaveProjectCommand(name, Set.of(responsibleId), plannedStart, plannedEnd, null, null);
    }

    private int countByName(String name) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM projects WHERE name = ?", Integer.class, name);
        return count == null ? 0 : count;
    }

    private void simulateCalculatedOn(UUID projectId, LocalDate calculatedOn) {
        int updated = jdbcTemplate.update(
                "UPDATE projects SET schedule_calculated_on = ? WHERE id = ?", calculatedOn, projectId);
        assertThat(updated).isEqualTo(1);
    }

    private LocalDate calculatedOn(UUID projectId) {
        return jdbcTemplate.queryForObject(
                "SELECT schedule_calculated_on FROM projects WHERE id = ?", LocalDate.class, projectId);
    }

    private long version(UUID projectId) {
        Long version = jdbcTemplate.queryForObject("SELECT version FROM projects WHERE id = ?", Long.class, projectId);
        return version == null ? -1 : version;
    }
}
