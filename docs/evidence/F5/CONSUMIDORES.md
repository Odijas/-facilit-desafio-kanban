# F5-L5 — CONSUMIDORES

Data: 2026-09-24

| Arquivo | Mudança | Consumidores |
|---|---|---|
| `backend/pom.xml` | versão `2.0.0` | Maven, CI e nome do jar `kanban-2.0.0.jar` |
| `backend/Dockerfile` | jar `kanban-2.0.0.jar` | `docker compose build` e gate de freeze |
| `frontend/package.json` | versão `2.0.0` | pnpm, CI e metadado do frontend; o lockfile não guarda a versão do pacote raiz |
| `CHANGELOG.md` | release 2.0.0 e quebras de contrato | avaliador e revisão da release |
| `README.md` | estado do F5-L5 e link do changelog | página pública do repositório |
| `docs/evidence/F5/*` | auditoria, gates e roteiro | freeze, release e rastreabilidade |

[EXECUTADO · 2026-09-24] Busca no snapshot de entrada por `1.0.0` mostrou como consumidores atuais da versão somente `backend/pom.xml`, `backend/Dockerfile` e `frontend/package.json`; as demais ocorrências são referências históricas à release v1.0.0 e permanecem intactas.
