# F5-C2 — RISCOS E LACUNAS

Data: 2026-09-24

- `[DESCONHECIDO]` compilação e execução dos testes alterados (Maven Central bloqueado aqui). Domínio, aplicação e `InputLimits` compilam com `-Werror`. Resolve: `F5_C2_BACKEND_GREEN`.
- `[HIPÓTESE]` a ordem das violações no REST é alfabética por campo (`RestExceptionHandler` ordena por `FieldError::getField`). Os testes contam com isso: `email`, `name`, `position`.
- `[HIPÓTESE]` o e-mail de 255 caracteres usado no teste só viola o `@Size`: parte local com 64, rótulos de domínio com até 63 e domínio total com 190. Se o `@Email` também recusar, o teste acusa `violations.length()` diferente de 3.
- `[VERIFICADO]` a classe `InputLimits` só tem constantes, que o compilador copia para o código que as usa. O construtor privado nunca roda e fica como linha não coberta no JaCoCo (impacto desprezível sobre 95,61%).
- `[VERIFICADO]` fora do lote: coleção, AI_USAGE, README sem texto de trabalho, CHANGELOG e auditoria (F5-C3).
