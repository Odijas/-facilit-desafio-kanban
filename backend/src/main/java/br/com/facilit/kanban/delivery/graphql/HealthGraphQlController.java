package br.com.facilit.kanban.delivery.graphql;

import br.com.facilit.kanban.application.health.HealthQuery;
import br.com.facilit.kanban.application.health.HealthStatus;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class HealthGraphQlController {

    private final HealthQuery healthQuery;

    public HealthGraphQlController(HealthQuery healthQuery) {
        this.healthQuery = healthQuery;
    }

    @QueryMapping
    public HealthStatus health() {
        return healthQuery.execute();
    }
}
