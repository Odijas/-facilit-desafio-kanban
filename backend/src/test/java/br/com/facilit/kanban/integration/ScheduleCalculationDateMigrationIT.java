package br.com.facilit.kanban.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * A V6 sobe sobre um banco da v1.0.0 (V1–V5) com dados e preenche a data do último cálculo.
 */
@Testcontainers
class ScheduleCalculationDateMigrationIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine3.24"));

    @Test
    void backfillsCalculationDateWithTheUtcDateOfTheLastSave() throws SQLException {
        migrate("5");
        UUID savedLateInTheEvening = UUID.randomUUID();
        UUID savedAtNoon = UUID.randomUUID();
        try (Connection connection = connect()) {
            // 20/09 às 22:30 em São Paulo = 21/09 01:30 UTC: a v1.0.0 calculou com "hoje" = 21/09.
            insertProject(connection, savedLateInTheEvening, "IN_PROGRESS", "2026-09-20T22:30:00-03:00");
            insertProject(connection, savedAtNoon, "COMPLETED", "2026-09-10T12:00:00Z");
        }

        migrate(null);

        try (Connection connection = connect()) {
            assertThat(calculatedOn(connection, savedLateInTheEvening)).isEqualTo(LocalDate.of(2026, 9, 21));
            assertThat(calculatedOn(connection, savedAtNoon)).isEqualTo(LocalDate.of(2026, 9, 10));
            assertThat(singleText(connection, """
                    SELECT is_nullable FROM information_schema.columns
                    WHERE table_name = 'projects' AND column_name = 'schedule_calculated_on'
                    """)).isEqualTo("NO");
            assertThat(singleText(connection, """
                    SELECT indexname FROM pg_indexes
                    WHERE tablename = 'projects' AND indexname = 'ix_projects_status_schedule_calculated_on'
                    """)).isEqualTo("ix_projects_status_schedule_calculated_on");
        }
    }

    private static void migrate(String target) {
        FluentConfiguration configuration = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration");
        if (target != null) {
            configuration = configuration.target(target);
        }
        configuration.load().migrate();
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static void insertProject(Connection connection, UUID id, String status, String savedAt)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO projects (
                    id, name, status, planned_start, planned_end, actual_start, actual_end,
                    delay_days, remaining_time_percentage, created_at, updated_at)
                VALUES (?, 'Projeto da v1.0.0', ?, DATE '2026-09-01', DATE '2026-09-30',
                        DATE '2026-09-01', ?, 0, 0, ?::timestamptz, ?::timestamptz)
                """)) {
            statement.setObject(1, id);
            statement.setString(2, status);
            statement.setObject(3, "COMPLETED".equals(status) ? LocalDate.of(2026, 9, 10) : null, Types.DATE);
            statement.setString(4, savedAt);
            statement.setString(5, savedAt);
            statement.executeUpdate();
        }
    }

    private static LocalDate calculatedOn(Connection connection, UUID id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT schedule_calculated_on FROM projects WHERE id = ?")) {
            statement.setObject(1, id);
            try (ResultSet result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                return result.getObject(1, LocalDate.class);
            }
        }
    }

    private static String singleText(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            assertThat(result.next()).isTrue();
            return result.getString(1);
        }
    }
}
