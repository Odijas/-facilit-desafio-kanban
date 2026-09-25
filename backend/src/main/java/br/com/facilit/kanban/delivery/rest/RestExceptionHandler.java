package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.delivery.common.ApiErrorCode;
import br.com.facilit.kanban.domain.common.BusinessRuleException;
import br.com.facilit.kanban.domain.project.ConfirmationRequiredException;
import br.com.facilit.kanban.domain.project.TransitionBlockedException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail handleConflict(ConflictException exception) {
        return problem(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrity(DataIntegrityViolationException exception) {
        // Corrida entre a checagem da aplicação e a restrição do banco (e-mail único, registro em uso).
        LOGGER.warn("Conflito de integridade no banco: tipo={}", exception.getClass().getSimpleName());
        return problem(
                HttpStatus.CONFLICT,
                ApiErrorCode.CONFLICT,
                "A operação conflita com dados já gravados (por exemplo, e-mail já cadastrado ou registro em uso). "
                        + "Recarregue os dados e tente de novo.");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail handleOptimisticLock(OptimisticLockingFailureException exception) {
        return problem(
                HttpStatus.CONFLICT,
                ApiErrorCode.CONFLICT,
                "O registro foi alterado por outra operação ao mesmo tempo. Recarregue os dados e tente de novo.");
    }

    @ExceptionHandler(TransitionBlockedException.class)
    ProblemDetail handleTransitionBlocked(TransitionBlockedException exception) {
        ProblemDetail detail = problem(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ApiErrorCode.TRANSITION_BLOCKED,
                exception.getMessage());
        detail.setProperty("currentStatus", exception.currentStatus().name());
        detail.setProperty("requestedStatus", exception.requestedStatus().name());
        return detail;
    }

    @ExceptionHandler(ConfirmationRequiredException.class)
    ProblemDetail handleConfirmationRequired(ConfirmationRequiredException exception) {
        ProblemDetail detail = problem(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ApiErrorCode.CONFIRMATION_REQUIRED,
                exception.getMessage());
        detail.setProperty("currentStatus", exception.currentStatus().name());
        detail.setProperty("requestedStatus", exception.requestedStatus().name());
        detail.setProperty("clearedField", exception.clearedField());
        return detail;
    }

    @ExceptionHandler(BusinessRuleException.class)
    ProblemDetail handleBusinessRule(BusinessRuleException exception) {
        return problem(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ApiErrorCode.BUSINESS_RULE_VIOLATION,
                exception.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    ProblemDetail handleForbidden(ForbiddenOperationException exception) {
        return problem(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException exception) {
        return problem(
                HttpStatus.UNAUTHORIZED,
                ApiErrorCode.UNAUTHORIZED,
                "E-mail ou senha inválidos.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleBadRequest(IllegalArgumentException exception) {
        return problem(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        ProblemDetail detail = problem(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_ERROR,
                "Dados de entrada inválidos.");
        List<ValidationViolation> violations = exception.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(error -> new ValidationViolation(
                        error.getField(),
                        error.getDefaultMessage() == null ? "valor inválido" : error.getDefaultMessage()))
                .toList();
        detail.setProperty("violations", violations);
        return detail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.INVALID_REQUEST,
                "Valor inválido para o parâmetro: " + exception.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadableBody(HttpMessageNotReadableException exception) {
        return problem(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_REQUEST, "Corpo da requisição malformado.");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception exception) {
        if (exception instanceof ErrorResponse errorResponse) {
            return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse.getBody());
        }
        String incidentId = UUID.randomUUID().toString();
        StackTraceElement origin = exception.getStackTrace().length == 0 ? null : exception.getStackTrace()[0];
        LOGGER.error(
                "Unexpected REST error incidentId={} type={} origin={}",
                incidentId,
                exception.getClass().getName(),
                origin);
        ProblemDetail detail = problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorCode.INTERNAL_ERROR,
                "Erro inesperado.");
        detail.setProperty("incidentId", incidentId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(detail);
    }

    private static ProblemDetail problem(HttpStatus status, ApiErrorCode code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(code.name());
        problem.setProperty("code", code.name());
        return problem;
    }

    public record ValidationViolation(String field, String message) {
    }
}
