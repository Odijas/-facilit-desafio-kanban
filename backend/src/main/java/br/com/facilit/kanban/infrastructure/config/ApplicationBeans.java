package br.com.facilit.kanban.infrastructure.config;

import br.com.facilit.kanban.application.project.ProjectRepository;
import br.com.facilit.kanban.application.project.ProjectService;
import br.com.facilit.kanban.application.responsible.ResponsibleRepository;
import br.com.facilit.kanban.application.responsible.ResponsibleService;
import br.com.facilit.kanban.application.responsible.SecretariatRepository;
import br.com.facilit.kanban.application.health.HealthQuery;
import br.com.facilit.kanban.domain.project.ProjectScheduleCalculator;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ApplicationBeans {

    @Bean
    HealthQuery healthQuery() {
        return new HealthQuery();
    }

    @Bean
    ProjectScheduleCalculator projectScheduleCalculator() {
        return new ProjectScheduleCalculator();
    }

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }

    @Bean
    ResponsibleService responsibleService(
            ResponsibleRepository responsibleRepository,
            SecretariatRepository secretariatRepository,
            ProjectRepository projectRepository,
            Clock applicationClock) {
        return new ResponsibleService(
                responsibleRepository,
                secretariatRepository,
                projectRepository,
                applicationClock);
    }

    @Bean
    ProjectService projectService(
            ProjectRepository projectRepository,
            ResponsibleRepository responsibleRepository,
            ProjectScheduleCalculator projectScheduleCalculator,
            Clock applicationClock) {
        return new ProjectService(
                projectRepository,
                responsibleRepository,
                projectScheduleCalculator,
                applicationClock);
    }
}
