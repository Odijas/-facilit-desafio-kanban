# F5-Q2 — GATE

Data: 2026-09-25

Estado: **CANDIDATE — AGUARDANDO GATE LOCAL**.

Escopo: F5-Q2 de `docs/governance/PLANO-CORRECAO-RELEASE-2.0.2.md`: documentação E1/E2, ADR D1, CHANGELOG/AI_USAGE, promoção do F5-Q1 e versionamento da auditoria `v2.0.1`.

GREEN exige:

- `F5_Q2_PRECONDITIONS_GREEN` — branch Q2 baseada exatamente na `origin/hotfix/2.0.2` fechada em `9ab3bbd...`, e somente arquivos documentais do pacote alterados;
- `F5_Q2_DOCS_GREEN` — `git diff --check`, nomes de testes resolvidos no código, referências antigas removidas, ADR/CHANGELOG/AI_USAGE coerentes, links válidos, auditoria original íntegra e Q1 promovido a GREEN;
- `F5_Q2_HISTORY_GREEN` — commits do Q1 desde `v2.0.1` em Conventional Commits;
- `F5_Q1_CI_GREEN` — CI público da `hotfix/2.0.2` no SHA base com jobs `frontend`, `backend` e `repository` em sucesso;
- `=== F5-Q2 GREEN ===` e `Resultado: exit code 0`.

O lote não altera código; Maven/Docker não são repetidos aqui. O freeze Q3 repete as provas de release.
