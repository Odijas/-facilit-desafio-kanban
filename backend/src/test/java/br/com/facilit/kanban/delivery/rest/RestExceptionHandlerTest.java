package br.com.facilit.kanban.delivery.rest;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.facilit.kanban.domain.common.BusinessRuleException;
import br.com.facilit.kanban.domain.project.ConfirmationRequiredException;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.domain.project.TransitionBlockedException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.server.ResponseStatusException;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void unexpectedExceptionBecomesOpaqueInternalError() {
        ResponseEntity<ProblemDetail> response = handler.handleUnexpected(
                new IllegalStateException("secret internal detail"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("Erro inesperado.");
        assertThat(response.getBody().getProperties())
                .containsEntry("code", "INTERNAL_ERROR")
                .containsKey("incidentId");
        assertThat(response.getBody().toString()).doesNotContain("secret internal detail");
    }

    @Test
    void frameworkErrorResponsesKeepTheirOwnStatus() {
        ResponseEntity<ProblemDetail> response = handler.handleUnexpected(
                new ResponseStatusException(HttpStatus.NOT_FOUND));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void blockedTransitionBecomesUnprocessableWithStableCodeAndStatuses() {
        ProblemDetail problem = handler.handleTransitionBlocked(new TransitionBlockedException(
                ProjectStatus.IN_PROGRESS, ProjectStatus.OVERDUE, "Em andamento → Atrasado bloqueado: ajuste as datas."));

        assertThat(problem.getStatus()).isEqualTo(422);
        assertThat(problem.getTitle()).isEqualTo("TRANSITION_BLOCKED");
        assertThat(problem.getDetail()).isEqualTo("Em andamento → Atrasado bloqueado: ajuste as datas.");
        assertThat(problem.getProperties())
                .containsEntry("code", "TRANSITION_BLOCKED")
                .containsEntry("currentStatus", "IN_PROGRESS")
                .containsEntry("requestedStatus", "OVERDUE");
    }

    @Test
    void missingConfirmationBecomesUnprocessableAndNamesTheFieldThatWouldBeCleared() {
        ProblemDetail problem = handler.handleConfirmationRequired(new ConfirmationRequiredException(
                ProjectStatus.COMPLETED, ProjectStatus.IN_PROGRESS, "actualEnd", "Confirme."));

        assertThat(problem.getStatus()).isEqualTo(422);
        assertThat(problem.getProperties())
                .containsEntry("code", "CONFIRMATION_REQUIRED")
                .containsEntry("currentStatus", "COMPLETED")
                .containsEntry("requestedStatus", "IN_PROGRESS")
                .containsEntry("clearedField", "actualEnd");
    }

    @Test
    void businessRuleViolationBecomesUnprocessable() {
        ProblemDetail problem = handler.handleBusinessRule(new BusinessRuleException("Regra violada."));

        assertThat(problem.getStatus()).isEqualTo(422);
        assertThat(problem.getDetail()).isEqualTo("Regra violada.");
        assertThat(problem.getProperties()).containsEntry("code", "BUSINESS_RULE_VIOLATION");
    }

    @Test
    void databaseConstraintRaceBecomesConflictInsteadOfInternalError() {
        ProblemDetail problem = handler.handleDataIntegrity(
                new DataIntegrityViolationException("duplicate key value violates unique constraint secret_detail"));

        assertThat(problem.getStatus()).isEqualTo(409);
        assertThat(problem.getProperties()).containsEntry("code", "CONFLICT");
        assertThat(problem.getDetail()).doesNotContain("secret_detail");
    }

    @Test
    void concurrentUpdateBecomesConflict() {
        ProblemDetail problem = handler.handleOptimisticLock(
                new ObjectOptimisticLockingFailureException(Object.class, "30000000-0000-4000-8000-000000000001"));

        assertThat(problem.getStatus()).isEqualTo(409);
        assertThat(problem.getProperties()).containsEntry("code", "CONFLICT");
    }
}
