package br.com.facilit.kanban.infrastructure.config;

import br.com.facilit.kanban.application.project.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Antecipa o recálculo diário de status e métricas: na subida e à meia-noite do fuso de negócio.
 * As leituras também recalculam (ADR 0002), então uma falha aqui não deixa dado errado exposto.
 */
@Component
class ProjectScheduleRefreshJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectScheduleRefreshJob.class);

    private final ProjectService projectService;

    ProjectScheduleRefreshJob(ProjectService projectService) {
        this.projectService = projectService;
    }

    @EventListener(ApplicationReadyEvent.class)
    void refreshOnStartup() {
        refresh("startup");
    }

    @Scheduled(cron = "${app.schedule-refresh.cron}", zone = "${app.time-zone}")
    void refreshDaily() {
        refresh("daily");
    }

    private void refresh(String trigger) {
        try {
            int refreshed = projectService.refreshSchedules();
            LOGGER.info("Recálculo de status concluído: gatilho={}, projetos={}", trigger, refreshed);
        } catch (RuntimeException exception) {
            LOGGER.error("Recálculo de status falhou: gatilho={}, erro={}",
                    trigger, exception.getClass().getSimpleName());
        }
    }
}
