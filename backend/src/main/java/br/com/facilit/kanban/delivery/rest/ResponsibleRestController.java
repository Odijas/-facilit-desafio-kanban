package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.responsible.ResponsibleCredentialService;
import br.com.facilit.kanban.application.responsible.ResponsibleService;
import br.com.facilit.kanban.application.responsible.SaveResponsibleCommand;
import br.com.facilit.kanban.delivery.common.ApiExamples;
import br.com.facilit.kanban.domain.responsible.Responsible;
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
    private final ResponsibleCredentialService credentialService;
    private final AuthenticatedActorResolver actorResolver;

    public ResponsibleRestController(
            ResponsibleService service,
            ResponsibleCredentialService credentialService,
            AuthenticatedActorResolver actorResolver) {
        this.service = service;
        this.credentialService = credentialService;
        this.actorResolver = actorResolver;
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
                                  "secretariatId": null,
                                  "createdAt": "2026-09-22T12:00:00Z",
                                  "updatedAt": "2026-09-22T12:00:00Z"
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
                                  "detail": "Já existe responsável com este e-mail.",
                                  "code": "CONFLICT"
                                }
                                """)))
    })
    public ResponseEntity<ResponsibleResponse> create(
            @Valid @RequestBody ResponsibleRequest request,
            Principal principal) {
        Responsible created = service.create(toCommand(request), actorResolver.resolve(principal));
        ResponsibleResponse response = ResponsibleResponse.from(created);
        return ResponseEntity.created(URI.create("/api/v1/responsibles/" + created.id())).body(response);
    }

    @GetMapping("/{id}")
    public ResponsibleResponse get(@Parameter(description = "Id do responsável", example = ApiExamples.RESPONSIBLE_ID) @PathVariable UUID id) {
        return ResponsibleResponse.from(service.get(id));
    }

    @GetMapping
    @Operation(summary = "Lista responsáveis com paginação")
    public PageResponse<ResponsibleResponse> list(
            @Parameter(description = "Página, a partir de 0", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página", example = "20")
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
            @Parameter(description = "Id do responsável", example = ApiExamples.RESPONSIBLE_ID) @PathVariable UUID id,
            @Valid @RequestBody ResponsibleRequest request,
            Principal principal) {
        return ResponsibleResponse.from(service.update(id, toCommand(request), actorResolver.resolve(principal)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Id do responsável", example = ApiExamples.RESPONSIBLE_ID) @PathVariable UUID id, Principal principal) {
        service.delete(id, actorResolver.resolve(principal));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/credentials")
    @Operation(summary = "Define ou redefine a senha de acesso de um responsável (somente ADMIN)")
    public ResponseEntity<Void> setCredentials(
            @Parameter(description = "Id do responsável", example = ApiExamples.RESPONSIBLE_ID) @PathVariable UUID id,
            @Valid @RequestBody ResponsibleCredentialsRequest request,
            Principal principal) {
        credentialService.setPassword(id, request.password(), actorResolver.resolve(principal));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/credentials")
    @Operation(summary = "Revoga o acesso de um responsável (somente ADMIN)")
    public ResponseEntity<Void> revokeCredentials(@Parameter(description = "Id do responsável", example = ApiExamples.RESPONSIBLE_ID) @PathVariable UUID id, Principal principal) {
        credentialService.revoke(id, actorResolver.resolve(principal));
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
