package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.delivery.common.ApiErrorCode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @ExceptionHandler(ForbiddenOperationException.class)
    ProblemDetail handleForbidden(ForbiddenOperationException exception) {
        return problem(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException exception) {
        return problem(
                HttpStatus.UNAUTHORIZED,
                ApiErrorCode.UNAUTHORIZED,
                "Invalid email or password");
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
                "Request validation failed");
        List<ValidationViolation> violations = exception.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(error -> new ValidationViolation(
                        error.getField(),
                        error.getDefaultMessage() == null ? "invalid value" : error.getDefaultMessage()))
                .toList();
        detail.setProperty("violations", violations);
        return detail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.INVALID_REQUEST,
                "Invalid value for parameter: " + exception.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadableBody(HttpMessageNotReadableException exception) {
        return problem(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_REQUEST, "Malformed request body");
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
                "Unexpected error");
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
