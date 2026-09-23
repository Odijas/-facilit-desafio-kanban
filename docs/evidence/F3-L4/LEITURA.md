# F3-L4 — LIVRO-RAZÃO DE LEITURA

Data: 2026-09-23

## Integral

- `docs/governance/REPLANEJAMENTO-F3.md`
- `README.md` (versão do F3-L3), `frontend/package.json`, `frontend/biome.json`, `frontend/tsconfig.node.json`, `frontend/pnpm-workspace.yaml`, `frontend/Dockerfile`, `frontend/.dockerignore`, `frontend/vite.config.ts`
- `AuthRestController.java`, `SpaCsrfTokenRequestHandler.java`, `RestExceptionHandler.java` (linhas 28–110), `ApiErrorCode.java`
- `ProjectRequest.java`, `ResponsibleRequest.java`, `SecretariatRequest.java`, `ProjectStatusRequest.java`, `ProjectIndicatorsResponse.java`
- `kanban.graphqls` (integral), `V1__create_kanban_core.sql` (tabelas e FKs)

## Amostrado

- `docs/governance/PROMPT-EXECUTIVO-KANBAN-v1.0.md` [AMOSTRAGEM: linhas 1–130 e 295–467 — objetivo, stack, F3, F4, pacote, prioridade]
- `docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md` [AMOSTRAGEM: linhas 1–120 — cláusulas 0 a 3]
- `ProjectRestController.java` [AMOSTRAGEM: linhas 108–170 e mapeamentos]
- `ResponsibleService.java` [AMOSTRAGEM: linhas 95–130 e assinaturas públicas], `SecretariatService.java` e `ProjectService.java` [AMOSTRAGEM: assinaturas públicas e uso de `Clock`]
- `ProjectStatusTransition.java` [AMOSTRAGEM: exceções lançadas]
- `HealthRestController.java`, `HealthStatus.java` [AMOSTRAGEM: contrato]
- `docs/evidence/*` [AMOSTRAGEM: busca por menções à IA e a erros próprios]
- árvores `backend/src/main/java` e `frontend/src` [AMOSTRAGEM: diretórios]

## Fontes externas

Ver `FONTES-RAG.md`.

## Reler após edição por script

`README.md`, `package.json`, `REPLANEJAMENTO-F3.md`, as evidências do F3-L3 e a coleção gerada por script foram revisados por diff ou execução (newman) antes do empacotamento.
