package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.responsible.ResponsibleService;
import br.com.facilit.kanban.application.responsible.SaveResponsibleCommand;
import br.com.facilit.kanban.domain.responsible.Responsible;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/responsibles")
@Tag(name = "Responsibles", description = "CRUD de responsáveis")
public class ResponsibleRestController {

    private final ResponsibleService service;

    public ResponsibleRestController(ResponsibleService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cria um responsável")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    schema = @Schema(implementation = ResponsibleRequest.class),
                    examples = @ExampleObject(value = """
                            {
                              "name": "Maria Silva",
                              "email": "maria.silva@example.com",
                              "position": "Analista",
                              "secretariatId": null
                            }
                            """)))
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Responsável criado",
                content = @Content(
                        schema = @Schema(implementation = ResponsibleResponse.class),
                        examples = @ExampleObject(value = """
                                {
                                  "id": "20000000-0000-4000-8000-000000000001",
                                  "name": "Maria Silva",
                                  "email": "maria.silva@example.com",
                                  "position": "Analista",
                                  "secretariatId": null
                                }
                                """))),
        @ApiResponse(
                responseCode = "409",
                description = "E-mail já utilizado",
                content = @Content(
                        mediaType = "application/problem+json",
                        examples = @ExampleObject(value = """
                                {
                                  "title": "CONFLICT",
                                  "status": 409,
                                  "detail": "Responsible email already exists",
                                  "code": "CONFLICT"
                                }
                                """)))
    })
    public ResponseEntity<ResponsibleResponse> create(@Valid @RequestBody ResponsibleRequest request) {
        Responsible created = service.create(toCommand(request));
        ResponsibleResponse response = ResponsibleResponse.from(created);
        return ResponseEntity.created(URI.create("/api/v1/responsibles/" + created.id())).body(response);
    }

    @GetMapping("/{id}")
    public ResponsibleResponse get(@PathVariable UUID id) {
        return ResponsibleResponse.from(service.get(id));
    }

    @GetMapping
    @Operation(summary = "Lista responsáveis com paginação")
    public PageResponse<ResponsibleResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<Responsible> result = service.list(new PageQuery(page, size));
        return new PageResponse<>(
                result.content().stream().map(ResponsibleResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }

    @PutMapping("/{id}")
    public ResponsibleResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody ResponsibleRequest request) {
        return ResponsibleResponse.from(service.update(id, toCommand(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static SaveResponsibleCommand toCommand(ResponsibleRequest request) {
        return new SaveResponsibleCommand(
                request.name(),
                request.email(),
                request.position(),
                request.secretariatId());
    }
}
