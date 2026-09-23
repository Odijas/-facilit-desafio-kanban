package br.com.facilit.kanban.delivery.graphql;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.secretariat.SaveSecretariatCommand;
import br.com.facilit.kanban.application.secretariat.SecretariatService;
import br.com.facilit.kanban.domain.secretariat.Secretariat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class SecretariatGraphQlController {

    private final SecretariatService service;

    public SecretariatGraphQlController(SecretariatService service) {
        this.service = service;
    }

    @QueryMapping
    public SecretariatGraphQlResponse secretariat(@Argument String id) {
        return SecretariatGraphQlResponse.from(service.get(UUID.fromString(id)));
    }

    @QueryMapping
    public SecretariatGraphQlPage secretariats(@Argument int page, @Argument int size) {
        PageResult<Secretariat> result = service.list(new PageQuery(page, size));
        return new SecretariatGraphQlPage(
                result.content().stream().map(SecretariatGraphQlResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }

    @MutationMapping
    public SecretariatGraphQlResponse createSecretariat(@Argument @Valid SecretariatGraphQlInput input) {
        return SecretariatGraphQlResponse.from(service.create(input.toCommand()));
    }

    @MutationMapping
    public SecretariatGraphQlResponse updateSecretariat(
            @Argument String id,
            @Argument @Valid SecretariatGraphQlInput input) {
        return SecretariatGraphQlResponse.from(service.update(UUID.fromString(id), input.toCommand()));
    }

    @MutationMapping
    public boolean deleteSecretariat(@Argument String id) {
        service.delete(UUID.fromString(id));
        return true;
    }

    public record SecretariatGraphQlInput(@NotBlank String name) {
        SaveSecretariatCommand toCommand() {
            return new SaveSecretariatCommand(name);
        }
    }

    public record SecretariatGraphQlResponse(
            String id,
            String name,
            String createdAt,
            String updatedAt) {

        static SecretariatGraphQlResponse from(Secretariat secretariat) {
            return new SecretariatGraphQlResponse(
                    secretariat.id().toString(),
                    secretariat.name(),
                    secretariat.audit().createdAt().toString(),
                    secretariat.audit().updatedAt().toString());
        }
    }

    public record SecretariatGraphQlPage(
            List<SecretariatGraphQlResponse> content,
            int page,
            int size,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious) {
    }
}
