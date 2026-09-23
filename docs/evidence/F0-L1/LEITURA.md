# F0-L1 — LIVRO-RAZÃO DE LEITURA

Data de fechamento: 2026-09-21.

## Arquivos lidos integralmente durante o F0-L1

| Arquivo | Estado |
|---|---|
| `.editorconfig` | lido integralmente |
| `.env.example` | lido integralmente |
| `.gitignore` | lido integralmente |
| `README.md` | lido integralmente e relido após fechamento |
| `backend/.dockerignore` | lido integralmente |
| `backend/Dockerfile` | lido integralmente |
| `backend/pom.xml` | lido integralmente |
| `backend/src/main/java/br/com/facilit/kanban/KanbanApplication.java` | lido integralmente |
| `backend/src/main/java/br/com/facilit/kanban/application/health/HealthQuery.java` | lido integralmente |
| `backend/src/main/java/br/com/facilit/kanban/application/health/HealthStatus.java` | lido integralmente |
| `backend/src/main/java/br/com/facilit/kanban/delivery/graphql/HealthGraphQlController.java` | lido integralmente |
| `backend/src/main/java/br/com/facilit/kanban/delivery/rest/HealthRestController.java` | lido integralmente |
| `backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java` | lido integralmente |
| `backend/src/main/resources/application.yml` | lido integralmente |
| `backend/src/main/resources/db/migration/.gitkeep` | lido integralmente |
| `backend/src/main/resources/graphql/health.graphqls` | lido integralmente |
| `backend/src/test/java/br/com/facilit/kanban/application/health/HealthQueryTest.java` | lido integralmente |
| `backend/src/test/java/br/com/facilit/kanban/delivery/graphql/HealthGraphQlControllerTest.java` | lido integralmente |
| `backend/src/test/java/br/com/facilit/kanban/delivery/rest/HealthRestControllerTest.java` | lido integralmente |
| `compose.yaml` | lido integralmente e relido após correções Docker/PostgreSQL |
| `docs/evidence/F0-L1/CONSUMIDORES.md` | lido integralmente |
| `docs/evidence/F0-L1/DECISOES.md` | lido integralmente e relido após fechamento |
| `docs/evidence/F0-L1/EXECUCAO.md` | lido integralmente e relido após fechamento |
| `docs/evidence/F0-L1/FONTES-RAG.md` | lido integralmente e relido após fechamento |
| `docs/evidence/F0-L1/GATE.md` | lido integralmente e relido após fechamento |
| `docs/evidence/F0-L1/MATRIZ.md` | lido integralmente e relido após fechamento |
| `docs/evidence/F0-L1/RISCOS.md` | lido integralmente e relido após fechamento |
| `docs/evidence/F0-L1/VERIFICACAO-USUARIO.md` | lido integralmente e relido após fechamento |
| `docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md` | lido integralmente |
| `docs/governance/PROMPT-EXECUTIVO-KANBAN-v1.0.md` | lido integralmente |
| `frontend/.dockerignore` | lido integralmente |
| `frontend/Dockerfile` | lido integralmente |
| `frontend/biome.json` | lido integralmente |
| `frontend/index.html` | lido integralmente |
| `frontend/package.json` | lido integralmente |
| `frontend/pnpm-workspace.yaml` | lido integralmente |
| `frontend/src/App.test.tsx` | lido integralmente |
| `frontend/src/App.tsx` | lido integralmente |
| `frontend/src/api/health.ts` | lido integralmente |
| `frontend/src/main.tsx` | lido integralmente |
| `frontend/src/test/setup.ts` | lido integralmente |
| `frontend/tsconfig.app.json` | lido integralmente |
| `frontend/tsconfig.json` | lido integralmente |
| `frontend/tsconfig.node.json` | lido integralmente |
| `frontend/vite.config.ts` | lido integralmente |
| `docs/evidence/F0-L1/LEITURA.md` | lido integralmente após esta consolidação |

## Arquivo amostrado

- `frontend/pnpm-lock.yaml` — amostrado para conferir cabeçalho e resoluções diretamente relacionadas às versões corrigidas. Não é declarado como lido integralmente. Sua validade operacional foi comprovada por `pnpm install`, lint, typecheck, testes/build e build Docker executados pelo usuário.

## Governança copiada

```text
1f39e6bff011a0cbb1b476058eaefb09733f17be8ef881f7b9aff230d243303c  docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md
78ddb682fc07551bd121e502c9aaf5f11c5d1bcec6fe0d40e35c675dddc9452f  docs/governance/PROMPT-EXECUTIVO-KANBAN-v1.0.md
```

## Artefatos excluídos da baseline

- `facilit-desafio-kanban-F0-L1-correcao-porta.tar.gz` — pacote incremental de transporte, não pertence ao repositório.
- `.github/modernize/java-upgrade/**` — arquivos locais de ferramenta de modernização observados como não versionados e sem relação com o escopo do desafio; não fazem parte da baseline F0-L1.
- `.git/**` — metadados internos não integram o livro-razão de código; estado, log, arquivos rastreados e remoto foram consultados por comandos Git.
