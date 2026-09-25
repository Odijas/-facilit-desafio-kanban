package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.project.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/by-secretariat")
    @Operation(summary = "Obtém quantidade e média de atraso dos projetos por secretaria")
    public List<ProjectGroupIndicatorResponse> bySecretariat() {
        return service.indicatorsBySecretariat().stream()
                .map(ProjectGroupIndicatorResponse::from)
                .toList();
    }

    @GetMapping("/by-responsible")
    @Operation(summary = "Obtém quantidade e média de atraso dos projetos por responsável")
    public List<ProjectGroupIndicatorResponse> byResponsible() {
        return service.indicatorsByResponsible().stream()
                .map(ProjectGroupIndicatorResponse::from)
                .toList();
    }

    @GetMapping("/deadlines")
    @Operation(summary = "Lista prazos de projetos não concluídos dentro da janela informada")
    public ProjectDeadlinesResponse deadlines(
            @Parameter(description = "Janela em dias a partir de hoje (1 a 90)", example = "7")
            @RequestParam(defaultValue = "7") int withinDays) {
        return ProjectDeadlinesResponse.from(service.deadlines(withinDays));
    }
}
