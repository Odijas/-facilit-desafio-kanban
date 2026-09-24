package br.com.facilit.kanban.domain.common;

import java.io.Serial;

/**
 * Regra de negócio violada: a requisição é bem formada, mas os dados não atendem às regras do domínio.
 */
public class BusinessRuleException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BusinessRuleException(String message) {
        super(message);
    }
}
