# F0-L1 — GATE

**Estado: GREEN — promovido em 2026-09-21.**

## Evidências de promoção

[EXECUTADO PELO USUÁRIO · 2026-09-21]

- frontend: Biome GREEN;
- frontend: TypeScript GREEN;
- frontend: Vitest 2/2 GREEN;
- frontend: Vite build GREEN;
- backend: Maven `clean verify` GREEN, 3/3 testes;
- Docker: imagens de backend e frontend construídas;
- PostgreSQL 18.6: container healthy;
- REST: `{"status":"UP"}`;
- GraphQL: `{"data":{"health":{"status":"UP"}}}`;
- frontend: HTTP 200;
- `git diff --check`: GREEN;
- gate final: `exit code 0`.

## Correções absorvidas no lote

- formatação Biome;
- compatibilidade TypeScript/MUI;
- compatibilidade Vitest/jest-dom;
- peer explícito do React Testing Library;
- React Query fora da janela de maturação;
- política pnpm compartilhada entre host e container;
- contextos Docker mínimos;
- remoção da publicação desnecessária da porta 5432;
- volume atualizado para o layout oficial do PostgreSQL 18+;
- gate de runtime com sondagem de readiness em vez de chamada imediata.

## Promoção

F0-L1 está formalmente fechado. F0-L2 pode iniciar somente a partir desta baseline GREEN.
