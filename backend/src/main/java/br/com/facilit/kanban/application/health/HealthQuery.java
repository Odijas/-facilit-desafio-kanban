package br.com.facilit.kanban.application.health;

public final class HealthQuery {

    public HealthStatus execute() {
        return new HealthStatus("UP");
    }
}
