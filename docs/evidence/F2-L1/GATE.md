# F2-L1 — GATE

Data: 2026-09-22

Estado: **CANDIDATE / aguardando execução local**.

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

Promoção somente após `VERIFICACAO-USUARIO.md` terminar com `=== F2-L1 GREEN ===` e `Resultado: exit code 0`.
