package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Contrato do OpenAPI: toda operação REST documenta suas respostas de erro no formato real
 * ({@code application/problem+json}, código estável e exemplo) e traz exemplo de parâmetros, de corpo e de resposta
 * de sucesso.
 */
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "DB_PASSWORD=test-only")
class OpenApiContractIT {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "patch", "delete");
    private static final Set<String> PUBLIC_PATHS = Set.of("/api/v1/health", "/api/v1/auth/csrf", "/api/v1/auth/login");
    private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";
    private static final int REST_OPERATIONS = 26;

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine3.24"));

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void everyRestOperationDocumentsItsErrorsAsProblemJsonWithExamples() {
        JsonNode docs = apiDocs();
        List<String> problems = new ArrayList<>();
        int operations = 0;

        for (Map.Entry<String, JsonNode> path : docs.path("paths").properties()) {
            if (!path.getKey().startsWith("/api/v1/")) {
                continue;
            }
            for (Map.Entry<String, JsonNode> method : path.getValue().properties()) {
                if (!HTTP_METHODS.contains(method.getKey())) {
                    continue;
                }
                operations++;
                String operation = method.getKey().toUpperCase(Locale.ROOT) + " " + path.getKey();
                JsonNode responses = method.getValue().path("responses");
                List<String> required = new ArrayList<>(List.of("500"));
                if (!PUBLIC_PATHS.contains(path.getKey())) {
                    required.addAll(List.of("401", "403"));
                    if (!"get".equals(method.getKey())) {
                        required.add("409");
                    }
                    if (path.getKey().contains("{id}")) {
                        required.add("404");
                    }
                }
                for (String code : required) {
                    JsonNode problemContent = responses.path(code).path("content").path(PROBLEM_JSON);
                    if (problemContent.isMissingNode()) {
                        problems.add(operation + " sem resposta " + code + " em " + PROBLEM_JSON);
                    } else if (!problemContent.has("examples") && !problemContent.has("example")) {
                        problems.add(operation + " resposta " + code + " sem exemplo");
                    }
                }
            }
        }

        assertThat(operations).isGreaterThanOrEqualTo(20);
        assertThat(problems).isEmpty();
    }

    @Test
    void everyRestOperationHasExamplesForParametersRequestBodyAndSuccessResponses() {
        JsonNode docs = apiDocs();
        JsonNode schemas = docs.path("components").path("schemas");
        List<String> missing = new ArrayList<>();
        int operations = 0;

        for (Map.Entry<String, JsonNode> path : docs.path("paths").properties()) {
            if (!path.getKey().startsWith("/api/v1/")) {
                continue;
            }
            for (Map.Entry<String, JsonNode> method : path.getValue().properties()) {
                if (!HTTP_METHODS.contains(method.getKey())) {
                    continue;
                }
                operations++;
                String operation = method.getKey().toUpperCase(Locale.ROOT) + " " + path.getKey();
                for (JsonNode parameter : method.getValue().path("parameters")) {
                    if (!hasExample(parameter)) {
                        missingExamples(parameter.path("schema"), schemas, "", new HashSet<>()).forEach(where ->
                                missing.add(operation + " parâmetro " + parameter.path("name").asText() + where));
                    }
                }
                JsonNode requestBody = method.getValue().path("requestBody");
                if (!requestBody.isMissingNode()) {
                    checkContent(operation + " corpo", requestBody.path("content"), schemas, missing);
                }
                for (Map.Entry<String, JsonNode> response : method.getValue().path("responses").properties()) {
                    if (response.getKey().startsWith("2")) {
                        checkContent(operation + " resposta " + response.getKey(),
                                response.getValue().path("content"), schemas, missing);
                    }
                }
            }
        }

        assertThat(operations).as("operações REST em /api/v1").isEqualTo(REST_OPERATIONS);
        assertThat(missing).as("exemplos ausentes no OpenAPI").isEmpty();
    }

    @Test
    void transitionDocumentsBlockedConfirmationAndBusinessRuleErrors() {
        JsonNode docs = apiDocs();
        JsonNode examples = docs.at("/paths/~1api~1v1~1projects~1{id}~1status/patch/responses/422/content")
                .path(PROBLEM_JSON)
                .path("examples");

        assertThat(examples.has("TRANSITION_BLOCKED")).isTrue();
        assertThat(examples.has("CONFIRMATION_REQUIRED")).isTrue();
        assertThat(examples.has("BUSINESS_RULE_VIOLATION")).isTrue();
        assertThat(examples.at("/CONFIRMATION_REQUIRED/value/clearedField").asText()).isEqualTo("actualStart");
        assertThat(docs.at("/components/schemas/ProjectStatusRequest/properties/confirm").isObject()).isTrue();
        assertThat(docs.at("/components/schemas/Problem/properties/code").isObject()).isTrue();
    }

    @Test
    void projectWritesDocumentBusinessRuleViolations() {
        JsonNode docs = apiDocs();

        for (String pointer : List.of(
                "/paths/~1api~1v1~1projects/post/responses/422",
                "/paths/~1api~1v1~1projects~1{id}/put/responses/422")) {
            assertThat(docs.at(pointer + "/content").path(PROBLEM_JSON).path("examples").has("BUSINESS_RULE_VIOLATION"))
                    .as(pointer)
                    .isTrue();
        }
    }

    @Test
    void errorExamplesMatchTheOperationResourceInputAndAuthorizationRule() {
        JsonNode docs = apiDocs();

        for (Map.Entry<String, JsonNode> path : docs.path("paths").properties()) {
            if (!path.getKey().startsWith("/api/v1/")) {
                continue;
            }
            for (Map.Entry<String, JsonNode> method : path.getValue().properties()) {
                if (!HTTP_METHODS.contains(method.getKey())) {
                    continue;
                }
                String operation = method.getKey().toUpperCase(Locale.ROOT) + " " + path.getKey();
                JsonNode operationNode = method.getValue();

                if (operationNode.has("requestBody")) {
                    String field = validationField(docs, path.getKey(), method.getKey());
                    assertThat(requestBodySchema(docs, operationNode).path("properties").has(field))
                            .as("%s: campo do exemplo 400 precisa existir no schema do corpo", operation)
                            .isTrue();
                }

                if (path.getKey().contains("{id}")
                        && operationNode.path("responses").has("404")) {
                    String detail = errorExample(docs, path.getKey(), method.getKey(), "404", "RESOURCE_NOT_FOUND")
                            .path("detail").asText();
                    String expectedPrefix = expectedNotFoundPrefix(path.getKey(), method.getKey());
                    assertThat(detail).as(operation + " 404").startsWith(expectedPrefix);
                }

                if ((path.getKey().startsWith("/api/v1/responsibles")
                                || path.getKey().startsWith("/api/v1/secretariats"))
                        && operationNode.path("responses").has("403")) {
                    assertThat(errorExample(docs, path.getKey(), method.getKey(), "403", "FORBIDDEN")
                                    .path("detail").asText()
                                    .toLowerCase(Locale.ROOT))
                            .as(operation + " 403")
                            .doesNotContain("projeto");
                }
            }
        }

        assertThat(errorExample(
                                docs,
                                "/api/v1/indicators/projects/deadlines",
                                "get",
                                "400",
                                "INVALID_REQUEST")
                        .path("detail").asText())
                .isEqualTo("withinDays deve estar entre 1 e 90.");
        assertThat(errorExample(docs, "/api/v1/projects", "get", "400", "INVALID_REQUEST")
                        .path("detail").asText())
                .isEqualTo("Tamanho de página inválido: size deve estar entre 1 e 100.");

        assertThat(errorExample(docs, "/api/v1/responsibles", "post", "403", "FORBIDDEN")
                        .path("detail").asText())
                .isEqualTo("Apenas o administrador pode realizar esta operação.");
        assertThat(errorExample(docs, "/api/v1/secretariats", "post", "403", "FORBIDDEN")
                        .path("detail").asText())
                .isEqualTo("Apenas o administrador pode realizar esta operação.");
        assertThat(errorExample(docs, "/api/v1/projects/{id}", "put", "403", "FORBIDDEN")
                        .path("detail").asText())
                .isEqualTo("O responsável só pode alterar projetos em que é responsável.");
        assertThat(errorExample(docs, "/api/v1/projects/{id}", "get", "403", "FORBIDDEN")
                        .path("detail").asText())
                .isEqualTo("Acesso negado.");
    }

    @Test
    void swaggerUiSendsTheCsrfTokenFromTheCookie() {
        // Com springdoc.swagger-ui.csrf.enabled, o springdoc injeta no swagger-initializer.js um requestInterceptor
        // que copia o cookie XSRF-TOKEN para o cabeçalho X-XSRF-TOKEN em chamadas da mesma origem.
        ResponseEntity<String> initializer = restTemplate.getForEntity(
                "http://localhost:" + port + "/swagger-ui/swagger-initializer.js", String.class);

        assertThat(initializer.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(initializer.getBody())
                .contains("requestInterceptor")
                .contains("XSRF-TOKEN=")
                .contains("request.headers['X-XSRF-TOKEN']");
    }

    private static JsonNode errorExample(
            JsonNode docs,
            String path,
            String method,
            String status,
            String code) {
        return docs.path("paths")
                .path(path)
                .path(method)
                .path("responses")
                .path(status)
                .path("content")
                .path(PROBLEM_JSON)
                .path("examples")
                .path(code)
                .path("value");
    }

    private static String validationField(JsonNode docs, String path, String method) {
        return validationExample(docs, path, method)
                .path("violations")
                .path(0)
                .path("field")
                .asText();
    }

    private static JsonNode validationExample(JsonNode docs, String path, String method) {
        JsonNode problemContent = docs.path("paths")
                .path(path)
                .path(method)
                .path("responses")
                .path("400")
                .path("content")
                .path(PROBLEM_JSON);
        JsonNode named = problemContent.path("examples").path("VALIDATION_ERROR").path("value");
        if (!named.isMissingNode()) {
            return parseExample(named);
        }
        JsonNode direct = problemContent.path("example");
        if (!direct.isMissingNode()) {
            return parseExample(direct);
        }
        for (JsonNode example : problemContent.path("examples")) {
            JsonNode value = parseExample(example.path("value"));
            if ("VALIDATION_ERROR".equals(value.path("code").asText())) {
                return value;
            }
        }
        return named;
    }

    private static JsonNode parseExample(JsonNode example) {
        if (!example.isTextual()) {
            return example;
        }
        try {
            return new ObjectMapper().readTree(example.asText());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Exemplo OpenAPI não contém JSON válido", exception);
        }
    }

    private static JsonNode requestBodySchema(JsonNode docs, JsonNode operation) {
        JsonNode schema = operation.path("requestBody").path("content").path("application/json").path("schema");
        if (!schema.has("$ref")) {
            return schema;
        }
        String ref = schema.path("$ref").asText();
        String name = ref.substring(ref.lastIndexOf('/') + 1);
        return docs.path("components").path("schemas").path(name);
    }

    private static String expectedNotFoundPrefix(String path, String method) {
        if ("/api/v1/responsibles/{id}/credentials".equals(path) && "delete".equals(method)) {
            return "Credencial não encontrada para o responsável:";
        }
        if (path.startsWith("/api/v1/projects")) {
            return "Projeto não encontrado:";
        }
        if (path.startsWith("/api/v1/responsibles")) {
            return "Responsável não encontrado:";
        }
        if (path.startsWith("/api/v1/secretariats")) {
            return "Secretaria não encontrada:";
        }
        throw new IllegalArgumentException("Path com {id} não reconhecido no contrato: " + path);
    }

    private static void checkContent(String where, JsonNode content, JsonNode schemas, List<String> missing) {
        // Sem "content" (por exemplo, 204 No Content) não há corpo para exemplificar.
        for (Map.Entry<String, JsonNode> media : content.properties()) {
            if (!hasExample(media.getValue())) {
                missingExamples(media.getValue().path("schema"), schemas, "", new HashSet<>()).forEach(field ->
                        missing.add(where + " (" + media.getKey() + ")" + field));
            }
        }
    }

    /**
     * Campos sem exemplo em um schema. Um schema está exemplificado quando tem {@code example} próprio, ou quando é
     * referência, lista ou objeto cujas partes estão todas exemplificadas (é assim que o Swagger UI monta o exemplo).
     */
    private static List<String> missingExamples(JsonNode schema, JsonNode schemas, String where, Set<String> visiting) {
        if (schema.isMissingNode() || schema.isNull()) {
            return List.of(where + " sem schema");
        }
        if (hasExample(schema)) {
            return List.of();
        }
        if (schema.has("$ref")) {
            String name = schema.get("$ref").asText().substring(SCHEMA_REF_PREFIX.length());
            if (!visiting.add(name)) {
                return List.of();
            }
            List<String> result = missingExamples(schemas.path(name), schemas, where + " → " + name, visiting);
            visiting.remove(name);
            return result;
        }
        if (schema.has("items")) {
            return missingExamples(schema.get("items"), schemas, where + "[]", visiting);
        }
        List<String> result = new ArrayList<>();
        for (String composition : List.of("allOf", "oneOf", "anyOf")) {
            for (JsonNode part : schema.path(composition)) {
                result.addAll(missingExamples(part, schemas, where, visiting));
            }
        }
        JsonNode properties = schema.path("properties");
        for (Map.Entry<String, JsonNode> property : properties.properties()) {
            result.addAll(missingExamples(property.getValue(), schemas, where + "." + property.getKey(), visiting));
        }
        boolean composed = schema.has("allOf") || schema.has("oneOf") || schema.has("anyOf");
        if (!composed && properties.isEmpty()) {
            return List.of(where + " sem exemplo");
        }
        return result;
    }

    private static boolean hasExample(JsonNode node) {
        return node.has("example") || node.has("examples");
    }

    private JsonNode apiDocs() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api-docs", JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }
}
