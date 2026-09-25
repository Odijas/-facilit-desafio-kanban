package br.com.facilit.kanban.application.project;

import br.com.facilit.kanban.application.common.Actor;
import br.com.facilit.kanban.application.common.BusinessLog;
import br.com.facilit.kanban.application.common.ForbiddenOperationException;
import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.application.common.TransactionRunner;
import br.com.facilit.kanban.application.responsible.ResponsibleRepository;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.common.BusinessRuleException;
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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ProjectService {

    private final ProjectRepository projectRepository;
    private final ResponsibleRepository responsibleRepository;
    private final ProjectScheduleCalculator scheduleCalculator;
    private final ProjectStatusTransition statusTransition;
    private final ProjectScheduleRefresher scheduleRefresher;
    private final TransactionRunner transactions;
    private final Clock clock;

    public ProjectService(
            ProjectRepository projectRepository,
            ResponsibleRepository responsibleRepository,
            ProjectScheduleCalculator scheduleCalculator,
            TransactionRunner transactions,
            Clock clock) {
        this.projectRepository = Objects.requireNonNull(projectRepository);
        this.responsibleRepository = Objects.requireNonNull(responsibleRepository);
        this.scheduleCalculator = Objects.requireNonNull(scheduleCalculator);
        this.statusTransition = new ProjectStatusTransition(scheduleCalculator);
        this.transactions = Objects.requireNonNull(transactions);
        this.clock = Objects.requireNonNull(clock);
        this.scheduleRefresher = new ProjectScheduleRefresher(projectRepository, scheduleCalculator, clock);
    }

    /**
     * Recalcula status e métricas dos projetos calculados antes de hoje. As leituras já chamam este
     * método; o agendamento diário só antecipa o trabalho.
     *
     * @return quantidade de projetos recalculados
     */
    public int refreshSchedules() {
        return scheduleRefresher.refreshStaleSchedules();
    }

    public Project create(SaveProjectCommand command, Actor actor) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(actor, "actor is required");
        Project saved = transactions.execute(() -> {
            requireMembership(command.responsibleIds(), actor);
            validateResponsibles(command.responsibleIds());

            LocalDate today = LocalDate.now(clock);
            ProjectDates dates = datesFrom(command);
            dates.requireActualDatesNotAfter(today);
            ProjectScheduleMetrics schedule = scheduleCalculator.calculate(dates, today);
            Instant now = clock.instant();
            Project project = new Project(
                    UUID.randomUUID(),
                    command.name(),
                    command.responsibleIds(),
                    dates,
                    schedule,
                    new AuditMetadata(now, now));
            return projectRepository.save(project, today);
        });
        BusinessLog.info("projeto.criado", "id=" + saved.id() + " status=" + saved.status()
                + " ator=" + actor.auditLabel());
        return saved;
    }

    public Project get(UUID id) {
        Objects.requireNonNull(id, "id is required");
        refreshSchedules();
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado: " + id));
    }

    public PageResult<Project> list(PageQuery pageQuery) {
        Objects.requireNonNull(pageQuery, "pageQuery is required");
        refreshSchedules();
        return projectRepository.findAll(pageQuery);
    }

    public PageResult<Project> listByStatus(ProjectStatus status, PageQuery pageQuery) {
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(pageQuery, "pageQuery is required");
        refreshSchedules();
        return projectRepository.findByStatus(status, pageQuery);
    }

    public PageResult<Project> search(ProjectFilter filter, PageQuery pageQuery) {
        Objects.requireNonNull(filter, "filter is required");
        Objects.requireNonNull(pageQuery, "pageQuery is required");
        refreshSchedules();
        return projectRepository.search(filter, pageQuery);
    }

    public ProjectIndicators indicators() {
        refreshSchedules();
        return projectRepository.indicators();
    }

    public List<ProjectGroupIndicator> indicatorsBySecretariat() {
        refreshSchedules();
        return projectRepository.indicatorsBySecretariat();
    }

    public List<ProjectGroupIndicator> indicatorsByResponsible() {
        refreshSchedules();
        return projectRepository.indicatorsByResponsible();
    }

    public ProjectDeadlineIndicators deadlines(int withinDays) {
        if (withinDays < 1 || withinDays > 90) {
            throw new IllegalArgumentException("withinDays deve estar entre 1 e 90.");
        }
        refreshSchedules();
        LocalDate today = LocalDate.now(clock);
        LocalDate to = today.plusDays(withinDays);
        return new ProjectDeadlineIndicators(
                withinDays,
                today,
                to,
                projectRepository.deadlines(today, to));
    }

    public Project update(UUID id, SaveProjectCommand command, Actor actor) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(actor, "actor is required");
        Project saved = transactions.execute(() -> {
            Project current = get(id);
            requireMembership(current.responsibleIds(), actor);
            requireMembership(command.responsibleIds(), actor);
            validateResponsibles(command.responsibleIds());

            LocalDate today = LocalDate.now(clock);
            ProjectDates dates = datesFrom(command);
            dates.requireActualDatesNotAfter(today);
            ProjectScheduleMetrics schedule = scheduleCalculator.calculate(dates, today);
            Project updated = new Project(
                    current.id(),
                    command.name(),
                    command.responsibleIds(),
                    dates,
                    schedule,
                    new AuditMetadata(current.audit().createdAt(), clock.instant()));
            return projectRepository.save(updated, today);
        });
        BusinessLog.info("projeto.atualizado", "id=" + saved.id() + " status=" + saved.status()
                + " ator=" + actor.auditLabel());
        return saved;
    }

    /**
     * Executa uma transição da tabela do desafio.
     *
     * @param confirmed confirmação explícita, exigida quando a ação automática apaga uma data registrada
     */
    public Project transition(UUID id, ProjectStatus requestedStatus, boolean confirmed, Actor actor) {
        Objects.requireNonNull(requestedStatus, "requestedStatus is required");
        Objects.requireNonNull(actor, "actor is required");
        TransitionOutcome outcome = transactions.execute(() -> {
            Project current = get(id);
            requireMembership(current.responsibleIds(), actor);
            LocalDate today = LocalDate.now(clock);
            ProjectTransitionResult transition;
            try {
                transition = statusTransition.transition(current, requestedStatus, today, confirmed);
            } catch (BusinessRuleException exception) {
                BusinessLog.info("projeto.transicao.recusada", "id=" + current.id() + " de=" + current.status()
                        + " para=" + requestedStatus + " motivo=" + exception.getClass().getSimpleName()
                        + " ator=" + actor.auditLabel());
                throw exception;
            }
            Project updated = new Project(
                    current.id(),
                    current.name(),
                    current.responsibleIds(),
                    transition.dates(),
                    transition.schedule(),
                    new AuditMetadata(current.audit().createdAt(), clock.instant()));
            return new TransitionOutcome(current.status(), projectRepository.save(updated, today));
        });
        Project saved = outcome.saved();
        BusinessLog.info("projeto.transicao", "id=" + saved.id() + " de=" + outcome.previousStatus()
                + " para=" + saved.status() + " confirmado=" + confirmed + " ator=" + actor.auditLabel());
        return saved;
    }

    public void delete(UUID id, Actor actor) {
        Objects.requireNonNull(actor, "actor is required");
        Project project = transactions.execute(() -> {
            Project current = get(id);
            requireMembership(current.responsibleIds(), actor);
            projectRepository.deleteById(current.id());
            return current;
        });
        BusinessLog.info("projeto.excluido", "id=" + project.id() + " ator=" + actor.auditLabel());
    }

    private static void requireMembership(Set<UUID> responsibleIds, Actor actor) {
        if (!actor.canManage(responsibleIds)) {
            throw new ForbiddenOperationException(
                    "O responsável só pode alterar projetos em que é responsável.");
        }
    }

    private void validateResponsibles(Set<UUID> responsibleIds) {
        if (responsibleIds.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um responsável (responsibleIds).");
        }
        if (!responsibleRepository.allExist(responsibleIds)) {
            throw new ResourceNotFoundException("Ao menos um responsável informado não foi encontrado.");
        }
    }

    private static ProjectDates datesFrom(SaveProjectCommand command) {
        return new ProjectDates(
                command.plannedStart(),
                command.plannedEnd(),
                command.actualStart(),
                command.actualEnd());
    }

    private record TransitionOutcome(ProjectStatus previousStatus, Project saved) {
    }
}
