# F4 — CONSUMIDORES

Data: 2026-09-23

Arquivos alterados ou criados pelo pacote da F4:

| Arquivo | Mudança | Consumidores |
|---|---|---|
| `backend/pom.xml` | versão `1.0.0` | build Maven, CI; o jar passa a ser `kanban-1.0.0.jar` |
| `backend/Dockerfile` | `COPY` do `kanban-1.0.0.jar` | `docker compose build` (gate F4, uso local) |
| `frontend/package.json` | versão `1.0.0` | pnpm (lockfile inalterado), CI, imagem do frontend |
| `README.md` | estado F3-L4 e F3 GREEN; linha da F4 | leitores; checagem de docs do gate |
| `docs/governance/REPLANEJAMENTO-F3.md` | F3-L4 GREEN | governança |
| `docs/evidence/F3-L4/{GATE,MATRIZ,EXECUCAO,RISCOS}.md` | promoção para GREEN | auditoria |
| `docs/evidence/F4/*` | novos: auditoria, revisão de segurança, roteiro, gates e evidências | release |

A busca por `0.0.1` no repositório, fora de `docs/evidence` e do lockfile, encontrou apenas `pom.xml`, `Dockerfile` e `package.json`.
