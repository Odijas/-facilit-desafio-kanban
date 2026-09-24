# F5-L1 — LIVRO-RAZÃO DE LEITURA

Data: 2026-09-24

`[VERIFICADO: docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md]` leitura integral é arquivo lido do início ao fim. Diff, busca ou contagem não substituem a leitura integral. Arquivo editado por automação é relido.

## Lidos integralmente neste lote

- **Documento do desafio:** PDF, 5 páginas, texto extraído com 206 linhas.
- **Domínio:**
  - `domain/project/Project.java`
  - `ProjectScheduleMetrics.java`
  - `ProjectDates.java`
  - `ProjectStatusTransition.java`
  - `domain/common/AuditMetadata.java`
- **Aplicação:**
  - `application/project/ProjectService.java`
  - `ProjectRepository.java`
- **Persistência:**
  - `infrastructure/persistence/project/ProjectPersistenceAdapter.java`
  - `ProjectJpaEntity.java`
  - `ProjectJpaRepository.java`
- **Configuração:**
  - `infrastructure/config/ApplicationBeans.java`
  - `backend/src/main/resources/application.yml`
  - `compose.yaml`
  - `.env.example`
- **Migração:** `V1__create_kanban_core.sql`
- **Testes:**
  - `application/support/InMemoryProjectRepository.java`
  - `domain/project/ProjectStatusTransitionTest.java`
  - `integration/KanbanApiIT.java`

Arquivos alterados neste lote, relidos depois da edição:

- `ProjectService.java`
- `ProjectRepository.java`
- `ProjectJpaRepository.java`
- `ProjectJpaEntity.java`
- `ProjectPersistenceAdapter.java`: releitura integral das partes alteradas e do cabeçalho; o restante está sem alteração e foi conferido por diff.
- `ProjectDates.java`
- `ProjectStatusTransition.java`
- `ApplicationBeans.java`
- `compose.yaml`
- `.env.example`
- `InMemoryProjectRepository.java`
- `ProjectStatusTransitionTest.java`
- `ResponsibleServiceTest.java`
- `ProjectServiceTest.java`: trecho alterado e métodos vizinhos.
- `KanbanApiIT.java` e `SecurityApiIT.java`: diff conferido; a edição troca só a origem de "hoje".

Os arquivos novos foram escritos inteiros neste lote.

## Lidos em parte (trechos usados)

- `ProjectScheduleCalculator.java`: regras de status, atraso e %.
- `ProjectScheduleCalculatorTest.java`: nomes dos testes.
- `ProjectServiceTest.java`
- `ResponsibleServiceTest.java`: montagem com `projectRepository.save`.
- `SecurityApiIT.java`: data de hoje.
- `RestExceptionHandler.java`: mapeamento de `IllegalArgumentException`.
- `ProjectRestController.java`: exemplos do Swagger.
- `ProjectRequest.java`
- Migrations V2 a V5: colunas de `responsibles` e `projects`.
- `README.md`: seções alteradas.
- `docs/adr/0001-camada-ia-agente-rag.md`: formato.
- `docs/evidence/F3-L3/*`: formato.
- `docs/evidence/F4/VERIFICACAO-USUARIO.md`: estrutura do gate, reaproveitada.
- `docs/api/facilit-kanban.postman_collection.json`: variável `today`.

## Não relidos neste lote

`PROMPT-EXECUTIVO-BASE-v1.1.md`, `PROMPT-EXECUTIVO-KANBAN-v1.0.md` e `REPLANEJAMENTO-F3.md` foram lidos nos lotes anteriores e não mudaram desde a v1.0.0. `PLANO-CONFORMIDADE-F5.md` foi escrito na sessão de 2026-09-23 e está copiado sem alteração.
