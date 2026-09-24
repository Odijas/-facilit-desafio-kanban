package br.com.facilit.kanban.delivery.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.project.ProjectFilter;
import br.com.facilit.kanban.application.project.ProjectService;
import br.com.facilit.kanban.application.project.SaveProjectCommand;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.common.BusinessRuleException;
import br.com.facilit.kanban.domain.project.ConfirmationRequiredException;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectDates;
import br.com.facilit.kanban.domain.project.ProjectScheduleMetrics;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.infrastructure.security.AuthenticatedActorResolver;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Controller GraphQL de projetos isolado (slice GraphQL), com o serviço e o resolvedor de ator simulados.
 */
@GraphQlTest(ProjectGraphQlController.class)
@WithMockUser(roles = "ADMIN")
class ProjectGraphQlControllerTest {

    private static final UUID PROJECT_ID = UUID.fromString("30000000-0000-4000-8000-000000000001");
    private static final UUID RESPONSIBLE_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");
    private static final Instant NOW = Instant.parse("2026-09-24T12:00:00Z");
    private static final Actor ADMIN = Actor.admin();
    private static final String TRANSITION = """
            mutation($id: ID!, $status: ProjectStatus!, $confirm: Boolean) {
              transitionProject(id: $id, status: $status, confirm: $confirm) { id status actualStart }
            }
            """;

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private ProjectService service;

    @MockitoBean
    private AuthenticatedActorResolver actorResolver;

    @BeforeEach
    void resolveAdministrator() {
        when(actorResolver.resolve(any())).thenReturn(ADMIN);
    }

    @Test
    void readsProjectWithCalculatedFields() {
        when(service.get(PROJECT_ID)).thenReturn(project(ProjectStatus.OVERDUE));

        graphQlTester.document("query($id: ID!) { project(id: $id) { id status delayDays remainingTimePercentage } }")
                .variable("id", PROJECT_ID.toString())
                .execute()
                .path("project.status").entity(String.class).isEqualTo("OVERDUE")
                .path("project.delayDays").entity(Integer.class).isEqualTo(2)
                .path("project.remainingTimePercentage").entity(Integer.class).isEqualTo(0);
    }

    @Test
    void searchesWithTheSameFiltersAsRest() {
        when(service.search(any(), any())).thenReturn(
                new PageResult<>(List.of(project(ProjectStatus.OVERDUE)), 0, 10, 1, 1));

        graphQlTester.document("""
                        query {
                          projects(page: 0, size: 10, status: OVERDUE, plannedFrom: "2026-09-01", text: "portal") {
                            content { id }
                            totalPages
                          }
                        }
                        """)
                .execute()
                .path("projects.content[0].id").entity(String.class).isEqualTo(PROJECT_ID.toString());

        ArgumentCaptor<ProjectFilter> filter = ArgumentCaptor.forClass(ProjectFilter.class);
        verify(service).search(filter.capture(), eq(new PageQuery(0, 10)));
        assertThat(filter.getValue()).isEqualTo(new ProjectFilter(
                ProjectStatus.OVERDUE, null, null, LocalDate.of(2026, 9, 1), null, "portal"));
    }

    @Test
    void forwardsExplicitConfirmation() {
        when(service.transition(PROJECT_ID, ProjectStatus.NOT_STARTED, true, ADMIN))
                .thenReturn(project(ProjectStatus.NOT_STARTED));

        graphQlTester.document(TRANSITION)
                .variable("id", PROJECT_ID.toString())
                .variable("status", "NOT_STARTED")
                .variable("confirm", true)
                .execute()
                .path("transitionProject.status").entity(String.class).isEqualTo("NOT_STARTED");

        verify(service).transition(PROJECT_ID, ProjectStatus.NOT_STARTED, true, ADMIN);
    }

    @Test
    void mapsMissingConfirmationToErrorExtensions() {
        when(service.transition(PROJECT_ID, ProjectStatus.NOT_STARTED, false, ADMIN)).thenThrow(
                new ConfirmationRequiredException(
                        ProjectStatus.IN_PROGRESS, ProjectStatus.NOT_STARTED, "actualStart", "Confirme."));

        graphQlTester.document(TRANSITION)
                .variable("id", PROJECT_ID.toString())
                .variable("status", "NOT_STARTED")
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).hasSize(1);
                    assertThat(errors.get(0).getMessage()).isEqualTo("Confirme.");
                    assertThat(errors.get(0).getExtensions())
                            .containsEntry("code", "CONFIRMATION_REQUIRED")
                            .containsEntry("clearedField", "actualStart")
                            .containsEntry("currentStatus", "IN_PROGRESS");
                });
    }

    @Test
    void mapsBusinessRuleViolationOnCreate() {
        when(service.create(any(), eq(ADMIN))).thenThrow(new BusinessRuleException("Regra de datas."));

        graphQlTester.document("""
                        mutation($input: ProjectInput!) { createProject(input: $input) { id } }
                        """)
                .variable("input", Map.of(
                        "name", "Portal",
                        "responsibleIds", List.of(RESPONSIBLE_ID.toString()),
                        "plannedStart", "2026-09-30",
                        "plannedEnd", "2026-09-01"))
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors.get(0).getExtensions())
                        .containsEntry("code", "BUSINESS_RULE_VIOLATION"));

        ArgumentCaptor<SaveProjectCommand> command = ArgumentCaptor.forClass(SaveProjectCommand.class);
        verify(service).create(command.capture(), eq(ADMIN));
        assertThat(command.getValue().plannedStart()).isEqualTo(LocalDate.of(2026, 9, 30));
    }

    private static Project project(ProjectStatus status) {
        boolean started = status != ProjectStatus.NOT_STARTED;
        return new Project(
                PROJECT_ID,
                "Portal",
                Set.of(RESPONSIBLE_ID),
                new ProjectDates(
                        LocalDate.of(2026, 9, 20),
                        LocalDate.of(2026, 9, 22),
                        started ? LocalDate.of(2026, 9, 20) : null,
                        null),
                new ProjectScheduleMetrics(status, status == ProjectStatus.OVERDUE ? 2 : 0, 0),
                new AuditMetadata(NOW, NOW));
    }
}
