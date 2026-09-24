package br.com.facilit.kanban.application.project;

import br.com.facilit.kanban.domain.project.ProjectScheduleCalculator;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Mantém status, dias de atraso e % de tempo restante coerentes com a data de hoje.
 *
 * <p>Os valores são gravados junto com a data de referência do cálculo. Quando o dia muda, os projetos
 * não concluídos calculados antes de hoje são recalculados em lotes. O estado fica no banco, então a
 * verificação é segura com várias instâncias e depois de reinícios, e repetir o recálculo não muda nada.
 */
final class ProjectScheduleRefresher {

    static final int BATCH_SIZE = 500;

    private static final Logger LOGGER = System.getLogger(ProjectScheduleRefresher.class.getName());

    private final ProjectRepository repository;
    private final ProjectScheduleCalculator calculator;
    private final Clock clock;

    ProjectScheduleRefresher(
            ProjectRepository repository,
            ProjectScheduleCalculator calculator,
            Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.calculator = Objects.requireNonNull(calculator);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Recalcula os projetos desatualizados.
     *
     * @return quantidade de projetos recalculados nesta chamada
     */
    int refreshStaleSchedules() {
        LocalDate today = LocalDate.now(clock);
        Set<UUID> failed = new HashSet<>();
        int refreshed = 0;
        while (true) {
            List<ProjectScheduleSnapshot> stale = repository.findStaleSchedules(today, BATCH_SIZE);
            List<ProjectScheduleUpdate> updates = new ArrayList<>(stale.size());
            for (ProjectScheduleSnapshot snapshot : stale) {
                if (failed.contains(snapshot.id())) {
                    continue;
                }
                try {
                    updates.add(new ProjectScheduleUpdate(
                            snapshot.id(),
                            calculator.calculate(snapshot.dates(), today)));
                } catch (RuntimeException exception) {
                    // Um registro inconsistente não pode derrubar a leitura do quadro inteiro.
                    failed.add(snapshot.id());
                    LOGGER.log(
                            Level.WARNING,
                            "Recálculo de status ignorado para o projeto {0}: {1}",
                            snapshot.id(),
                            exception.getClass().getSimpleName());
                }
            }
            int updated = updates.isEmpty() ? 0 : repository.updateSchedules(updates, today);
            refreshed += updated;
            if (stale.size() < BATCH_SIZE || updated == 0) {
                return refreshed;
            }
        }
    }
}
