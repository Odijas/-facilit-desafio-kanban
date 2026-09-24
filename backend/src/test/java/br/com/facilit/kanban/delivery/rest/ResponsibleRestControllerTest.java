package br.com.facilit.kanban.delivery.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.application.responsible.ResponsibleCredentialService;
import br.com.facilit.kanban.application.responsible.ResponsibleService;
import br.com.facilit.kanban.application.responsible.SaveResponsibleCommand;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.responsible.Responsible;
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
 * Controller REST de responsáveis e credenciais isolado (slice MVC), com os serviços simulados (Mockito).
 */
@WebMvcTest(ResponsibleRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResponsibleRestControllerTest {

    private static final UUID RESPONSIBLE_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");
    private static final Instant NOW = Instant.parse("2026-09-24T12:00:00Z");
    private static final Actor ADMIN = Actor.admin();
    private static final Responsible MARIA = new Responsible(
            RESPONSIBLE_ID, "Maria Silva", "maria.silva@example.com", "Analista", null, new AuditMetadata(NOW, NOW));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResponsibleService service;

    @MockitoBean
    private ResponsibleCredentialService credentialService;

    @MockitoBean
    private AuthenticatedActorResolver actorResolver;

    @BeforeEach
    void resolveAdministrator() {
        when(actorResolver.resolve(any())).thenReturn(ADMIN);
    }

    @Test
    void createsResponsible() throws Exception {
        SaveResponsibleCommand command = new SaveResponsibleCommand(
                "Maria Silva", "maria.silva@example.com", "Analista", null);
        when(service.create(command, ADMIN)).thenReturn(MARIA);

        mockMvc.perform(post("/api/v1/responsibles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Maria Silva", "email": "maria.silva@example.com", "position": "Analista"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/responsibles/" + RESPONSIBLE_ID))
                .andExpect(jsonPath("$.email").value("maria.silva@example.com"));

        verify(service).create(command, ADMIN);
    }

    @Test
    void rejectsMalformedEmailWithMessageInPortuguese() throws Exception {
        mockMvc.perform(post("/api/v1/responsibles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Maria\", \"email\": \"sem-arroba\", \"position\": \"Analista\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.violations[0].field").value("email"))
                .andExpect(jsonPath("$.violations[0].message").value("deve ser um endereço de e-mail bem formado"));

        verifyNoInteractions(service);
    }

    @Test
    void translatesDuplicateEmailToConflict() throws Exception {
        when(service.create(any(), eq(ADMIN))).thenThrow(new ConflictException("Já existe responsável com este e-mail."));

        mockMvc.perform(post("/api/v1/responsibles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Maria Silva", "email": "maria.silva@example.com", "position": "Analista"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.detail").value("Já existe responsável com este e-mail."));
    }

    @Test
    void returnsNotFound() throws Exception {
        when(service.get(RESPONSIBLE_ID)).thenThrow(
                new ResourceNotFoundException("Responsável não encontrado: " + RESPONSIBLE_ID));

        mockMvc.perform(get("/api/v1/responsibles/{id}", RESPONSIBLE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void listsWithPagination() throws Exception {
        when(service.list(new PageQuery(1, 5))).thenReturn(new PageResult<>(List.of(MARIA), 1, 5, 6, 2));

        mockMvc.perform(get("/api/v1/responsibles").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Maria Silva"))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.hasPrevious").value(true))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void updatesResponsible() throws Exception {
        when(service.update(eq(RESPONSIBLE_ID), any(), eq(ADMIN))).thenReturn(MARIA);

        mockMvc.perform(put("/api/v1/responsibles/{id}", RESPONSIBLE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Maria Silva", "email": "maria.silva@example.com", "position": "Analista"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(RESPONSIBLE_ID.toString()));
    }

    @Test
    void translatesDeleteOfLinkedResponsibleToConflict() throws Exception {
        doThrow(new ConflictException("O responsável está vinculado a ao menos um projeto e não pode ser excluído."))
                .when(service).delete(RESPONSIBLE_ID, ADMIN);

        mockMvc.perform(delete("/api/v1/responsibles/{id}", RESPONSIBLE_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void definesAndRevokesCredentials() throws Exception {
        mockMvc.perform(put("/api/v1/responsibles/{id}/credentials", RESPONSIBLE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\": \"senha-segura-123\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/responsibles/{id}/credentials", RESPONSIBLE_ID))
                .andExpect(status().isNoContent());

        verify(credentialService).setPassword(RESPONSIBLE_ID, "senha-segura-123", ADMIN);
        verify(credentialService).revoke(RESPONSIBLE_ID, ADMIN);
    }

    @Test
    void translatesWeakPasswordToInvalidRequest() throws Exception {
        doThrow(new IllegalArgumentException("A senha deve ter ao menos 12 caracteres."))
                .when(credentialService).setPassword(RESPONSIBLE_ID, "curta", ADMIN);

        mockMvc.perform(put("/api/v1/responsibles/{id}/credentials", RESPONSIBLE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\": \"curta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.detail").value("A senha deve ter ao menos 12 caracteres."));
    }
}
