package br.com.facilit.kanban.infrastructure.persistence.project;

import br.com.facilit.kanban.domain.project.ProjectStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("""
            select p.status as status,
                   count(p) as projectCount,
                   avg(p.delayDays) as averageDelayDays
            from ProjectJpaEntity p
            group by p.status
            """)
    List<ProjectStatusSummaryView> summarizeByStatus();

    long countByDelayDaysGreaterThan(long delayDays);

    @Query(value = """
            select grouped.secretariat_id as "groupId",
                   count(*) as "projectCount",
                   avg(grouped.delay_days) as "averageDelayDays"
            from (
                select distinct r.secretariat_id, p.id, p.delay_days
                from project_responsibles pr
                join responsibles r on r.id = pr.responsible_id
                join projects p on p.id = pr.project_id
                where r.secretariat_id is not null
            ) grouped
            group by grouped.secretariat_id
            order by grouped.secretariat_id
            """, nativeQuery = true)
    List<ProjectGroupSummaryView> summarizeBySecretariat();

    @Query(value = """
            select r.id as "groupId",
                   count(pr.project_id) as "projectCount",
                   avg(p.delay_days) as "averageDelayDays"
            from project_responsibles pr
            join responsibles r on r.id = pr.responsible_id
            join projects p on p.id = pr.project_id
            group by r.id
            order by r.id
            """, nativeQuery = true)
    List<ProjectGroupSummaryView> summarizeByResponsible();

    @Query("""
            select p.id as projectId,
                   p.name as projectName,
                   p.status as status,
                   p.plannedEnd as plannedEnd
            from ProjectJpaEntity p
            where p.status <> :completed
              and p.plannedEnd between :from and :to
            order by p.plannedEnd, p.name, p.id
            """)
    List<ProjectDeadlineView> findDeadlines(
            @Param("completed") ProjectStatus completed,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            select p.id as id,
                   p.plannedStart as plannedStart,
                   p.plannedEnd as plannedEnd,
                   p.actualStart as actualStart,
                   p.actualEnd as actualEnd
            from ProjectJpaEntity p
            where p.status in :statuses
              and p.scheduleCalculatedOn < :today
            order by p.id
            """)
    List<StaleScheduleView> findStaleSchedules(
            @Param("statuses") Collection<ProjectStatus> statuses,
            @Param("today") LocalDate today,
            Pageable pageable);

    @Modifying
    @Query("""
            update ProjectJpaEntity p
            set p.status = :status,
                p.delayDays = :delayDays,
                p.remainingTimePercentage = :remainingTimePercentage,
                p.scheduleCalculatedOn = :calculatedOn
            where p.id = :id
              and p.scheduleCalculatedOn < :calculatedOn
            """)
    int updateSchedule(
            @Param("id") UUID id,
            @Param("status") ProjectStatus status,
            @Param("delayDays") long delayDays,
            @Param("remainingTimePercentage") short remainingTimePercentage,
            @Param("calculatedOn") LocalDate calculatedOn);

    long countByResponsibles_Id(UUID responsibleId);

    interface StaleScheduleView {
        UUID getId();

        LocalDate getPlannedStart();

        LocalDate getPlannedEnd();

        LocalDate getActualStart();

        LocalDate getActualEnd();
    }

    interface ProjectStatusSummaryView {
        ProjectStatus getStatus();

        long getProjectCount();

        Double getAverageDelayDays();
    }

    interface ProjectGroupSummaryView {
        UUID getGroupId();

        long getProjectCount();

        BigDecimal getAverageDelayDays();
    }

    interface ProjectDeadlineView {
        UUID getProjectId();

        String getProjectName();

        ProjectStatus getStatus();

        LocalDate getPlannedEnd();
    }
}
