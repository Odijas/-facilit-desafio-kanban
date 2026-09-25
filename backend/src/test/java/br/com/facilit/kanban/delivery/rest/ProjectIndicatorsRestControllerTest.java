package br.com.facilit.kanban.delivery.rest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.facilit.kanban.application.project.ProjectDeadlineIndicator;
import br.com.facilit.kanban.application.project.ProjectDeadlineIndicators;
import br.com.facilit.kanban.application.project.ProjectGroupIndicator;
import br.com.facilit.kanban.application.project.ProjectIndicators;
import br.com.facilit.kanban.application.project.ProjectService;
import br.com.facilit.kanban.application.project.ProjectStatusIndicator;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Controller REST de indicadores isolado (slice MVC), com o serviço simulado (Mockito).
 */
@WebMvcTest(ProjectIndicatorsRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectIndicatorsRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService service;

    @Test
    void mapsCountAndAverageDelayByStatus() throws Exception {
        when(service.indicators()).thenReturn(new ProjectIndicators(4, 2, List.of(
                new ProjectStatusIndicator(ProjectStatus.NOT_STARTED, 1, 0),
                new ProjectStatusIndicator(ProjectStatus.IN_PROGRESS, 1, 0),
                new ProjectStatusIndicator(ProjectStatus.OVERDUE, 2, 4.5),
                new ProjectStatusIndicator(ProjectStatus.COMPLETED, 0, 0))));

        mockMvc.perform(get("/api/v1/indicators/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(4))
                .andExpect(jsonPath("$.delayedProjects").value(2))
                .andExpect(jsonPath("$.byStatus[2].status").value("OVERDUE"))
                .andExpect(jsonPath("$.byStatus[2].projectCount").value(2))
                .andExpect(jsonPath("$.byStatus[2].averageDelayDays").value(4.5));
    }

    @Test
    void mapsIndicatorsBySecretariat() throws Exception {
        UUID secretariatId = UUID.fromString("10000000-0000-4000-8000-000000000001");
        when(service.indicatorsBySecretariat())
                .thenReturn(List.of(new ProjectGroupIndicator(secretariatId, 3, 2.5)));

        mockMvc.perform(get("/api/v1/indicators/projects/by-secretariat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(secretariatId.toString()))
                .andExpect(jsonPath("$[0].projectCount").value(3))
                .andExpect(jsonPath("$[0].averageDelayDays").value(2.5));
    }

    @Test
    void mapsIndicatorsByResponsible() throws Exception {
        UUID responsibleId = UUID.fromString("20000000-0000-4000-8000-000000000001");
        when(service.indicatorsByResponsible())
                .thenReturn(List.of(new ProjectGroupIndicator(responsibleId, 2, 1.5)));

        mockMvc.perform(get("/api/v1/indicators/projects/by-responsible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responsibleId.toString()))
                .andExpect(jsonPath("$[0].projectCount").value(2))
                .andExpect(jsonPath("$[0].averageDelayDays").value(1.5));
    }

    @Test
    void mapsDeadlinesUsingSevenDaysByDefault() throws Exception {
        UUID projectId = UUID.fromString("30000000-0000-4000-8000-000000000001");
        LocalDate today = LocalDate.of(2026, 9, 24);
        when(service.deadlines(7)).thenReturn(new ProjectDeadlineIndicators(
                7,
                today,
                today.plusDays(7),
                List.of(new ProjectDeadlineIndicator(
                        projectId,
                        "Portal",
                        ProjectStatus.IN_PROGRESS,
                        today.plusDays(3),
                        3))));

        mockMvc.perform(get("/api/v1/indicators/projects/deadlines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.withinDays").value(7))
                .andExpect(jsonPath("$.from").value("2026-09-24"))
                .andExpect(jsonPath("$.to").value("2026-10-01"))
                .andExpect(jsonPath("$.projects[0].projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.projects[0].daysUntilDeadline").value(3));

        verify(service).deadlines(7);
    }

    @Test
    void mapsInvalidDeadlineWindowToBadRequest() throws Exception {
        when(service.deadlines(0)).thenThrow(new IllegalArgumentException("withinDays deve estar entre 1 e 90."));

        mockMvc.perform(get("/api/v1/indicators/projects/deadlines?withinDays=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.detail").value("withinDays deve estar entre 1 e 90."));
    }

    @Test
    void hidesUnexpectedErrorsBehindAnIncidentId() throws Exception {
        when(service.indicators()).thenThrow(new IllegalStateException("detalhe interno sigiloso"));

        mockMvc.perform(get("/api/v1/indicators/projects"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.detail").value("Erro inesperado."))
                .andExpect(jsonPath("$.incidentId").exists());
    }
}
