# F5-Q1 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-25

| Item | Implementação | Teste/prova | Estado final |
|---|---|---|---|
| versão 2.0.2 | `backend/pom.xml`, `backend/Dockerfile`, `frontend/package.json` | gate confere versão; Maven/Docker executados | `[EXECUTADO]` GREEN |
| E2 | `ProjectService.update`; caso específico em `ProjectServiceTest` | `updateCanClearActualStartWithoutTransitionConfirmationAndRecalculatesStatus` | `[EXECUTADO]` GREEN |
| E3 | `ApiErrorDocumentation` contextualiza 400/403/404 por operação | `OpenApiContractIT.errorExamplesMatchTheOperationResourceInputAndAuthorizationRule` + `/api-docs` e 404 real | `[EXECUTADO]` GREEN após RED 1 corrigido |
| E4 | `ProjectStatusTransition.mismatchMessage` orienta datas ausentes sem `null` | `line02WithMissingPlannedDatesExplainsWhatMustBeProvidedWithoutNullText` + API real | `[EXECUTADO]` GREEN |
| E5 | linha 11 orienta `actualStart` ou `plannedEnd` conforme status recalculado | `line11WhenRecalculatedAsNotStartedAsksForActualStart` e cenário Atrasado | `[EXECUTADO]` GREEN |
| D1 | removida cadeia `list/listByStatus/findAll/findByStatus`; testes migrados para `search` | gate de escopo + suíte completa | `[EXECUTADO]` GREEN |
| regressão | contrato REST/GraphQL preservado | 177 unitários/BDD + 52 integração; 12 BDD; Docker/API | `[EXECUTADO]` GREEN |

Evidência decisiva: `SAIDA-GATE.txt`, `Resultado: exit code 0`; fechamento Gitflow em `GATE.md` e `FECHAMENTO.txt`.
