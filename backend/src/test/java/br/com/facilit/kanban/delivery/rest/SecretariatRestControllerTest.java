package br.com.facilit.kanban.delivery.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.application.secretariat.SaveSecretariatCommand;
import br.com.facilit.kanban.application.secretariat.SecretariatService;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.secretariat.Secretariat;
import br.com.facilit.kanban.infrastructure.security.AuthenticatedActorResolver;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Controller REST de secretarias isolado (slice MVC), com o serviço simulado (Mockito).
 */
@WebMvcTest(SecretariatRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class SecretariatRestControllerTest {

    private static final UUID SECRETARIAT_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final Instant NOW = Instant.parse("2026-09-24T12:00:00Z");
    private static final Actor ADMIN = Actor.admin();
    private static final Secretariat DIGITAL = new Secretariat(SECRETARIAT_ID, "Secretaria Digital",
            new AuditMetadata(NOW, NOW));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SecretariatService service;

    @MockitoBean
    private AuthenticatedActorResolver actorResolver;

    @BeforeEach
    void resolveAdministrator() {
        when(actorResolver.resolve(any())).thenReturn(ADMIN);
    }

    @Test
    void createsSecretariat() throws Exception {
        when(service.create(new SaveSecretariatCommand("Secretaria Digital"), ADMIN)).thenReturn(DIGITAL);

        mockMvc.perform(post("/api/v1/secretariats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Secretaria Digital\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(SECRETARIAT_ID.toString()))
                .andExpect(jsonPath("$.name").value("Secretaria Digital"));
    }

    @Test
    void rejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/v1/secretariats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.violations[0].field").value("name"));

        verifyNoInteractions(service);
    }

    @Test
    void translatesForbiddenCreationByResponsible() throws Exception {
        when(service.create(any(), any())).thenThrow(
                new ForbiddenOperationException("Apenas o administrador pode realizar esta operação."));

        mockMvc.perform(post("/api/v1/secretariats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Secretaria Digital\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.detail").value("Apenas o administrador pode realizar esta operação."));
    }

    @Test
    void getsListsAndUpdatesSecretariats() throws Exception {
        when(service.get(SECRETARIAT_ID)).thenReturn(DIGITAL);
        when(service.list(new PageQuery(0, 20))).thenReturn(new PageResult<>(List.of(DIGITAL), 0, 20, 1, 1));
        when(service.update(SECRETARIAT_ID, new SaveSecretariatCommand("Secretaria Digital"), ADMIN))
                .thenReturn(DIGITAL);

        mockMvc.perform(get("/api/v1/secretariats/{id}", SECRETARIAT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Secretaria Digital"));
        mockMvc.perform(get("/api/v1/secretariats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(SECRETARIAT_ID.toString()));
        mockMvc.perform(put("/api/v1/secretariats/{id}", SECRETARIAT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Secretaria Digital\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void returnsNotFound() throws Exception {
        when(service.get(SECRETARIAT_ID)).thenThrow(
                new ResourceNotFoundException("Secretaria não encontrada: " + SECRETARIAT_ID));

        mockMvc.perform(get("/api/v1/secretariats/{id}", SECRETARIAT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Secretaria não encontrada: " + SECRETARIAT_ID));
    }

    @Test
    void deletesOrReportsLinkedResponsibles() throws Exception {
        mockMvc.perform(delete("/api/v1/secretariats/{id}", SECRETARIAT_ID))
                .andExpect(status().isNoContent());
        verify(service).delete(SECRETARIAT_ID, ADMIN);

        doThrow(new ConflictException("A secretaria tem responsáveis vinculados e não pode ser excluída."))
                .when(service).delete(SECRETARIAT_ID, ADMIN);
        mockMvc.perform(delete("/api/v1/secretariats/{id}", SECRETARIAT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }
}
