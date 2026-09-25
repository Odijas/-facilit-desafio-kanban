package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.delivery.common.ApiExamples;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exemplo dos itens das listas de texto e de ids no OpenAPI. O Swagger UI monta o exemplo da lista a partir do item.
 *
 * <p>O exemplo em {@code @ArraySchema(arraySchema = @Schema(example = ...))} não chega ao {@code /api-docs} gerado pelo
 * springdoc 2.8.17 (constatado no gate local do F5-P1), por isso o exemplo é posto no item aqui, depois da geração. Se
 * uma dessas listas deixar de existir no schema, a geração do {@code /api-docs} falha (e o {@code OpenApiContractIT}
 * acusa), em vez de o exemplo sumir sem aviso.
 */
@Configuration(proxyBeanMethods = false)
public class ApiListExampleDocumentation {

    /** Schema → propriedade do tipo lista → exemplo de um item. */
    private static final Map<String, Map<String, String>> ITEM_EXAMPLES = Map.of(
            "ProjectRequest", Map.of("responsibleIds", ApiExamples.RESPONSIBLE_ID),
            "ProjectResponse", Map.of("responsibleIds", ApiExamples.RESPONSIBLE_ID),
            "AuthResponse", Map.of("authorities", "ROLE_RESPONSIBLE"));

    @Bean
    OpenApiCustomizer listItemExamplesCustomizer() {
        return openApi -> ITEM_EXAMPLES.forEach((schemaName, properties) ->
                properties.forEach((property, example) -> items(openApi, schemaName, property).setExample(example)));
    }

    private static Schema<?> items(OpenAPI openApi, String schemaName, String property) {
        Schema<?> schema = openApi.getComponents() == null || openApi.getComponents().getSchemas() == null
                ? null
                : openApi.getComponents().getSchemas().get(schemaName);
        Schema<?> list = schema == null || schema.getProperties() == null ? null : schema.getProperties().get(property);
        if (list == null || list.getItems() == null) {
            throw new IllegalStateException("OpenAPI sem a lista " + schemaName + "." + property);
        }
        return list.getItems();
    }
}
