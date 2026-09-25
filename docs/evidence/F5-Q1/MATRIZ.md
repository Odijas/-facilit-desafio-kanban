# F5-Q1 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-25

| Item | Implementação | Teste/prova | Estado antes do gate |
|---|---|---|---|
| versão 2.0.2 | `backend/pom.xml`, `backend/Dockerfile`, `frontend/package.json` | gate verifica versão e JAR | `[VERIFICADO]` arquivos; `[DESCONHECIDO]` build JAR |
| E2 | comportamento existente de `ProjectService.update`; novo caso em `ProjectServiceTest` | `updateCanClearActualStartWithoutTransitionConfirmationAndRecalculatesStatus` + harness | `[EXECUTADO]` harness GREEN; Maven pendente |
| E3 | `ApiErrorDocumentation` seleciona campo/query/403/404 por operação | `OpenApiContractIT.errorExamplesMatchTheOperationResourceInputAndAuthorizationRule` | `[EXECUTADO]` RED 1 expôs suposição do helper sobre `examples`; helper e gate corrigidos para `example`/`examples`; reexecução Maven pendente |
| E4 | `ProjectStatusTransition.mismatchMessage` não concatena datas ausentes na linha 2 e orienta o campo | teste linha 2 sem datas + harness | `[EXECUTADO]` harness GREEN; Maven pendente |
| E5 | linha 11 orienta `actualStart` quando recalcula A iniciar e `plannedEnd` quando Atrasado | dois cenários em `ProjectStatusTransitionTest` + harness | `[EXECUTADO]` harness GREEN; Maven pendente |
| D1 | removida cadeia `list/listByStatus/findAll/findByStatus`; testes migrados para `search` | busca de consumidores + testes existentes migrados | `[EXECUTADO]` busca sem ocorrências antigas; Maven pendente |
| contrato REST/GraphQL | nenhum endpoint/schema alterado; produção já chama `search` | controllers mapeados + gate de regressão | `[VERIFICADO]` fonte; runtime pendente |

`[HIPÓTESE]` Pela baseline 174/51 e pelos 3 testes unitários + 1 integração adicionados, espera-se 177 unitários/BDD e 52 de integração. Somente `mvn clean verify` confirma e o gate exige exatamente esses totais.
