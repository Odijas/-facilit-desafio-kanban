package br.com.facilit.kanban.application.common;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;

/**
 * Registro dos eventos de negócio (criação, alteração, transição, exclusão) em formato {@code chave=valor}.
 *
 * <p>Só identificadores, status e perfil do ator: nunca nome, e-mail, senha ou outro dado pessoal. Usa
 * {@link System.Logger} para manter a aplicação sem dependência de framework; no Spring Boot, o registro
 * chega ao Logback (logs ECS no Compose) pela ponte JUL→SLF4J.
 */
public final class BusinessLog {

    private static final Logger LOGGER = System.getLogger("br.com.facilit.kanban.business");

    private BusinessLog() {
    }

    public static void info(String event, String details) {
        Objects.requireNonNull(event, "event is required");
        Objects.requireNonNull(details, "details is required");
        LOGGER.log(Level.INFO, "evento=" + event + " " + details);
    }
}
