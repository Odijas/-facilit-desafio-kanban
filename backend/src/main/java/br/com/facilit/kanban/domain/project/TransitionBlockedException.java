package br.com.facilit.kanban.domain.project;

import br.com.facilit.kanban.domain.common.BusinessRuleException;
import java.io.Serial;
import java.util.Objects;

/**
 * Transição recusada pela tabela de transição do desafio, com a orientação do que ajustar.
 */
public final class TransitionBlockedException extends BusinessRuleException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ProjectStatus currentStatus;
    private final ProjectStatus requestedStatus;

    public TransitionBlockedException(ProjectStatus currentStatus, ProjectStatus requestedStatus, String message) {
        super(message);
        this.currentStatus = Objects.requireNonNull(currentStatus);
        this.requestedStatus = Objects.requireNonNull(requestedStatus);
    }

    public ProjectStatus currentStatus() {
        return currentStatus;
    }

    public ProjectStatus requestedStatus() {
        return requestedStatus;
    }
}
