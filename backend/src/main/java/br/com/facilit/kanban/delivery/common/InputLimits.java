package br.com.facilit.kanban.delivery.common;

/**
 * Tamanhos máximos das entradas de texto e de listas, iguais em REST e GraphQL. O banco guarda texto sem limite
 * ({@code TEXT}); o limite fica na borda da API, onde a entrada é validada e a resposta é 400 {@code VALIDATION_ERROR}.
 */
public final class InputLimits {

    /** Nome de projeto, responsável ou secretaria e cargo do responsável. */
    public static final int NAME_MAX_LENGTH = 200;

    /** E-mail: 254 caracteres, o maior endereço utilizável em SMTP (RFC 5321, limite do caminho de 256 menos os colchetes). */
    public static final int EMAIL_MAX_LENGTH = 254;

    /** Responsáveis por projeto. */
    public static final int RESPONSIBLES_MAX = 50;

    private InputLimits() {
    }
}
