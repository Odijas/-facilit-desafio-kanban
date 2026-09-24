package br.com.facilit.kanban.infrastructure.config;

import br.com.facilit.kanban.application.project.ProjectRepository;
import br.com.facilit.kanban.application.project.ProjectService;
import br.com.facilit.kanban.application.responsible.ResponsibleCredentialRepository;
import br.com.facilit.kanban.application.responsible.ResponsibleCredentialService;
import br.com.facilit.kanban.application.responsible.ResponsibleRepository;
import br.com.facilit.kanban.application.responsible.ResponsibleService;
import br.com.facilit.kanban.application.secretariat.SecretariatRepository;
import br.com.facilit.kanban.application.secretariat.SecretariatService;
import br.com.facilit.kanban.application.health.HealthQuery;
import br.com.facilit.kanban.domain.project.ProjectScheduleCalculator;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * Relógio da aplicação. "Hoje" das regras de status é a data no fuso de negócio (padrão
     * America/Sao_Paulo), não a data UTC; instantes de auditoria continuam absolutos. Fuso inválido
     * impede a subida.
     */
    @Bean
    Clock applicationClock(@Value("${app.time-zone}") String timeZone) {
        return Clock.system(ZoneId.of(timeZone));
    }

    @Bean
    ResponsibleService responsibleService(
            ResponsibleRepository responsibleRepository,
            SecretariatRepository secretariatRepository,
            ProjectRepository projectRepository,
            ResponsibleCredentialRepository responsibleCredentialRepository,
            Clock applicationClock) {
        return new ResponsibleService(
                responsibleRepository,
                secretariatRepository,
                projectRepository,
                responsibleCredentialRepository,
                applicationClock);
    }

    @Bean
    ResponsibleCredentialService responsibleCredentialService(
            ResponsibleRepository responsibleRepository,
            ResponsibleCredentialRepository responsibleCredentialRepository,
            Clock applicationClock) {
        return new ResponsibleCredentialService(
                responsibleRepository,
                responsibleCredentialRepository,
                applicationClock);
    }

    @Bean
    SecretariatService secretariatService(
            SecretariatRepository secretariatRepository,
            ResponsibleRepository responsibleRepository,
            Clock applicationClock) {
        return new SecretariatService(secretariatRepository, responsibleRepository, applicationClock);
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
