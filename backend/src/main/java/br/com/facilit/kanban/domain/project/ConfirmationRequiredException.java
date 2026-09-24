package br.com.facilit.kanban.domain.project;

import br.com.facilit.kanban.domain.common.BusinessRuleException;
import java.io.Serial;
import java.util.Objects;

/**
 * Transição válida cuja ação automática apaga uma data já registrada: só é aplicada com confirmação explícita.
 */
public final class ConfirmationRequiredException extends BusinessRuleException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ProjectStatus currentStatus;
    private final ProjectStatus requestedStatus;
    private final String clearedField;

    public ConfirmationRequiredException(
            ProjectStatus currentStatus,
            ProjectStatus requestedStatus,
            String clearedField,
            String message) {
        super(message);
        this.currentStatus = Objects.requireNonNull(currentStatus);
        this.requestedStatus = Objects.requireNonNull(requestedStatus);
        this.clearedField = Objects.requireNonNull(clearedField);
    }

    public ProjectStatus currentStatus() {
        return currentStatus;
    }

    public ProjectStatus requestedStatus() {
        return requestedStatus;
    }

    /**
     * Campo da API que a transição apaga ({@code actualStart} ou {@code actualEnd}).
     */
    public String clearedField() {
        return clearedField;
    }
}
