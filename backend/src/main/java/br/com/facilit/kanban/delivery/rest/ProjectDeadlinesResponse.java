package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.project.ProjectDeadlineIndicator;
import br.com.facilit.kanban.application.project.ProjectDeadlineIndicators;
import java.util.List;

public record ProjectDeadlinesResponse(
        int withinDays,
        String from,
        String to,
        List<ProjectDeadlineResponse> projects) {

    static ProjectDeadlinesResponse from(ProjectDeadlineIndicators indicators) {
        return new ProjectDeadlinesResponse(
                indicators.withinDays(),
                indicators.from().toString(),
                indicators.to().toString(),
                indicators.projects().stream().map(ProjectDeadlineResponse::from).toList());
    }

    public record ProjectDeadlineResponse(
            String projectId,
            String projectName,
            String status,
            String plannedEnd,
            long daysUntilDeadline) {

        static ProjectDeadlineResponse from(ProjectDeadlineIndicator indicator) {
            return new ProjectDeadlineResponse(
                    indicator.projectId().toString(),
                    indicator.projectName(),
                    indicator.status().name(),
                    indicator.plannedEnd().toString(),
                    indicator.daysUntilDeadline());
        }
    }
}
