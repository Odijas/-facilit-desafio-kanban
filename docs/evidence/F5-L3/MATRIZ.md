# F5-L3 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-24

Itens da análise de aderência (`docs/governance/PLANO-CONFORMIDADE-F5.md`, §4) fechados por este lote:

| # | Requisito (PDF) | Implementação | Teste | Evidência | Estado |
|---|---|---|---|---|---|
| 24 | Testes unitários (JUnit) para services **e controllers** | controllers REST e GraphQL testados isolados, com o service simulado (`@MockitoBean`); service com Mockito | `ProjectRestControllerTest` (17), `ResponsibleRestControllerTest` (9), `SecretariatRestControllerTest` (6), `ProjectIndicatorsRestControllerTest` (2), `ProjectGraphQlControllerTest` (5), `ResponsibleGraphQlControllerTest` (3), `SecretariatGraphQlControllerTest` (2), `ProjectServiceMockitoTest` (5) | gate `F5_L3_BACKEND_GREEN` | GREEN |
| 29 | Testes de cada linha da tabela, **incluindo confirmações obrigatórias** | tabela no domínio (F5-L2) exercida pela API real | `ProjectStatusTransitionTest` (24, domínio); `StatusTransitionApiIT` (12 linhas pela REST + bloqueio da linha 12, 3 pela GraphQL) | gates `F5_L3_BACKEND_GREEN` e `F5_L3_API_GREEN` | GREEN |
| 37 | Testes de integração: **repositórios e transações** | `ProjectPersistenceAdapter` sobre PostgreSQL real; `TransactionRunner` por caso de uso | `ProjectPersistenceAdapterIT` (5); `TransactionIT` (4: rollback, recálculo desfeito, edição concorrente, versão) | gates `F5_L3_BACKEND_GREEN` e `F5_L3_API_GREEN` (versão e recusas sem gravação no contêiner) | GREEN |
| — | Transação e concorrência (D5 do plano) | `TransactionRunner`/`SpringTransactionRunner`; V7 + `@Version`; 409 `CONFLICT` na edição concorrente | `TransactionIT`, `ProjectServiceMockitoTest`, `ProjectRestControllerTest.translatesConcurrentUpdateToConflict` | gate `F5_L3_DOCKER_GREEN` (V7) e `F5_L3_API_GREEN` | GREEN |
| — | Build sem aviso de depreciação | Mockito como agente Java (Surefire e Failsafe); `@MockitoBean` em vez de `@MockBean` | checagem do log do Maven | gates `F5_L3_BACKEND_GREEN` e `F5_L3_STATIC_GREEN` | GREEN |
