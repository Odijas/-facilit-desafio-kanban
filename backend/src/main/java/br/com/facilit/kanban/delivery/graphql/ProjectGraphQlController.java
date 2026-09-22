package br.com.facilit.kanban.delivery.graphql;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.project.ProjectService;
import br.com.facilit.kanban.application.project.SaveProjectCommand;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    public ProjectGraphQlController(ProjectService service) {
        this.service = service;
    }

    @QueryMapping
    public ProjectGraphQlResponse project(@Argument String id) {
        return ProjectGraphQlResponse.from(service.get(UUID.fromString(id)));
    }

    @QueryMapping
    public ProjectGraphQlPage projects(
            @Argument int page,
            @Argument int size,
            @Argument ProjectStatus status) {
        PageQuery pageQuery = new PageQuery(page, size);
        PageResult<Project> result = status == null
                ? service.list(pageQuery)
                : service.listByStatus(status, pageQuery);
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

    @MutationMapping
    public ProjectGraphQlResponse createProject(@Argument @Valid ProjectGraphQlInput input) {
        return ProjectGraphQlResponse.from(service.create(input.toCommand()));
    }

    @MutationMapping
    public ProjectGraphQlResponse updateProject(
            @Argument String id,
            @Argument @Valid ProjectGraphQlInput input) {
        return ProjectGraphQlResponse.from(service.update(UUID.fromString(id), input.toCommand()));
    }

    @MutationMapping
    public ProjectGraphQlResponse transitionProject(
            @Argument String id,
            @Argument ProjectStatus status) {
        return ProjectGraphQlResponse.from(service.transition(UUID.fromString(id), status));
    }

    @MutationMapping
    public boolean deleteProject(@Argument String id) {
        service.delete(UUID.fromString(id));
        return true;
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
}
