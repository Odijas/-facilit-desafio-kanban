package br.com.facilit.kanban.delivery.rest;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documenta no OpenAPI as respostas de erro de cada operação REST, no formato real da API
 * ({@code application/problem+json}, com {@code code} estável e exemplo).
 *
 * <p>Respostas já declaradas com {@code @ApiResponses} no controller são mantidas; aqui entram as que faltam.
 */
@Configuration(proxyBeanMethods = false)
public class ApiErrorDocumentation {

    static final String PROBLEM_JSON = "application/problem+json";
    private static final String PROBLEM_SCHEMA = "Problem";
    private static final String PROBLEM_REF = "#/components/schemas/" + PROBLEM_SCHEMA;
    private static final Set<String> PUBLIC_PATHS = Set.of("/api/v1/health", "/api/v1/auth/csrf");
    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String PROJECTS_PATH = "/api/v1/projects";
    private static final String TRANSITION_PATH = "/api/v1/projects/{id}/status";

    @Bean
    OpenApiCustomizer apiErrorResponsesCustomizer() {
        return openApi -> {
            registerProblemSchema(openApi);
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((path, item) -> item.readOperationsMap()
                    .forEach((method, operation) -> document(path, method, operation)));
        };
    }

    private static void registerProblemSchema(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }
        ObjectSchema violation = new ObjectSchema();
        violation.addProperty("field", new StringSchema());
        violation.addProperty("message", new StringSchema());
        ObjectSchema problem = new ObjectSchema();
        problem.description("Erro no formato RFC 9457 (ProblemDetail) com código estável em `code`.");
        problem.addProperty("type", new StringSchema());
        problem.addProperty("title", new StringSchema());
        problem.addProperty("status", new IntegerSchema());
        problem.addProperty("detail", new StringSchema().description("Mensagem em pt-BR, com orientação quando cabível"));
        problem.addProperty("instance", new StringSchema());
        problem.addProperty("code", new StringSchema()._enum(List.of(
                "VALIDATION_ERROR", "INVALID_REQUEST", "UNAUTHORIZED", "FORBIDDEN", "RESOURCE_NOT_FOUND",
                "CONFLICT", "BUSINESS_RULE_VIOLATION", "TRANSITION_BLOCKED", "CONFIRMATION_REQUIRED",
                "INTERNAL_ERROR")));
        problem.addProperty("violations", new ArraySchema().items(violation));
        problem.addProperty("currentStatus", new StringSchema().description("Transição: status de hoje"));
        problem.addProperty("requestedStatus", new StringSchema().description("Transição: status pedido"));
        problem.addProperty("clearedField", new StringSchema().description("Confirmação: data que será apagada"));
        problem.addProperty("incidentId", new StringSchema().description("Erro 500: identificador no log"));
        openApi.getComponents().addSchemas(PROBLEM_SCHEMA, problem);
    }

    private static void document(String path, PathItem.HttpMethod method, Operation operation) {
        if (!path.startsWith("/api/v1/")) {
            return;
        }
        if (operation.getResponses() == null) {
            operation.setResponses(new ApiResponses());
        }
        ApiResponses responses = operation.getResponses();
        boolean write = method != PathItem.HttpMethod.GET;

        if (PUBLIC_PATHS.contains(path)) {
            add(responses, "500", "Erro inesperado", internalError());
            return;
        }
        if (LOGIN_PATH.equals(path)) {
            add(responses, "400", "Entrada inválida", validationError("email"));
            add(responses, "401", "E-mail ou senha inválidos",
                    example("UNAUTHORIZED", 401, "E-mail ou senha inválidos."));
            add(responses, "403", "Token CSRF ausente ou inválido",
                    example("FORBIDDEN", 403, "Acesso negado."));
            add(responses, "500", "Erro inesperado", internalError());
            return;
        }

        if (operation.getRequestBody() != null) {
            add(responses, "400", "Entrada inválida", validationError("name"));
        } else if (operation.getParameters() != null
                && operation.getParameters().stream().anyMatch(parameter -> "query".equals(parameter.getIn()))) {
            add(responses, "400", "Parâmetro inválido", example(
                    "INVALID_REQUEST", 400, "Tamanho de página inválido: size deve estar entre 1 e 100."));
        }
        add(responses, "401", "Sem sessão autenticada", example("UNAUTHORIZED", 401, "Autenticação obrigatória."));
        add(responses, "403", "Sem permissão (ou sem token CSRF em escrita)",
                example("FORBIDDEN", 403, "O responsável só pode alterar projetos em que é responsável."));
        if (path.contains("{id}")) {
            add(responses, "404", "Recurso não encontrado", example(
                    "RESOURCE_NOT_FOUND", 404, "Projeto não encontrado: 30000000-0000-4000-8000-000000000001"));
        }
        if (write) {
            add(responses, "409", "Conflito com dados já gravados", example(
                    "CONFLICT", 409, "A operação conflita com dados já gravados (por exemplo, e-mail já cadastrado "
                            + "ou registro em uso). Recarregue os dados e tente de novo."));
        }
        if (TRANSITION_PATH.equals(path)) {
            add(responses, "422", "Transição bloqueada, confirmação exigida ou regra de negócio", transitionErrors());
        } else if (write && path.startsWith(PROJECTS_PATH) && method != PathItem.HttpMethod.DELETE) {
            add(responses, "422", "Regra de negócio violada", example(
                    "BUSINESS_RULE_VIOLATION", 422, "Início realizado (2026-09-25) não pode ser posterior a hoje "
                            + "(2026-09-24): informe a data em que o projeto de fato começou ou deixe o campo vazio"));
        }
        add(responses, "500", "Erro inesperado", internalError());
    }

    private static void add(ApiResponses responses, String code, String description, Map<String, Example> examples) {
        if (responses.containsKey(code)) {
            return;
        }
        MediaType mediaType = new MediaType().schema(new Schema<Object>().$ref(PROBLEM_REF));
        examples.forEach(mediaType::addExamples);
        responses.addApiResponse(code, new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(PROBLEM_JSON, mediaType)));
    }

    private static Map<String, Example> example(String code, int status, String detail) {
        return Map.of(code, new Example().value(problem(code, status, detail)));
    }

    private static Map<String, Example> validationError(String field) {
        Map<String, Object> body = problem("VALIDATION_ERROR", 400, "Dados de entrada inválidos.");
        body.put("violations", List.of(Map.of("field", field, "message", "não deve estar em branco")));
        return Map.of("VALIDATION_ERROR", new Example().value(body));
    }

    private static Map<String, Example> internalError() {
        Map<String, Object> body = problem("INTERNAL_ERROR", 500, "Erro inesperado.");
        body.put("incidentId", "5f0c2a8e-8a4b-4c55-9d11-2f0f8d6a1c42");
        return Map.of("INTERNAL_ERROR", new Example().value(body));
    }

    private static Map<String, Example> transitionErrors() {
        Map<String, Object> blocked = problem("TRANSITION_BLOCKED", 422,
                "Em andamento → Atrasado bloqueado: com as datas atuais o projeto não fica Atrasado. Remova o início "
                        + "realizado (actualStart) para voltar a não iniciado, com atraso se cabível, ou ajuste o "
                        + "início ou o término previsto (plannedStart/plannedEnd) para uma data anterior a hoje "
                        + "(2026-09-24).");
        blocked.put("currentStatus", "IN_PROGRESS");
        blocked.put("requestedStatus", "OVERDUE");
        Map<String, Object> confirmation = problem("CONFIRMATION_REQUIRED", 422,
                "Confirme Em andamento → A iniciar: o início realizado (2026-09-20) será apagado. Reenvie com "
                        + "confirm = true.");
        confirmation.put("currentStatus", "IN_PROGRESS");
        confirmation.put("requestedStatus", "NOT_STARTED");
        confirmation.put("clearedField", "actualStart");
        Map<String, Object> rule = problem("BUSINESS_RULE_VIOLATION", 422,
                "Informe o término previsto (plannedEnd): projeto com início realizado e sem término realizado "
                        + "precisa dele para ser classificado como Em andamento ou Atrasado.");
        Map<String, Example> examples = new LinkedHashMap<>();
        examples.put("TRANSITION_BLOCKED", new Example().summary("Bloqueio da tabela de transição").value(blocked));
        examples.put("CONFIRMATION_REQUIRED",
                new Example().summary("Transição que apaga data registrada, sem confirm = true").value(confirmation));
        examples.put("BUSINESS_RULE_VIOLATION", new Example().summary("Regra de datas").value(rule));
        return examples;
    }

    private static Map<String, Object> problem(String code, int status, String detail) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", code);
        body.put("status", status);
        body.put("detail", detail);
        body.put("code", code);
        return body;
    }
}
