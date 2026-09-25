package br.com.facilit.kanban.delivery.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
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
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Controller GraphQL de responsáveis isolado (slice GraphQL), com os serviços simulados.
 */
@GraphQlTest(ResponsibleGraphQlController.class)
@WithMockUser(roles = "ADMIN")
class ResponsibleGraphQlControllerTest {

    private static final UUID RESPONSIBLE_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");
    private static final Instant NOW = Instant.parse("2026-09-24T12:00:00Z");
    private static final Actor ADMIN = Actor.admin();

    @Autowired
    private GraphQlTester graphQlTester;

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
    void createsResponsible() {
        SaveResponsibleCommand command = new SaveResponsibleCommand(
                "Maria Silva", "maria.silva@example.com", "Analista", null);
        when(service.create(command, ADMIN)).thenReturn(new Responsible(
                RESPONSIBLE_ID, "Maria Silva", "maria.silva@example.com", "Analista", null,
                new AuditMetadata(NOW, NOW)));

        graphQlTester.document("mutation($input: ResponsibleInput!) { createResponsible(input: $input) { id email } }")
                .variable("input", Map.of(
                        "name", "Maria Silva",
                        "email", "maria.silva@example.com",
                        "position", "Analista"))
                .execute()
                .path("createResponsible.id").entity(String.class).isEqualTo(RESPONSIBLE_ID.toString());

        verify(service).create(command, ADMIN);
    }

    @Test
    void readsListsUpdatesDeletesAndRevokesResponsible() {
        UUID secretariatId = UUID.fromString("10000000-0000-4000-8000-000000000001");
        Responsible responsible = new Responsible(
                RESPONSIBLE_ID,
                "Maria Silva",
                "maria.silva@example.com",
                "Analista",
                secretariatId,
                new AuditMetadata(NOW, NOW));
        SaveResponsibleCommand command = new SaveResponsibleCommand(
                "Maria Silva", "maria.silva@example.com", "Analista", secretariatId);

        when(service.get(RESPONSIBLE_ID)).thenReturn(responsible);
        when(service.list(new PageQuery(1, 2))).thenReturn(new PageResult<>(List.of(responsible), 1, 2, 3, 2));
        when(service.update(RESPONSIBLE_ID, command, ADMIN)).thenReturn(responsible);

        graphQlTester.document("query($id: ID!) { responsible(id: $id) { id secretariatId } }")
                .variable("id", RESPONSIBLE_ID.toString())
                .execute()
                .path("responsible.secretariatId").entity(String.class).isEqualTo(secretariatId.toString());

        graphQlTester.document("{ responsibles(page: 1, size: 2) { content { id } page size totalPages hasNext hasPrevious } }")
                .execute()
                .path("responsibles.page").entity(Integer.class).isEqualTo(1)
                .path("responsibles.totalPages").entity(Integer.class).isEqualTo(2)
                .path("responsibles.hasPrevious").entity(Boolean.class).isEqualTo(true);

        graphQlTester.document("""
                        mutation($id: ID!, $input: ResponsibleInput!) {
                          updateResponsible(id: $id, input: $input) { id secretariatId }
                        }
                        """)
                .variable("id", RESPONSIBLE_ID.toString())
                .variable("input", Map.of(
                        "name", "Maria Silva",
                        "email", "maria.silva@example.com",
                        "position", "Analista",
                        "secretariatId", secretariatId.toString()))
                .execute()
                .path("updateResponsible.id").entity(String.class).isEqualTo(RESPONSIBLE_ID.toString());

        graphQlTester.document("mutation($id: ID!) { deleteResponsible(id: $id) }")
                .variable("id", RESPONSIBLE_ID.toString())
                .execute()
                .path("deleteResponsible").entity(Boolean.class).isEqualTo(true);

        graphQlTester.document("mutation($id: ID!) { revokeResponsibleCredentials(responsibleId: $id) }")
                .variable("id", RESPONSIBLE_ID.toString())
                .execute()
                .path("revokeResponsibleCredentials").entity(Boolean.class).isEqualTo(true);

        verify(service).get(RESPONSIBLE_ID);
        verify(service).list(new PageQuery(1, 2));
        verify(service).update(RESPONSIBLE_ID, command, ADMIN);
        verify(service).delete(RESPONSIBLE_ID, ADMIN);
        verify(credentialService).revoke(RESPONSIBLE_ID, ADMIN);
    }

    @Test
    void mapsPreviouslyUncoveredGraphQlErrors() {
        String query = "query($id: ID!) { responsible(id: $id) { id } }";

        doThrow(new ResourceNotFoundException("Responsável não encontrado."))
                .when(service).get(RESPONSIBLE_ID);
        graphQlTester.document(query)
                .variable("id", RESPONSIBLE_ID.toString())
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors.get(0).getExtensions())
                        .containsEntry("code", "RESOURCE_NOT_FOUND"));

        doThrow(new DataIntegrityViolationException("conflito"))
                .when(service).get(RESPONSIBLE_ID);
        graphQlTester.document(query)
                .variable("id", RESPONSIBLE_ID.toString())
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors.get(0).getExtensions())
                        .containsEntry("code", "CONFLICT"));

        doThrow(new OptimisticLockingFailureException("concorrência"))
                .when(service).get(RESPONSIBLE_ID);
        graphQlTester.document(query)
                .variable("id", RESPONSIBLE_ID.toString())
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors.get(0).getExtensions())
                        .containsEntry("code", "CONFLICT"));

        doThrow(new RuntimeException("falha interna"))
                .when(service).get(RESPONSIBLE_ID);
        graphQlTester.document(query)
                .variable("id", RESPONSIBLE_ID.toString())
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.get(0).getMessage()).isEqualTo("Erro inesperado.");
                    assertThat(errors.get(0).getExtensions())
                            .containsEntry("code", "INTERNAL_ERROR")
                            .containsKey("incidentId");
                });
    }

    @Test
    void setsCredentials() {
        graphQlTester.document("""
                        mutation($id: ID!) { setResponsibleCredentials(responsibleId: $id, password: "senha-segura-123") }
                        """)
                .variable("id", RESPONSIBLE_ID.toString())
                .execute()
                .path("setResponsibleCredentials").entity(Boolean.class).isEqualTo(true);

        verify(credentialService).setPassword(RESPONSIBLE_ID, "senha-segura-123", ADMIN);
    }

    @Test
    void mapsForbiddenOperation() {
        doThrow(new ForbiddenOperationException("Apenas o administrador pode realizar esta operação."))
                .when(service).delete(RESPONSIBLE_ID, ADMIN);

        graphQlTester.document("mutation($id: ID!) { deleteResponsible(id: $id) }")
                .variable("id", RESPONSIBLE_ID.toString())
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors.get(0).getMessage())
                            .isEqualTo("Apenas o administrador pode realizar esta operação.");
                    assertThat(errors.get(0).getExtensions()).containsEntry("code", "FORBIDDEN");
                });
    }
}
