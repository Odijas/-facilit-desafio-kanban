package br.com.facilit.kanban.application.common;

import java.io.Serial;

public final class ForbiddenOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
