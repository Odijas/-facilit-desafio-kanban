# F5-Q3 — RISCOS E LACUNAS

Data: 2026-09-25

1. `[DESCONHECIDO]` O freeze real ainda não foi executado; nenhum resultado de Maven/pnpm/Docker/API/observabilidade/CI é inferido pelo candidate.
2. `[DESCONHECIDO]` A tag `v2.0.2`, o merge na `main` e o back-merge na `develop` ainda não existem; o roteiro manda parar no primeiro erro e nunca usar `--force`.
3. `[VERIFICADO]` A auditoria `ADERENCIA-V2.0.1.md` é histórica e não pode ser reescrita como auditoria da 2.0.2.
4. `[DESCONHECIDO]` A nota final da `v2.0.2` só pode ser declarada depois da auditoria do `git archive v2.0.2`; a nota prevista no plano não é resultado executado.
5. `[DESCONHECIDO]` Conferência visual manual do Swagger UI não foi fornecida nesta sessão; o freeze cobre automaticamente conteúdo OpenAPI e configuração CSRF.
