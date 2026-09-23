# F2-L1 — GATE

Data: 2026-09-22

Estado: **GREEN**.

Critérios para GREEN:

- Maven `clean verify` completo, incluindo `KanbanApiIT` e `SecurityApiIT`, sem falhas;
- frontend lint/typecheck/test/build sem regressão;
- migration V4 aplicada sobre o volume prevalente;
- health e documentação pública preservados;
- negócio REST e GraphQL bloqueados para anônimo;
- login válido cria sessão `HttpOnly`/`SameSite=Lax`; credencial inválida retorna 401 genérico;
- POST autenticado sem CSRF é bloqueado e com CSRF legítimo funciona;
- senha persistida não é plaintext e usa hash delegado/bcrypt;
- logout invalida a sessão;
- credenciais de prova não aparecem nos logs;
- `git diff --check` limpo.

`[EXECUTADO PELO USUÁRIO · 2026-09-22]` O gate isolado terminou com `Resultado: exit code 0`; F2-L1 foi promovido para GREEN.
