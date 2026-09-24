package br.com.facilit.kanban.delivery.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.facilit.kanban.application.project.ProjectIndicators;
import br.com.facilit.kanban.application.project.ProjectService;
import br.com.facilit.kanban.application.project.ProjectStatusIndicator;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.util.List;
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
    void hidesUnexpectedErrorsBehindAnIncidentId() throws Exception {
        when(service.indicators()).thenThrow(new IllegalStateException("detalhe interno sigiloso"));

        mockMvc.perform(get("/api/v1/indicators/projects"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.detail").value("Erro inesperado."))
                .andExpect(jsonPath("$.incidentId").exists());
    }
}
