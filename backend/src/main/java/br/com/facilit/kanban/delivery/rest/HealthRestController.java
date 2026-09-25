package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.health.HealthQuery;
import br.com.facilit.kanban.application.health.HealthStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthRestController {

    private final HealthQuery healthQuery;

    public HealthRestController(HealthQuery healthQuery) {
        this.healthQuery = healthQuery;
    }

    @GetMapping
    @Operation(summary = "Informa se a API está no ar (público)")
    // O exemplo fica aqui, e não em HealthStatus, para a camada de aplicação não depender do OpenAPI.
    @ApiResponse(
            responseCode = "200",
            description = "API no ar",
            content = @Content(
                    schema = @Schema(implementation = HealthStatus.class),
                    examples = @ExampleObject(value = """
                            {"status": "UP"}
                            """)))
    public HealthStatus health() {
        return healthQuery.execute();
    }
}
