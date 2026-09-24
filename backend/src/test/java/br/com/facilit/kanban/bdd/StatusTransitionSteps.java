package br.com.facilit.kanban.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectDates;
import br.com.facilit.kanban.domain.project.ProjectScheduleCalculator;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.domain.project.ProjectStatusTransition;
import br.com.facilit.kanban.domain.project.ProjectTransitionResult;
import br.com.facilit.kanban.domain.project.TransitionBlockedException;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public class StatusTransitionSteps {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 24);
    private static final Instant NOW = Instant.parse("2026-09-24T12:00:00Z");
    private static final UUID RESPONSIBLE_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");

    private final ProjectScheduleCalculator calculator = new ProjectScheduleCalculator();
    private final ProjectStatusTransition transition = new ProjectStatusTransition(calculator);

    private Project project;
    private ProjectTransitionResult result;
    private RuntimeException failure;

    @Dado("um projeto no cenário {string} classificado como {string}")
    public void givenProject(String scenario, String expectedOrigin) {
        ProjectDates dates = switch (scenario) {
            case "not_started_future" -> new ProjectDates(TODAY.plusDays(1), TODAY.plusDays(10), null, null);
            case "in_progress_future" -> new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, null);
            case "overdue_not_started" -> new ProjectDates(TODAY.minusDays(2), TODAY.plusDays(5), null, null);
            case "completed_future" -> new ProjectDates(TODAY, TODAY.plusDays(10), TODAY, TODAY);
            case "completed_overdue" ->
                new ProjectDates(TODAY.minusDays(10), TODAY.minusDays(1), TODAY.minusDays(10), TODAY);
            default -> throw new IllegalArgumentException("Cenário BDD desconhecido: " + scenario);
        };
        var schedule = calculator.calculate(dates, TODAY);
        assertThat(schedule.status()).isEqualTo(ProjectStatus.valueOf(expectedOrigin));
        project = new Project(
                UUID.randomUUID(),
                "Projeto BDD",
                Set.of(RESPONSIBLE_ID),
                dates,
                schedule,
                new AuditMetadata(NOW, NOW));
        result = null;
        failure = null;
    }

    @Quando("solicito a transição para {string} com confirmação {string}")
    public void whenTransition(String target, String confirmed) {
        try {
            result = transition.transition(
                    project,
                    ProjectStatus.valueOf(target),
                    TODAY,
                    Boolean.parseBoolean(confirmed));
        } catch (RuntimeException exception) {
            failure = exception;
        }
    }

    @Entao("o resultado da transição é {string}")
    public void thenResult(String expected) {
        if ("BLOCKED".equals(expected)) {
            assertThat(failure).isInstanceOf(TransitionBlockedException.class);
            assertThat(result).isNull();
            return;
        }
        assertThat(failure).isNull();
        assertThat(result).isNotNull();
        assertThat(result.schedule().status()).isEqualTo(ProjectStatus.valueOf(expected));
    }
}
