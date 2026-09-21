package br.com.facilit.kanban.application.health;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HealthQueryTest {

    @Test
    void returnsUpStatus() {
        HealthStatus result = new HealthQuery().execute();

        assertThat(result.status()).isEqualTo("UP");
    }
}
