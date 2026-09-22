package br.com.facilit.kanban.application.common;

import java.io.Serial;

public final class ConflictException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super(message);
    }
}
