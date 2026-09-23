package br.com.facilit.kanban.infrastructure.persistence.project;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.application.common.PageResult;
import br.com.facilit.kanban.application.common.ResourceNotFoundException;
import br.com.facilit.kanban.application.project.ProjectFilter;
import br.com.facilit.kanban.application.project.ProjectIndicators;
import br.com.facilit.kanban.application.project.ProjectRepository;
import br.com.facilit.kanban.application.project.ProjectStatusIndicator;
import br.com.facilit.kanban.domain.common.AuditMetadata;
import br.com.facilit.kanban.domain.project.Project;
import br.com.facilit.kanban.domain.project.ProjectDates;
import br.com.facilit.kanban.domain.project.ProjectScheduleMetrics;
import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.infrastructure.persistence.responsible.ResponsibleJpaEntity;
import br.com.facilit.kanban.infrastructure.persistence.responsible.ResponsibleJpaRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProjectPersistenceAdapter implements ProjectRepository {

    private final ProjectJpaRepository repository;
    private final ResponsibleJpaRepository responsibleRepository;

    public ProjectPersistenceAdapter(
            ProjectJpaRepository repository,
            ResponsibleJpaRepository responsibleRepository) {
        this.repository = repository;
        this.responsibleRepository = responsibleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Project> findById(UUID id) {
        return repository.findDetailedById(id).map(ProjectPersistenceAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Project> findAll(PageQuery pageQuery) {
        PageRequest pageable = pageable(pageQuery);
        return toPageResult(repository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Project> findByStatus(ProjectStatus status, PageQuery pageQuery) {
        return toPageResult(repository.findByStatus(status, pageable(pageQuery)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Project> search(ProjectFilter filter, PageQuery pageQuery) {
        return toPageResult(repository.findAll(matching(filter), pageable(pageQuery)));
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectIndicators indicators() {
        Map<ProjectStatus, ProjectJpaRepository.ProjectStatusSummaryView> summaries =
                repository.summarizeByStatus().stream()
                        .collect(Collectors.toMap(
                                ProjectJpaRepository.ProjectStatusSummaryView::getStatus,
                                Function.identity()));
        List<ProjectStatusIndicator> byStatus = Arrays.stream(ProjectStatus.values())
                .map(status -> {
                    ProjectJpaRepository.ProjectStatusSummaryView summary = summaries.get(status);
                    return summary == null
                            ? new ProjectStatusIndicator(status, 0, 0)
                            : new ProjectStatusIndicator(
                                    status,
                                    summary.getProjectCount(),
                                    summary.getAverageDelayDays() == null
                                            ? 0
                                            : summary.getAverageDelayDays());
                })
                .toList();
        return new ProjectIndicators(
                repository.count(),
                repository.countByDelayDaysGreaterThan(0),
                byStatus);
    }

    @Override
    @Transactional
    public Project save(Project project) {
        Set<ResponsibleJpaEntity> responsibles = Set.copyOf(responsibleRepository.findAllById(project.responsibleIds()));
        if (responsibles.size() != project.responsibleIds().size()) {
            throw new ResourceNotFoundException("At least one responsible was not found");
        }

        ProjectJpaEntity entity = repository.findById(project.id()).orElseGet(ProjectJpaEntity::new);
        entity.replace(
                project.id(),
                project.name(),
                project.status(),
                project.dates().plannedStart(),
                project.dates().plannedEnd(),
                project.dates().actualStart(),
                project.dates().actualEnd(),
                project.delayDays(),
                project.remainingTimePercentage(),
                project.audit().createdAt(),
                project.audit().updatedAt(),
                responsibles);
        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByResponsibleId(UUID responsibleId) {
        return repository.countByResponsibles_Id(responsibleId) > 0;
    }

    private PageResult<Project> toPageResult(Page<ProjectJpaEntity> page) {
        List<UUID> ids = page.getContent().stream().map(ProjectJpaEntity::getId).toList();
        Map<UUID, ProjectJpaEntity> detailed = loadDetailed(ids);
        List<Project> content = ids.stream()
                .map(detailed::get)
                .map(ProjectPersistenceAdapter::toDomain)
                .toList();
        return new PageResult<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    private static Specification<ProjectJpaEntity> matching(ProjectFilter filter) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.status() != null) {
                predicates.add(builder.equal(root.get("status"), filter.status()));
            }
            if (filter.responsibleId() != null || filter.secretariatId() != null) {
                Join<ProjectJpaEntity, ResponsibleJpaEntity> responsibles = root.join("responsibles");
                query.distinct(true);
                if (filter.responsibleId() != null) {
                    predicates.add(builder.equal(responsibles.get("id"), filter.responsibleId()));
                }
                if (filter.secretariatId() != null) {
                    predicates.add(builder.equal(responsibles.get("secretariatId"), filter.secretariatId()));
                }
            }
            if (filter.plannedFrom() != null) {
                predicates.add(builder.greaterThanOrEqualTo(
                        root.<LocalDate>get("plannedEnd"), filter.plannedFrom()));
            }
            if (filter.plannedTo() != null) {
                predicates.add(builder.lessThanOrEqualTo(
                        root.<LocalDate>get("plannedStart"), filter.plannedTo()));
            }
            if (filter.text() != null) {
                predicates.add(builder.like(
                        builder.lower(root.<String>get("name")),
                        builder.lower(builder.literal("%" + filter.text() + "%"))));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static PageRequest pageable(PageQuery pageQuery) {
        return PageRequest.of(
                pageQuery.page(),
                pageQuery.size(),
                Sort.by("name").ascending().and(Sort.by("id").ascending()));
    }

    private Map<UUID, ProjectJpaEntity> loadDetailed(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return repository.findDetailedByIdIn(ids).stream()
                .collect(Collectors.toMap(ProjectJpaEntity::getId, Function.identity()));
    }

    private static Project toDomain(ProjectJpaEntity entity) {
        Set<UUID> responsibleIds = entity.getResponsibles().stream()
                .map(ResponsibleJpaEntity::getId)
                .collect(Collectors.toUnmodifiableSet());
        ProjectDates dates = new ProjectDates(
                entity.getPlannedStart(),
                entity.getPlannedEnd(),
                entity.getActualStart(),
                entity.getActualEnd());
        ProjectScheduleMetrics schedule = new ProjectScheduleMetrics(
                entity.getStatus(),
                entity.getDelayDays(),
                entity.getRemainingTimePercentage());
        return new Project(
                entity.getId(),
                entity.getName(),
                responsibleIds,
                dates,
                schedule,
                new AuditMetadata(entity.getCreatedAt(), entity.getUpdatedAt()));
    }
}
