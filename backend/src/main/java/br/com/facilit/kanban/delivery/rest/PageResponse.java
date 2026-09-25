package br.com.facilit.kanban.delivery.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Página de uma listagem. O exemplo de {@code content} vem do schema de cada item. */
public record PageResponse<T>(
        List<T> content,
        @Schema(example = "0", description = "Página atual, a partir de 0") int page,
        @Schema(example = "20", description = "Itens por página") int size,
        @Schema(example = "1") long totalElements,
        @Schema(example = "1") int totalPages,
        @Schema(example = "false") boolean hasNext,
        @Schema(example = "false") boolean hasPrevious) {
}
