package br.com.facilit.kanban.delivery.common;

/**
 * Valores fictícios dos exemplos do OpenAPI, os mesmos em todas as operações para que um exemplo de resposta
 * aponte para os ids dos outros (projeto → responsável → secretaria). Não são dados reais nem credenciais válidas.
 */
public final class ApiExamples {

    public static final String SECRETARIAT_ID = "10000000-0000-4000-8000-000000000001";
    public static final String SECRETARIAT_NAME = "Secretaria de Saúde";

    public static final String RESPONSIBLE_ID = "20000000-0000-4000-8000-000000000001";
    public static final String RESPONSIBLE_NAME = "Maria Silva";
    public static final String RESPONSIBLE_EMAIL = "maria.silva@example.com";
    public static final String RESPONSIBLE_POSITION = "Analista";
    public static final String PASSWORD = "senha-de-exemplo-2026";

    public static final String PROJECT_ID = "30000000-0000-4000-8000-000000000001";
    public static final String PROJECT_NAME = "Implantação do portal";

    public static final String CREATED_AT = "2026-09-01T12:00:00Z";
    public static final String UPDATED_AT = "2026-09-18T17:30:00Z";

    private ApiExamples() {
    }
}
