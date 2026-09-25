package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.project.ProjectGroupIndicator;
import br.com.facilit.kanban.delivery.common.ApiExamples;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProjectGroupIndicatorResponse(
        @Schema(example = ApiExamples.SECRETARIAT_ID, description = "Id da secretaria ou do responsável do grupo") String id,
        @Schema(example = "4") long projectCount,
        @Schema(example = "1.5", description = "Média de dias de atraso dos projetos do grupo") double averageDelayDays) {

    static ProjectGroupIndicatorResponse from(ProjectGroupIndicator indicator) {
        return new ProjectGroupIndicatorResponse(
                indicator.id().toString(),
                indicator.projectCount(),
                indicator.averageDelayDays());
    }
}
