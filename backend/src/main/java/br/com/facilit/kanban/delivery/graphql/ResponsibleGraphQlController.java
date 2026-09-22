package br.com.facilit.kanban.delivery.graphql;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.responsible.ResponsibleService;
import br.com.facilit.kanban.application.responsible.SaveResponsibleCommand;
import br.com.facilit.kanban.domain.responsible.Responsible;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ResponsibleGraphQlController {

    private final ResponsibleService service;

    public ResponsibleGraphQlController(ResponsibleService service) {
        this.service = service;
    }

    @QueryMapping
    public ResponsibleGraphQlResponse responsible(@Argument String id) {
        return ResponsibleGraphQlResponse.from(service.get(UUID.fromString(id)));
    }

    @QueryMapping
    public ResponsibleGraphQlPage responsibles(@Argument int page, @Argument int size) {
        PageResult<Responsible> result = service.list(new PageQuery(page, size));
        List<ResponsibleGraphQlResponse> content = result.content().stream()
                .map(ResponsibleGraphQlResponse::from)
                .toList();
        return new ResponsibleGraphQlPage(
                content,
                result.page(),
                result.size(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }

    @MutationMapping
    public ResponsibleGraphQlResponse createResponsible(@Argument @Valid ResponsibleGraphQlInput input) {
        return ResponsibleGraphQlResponse.from(service.create(input.toCommand()));
    }

    @MutationMapping
    public ResponsibleGraphQlResponse updateResponsible(
            @Argument String id,
            @Argument @Valid ResponsibleGraphQlInput input) {
        return ResponsibleGraphQlResponse.from(service.update(UUID.fromString(id), input.toCommand()));
    }

    @MutationMapping
    public boolean deleteResponsible(@Argument String id) {
        service.delete(UUID.fromString(id));
        return true;
    }

    public record ResponsibleGraphQlInput(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank String position,
            String secretariatId) {

        SaveResponsibleCommand toCommand() {
            UUID parsedSecretariatId = secretariatId == null ? null : UUID.fromString(secretariatId);
            return new SaveResponsibleCommand(name, email, position, parsedSecretariatId);
        }
    }

    public record ResponsibleGraphQlResponse(
            String id,
            String name,
            String email,
            String position,
            String secretariatId,
            String createdAt,
            String updatedAt) {

        static ResponsibleGraphQlResponse from(Responsible responsible) {
            return new ResponsibleGraphQlResponse(
                    responsible.id().toString(),
                    responsible.name(),
                    responsible.email(),
                    responsible.position(),
                    responsible.secretariatId() == null ? null : responsible.secretariatId().toString(),
                    responsible.audit().createdAt().toString(),
                    responsible.audit().updatedAt().toString());
        }
    }

    public record ResponsibleGraphQlPage(
            List<ResponsibleGraphQlResponse> content,
            int page,
            int size,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious) {
    }
}
