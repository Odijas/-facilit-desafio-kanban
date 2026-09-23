package br.com.facilit.kanban.infrastructure.persistence.project;

import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ProjectJpaRepository
        extends JpaRepository<ProjectJpaEntity, UUID>, JpaSpecificationExecutor<ProjectJpaEntity> {

    @EntityGraph(attributePaths = "responsibles")
    @Query("select p from ProjectJpaEntity p where p.id = :id")
    Optional<ProjectJpaEntity> findDetailedById(@Param("id") UUID id);

    @EntityGraph(attributePaths = "responsibles")
    @Query("select distinct p from ProjectJpaEntity p where p.id in :ids")
    List<ProjectJpaEntity> findDetailedByIdIn(@Param("ids") Collection<UUID> ids);

    Page<ProjectJpaEntity> findByStatus(ProjectStatus status, Pageable pageable);

    @Query("""
            select p.status as status,
                   count(p) as projectCount,
                   avg(p.delayDays) as averageDelayDays
            from ProjectJpaEntity p
            group by p.status
            """)
    List<ProjectStatusSummaryView> summarizeByStatus();

    long countByDelayDaysGreaterThan(long delayDays);

    long countByResponsibles_Id(UUID responsibleId);

    interface ProjectStatusSummaryView {
        ProjectStatus getStatus();

        long getProjectCount();

        Double getAverageDelayDays();
    }
}
