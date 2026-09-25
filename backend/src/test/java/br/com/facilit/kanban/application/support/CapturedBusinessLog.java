package br.com.facilit.kanban.application.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Captura as mensagens do log de negócio ({@code br.com.facilit.kanban.business}) durante um teste.
 *
 * <p>Sem provedor próprio, {@link System.Logger} usa o java.util.logging com o mesmo nome de logger; o
 * handler é anexado direto a ele, sem depender de onde a saída padrão está.
 */
public final class CapturedBusinessLog implements AutoCloseable {

    private final Logger logger = Logger.getLogger("br.com.facilit.kanban.business");
    private final List<String> messages = Collections.synchronizedList(new ArrayList<>());
    private final Handler handler = new Handler() {
        @Override
        public void publish(LogRecord logRecord) {
            if (logRecord.getLevel().intValue() >= Level.INFO.intValue()) {
                messages.add(logRecord.getMessage());
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

    private CapturedBusinessLog() {
        logger.addHandler(handler);
    }

    public static CapturedBusinessLog start() {
        return new CapturedBusinessLog();
    }

    public List<String> messages() {
        synchronized (messages) {
            return List.copyOf(messages);
        }
    }

    @Override
    public void close() {
        logger.removeHandler(handler);
    }
}
