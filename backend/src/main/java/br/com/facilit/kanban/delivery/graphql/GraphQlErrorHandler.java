package br.com.facilit.kanban.delivery.graphql;

import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.delivery.common.ApiErrorCode;
import br.com.facilit.kanban.domain.common.BusinessRuleException;
import br.com.facilit.kanban.domain.project.ConfirmationRequiredException;
import br.com.facilit.kanban.domain.project.TransitionBlockedException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import jakarta.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GraphQlErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQlErrorHandler.class);

    @GraphQlExceptionHandler
    public GraphQLError handleNotFound(
            GraphqlErrorBuilder<?> errorBuilder,
            ResourceNotFoundException exception) {
        return errorBuilder
                .errorType(ErrorType.NOT_FOUND)
                .message(exception.getMessage())
                .extensions(code(ApiErrorCode.RESOURCE_NOT_FOUND))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleConflict(
            GraphqlErrorBuilder<?> errorBuilder,
            ConflictException exception) {
        return errorBuilder
                .errorType(ErrorType.BAD_REQUEST)
                .message(exception.getMessage())
                .extensions(code(ApiErrorCode.CONFLICT))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleDataIntegrity(
            GraphqlErrorBuilder<?> errorBuilder,
            DataIntegrityViolationException exception) {
        LOGGER.warn("Conflito de integridade no banco: tipo={}", exception.getClass().getSimpleName());
        return errorBuilder
                .errorType(ErrorType.BAD_REQUEST)
                .message("A operação conflita com dados já gravados (por exemplo, e-mail já cadastrado ou registro "
                        + "em uso). Recarregue os dados e tente de novo.")
                .extensions(code(ApiErrorCode.CONFLICT))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleOptimisticLock(
            GraphqlErrorBuilder<?> errorBuilder,
            OptimisticLockingFailureException exception) {
        return errorBuilder
                .errorType(ErrorType.BAD_REQUEST)
                .message("O registro foi alterado por outra operação ao mesmo tempo. Recarregue os dados e tente "
                        + "de novo.")
                .extensions(code(ApiErrorCode.CONFLICT))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleTransitionBlocked(
            GraphqlErrorBuilder<?> errorBuilder,
            TransitionBlockedException exception) {
        return errorBuilder
                .errorType(ErrorType.BAD_REQUEST)
                .message(exception.getMessage())
                .extensions(Map.of(
                        "code", ApiErrorCode.TRANSITION_BLOCKED.name(),
                        "currentStatus", exception.currentStatus().name(),
                        "requestedStatus", exception.requestedStatus().name()))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleConfirmationRequired(
            GraphqlErrorBuilder<?> errorBuilder,
            ConfirmationRequiredException exception) {
        return errorBuilder
                .errorType(ErrorType.BAD_REQUEST)
                .message(exception.getMessage())
                .extensions(Map.of(
                        "code", ApiErrorCode.CONFIRMATION_REQUIRED.name(),
                        "currentStatus", exception.currentStatus().name(),
                        "requestedStatus", exception.requestedStatus().name(),
                        "clearedField", exception.clearedField()))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleBusinessRule(
            GraphqlErrorBuilder<?> errorBuilder,
            BusinessRuleException exception) {
        return errorBuilder
                .errorType(ErrorType.BAD_REQUEST)
                .message(exception.getMessage())
                .extensions(code(ApiErrorCode.BUSINESS_RULE_VIOLATION))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleForbidden(
            GraphqlErrorBuilder<?> errorBuilder,
            ForbiddenOperationException exception) {
        return errorBuilder
                .errorType(ErrorType.FORBIDDEN)
                .message(exception.getMessage())
                .extensions(code(ApiErrorCode.FORBIDDEN))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleBadRequest(
            GraphqlErrorBuilder<?> errorBuilder,
            IllegalArgumentException exception) {
        return errorBuilder
                .errorType(ErrorType.BAD_REQUEST)
                .message(exception.getMessage())
                .extensions(code(ApiErrorCode.INVALID_REQUEST))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleDateParse(
            GraphqlErrorBuilder<?> errorBuilder,
            DateTimeParseException exception) {
        return errorBuilder
                .errorType(ErrorType.BAD_REQUEST)
                .message("Data inválida.")
                .extensions(code(ApiErrorCode.INVALID_REQUEST))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleValidation(
            GraphqlErrorBuilder<?> errorBuilder,
            ConstraintViolationException exception) {
        return errorBuilder
                .errorType(ErrorType.BAD_REQUEST)
                .message("Dados de entrada inválidos.")
                .extensions(code(ApiErrorCode.VALIDATION_ERROR))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleUnexpected(
            GraphqlErrorBuilder<?> errorBuilder,
            Exception exception) {
        String incidentId = UUID.randomUUID().toString();
        StackTraceElement origin = exception.getStackTrace().length == 0 ? null : exception.getStackTrace()[0];
        LOGGER.error(
                "Unexpected GraphQL error incidentId={} type={} origin={}",
                incidentId,
                exception.getClass().getName(),
                origin);
        return errorBuilder
                .errorType(ErrorType.INTERNAL_ERROR)
                .message("Erro inesperado.")
                .extensions(Map.of("code", ApiErrorCode.INTERNAL_ERROR.name(), "incidentId", incidentId))
                .build();
    }

    private static Map<String, Object> code(ApiErrorCode code) {
        return Map.of("code", code.name());
    }
}
