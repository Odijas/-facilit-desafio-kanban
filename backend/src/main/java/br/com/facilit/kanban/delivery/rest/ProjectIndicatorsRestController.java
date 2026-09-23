package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.project.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/indicators/projects")
@Tag(name = "Indicators", description = "Indicadores consolidados de projetos")
public class ProjectIndicatorsRestController {

    private final ProjectService service;

    public ProjectIndicatorsRestController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Obtém quantidade e média de atraso dos projetos por status")
    public ProjectIndicatorsResponse get() {
        return ProjectIndicatorsResponse.from(service.indicators());
    }
}
