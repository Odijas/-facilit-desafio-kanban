package br.com.facilit.kanban.infrastructure.persistence.project;

import br.com.facilit.kanban.domain.project.ProjectStatus;
import br.com.facilit.kanban.infrastructure.persistence.responsible.ResponsibleJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class ProjectJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Column(name = "planned_start")
    private LocalDate plannedStart;

    @Column(name = "planned_end")
    private LocalDate plannedEnd;

    @Column(name = "actual_start")
    private LocalDate actualStart;

    @Column(name = "actual_end")
    private LocalDate actualEnd;

    @Column(name = "delay_days", nullable = false)
    private long delayDays;

    @Column(name = "remaining_time_percentage", nullable = false)
    private short remainingTimePercentage;

    @Column(name = "schedule_calculated_on", nullable = false)
    private LocalDate scheduleCalculatedOn;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_responsibles",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "responsible_id"))
    private Set<ResponsibleJpaEntity> responsibles = new HashSet<>();

    protected ProjectJpaEntity() {
    }

    void replace(
            UUID id,
            String name,
            ProjectStatus status,
            LocalDate plannedStart,
            LocalDate plannedEnd,
            LocalDate actualStart,
            LocalDate actualEnd,
            long delayDays,
            int remainingTimePercentage,
            LocalDate scheduleCalculatedOn,
            Instant createdAt,
            Instant updatedAt,
            Set<ResponsibleJpaEntity> responsibles) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.plannedStart = plannedStart;
        this.plannedEnd = plannedEnd;
        this.actualStart = actualStart;
        this.actualEnd = actualEnd;
        this.delayDays = delayDays;
        this.remainingTimePercentage = (short) remainingTimePercentage;
        this.scheduleCalculatedOn = scheduleCalculatedOn;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.responsibles.clear();
        this.responsibles.addAll(responsibles);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public LocalDate getPlannedStart() {
        return plannedStart;
    }

    public LocalDate getPlannedEnd() {
        return plannedEnd;
    }

    public LocalDate getActualStart() {
        return actualStart;
    }

    public LocalDate getActualEnd() {
        return actualEnd;
    }

    public long getDelayDays() {
        return delayDays;
    }

    public short getRemainingTimePercentage() {
        return remainingTimePercentage;
    }

    public LocalDate getScheduleCalculatedOn() {
        return scheduleCalculatedOn;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Set<ResponsibleJpaEntity> getResponsibles() {
        return Set.copyOf(responsibles);
    }
}
