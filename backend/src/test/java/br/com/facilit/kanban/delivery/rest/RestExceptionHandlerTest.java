package br.com.facilit.kanban.delivery.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void unexpectedExceptionBecomesOpaqueInternalError() {
        ResponseEntity<ProblemDetail> response = handler.handleUnexpected(
                new IllegalStateException("secret internal detail"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("Unexpected error");
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
}
