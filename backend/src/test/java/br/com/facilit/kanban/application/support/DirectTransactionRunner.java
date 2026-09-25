package br.com.facilit.kanban.application.support;

import br.com.facilit.kanban.application.common.TransactionRunner;
import java.util.function.Supplier;

/**
 * Executa o trabalho direto, sem transação: para testes de aplicação com repositórios em memória.
 * Conta as execuções para os testes que verificam que o caso de uso passou pela porta de transação.
 */
public final class DirectTransactionRunner implements TransactionRunner {

    private int executions;

    @Override
    public <T> T execute(Supplier<T> work) {
        executions++;
        return work.get();
    }

    public int executions() {
        return executions;
    }
}
