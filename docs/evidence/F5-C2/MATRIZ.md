# F5-C2 — MATRIZ LACUNA → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-24

| Lacuna (`ADERENCIA-FINAL.md`) | Requisito (PDF) | Implementação | Teste | Evidência | Estado |
|---|---|---|---|---|---|
| 4 — métricas por linha | Etapa 2: testes com "cálculo de status/métricas — linha a linha" (obrigatório #29) | colunas `atraso` e `percentual` e o passo de métricas no BDD | `transicoes.feature` (12 cenários × 4 passos) | `F5_C2_BACKEND_GREEN` (relatório Cucumber com o passo de métricas nos 12) | GREEN |
| 5 — tamanho das entradas | "Segurança: validação de inputs" (obrigatório #32) | `@Size` com `InputLimits` (REST e GraphQL); texto no `ProjectFilter` | `ProjectRestControllerTest` (+3), `ResponsibleRestControllerTest` (+1), `SecretariatRestControllerTest` (+1), `ProjectGraphQlControllerTest` (+1), `ProjectFilterTest` (3) | `F5_C2_API_GREEN` (limites e fronteiras na API real) e `/api-docs` com `maxLength`/`maxItems` | GREEN |
