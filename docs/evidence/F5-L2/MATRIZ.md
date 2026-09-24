# F5-L2 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-24

Itens da análise de aderência (`docs/governance/PLANO-CONFORMIDADE-F5.md`, §4) fechados por este lote:

| # | Requisito (PDF) | Implementação | Teste | Evidência | Estado |
|---|---|---|---|---|---|
| 28 | Mensagens de erro claras, com dicas (Etapa 2 e Nota da tabela) | mensagens em pt-BR com orientação específica em cada linha bloqueável (`ProjectStatusTransition`) | `ProjectStatusTransitionTest` (24, mensagens exatas) | gate `F5_L2_API_GREEN` | GREEN |
| 29 | Testes linha a linha com **confirmações obrigatórias** | `ConfirmationRequiredException`; `confirm` em REST, GraphQL e UI | `ProjectStatusTransitionTest.line04*`, `line11*`, `line12*`; `ProjectServiceTest.clearsRecordedActualStartOnlyWithExplicitConfirmation`; `KanbanBoard.test.tsx` (2 novos) | gate `F5_L2_API_GREEN` (REST e GraphQL) | GREEN no escopo do F5-L2 (a tabela inteira pela API em IT fica no F5-L3) |
| 31 | Exceções: tratamento e padronização | `ApiErrorCode` + tratadores REST e GraphQL (422 por tipo de regra; 409 para integridade e concorrência) | `RestExceptionHandlerTest` (7) | gate `F5_L2_API_GREEN` | GREEN |
| 33 | Logs adequados | `BusinessLog` (eventos de negócio sem dado pessoal) | `ProjectServiceTest.recordsBusinessEventsWithIdentifiersOnlyAndNoPersonalData` | gate `F5_L2_BUSINESS_LOGS_GREEN` (log ECS do contêiner) | GREEN |
| 40 | Swagger com exemplos, schemas e **mensagens de erro** | `ApiErrorDocumentation` (todas as operações) | `OpenApiContractIT` (3) | gate `F5_L2_OPENAPI_GREEN` | GREEN |
| — | Validação de entrada com mensagens claras | Bean Validation em pt-BR (`spring.web.locale`) | — | gate `F5_L2_API_GREEN` (violations sem texto em inglês) | GREEN |
