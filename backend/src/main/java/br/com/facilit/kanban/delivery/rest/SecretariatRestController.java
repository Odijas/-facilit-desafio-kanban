package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.secretariat.SaveSecretariatCommand;
import br.com.facilit.kanban.application.secretariat.SecretariatService;
import br.com.facilit.kanban.domain.secretariat.Secretariat;
import br.com.facilit.kanban.infrastructure.security.AuthenticatedActorResolver;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/secretariats")
@Tag(name = "Secretariats", description = "CRUD de secretarias")
public class SecretariatRestController {

    private final SecretariatService service;
    private final AuthenticatedActorResolver actorResolver;

    public SecretariatRestController(SecretariatService service, AuthenticatedActorResolver actorResolver) {
        this.service = service;
        this.actorResolver = actorResolver;
    }

    @PostMapping
    @Operation(summary = "Cria uma secretaria")
    public ResponseEntity<SecretariatResponse> create(
            @Valid @RequestBody SecretariatRequest request,
            Principal principal) {
        Secretariat created = service.create(
                new SaveSecretariatCommand(request.name()),
                actorResolver.resolve(principal));
        return ResponseEntity.created(URI.create("/api/v1/secretariats/" + created.id()))
                .body(SecretariatResponse.from(created));
    }

    @GetMapping("/{id}")
    public SecretariatResponse get(@PathVariable UUID id) {
        return SecretariatResponse.from(service.get(id));
    }

    @GetMapping
    @Operation(summary = "Lista secretarias com paginação")
    public PageResponse<SecretariatResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<Secretariat> result = service.list(new PageQuery(page, size));
        return new PageResponse<>(
                result.content().stream().map(SecretariatResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }

    @PutMapping("/{id}")
    public SecretariatResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody SecretariatRequest request,
            Principal principal) {
        return SecretariatResponse.from(service.update(
                id,
                new SaveSecretariatCommand(request.name()),
                actorResolver.resolve(principal)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Principal principal) {
        service.delete(id, actorResolver.resolve(principal));
        return ResponseEntity.noContent().build();
    }
}
