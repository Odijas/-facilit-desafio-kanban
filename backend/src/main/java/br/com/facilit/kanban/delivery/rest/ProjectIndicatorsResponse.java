package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.project.ProjectIndicators;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ProjectIndicatorsResponse(
        @Schema(example = "12") long totalProjects,
        @Schema(example = "3", description = "Projetos com status Atrasado hoje") long delayedProjects,
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
            @Schema(example = "OVERDUE") String status,
            @Schema(example = "3") long projectCount,
            @Schema(example = "4.5", description = "Média de dias de atraso dos projetos do status") double averageDelayDays) {
    }
}
