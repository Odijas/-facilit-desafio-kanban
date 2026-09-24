package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.project.ProjectGroupIndicator;

public record ProjectGroupIndicatorResponse(
        String id,
        long projectCount,
        double averageDelayDays) {

    static ProjectGroupIndicatorResponse from(ProjectGroupIndicator indicator) {
        return new ProjectGroupIndicatorResponse(
                indicator.id().toString(),
                indicator.projectCount(),
                indicator.averageDelayDays());
    }
}
