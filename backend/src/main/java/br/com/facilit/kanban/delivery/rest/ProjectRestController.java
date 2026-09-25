package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.project.ProjectFilter;
import br.com.facilit.kanban.application.project.ProjectService;
import br.com.facilit.kanban.application.project.SaveProjectCommand;
import br.com.facilit.kanban.delivery.common.ApiExamples;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.infrastructure.security.AuthenticatedActorResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
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
    private final AuthenticatedActorResolver actorResolver;

    public ProjectRestController(ProjectService service, AuthenticatedActorResolver actorResolver) {
        this.service = service;
        this.actorResolver = actorResolver;
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
                                  "remainingTimePercentage": 100,
                                  "createdAt": "2026-09-22T12:00:00Z",
                                  "updatedAt": "2026-09-22T12:00:00Z"
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
                                  "detail": "Dados de entrada inválidos.",
                                  "code": "VALIDATION_ERROR",
                                  "violations": [{"field": "name", "message": "não deve estar em branco"}]
                                }
                                """)))
    })
    public ResponseEntity<ProjectResponse> create(
            @Valid @RequestBody ProjectRequest request,
            Principal principal) {
        Project created = service.create(toCommand(request), actorResolver.resolve(principal));
        ProjectResponse response = ProjectResponse.from(created);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + created.id())).body(response);
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@Parameter(description = "Id do projeto", example = ApiExamples.PROJECT_ID) @PathVariable UUID id) {
        return ProjectResponse.from(service.get(id));
    }

    @GetMapping
    @Operation(summary = "Lista projetos com paginação e filtros avançados")
    public PageResponse<ProjectResponse> list(
            @Parameter(description = "Página, a partir de 0", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Status calculado para hoje", example = "IN_PROGRESS")
            @RequestParam(required = false) ProjectStatus status,
            @Parameter(description = "Secretaria de algum responsável do projeto", example = ApiExamples.SECRETARIAT_ID)
            @RequestParam(required = false) UUID secretariatId,
            @Parameter(description = "Responsável do projeto", example = ApiExamples.RESPONSIBLE_ID)
            @RequestParam(required = false) UUID responsibleId,
            @Parameter(description = "Projetos com término previsto a partir desta data", example = "2026-09-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate plannedFrom,
            @Parameter(description = "Projetos com início previsto até esta data", example = "2026-12-31")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate plannedTo,
            @Parameter(
                    description = "Trecho do nome, sem diferenciar maiúsculas; % e _ são literais (até 100 caracteres)",
                    example = "portal")
            @RequestParam(required = false) String text) {
        PageQuery pageQuery = new PageQuery(page, size);
        ProjectFilter filter = new ProjectFilter(
                status,
                secretariatId,
                responsibleId,
                plannedFrom,
                plannedTo,
                text);
        PageResult<Project> result = service.search(filter, pageQuery);
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
            @Parameter(description = "Id do projeto", example = ApiExamples.PROJECT_ID) @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest request,
            Principal principal) {
        return ProjectResponse.from(service.update(id, toCommand(request), actorResolver.resolve(principal)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Executa uma transição Kanban")
    public ProjectResponse transition(
            @Parameter(description = "Id do projeto", example = ApiExamples.PROJECT_ID) @PathVariable UUID id,
            @Valid @RequestBody ProjectStatusRequest request,
            Principal principal) {
        return ProjectResponse.from(service.transition(
                id,
                request.status(),
                request.confirmed(),
                actorResolver.resolve(principal)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Id do projeto", example = ApiExamples.PROJECT_ID) @PathVariable UUID id, Principal principal) {
        service.delete(id, actorResolver.resolve(principal));
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
