# F3-L4 — CONSUMIDORES

Data: 2026-09-23

- `frontend/package.json`: script novo `check:strict`. Nenhum script existente foi alterado.
  - Consumidores dos scripts: gates (`pnpm format/lint/typecheck/test/build`), `Dockerfile` do frontend (`pnpm install`, `pnpm dev`) e CI.
  - `pnpm-lock.yaml` inalterado; `pnpm install --frozen-lockfile` segue válido.
- `frontend/scripts/check-strict-types.mjs` (novo): lido pelo Biome (`files.includes: **`) e aprovado (`Checked 39 files. No fixes applied.`). Fora do `tsconfig` (só `src/` e `vite.config.ts`). Copiado para a imagem do frontend pelo `COPY . .`, sem efeito em runtime.
- `.github/workflows/ci.yml` (novo): sem consumidor no código. `.github/modernize/` local continua ignorado pelo `.gitignore` interno.
- `README.md`: reescrito. Os links internos foram conferidos pelo gate (`F3_L4_DOCS_GREEN`). Nenhum arquivo do projeto aponta para âncoras antigas do README.
- `docs/api`, `docs/adr` e `AI_USAGE.md` (novos): referenciados pelo README.
- Backend, `compose.yaml`, `compose.observability.yaml` e `observability/`: sem alteração.
