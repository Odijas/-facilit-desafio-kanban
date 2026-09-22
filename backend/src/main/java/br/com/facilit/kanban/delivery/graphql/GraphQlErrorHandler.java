package br.com.facilit.kanban.delivery.graphql;

import br.com.facilit.kanban.application.common.ConflictException;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.delivery.common.ApiErrorCode;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import jakarta.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GraphQlErrorHandler {

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
                .message("Invalid date value")
                .extensions(code(ApiErrorCode.INVALID_REQUEST))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleValidation(
            GraphqlErrorBuilder<?> errorBuilder,
            ConstraintViolationException exception) {
        return errorBuilder
                .errorType(ErrorType.BAD_REQUEST)
                .message("Request validation failed")
                .extensions(code(ApiErrorCode.VALIDATION_ERROR))
                .build();
    }

    private static Map<String, Object> code(ApiErrorCode code) {
        return Map.of("code", code.name());
    }
}
