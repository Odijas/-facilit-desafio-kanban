package br.com.facilit.kanban.application.common;

import java.util.function.Supplier;

/**
 * Porta de transação dos casos de uso: cada caso de uso de escrita roda inteiro numa transação, e uma exceção
 * desfaz tudo o que ele gravou. A aplicação não depende de Spring; a implementação fica na infraestrutura.
 */
public interface TransactionRunner {

    /**
     * Executa {@code work} numa transação (participa da transação corrente, se houver) e devolve o resultado.
     */
    <T> T execute(Supplier<T> work);

    /**
     * Executa {@code work} numa transação (participa da transação corrente, se houver).
     */
    default void run(Runnable work) {
        execute(() -> {
            work.run();
            return null;
        });
    }
}
