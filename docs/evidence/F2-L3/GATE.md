# F2-L3 — GATE

Data: 2026-09-22

Estado: **CANDIDATE corrigido estruturalmente / aguardando nova execução local**.

Critérios para GREEN:

- `pnpm format` (`biome check --write .`) antes das verificações;
- lint, typecheck, testes e build frontend sem falhas;
- regressão backend GREEN;
- Docker Compose sobe com banco healthy;
- autenticação/CSRF funcionam pela origem do frontend;
- responsável e projeto podem ser criados pela API via proxy;
- projeto é listado e atualizado;
- transição legítima `NOT_STARTED → IN_PROGRESS` funciona;
- transição inválida `IN_PROGRESS → OVERDUE` retorna 400 com mensagem de domínio;
- exclusão e cleanup funcionam;
- `git diff --check` limpo.

Promoção somente após `=== F2-L3 / F2 GREEN ===` e `Resultado: exit code 0`.
