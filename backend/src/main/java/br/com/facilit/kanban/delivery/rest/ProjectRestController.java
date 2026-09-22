package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.project.ProjectService;
import br.com.facilit.kanban.application.project.SaveProjectCommand;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "CRUD e operações Kanban de projetos")
public class ProjectRestController {

    private final ProjectService service;

    public ProjectRestController(ProjectService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cria um projeto")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    schema = @Schema(implementation = ProjectRequest.class),
                    examples = @ExampleObject(value = """
                            {
                              "name": "Implantação do portal",
                              "responsibleIds": ["20000000-0000-4000-8000-000000000001"],
                              "plannedStart": "2026-09-22",
                              "plannedEnd": "2026-10-10",
                              "actualStart": null,
                              "actualEnd": null
                            }
                            """)))
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Projeto criado",
                content = @Content(
                        schema = @Schema(implementation = ProjectResponse.class),
                        examples = @ExampleObject(value = """
                                {
                                  "id": "30000000-0000-4000-8000-000000000001",
                                  "name": "Implantação do portal",
                                  "status": "NOT_STARTED",
                                  "responsibleIds": ["20000000-0000-4000-8000-000000000001"],
                                  "plannedStart": "2026-09-22",
                                  "plannedEnd": "2026-10-10",
                                  "actualStart": null,
                                  "actualEnd": null,
                                  "delayDays": 0,
                                  "remainingTimePercentage": 100
                                }
                                """))),
        @ApiResponse(
                responseCode = "400",
                description = "Entrada inválida",
                content = @Content(
                        mediaType = "application/problem+json",
                        examples = @ExampleObject(value = """
                                {
                                  "title": "VALIDATION_ERROR",
                                  "status": 400,
                                  "detail": "Request validation failed",
                                  "code": "VALIDATION_ERROR",
                                  "violations": [{"field": "name", "message": "must not be blank"}]
                                }
                                """)))
    })
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        Project created = service.create(toCommand(request));
        ProjectResponse response = ProjectResponse.from(created);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + created.id())).body(response);
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable UUID id) {
        return ProjectResponse.from(service.get(id));
    }

    @GetMapping
    @Operation(summary = "Lista projetos com paginação e filtro opcional por status")
    public PageResponse<ProjectResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ProjectStatus status) {
        PageQuery pageQuery = new PageQuery(page, size);
        PageResult<Project> result = status == null
                ? service.list(pageQuery)
                : service.listByStatus(status, pageQuery);
        return new PageResponse<>(
                result.content().stream().map(ProjectResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }

    @PutMapping("/{id}")
    public ProjectResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest request) {
        return ProjectResponse.from(service.update(id, toCommand(request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Executa uma transição Kanban")
    public ProjectResponse transition(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectStatusRequest request) {
        return ProjectResponse.from(service.transition(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static SaveProjectCommand toCommand(ProjectRequest request) {
        return new SaveProjectCommand(
                request.name(),
                request.responsibleIds(),
                request.plannedStart(),
                request.plannedEnd(),
                request.actualStart(),
                request.actualEnd());
    }
}
