package br.com.facilit.kanban.application.common;

public record PageQuery(int page, int size) {

    private static final int MAX_SIZE = 100;

    public PageQuery {
        if (page < 0) {
            throw new IllegalArgumentException("Página inválida: page não pode ser negativa.");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("Tamanho de página inválido: size deve estar entre 1 e " + MAX_SIZE + ".");
        }
    }
}
