package br.com.facilit.kanban.delivery.graphql;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.project.ProjectFilter;
import br.com.facilit.kanban.application.project.ProjectIndicators;
import br.com.facilit.kanban.application.project.ProjectService;
import br.com.facilit.kanban.application.project.SaveProjectCommand;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.infrastructure.security.AuthenticatedActorResolver;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ProjectGraphQlController {

    private final ProjectService service;
    private final AuthenticatedActorResolver actorResolver;

    public ProjectGraphQlController(ProjectService service, AuthenticatedActorResolver actorResolver) {
        this.service = service;
        this.actorResolver = actorResolver;
    }

    @QueryMapping
    public ProjectGraphQlResponse project(@Argument String id) {
        return ProjectGraphQlResponse.from(service.get(UUID.fromString(id)));
    }

    @QueryMapping
    public ProjectGraphQlPage projects(
            @Argument int page,
            @Argument int size,
            @Argument ProjectStatus status,
            @Argument String secretariatId,
            @Argument String responsibleId,
            @Argument String plannedFrom,
            @Argument String plannedTo,
            @Argument String text) {
        PageQuery pageQuery = new PageQuery(page, size);
        ProjectFilter filter = new ProjectFilter(
                status,
                parseUuid(secretariatId),
                parseUuid(responsibleId),
                parseDate(plannedFrom),
                parseDate(plannedTo),
                text);
        PageResult<Project> result = service.search(filter, pageQuery);
        List<ProjectGraphQlResponse> content = result.content().stream()
                .map(ProjectGraphQlResponse::from)
                .toList();
        return new ProjectGraphQlPage(
                content,
                result.page(),
                result.size(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }

    @QueryMapping
    public ProjectIndicatorsGraphQlResponse projectIndicators() {
        return ProjectIndicatorsGraphQlResponse.from(service.indicators());
    }

    @MutationMapping
    public ProjectGraphQlResponse createProject(
            @Argument @Valid ProjectGraphQlInput input,
            Principal principal) {
        return ProjectGraphQlResponse.from(service.create(input.toCommand(), actorResolver.resolve(principal)));
    }

    @MutationMapping
    public ProjectGraphQlResponse updateProject(
            @Argument String id,
            @Argument @Valid ProjectGraphQlInput input,
            Principal principal) {
        return ProjectGraphQlResponse.from(service.update(
                UUID.fromString(id),
                input.toCommand(),
                actorResolver.resolve(principal)));
    }

    @MutationMapping
    public ProjectGraphQlResponse transitionProject(
            @Argument String id,
            @Argument ProjectStatus status,
            Principal principal) {
        return ProjectGraphQlResponse.from(service.transition(
                UUID.fromString(id),
                status,
                actorResolver.resolve(principal)));
    }

    @MutationMapping
    public boolean deleteProject(@Argument String id, Principal principal) {
        service.delete(UUID.fromString(id), actorResolver.resolve(principal));
        return true;
    }

    private static UUID parseUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private static LocalDate parseDate(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    public record ProjectGraphQlInput(
            @NotBlank String name,
            @NotEmpty List<@NotBlank String> responsibleIds,
            String plannedStart,
            String plannedEnd,
            String actualStart,
            String actualEnd) {

        SaveProjectCommand toCommand() {
            Set<UUID> parsedResponsibleIds = responsibleIds.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toUnmodifiableSet());
            return new SaveProjectCommand(
                    name,
                    parsedResponsibleIds,
                    parseDate(plannedStart),
                    parseDate(plannedEnd),
                    parseDate(actualStart),
                    parseDate(actualEnd));
        }

        private static LocalDate parseDate(String value) {
            return value == null ? null : LocalDate.parse(value);
        }
    }

    public record ProjectGraphQlResponse(
            String id,
            String name,
            String status,
            List<String> responsibleIds,
            String plannedStart,
            String plannedEnd,
            String actualStart,
            String actualEnd,
            long delayDays,
            int remainingTimePercentage,
            String createdAt,
            String updatedAt) {

        static ProjectGraphQlResponse from(Project project) {
            List<String> responsibleIds = project.responsibleIds().stream()
                    .map(UUID::toString)
                    .sorted()
                    .toList();
            return new ProjectGraphQlResponse(
                    project.id().toString(),
                    project.name(),
                    project.status().name(),
                    responsibleIds,
                    formatDate(project.dates().plannedStart()),
                    formatDate(project.dates().plannedEnd()),
                    formatDate(project.dates().actualStart()),
                    formatDate(project.dates().actualEnd()),
                    project.delayDays(),
                    project.remainingTimePercentage(),
                    project.audit().createdAt().toString(),
                    project.audit().updatedAt().toString());
        }

        private static String formatDate(LocalDate value) {
            return value == null ? null : value.toString();
        }
    }

    public record ProjectGraphQlPage(
            List<ProjectGraphQlResponse> content,
            int page,
            int size,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious) {
    }

    public record ProjectIndicatorsGraphQlResponse(
            long totalProjects,
            long delayedProjects,
            List<ProjectStatusIndicatorGraphQlResponse> byStatus) {

        static ProjectIndicatorsGraphQlResponse from(ProjectIndicators indicators) {
            return new ProjectIndicatorsGraphQlResponse(
                    indicators.totalProjects(),
                    indicators.delayedProjects(),
                    indicators.byStatus().stream()
                            .map(item -> new ProjectStatusIndicatorGraphQlResponse(
                                    item.status().name(),
                                    item.projectCount(),
                                    item.averageDelayDays()))
                            .toList());
        }
    }

    public record ProjectStatusIndicatorGraphQlResponse(
            String status,
            long projectCount,
            double averageDelayDays) {
    }

}
