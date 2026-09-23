package br.com.facilit.kanban.application.project;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.application.responsible.ResponsibleRepository;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectDates;
import br.com.facilit.kanban.domain.project.ProjectScheduleCalculator;
import br.com.facilit.kanban.domain.project.ProjectScheduleMetrics;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.domain.project.ProjectStatusTransition;
import br.com.facilit.kanban.domain.project.ProjectTransitionResult;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ProjectService {

    private final ProjectRepository projectRepository;
    private final ResponsibleRepository responsibleRepository;
    private final ProjectScheduleCalculator scheduleCalculator;
    private final ProjectStatusTransition statusTransition;
    private final Clock clock;

    public ProjectService(
            ProjectRepository projectRepository,
            ResponsibleRepository responsibleRepository,
            ProjectScheduleCalculator scheduleCalculator,
            Clock clock) {
        this.projectRepository = Objects.requireNonNull(projectRepository);
        this.responsibleRepository = Objects.requireNonNull(responsibleRepository);
        this.scheduleCalculator = Objects.requireNonNull(scheduleCalculator);
        this.statusTransition = new ProjectStatusTransition(scheduleCalculator);
        this.clock = Objects.requireNonNull(clock);
    }

    public Project create(SaveProjectCommand command, Actor actor) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(actor, "actor is required");
        requireMembership(command.responsibleIds(), actor);
        validateResponsibles(command.responsibleIds());

        ProjectDates dates = datesFrom(command);
        ProjectScheduleMetrics schedule = scheduleCalculator.calculate(dates, LocalDate.now(clock));
        Instant now = clock.instant();
        Project project = new Project(
                UUID.randomUUID(),
                command.name(),
                command.responsibleIds(),
                dates,
                schedule,
                new AuditMetadata(now, now));
        return projectRepository.save(project);
    }

    public Project get(UUID id) {
        Objects.requireNonNull(id, "id is required");
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    public PageResult<Project> list(PageQuery pageQuery) {
        Objects.requireNonNull(pageQuery, "pageQuery is required");
        return projectRepository.findAll(pageQuery);
    }

    public PageResult<Project> listByStatus(ProjectStatus status, PageQuery pageQuery) {
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(pageQuery, "pageQuery is required");
        return projectRepository.findByStatus(status, pageQuery);
    }

    public PageResult<Project> search(ProjectFilter filter, PageQuery pageQuery) {
        Objects.requireNonNull(filter, "filter is required");
        Objects.requireNonNull(pageQuery, "pageQuery is required");
        return projectRepository.search(filter, pageQuery);
    }

    public ProjectIndicators indicators() {
        return projectRepository.indicators();
    }

    public Project update(UUID id, SaveProjectCommand command, Actor actor) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(actor, "actor is required");
        Project current = get(id);
        requireMembership(current.responsibleIds(), actor);
        requireMembership(command.responsibleIds(), actor);
        validateResponsibles(command.responsibleIds());

        ProjectDates dates = datesFrom(command);
        ProjectScheduleMetrics schedule = scheduleCalculator.calculate(dates, LocalDate.now(clock));
        Project updated = new Project(
                current.id(),
                command.name(),
                command.responsibleIds(),
                dates,
                schedule,
                new AuditMetadata(current.audit().createdAt(), clock.instant()));
        return projectRepository.save(updated);
    }

    public Project transition(UUID id, ProjectStatus requestedStatus, Actor actor) {
        Objects.requireNonNull(requestedStatus, "requestedStatus is required");
        Objects.requireNonNull(actor, "actor is required");
        Project current = get(id);
        requireMembership(current.responsibleIds(), actor);
        ProjectTransitionResult transition = statusTransition.transition(
                current,
                requestedStatus,
                LocalDate.now(clock));
        Project updated = new Project(
                current.id(),
                current.name(),
                current.responsibleIds(),
                transition.dates(),
                transition.schedule(),
                new AuditMetadata(current.audit().createdAt(), clock.instant()));
        return projectRepository.save(updated);
    }

    public void delete(UUID id, Actor actor) {
        Objects.requireNonNull(actor, "actor is required");
        Project project = get(id);
        requireMembership(project.responsibleIds(), actor);
        projectRepository.deleteById(project.id());
    }

    private static void requireMembership(Set<UUID> responsibleIds, Actor actor) {
        if (!actor.canManage(responsibleIds)) {
            throw new ForbiddenOperationException(
                    "Responsible users can only manage projects they are assigned to");
        }
    }

    private void validateResponsibles(Set<UUID> responsibleIds) {
        if (responsibleIds.isEmpty()) {
            throw new IllegalArgumentException("responsibleIds must contain at least one responsible");
        }
        if (!responsibleRepository.allExist(responsibleIds)) {
            throw new ResourceNotFoundException("At least one responsible was not found");
        }
    }

    private static ProjectDates datesFrom(SaveProjectCommand command) {
        return new ProjectDates(
                command.plannedStart(),
                command.plannedEnd(),
                command.actualStart(),
                command.actualEnd());
    }
}
