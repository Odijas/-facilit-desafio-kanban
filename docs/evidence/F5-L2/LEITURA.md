# F5-L2 — LIVRO-RAZÃO DE LEITURA

Data: 2026-09-24

`[VERIFICADO: docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md]` leitura integral é arquivo lido do início ao fim. Arquivo editado por automação é relido.

## Lidos integralmente neste lote

- **Backend:**
  - `delivery/rest/RestExceptionHandler.java`
  - `delivery/common/ApiErrorCode.java`
  - `delivery/graphql/GraphQlErrorHandler.java`
  - `delivery/rest/ProjectStatusRequest.java`
  - `application/common/{Actor, ConflictException, ForbiddenOperationException, PageQuery, PageResult, ResourceNotFoundException}.java`
  - `application/responsible/ResponsibleService.java`
  - `application/secretariat/SecretariatService.java`
  - `domain/project/ProjectStatus.java`
  - `resources/graphql/kanban.graphqls`
  - `infrastructure/config/OpenApiConfiguration.java`
- **Testes:**
  - `delivery/rest/RestExceptionHandlerTest.java`
  - `domain/project/ProjectStatusTransitionTest.java`, reescrito inteiro neste lote
- **Frontend:**
  - `src/api/kanban.ts`
  - `src/features/kanban/KanbanBoard.tsx`
  - `src/features/kanban/DeleteProjectDialog.tsx`

Os arquivos novos foram escritos inteiros:

- `BusinessRuleException`, `TransitionBlockedException`, `ConfirmationRequiredException`
- `BusinessLog`, `ApiErrorDocumentation`, `ProjectStatusTransition` (reescrito)
- `CapturedBusinessLog`, `OpenApiContractIT`, `ConfirmTransitionDialog.tsx`
- evidências do lote

## Lidos em parte (trechos usados)

- Todos os pontos de `throw new` em `domain` e `application`, e as mensagens em `infrastructure/security`: levantamento por grep, com leitura do trecho de cada um.
- `ResponsibleCredentialService.java`: linhas 15–80.
- `ProjectRestController.java`: exemplos e transição.
- `ResponsibleRestController.java`: exemplo de 409.
- `ProjectGraphQlController.java`: mutation de transição.
- `SecurityConfiguration.java`: entry point e access denied.
- `AuthenticatedActorResolver.java`
- `ProjectServiceTest.java` e demais testes com asserção de mensagem: trechos alterados.
- `KanbanBoard.test.tsx`: preparação, drag-and-drop e bloqueio.
- `kanban.test.ts`: início e teste de transição.
- Coleção Postman: pasta 04.
- `README.md`: seções alteradas.

## Conferidos depois da edição automatizada

- **Por diff:** as substituições de mensagem, `l2msgs.py`, com 41 trocas, cada uma confirmada 1×. O `ProjectStatusTransition` foi reescrito e conferido pelo harness de mensagens.
- **Pelas ferramentas:** frontend reformatado pelo Biome e verificado por lint, typecheck e testes.
