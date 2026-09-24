package br.com.facilit.kanban.delivery.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
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
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Controller GraphQL de secretarias isolado (slice GraphQL), com o serviço simulado.
 */
@GraphQlTest(SecretariatGraphQlController.class)
@WithMockUser(roles = "ADMIN")
class SecretariatGraphQlControllerTest {

    private static final UUID SECRETARIAT_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final Instant NOW = Instant.parse("2026-09-24T12:00:00Z");
    private static final Actor ADMIN = Actor.admin();
    private static final Secretariat DIGITAL = new Secretariat(SECRETARIAT_ID, "Secretaria Digital",
            new AuditMetadata(NOW, NOW));

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private SecretariatService service;

    @MockitoBean
    private AuthenticatedActorResolver actorResolver;

    @BeforeEach
    void resolveAdministrator() {
        when(actorResolver.resolve(any())).thenReturn(ADMIN);
    }

    @Test
    void createsAndListsSecretariats() {
        when(service.create(new SaveSecretariatCommand("Secretaria Digital"), ADMIN)).thenReturn(DIGITAL);
        when(service.list(new PageQuery(0, 20))).thenReturn(new PageResult<>(List.of(DIGITAL), 0, 20, 1, 1));

        graphQlTester.document("mutation { createSecretariat(input: {name: \"Secretaria Digital\"}) { id name } }")
                .execute()
                .path("createSecretariat.id").entity(String.class).isEqualTo(SECRETARIAT_ID.toString());
        graphQlTester.document("{ secretariats { content { name } totalPages } }")
                .execute()
                .path("secretariats.content[0].name").entity(String.class).isEqualTo("Secretaria Digital");
    }

    @Test
    void mapsConflictWhenSecretariatHasResponsibles() {
        doThrow(new ConflictException("A secretaria tem responsáveis vinculados e não pode ser excluída."))
                .when(service).delete(SECRETARIAT_ID, ADMIN);

        graphQlTester.document("mutation($id: ID!) { deleteSecretariat(id: $id) }")
                .variable("id", SECRETARIAT_ID.toString())
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors.get(0).getExtensions()).containsEntry("code", "CONFLICT"));
    }
}
