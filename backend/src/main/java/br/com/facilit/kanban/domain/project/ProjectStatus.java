package br.com.facilit.kanban.domain.project;

public enum ProjectStatus {
    NOT_STARTED("A iniciar"),
    IN_PROGRESS("Em andamento"),
    OVERDUE("Atrasado"),
    COMPLETED("Concluído");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }

    /**
     * Nome do status como aparece no quadro e nas mensagens.
     */
    public String label() {
        return label;
    }
}
