package br.com.facilit.kanban.infrastructure.config;

import br.com.facilit.kanban.application.health.HealthQuery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ApplicationBeans {

    @Bean
    HealthQuery healthQuery() {
        return new HealthQuery();
    }
}
