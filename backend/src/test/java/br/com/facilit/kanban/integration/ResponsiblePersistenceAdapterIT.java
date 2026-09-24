package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.infrastructure.persistence.responsible.ResponsiblePersistenceAdapter;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@DataJpaTest(properties = "DB_PASSWORD=test-only")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ResponsiblePersistenceAdapter.class)
class ResponsiblePersistenceAdapterIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine3.24"));

    @Autowired
    private ResponsiblePersistenceAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void listsResponsiblesOrderedByNameWithRealPaginationMetadata() {
        insertResponsible("Carla", "carla@example.invalid");
        insertResponsible("Ana", "ana@example.invalid");
        insertResponsible("Bruno", "bruno@example.invalid");

        var firstPage = adapter.findAll(new PageQuery(0, 2));
        var secondPage = adapter.findAll(new PageQuery(1, 2));

        assertThat(firstPage.content()).extracting(responsible -> responsible.name())
                .containsExactly("Ana", "Bruno");
        assertThat(firstPage.page()).isZero();
        assertThat(firstPage.size()).isEqualTo(2);
        assertThat(firstPage.totalElements()).isEqualTo(3);
        assertThat(firstPage.totalPages()).isEqualTo(2);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.hasPrevious()).isFalse();
        assertThat(secondPage.content()).extracting(responsible -> responsible.name())
                .containsExactly("Carla");
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.hasPrevious()).isTrue();
    }

    private void insertResponsible(String name, String email) {
        jdbcTemplate.update(
                """
                INSERT INTO responsibles (id, name, email, position, created_at, updated_at)
                VALUES (?, ?, ?, 'Analista', now(), now())
                """,
                UUID.randomUUID(), name, email);
    }
}
