package br.com.facilit.kanban.delivery.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
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
import br.com.facilit.kanban.domain.project.TransitionBlockedException;
import br.com.facilit.kanban.infrastructure.security.AuthenticatedActorResolver;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Controller REST de projetos isolado (slice MVC) com o serviço e o resolvedor de ator simulados (Mockito):
 * mapeamento de requisição e resposta, validação e tradução das exceções para o contrato de erro.
 */
@WebMvcTest(ProjectRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectRestControllerTest {

    private static final UUID PROJECT_ID = UUID.fromString("30000000-0000-4000-8000-000000000001");
    private static final UUID RESPONSIBLE_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");
    private static final Instant NOW = Instant.parse("2026-09-24T12:00:00Z");
    private static final Actor ADMIN = Actor.admin();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService service;

    @MockitoBean
    private AuthenticatedActorResolver actorResolver;

    @BeforeEach
    void resolveAdministrator() {
        when(actorResolver.resolve(any())).thenReturn(ADMIN);
    }

    @Test
    void createsProjectAndReturnsLocationAndCalculatedFields() throws Exception {
        when(service.create(any(), eq(ADMIN))).thenReturn(project(ProjectStatus.NOT_STARTED, null));

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Portal", "responsibleIds": ["%s"],
                                 "plannedStart": "2026-09-24", "plannedEnd": "2026-10-04"}
                                """.formatted(RESPONSIBLE_ID)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/projects/" + PROJECT_ID))
                .andExpect(jsonPath("$.id").value(PROJECT_ID.toString()))
                .andExpect(jsonPath("$.status").value("NOT_STARTED"))
                .andExpect(jsonPath("$.remainingTimePercentage").value(100))
                .andExpect(jsonPath("$.delayDays").value(0));

        ArgumentCaptor<SaveProjectCommand> command = ArgumentCaptor.forClass(SaveProjectCommand.class);
        verify(service).create(command.capture(), eq(ADMIN));
        assertThat(command.getValue().name()).isEqualTo("Portal");
        assertThat(command.getValue().responsibleIds()).containsExactly(RESPONSIBLE_ID);
        assertThat(command.getValue().plannedStart()).isEqualTo(LocalDate.of(2026, 9, 24));
        assertThat(command.getValue().actualStart()).isNull();
    }

    @Test
    void rejectsInvalidBodyBeforeCallingTheService() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\", \"responsibleIds\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.detail").value("Dados de entrada inválidos."))
                .andExpect(jsonPath("$.violations[*].field", containsInAnyOrder("name", "responsibleIds")));

        verifyNoInteractions(service);
    }

    @Test
    void rejectsMalformedBody() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Portal\", \"plannedStart\": \"24/09/2026\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.detail").value("Corpo da requisição malformado."));

        verifyNoInteractions(service);
    }

    @Test
    void translatesBusinessRuleViolationOnCreate() throws Exception {
        when(service.create(any(), eq(ADMIN))).thenThrow(new BusinessRuleException(
                "Início realizado (2026-09-25) não pode ser posterior a hoje (2026-09-24)"));

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Portal", "responsibleIds": ["%s"], "actualStart": "2026-09-25"}
                                """.formatted(RESPONSIBLE_ID)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.detail").value(
                        "Início realizado (2026-09-25) não pode ser posterior a hoje (2026-09-24)"));
    }

    @Test
    void returnsNotFoundWithStableCode() throws Exception {
        when(service.get(PROJECT_ID)).thenThrow(new ResourceNotFoundException("Projeto não encontrado: " + PROJECT_ID));

        mockMvc.perform(get("/api/v1/projects/{id}", PROJECT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("Projeto não encontrado: " + PROJECT_ID));
    }

    @Test
    void listsWithAdvancedFiltersAndPagination() throws Exception {
        when(service.search(any(), any())).thenReturn(
                new PageResult<>(List.of(project(ProjectStatus.OVERDUE, null)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/v1/projects")
                        .param("page", "0")
                        .param("size", "20")
                        .param("status", "OVERDUE")
                        .param("responsibleId", RESPONSIBLE_ID.toString())
                        .param("plannedFrom", "2026-09-01")
                        .param("plannedTo", "2026-09-30")
                        .param("text", "portal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(PROJECT_ID.toString()))
                .andExpect(jsonPath("$.content[0].status").value("OVERDUE"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));

        ArgumentCaptor<ProjectFilter> filter = ArgumentCaptor.forClass(ProjectFilter.class);
        verify(service).search(filter.capture(), eq(new PageQuery(0, 20)));
        assertThat(filter.getValue()).isEqualTo(new ProjectFilter(
                ProjectStatus.OVERDUE,
                null,
                RESPONSIBLE_ID,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                "portal"));
    }

    @Test
    void rejectsInvalidPageSize() throws Exception {
        mockMvc.perform(get("/api/v1/projects").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.detail").value("Tamanho de página inválido: size deve estar entre 1 e 100."));

        verifyNoInteractions(service);
    }

    @Test
    void rejectsUnknownStatusParameter() throws Exception {
        mockMvc.perform(get("/api/v1/projects").param("status", "PAUSED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.detail").value("Valor inválido para o parâmetro: status"));
    }

    @Test
    void translatesForbiddenUpdate() throws Exception {
        when(service.update(eq(PROJECT_ID), any(), eq(ADMIN))).thenThrow(new ForbiddenOperationException(
                "O responsável só pode alterar projetos em que é responsável."));

        mockMvc.perform(put("/api/v1/projects/{id}", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Portal\", \"responsibleIds\": [\"%s\"]}".formatted(RESPONSIBLE_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void translatesConcurrentUpdateToConflict() throws Exception {
        when(service.update(eq(PROJECT_ID), any(), eq(ADMIN))).thenThrow(
                new ObjectOptimisticLockingFailureException(Object.class, PROJECT_ID));

        mockMvc.perform(put("/api/v1/projects/{id}", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Portal\", \"responsibleIds\": [\"%s\"]}".formatted(RESPONSIBLE_ID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.detail").value(
                        "O registro foi alterado por outra operação ao mesmo tempo. Recarregue os dados e tente de novo."));
    }

    @Test
    void sendsExplicitConfirmationToTheService() throws Exception {
        when(service.transition(PROJECT_ID, ProjectStatus.NOT_STARTED, true, ADMIN))
                .thenReturn(project(ProjectStatus.NOT_STARTED, null));

        mockMvc.perform(patch("/api/v1/projects/{id}/status", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"NOT_STARTED\", \"confirm\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_STARTED"));

        verify(service).transition(PROJECT_ID, ProjectStatus.NOT_STARTED, true, ADMIN);
    }

    @Test
    void treatsMissingConfirmationAsNotConfirmed() throws Exception {
        when(service.transition(PROJECT_ID, ProjectStatus.COMPLETED, false, ADMIN))
                .thenReturn(project(ProjectStatus.COMPLETED, LocalDate.of(2026, 9, 24)));

        mockMvc.perform(patch("/api/v1/projects/{id}/status", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actualEnd").value("2026-09-24"));

        verify(service).transition(PROJECT_ID, ProjectStatus.COMPLETED, false, ADMIN);
    }

    @Test
    void requiresTheRequestedStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/projects/{id}/status", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirm\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.violations[0].field").value("status"));

        verifyNoInteractions(service);
    }

    @Test
    void translatesBlockedTransition() throws Exception {
        when(service.transition(PROJECT_ID, ProjectStatus.OVERDUE, false, ADMIN)).thenThrow(
                new TransitionBlockedException(ProjectStatus.IN_PROGRESS, ProjectStatus.OVERDUE, "Bloqueado: ajuste."));

        mockMvc.perform(patch("/api/v1/projects/{id}/status", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"OVERDUE\"}"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("TRANSITION_BLOCKED"))
                .andExpect(jsonPath("$.currentStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.requestedStatus").value("OVERDUE"))
                .andExpect(jsonPath("$.detail").value("Bloqueado: ajuste."));
    }

    @Test
    void translatesMissingConfirmation() throws Exception {
        when(service.transition(PROJECT_ID, ProjectStatus.NOT_STARTED, false, ADMIN)).thenThrow(
                new ConfirmationRequiredException(
                        ProjectStatus.IN_PROGRESS, ProjectStatus.NOT_STARTED, "actualStart", "Confirme."));

        mockMvc.perform(patch("/api/v1/projects/{id}/status", PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"NOT_STARTED\"}"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("CONFIRMATION_REQUIRED"))
                .andExpect(jsonPath("$.clearedField").value("actualStart"));
    }

    @Test
    void deletesProject() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/{id}", PROJECT_ID))
                .andExpect(status().isNoContent());

        verify(service).delete(PROJECT_ID, ADMIN);
    }

    @Test
    void translatesDatabaseConflictOnDelete() throws Exception {
        doThrow(new DataIntegrityViolationException("fk violation")).when(service).delete(PROJECT_ID, ADMIN);

        mockMvc.perform(delete("/api/v1/projects/{id}", PROJECT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    private static Project project(ProjectStatus status, LocalDate actualEnd) {
        LocalDate actualStart = status == ProjectStatus.NOT_STARTED ? null : LocalDate.of(2026, 9, 20);
        LocalDate plannedEnd = status == ProjectStatus.OVERDUE ? LocalDate.of(2026, 9, 22) : LocalDate.of(2026, 10, 4);
        return new Project(
                PROJECT_ID,
                "Portal",
                Set.of(RESPONSIBLE_ID),
                new ProjectDates(LocalDate.of(2026, 9, 20), plannedEnd, actualStart, actualEnd),
                new ProjectScheduleMetrics(status, status == ProjectStatus.OVERDUE ? 2 : 0,
                        status == ProjectStatus.NOT_STARTED ? 100 : 0),
                new AuditMetadata(NOW, NOW));
    }
}
