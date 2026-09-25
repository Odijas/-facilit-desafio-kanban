package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.project.ProjectDeadlineIndicator;
import br.com.facilit.kanban.application.project.ProjectDeadlineIndicators;
import br.com.facilit.kanban.delivery.common.ApiExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ProjectDeadlinesResponse(
        @Schema(example = "7") int withinDays,
        @Schema(example = "2026-09-25", description = "Início da janela (hoje)") String from,
        @Schema(example = "2026-10-02", description = "Fim da janela (hoje + withinDays)") String to,
        List<ProjectDeadlineResponse> projects) {

    static ProjectDeadlinesResponse from(ProjectDeadlineIndicators indicators) {
        return new ProjectDeadlinesResponse(
                indicators.withinDays(),
                indicators.from().toString(),
                indicators.to().toString(),
                indicators.projects().stream().map(ProjectDeadlineResponse::from).toList());
    }

    public record ProjectDeadlineResponse(
            @Schema(example = ApiExamples.PROJECT_ID) String projectId,
            @Schema(example = ApiExamples.PROJECT_NAME) String projectName,
            @Schema(example = "IN_PROGRESS") String status,
            @Schema(example = "2026-09-30") String plannedEnd,
            @Schema(example = "5") long daysUntilDeadline) {

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
