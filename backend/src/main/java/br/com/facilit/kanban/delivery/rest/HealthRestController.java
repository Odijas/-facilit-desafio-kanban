package br.com.facilit.kanban.delivery.rest;

import br.com.facilit.kanban.application.health.HealthQuery;
import br.com.facilit.kanban.application.health.HealthStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthRestController {

    private final HealthQuery healthQuery;

    public HealthRestController(HealthQuery healthQuery) {
        this.healthQuery = healthQuery;
    }

    @GetMapping
    public HealthStatus health() {
        return healthQuery.execute();
    }
}
