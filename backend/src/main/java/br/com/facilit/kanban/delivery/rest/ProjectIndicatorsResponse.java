package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.project.ProjectIndicators;
import java.util.List;

public record ProjectIndicatorsResponse(
        long totalProjects,
        long delayedProjects,
        List<StatusIndicatorResponse> byStatus) {

    static ProjectIndicatorsResponse from(ProjectIndicators indicators) {
        return new ProjectIndicatorsResponse(
                indicators.totalProjects(),
                indicators.delayedProjects(),
                indicators.byStatus().stream()
                        .map(item -> new StatusIndicatorResponse(
                                item.status().name(),
                                item.projectCount(),
                                item.averageDelayDays()))
                        .toList());
    }

    public record StatusIndicatorResponse(
            String status,
            long projectCount,
            double averageDelayDays) {
    }
}
