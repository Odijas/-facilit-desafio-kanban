package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.facilit.kanban.application.common.PageQuery;
import br.com.facilit.kanban.infrastructure.persistence.secretariat.SecretariatPersistenceAdapter;
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
@Import(SecretariatPersistenceAdapter.class)
class SecretariatPersistenceAdapterIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine3.24"));

    @Autowired
    private SecretariatPersistenceAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void listsSecretariatsOrderedByNameWithRealPaginationMetadata() {
        insertSecretariat("Saúde");
        insertSecretariat("Administração");
        insertSecretariat("Educação");

        var firstPage = adapter.findAll(new PageQuery(0, 2));
        var secondPage = adapter.findAll(new PageQuery(1, 2));

        assertThat(firstPage.content()).extracting(secretariat -> secretariat.name())
                .containsExactly("Administração", "Educação");
        assertThat(firstPage.page()).isZero();
        assertThat(firstPage.size()).isEqualTo(2);
        assertThat(firstPage.totalElements()).isEqualTo(3);
        assertThat(firstPage.totalPages()).isEqualTo(2);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.hasPrevious()).isFalse();
        assertThat(secondPage.content()).extracting(secretariat -> secretariat.name())
                .containsExactly("Saúde");
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.hasPrevious()).isTrue();
    }

    private void insertSecretariat(String name) {
        jdbcTemplate.update(
                "INSERT INTO secretariats (id, name, created_at, updated_at) VALUES (?, ?, now(), now())",
                UUID.randomUUID(), name);
    }
}
