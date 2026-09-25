package br.com.facilit.kanban.infrastructure.config;

import br.com.facilit.kanban.application.common.TransactionRunner;
import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * {@link TransactionRunner} sobre o gerenciador de transações do Spring (propagação REQUIRED; rollback em
 * qualquer exceção não verificada).
 */
public final class SpringTransactionRunner implements TransactionRunner {

    private final TransactionTemplate transactionTemplate;

    public SpringTransactionRunner(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(Objects.requireNonNull(transactionManager));
    }

    @Override
    public <T> T execute(Supplier<T> work) {
        Objects.requireNonNull(work, "work is required");
        return transactionTemplate.execute(status -> work.get());
    }
}
