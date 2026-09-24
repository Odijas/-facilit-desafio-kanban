package br.com.facilit.kanban.domain.project;

import java.time.LocalDate;
import java.util.Objects;

public final class ProjectStatusTransition {

    private final ProjectScheduleCalculator scheduleCalculator;

    public ProjectStatusTransition(ProjectScheduleCalculator scheduleCalculator) {
        this.scheduleCalculator = Objects.requireNonNull(scheduleCalculator);
    }

    public ProjectTransitionResult transition(
            Project project,
            ProjectStatus requestedStatus,
            LocalDate today) {
        Objects.requireNonNull(project, "project is required");
        Objects.requireNonNull(requestedStatus, "requestedStatus is required");
        Objects.requireNonNull(today, "today is required");

        // O status gravado pode ter sido calculado em outro dia; a origem da transição é sempre o status de hoje.
        ProjectStatus currentStatus = scheduleCalculator.calculate(project.dates(), today).status();
        if (currentStatus == requestedStatus) {
            throw new IllegalArgumentException("Project is already in status " + requestedStatus);
        }

        validateBeforeTransition(currentStatus, requestedStatus, project.dates(), today);
        ProjectDates transitionedDates = applyAutomaticActions(
                currentStatus,
                requestedStatus,
                project.dates(),
                today);
        ProjectScheduleMetrics recalculated = scheduleCalculator.calculate(transitionedDates, today);

        if (recalculated.status() != requestedStatus) {
            throw new IllegalArgumentException(mismatchMessage(
                    currentStatus,
                    requestedStatus,
                    recalculated.status()));
        }

        return new ProjectTransitionResult(transitionedDates, recalculated);
    }

    private static void validateBeforeTransition(
            ProjectStatus currentStatus,
            ProjectStatus requestedStatus,
            ProjectDates dates,
            LocalDate today) {
        if (currentStatus == ProjectStatus.NOT_STARTED
                && requestedStatus == ProjectStatus.OVERDUE
                && dates.plannedStart() != null
                && today.isBefore(dates.plannedStart())) {
            throw new IllegalArgumentException(
                    "Cannot transition NOT_STARTED to OVERDUE before plannedStart");
        }
    }

    private static ProjectDates applyAutomaticActions(
            ProjectStatus currentStatus,
            ProjectStatus requestedStatus,
            ProjectDates dates,
            LocalDate today) {
        if (requestedStatus == ProjectStatus.COMPLETED
                && currentStatus != ProjectStatus.COMPLETED) {
            return withActualEnd(dates, today);
        }

        if (currentStatus == ProjectStatus.NOT_STARTED
                && requestedStatus == ProjectStatus.IN_PROGRESS) {
            return withActualStart(dates, today);
        }

        if (currentStatus == ProjectStatus.IN_PROGRESS
                && requestedStatus == ProjectStatus.NOT_STARTED) {
            return withActualStart(dates, null);
        }

        if (currentStatus == ProjectStatus.COMPLETED
                && (requestedStatus == ProjectStatus.IN_PROGRESS
                        || requestedStatus == ProjectStatus.OVERDUE)) {
            return withActualEnd(dates, null);
        }

        return dates;
    }

    private static ProjectDates withActualStart(ProjectDates dates, LocalDate actualStart) {
        return new ProjectDates(
                dates.plannedStart(),
                dates.plannedEnd(),
                actualStart,
                dates.actualEnd());
    }

    private static ProjectDates withActualEnd(ProjectDates dates, LocalDate actualEnd) {
        return new ProjectDates(
                dates.plannedStart(),
                dates.plannedEnd(),
                dates.actualStart(),
                actualEnd);
    }

    private static String mismatchMessage(
            ProjectStatus currentStatus,
            ProjectStatus requestedStatus,
            ProjectStatus recalculatedStatus) {
        if (currentStatus == ProjectStatus.IN_PROGRESS
                && requestedStatus == ProjectStatus.OVERDUE) {
            return "Cannot transition IN_PROGRESS to OVERDUE: remove actualStart so overdue rules can apply, "
                    + "or adjust planned dates so the project recalculates as OVERDUE";
        }
        if (currentStatus == ProjectStatus.OVERDUE
                && requestedStatus == ProjectStatus.NOT_STARTED) {
            return "Cannot transition OVERDUE to NOT_STARTED: remove actualStart and adjust planned dates "
                    + "so the project is no longer overdue";
        }
        if (currentStatus == ProjectStatus.OVERDUE
                && requestedStatus == ProjectStatus.IN_PROGRESS) {
            return "Cannot transition OVERDUE to IN_PROGRESS: fill actualStart when needed and adjust plannedEnd "
                    + "so it is not before today";
        }
        if (currentStatus == ProjectStatus.COMPLETED
                && requestedStatus == ProjectStatus.NOT_STARTED) {
            return "Cannot transition COMPLETED to NOT_STARTED: remove actualEnd and adjust planned dates "
                    + "so the project recalculates as NOT_STARTED";
        }
        if (currentStatus == ProjectStatus.COMPLETED
                && requestedStatus == ProjectStatus.IN_PROGRESS) {
            return "Cannot transition COMPLETED to IN_PROGRESS: after removing actualEnd the project must not be "
                    + "overdue and actualStart must remain filled";
        }
        if (currentStatus == ProjectStatus.COMPLETED
                && requestedStatus == ProjectStatus.OVERDUE) {
            return "Cannot transition COMPLETED to OVERDUE: after removing actualEnd the project must satisfy "
                    + "the overdue date rules";
        }
        return "Requested status " + requestedStatus
                + " does not match recalculated status " + recalculatedStatus
                + "; adjust project dates before retrying";
    }
}
