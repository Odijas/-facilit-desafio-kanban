package br.com.facilit.kanban.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(
        info = @Info(
                title = "Facilit Kanban API",
                version = "v1",
                description = "API REST do desafio técnico Kanban"))
public class OpenApiConfiguration {
}
