# F1-L1 — RISCOS E LACUNAS

Data: 2026-09-22

- `[EXECUTADO PELO USUÁRIO]` Compilação/testes Java 25, Spring Context, PostgreSQL/Flyway, REST, GraphQL e Docker passaram no gate pós-correção.
- `[EXECUTADO PELO USUÁRIO]` O primeiro gate encontrou regressão nos slice tests de health; a correção mínima, importando somente `HealthQuery`, passou na reexecução.
- `[VERIFICADO]` O aviso existente de auto-attach do Mockito pertence à baseline F0 e permanece pendência não bloqueante.
- `[VERIFICADO]` O contrato de erros continua propositalmente mínimo; padronização detalhada pertence ao F1-L3.
- `[VERIFICADO]` Não há autenticação neste lote; segurança dos endpoints pertence à F2-L1.
