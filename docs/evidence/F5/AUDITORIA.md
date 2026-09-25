# F5 — AUDITORIA FINAL REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-24 · Release `2.0.0` (publicada em 2026-09-24; estado atualizado em 2026-09-25 na patch 2.0.1).

A auditoria consolida a aderência funcional promovida nos lotes F5-L1 a F5-L4 e nos lotes de correção F5-C1 a F5-C3. Os lotes C vieram de uma segunda auditoria independente sobre a release candidata (`ADERENCIA-FINAL.md`, 24/09). Freeze e release terminaram GREEN (`GATE.md`).

## Obrigatórios do desafio

| Requisito | Implementação | Teste/evidência | Estado funcional |
|---|---|---|---|
| CRUD Projeto e Responsável, e-mail único, auditoria | services REST/GraphQL + JPA/Flyway | F1-L1; F5-L3 controllers/persistência | GREEN |
| Status pelas datas e métricas | domínio + `ProjectScheduleRefresher` | F5-L1, `ScheduleFreshnessIT` | GREEN |
| 12 transições, bloqueios, confirmação e recálculo | `ProjectStatusTransition` + REST/GraphQL | F5-L2/L3; Cucumber F5-L4 com atraso e percentual conferidos linha a linha (F5-C2) | GREEN |
| Listagem Kanban por status | `ProjectService.listByStatus/search` | F5-L1/L3 | GREEN |
| REST obrigatório | controllers `/api/v1/*` | suites unitárias/API/integração | GREEN |
| Swagger/OpenAPI com exemplos e erros | springdoc + `ApiErrorDocumentation`; CSRF no Swagger UI (F5-C1); `maxLength`/`maxItems` (F5-C2) | `OpenApiContractIT`, F5-L2, F5-C1 | GREEN |
| Testes JUnit de services/controllers com mocks | unitários + `@WebMvcTest`/`@GraphQlTest` + Mockito | F5-L3 | GREEN |
| Erros padronizados e validação | ProblemDetail + códigos estáveis, Bean Validation, limites de tamanho (F5-C2) | F5-L2/L3, F5-C2 | GREEN |
| Paginação e índices | page contracts + migrations V1/V3 | integração e freeze | GREEN |
| Docker Compose app + banco | `compose.yaml`; build da imagem corrigido no F5-C1 (RED reproduzido: 59% de cobertura só com unitários) | F5-C1 e F5-C2 (`up --build` com banco novo); CI constrói a imagem; freeze final revalida | GREEN |
| README obrigatório | `README.md` | checagem estática do freeze | GREEN promovido |
| AI_USAGE.md obrigatório | `AI_USAGE.md` | checagem estática | GREEN promovido |
| Repositório público e histórico granular | Gitflow + Conventional Commits | F4 + commits F5; freeze/release revalidam | GREEN promovido |
| Segurança, validação e logs adequados | Spring Security, CSRF, bcrypt, ECS, logs de negócio | F3-L2/L3, F5-L2, freeze | GREEN promovido |

## Etapa 3 — Indicadores e resumos

| Requisito | Implementação | Evidência | Estado |
|---|---|---|---|
| Média de dias de atraso por status | `/api/v1/indicators/projects`, GraphQL | F3-L1/F5-L4 | GREEN |
| Quantidade por status | mesmo contrato | F3-L1/F5-L4 | GREEN |
| Novos endpoints/operações | por secretaria, responsável e prazo | `ProjectIndicatorsIT`, F5-L4 | GREEN |
| Sucesso e erros | REST/GraphQL + `withinDays` 1–90 | F5-L4 | GREEN |

## Diferenciais

| Diferencial | Implementação/evidência | Estado |
|---|---|---|
| GraphQL | schema/controllers sobre os mesmos casos de uso; testes controller/API | GREEN |
| UI Kanban drag-and-drop | React/MUI; Vitest/Testing Library | GREEN |
| CRUD de Secretaria | REST/GraphQL/UI + testes | GREEN |
| Filtros avançados | secretaria, período, responsável e texto | GREEN |
| Observabilidade | Actuator, Prometheus, Grafana, ECS | GREEN |
| CI/CD | GitHub Actions com frontend/backend/repository | GREEN |
| BDD e TDD | 12 cenários Cucumber + evidências RED→GREEN por lote | GREEN |
| Autenticação do responsável | perfil RESPONSIBLE e posse na aplicação | GREEN |
| Camada de IA / agentes | ADR 0001 com arquitetura RAG, contrato, contexto, falhas/timeouts e trade-offs | GREEN como proposta documental permitida pelo desafio |
| Cobertura automatizada | JaCoCo bloqueante ≥95%; última medição 95,61% | GREEN |

## Conformidade do plano F5

[VERIFICADO: `docs/governance/PLANO-CONFORMIDADE-F5.md` · 2026-09-24] Os itens de lacuna numerados pelo plano (#9, #13, #17, #18, #24, #27, #28, #29, #31, #32, #33, #37, #39, #40 e E3a–d/D8) têm implementação e teste nos L1–L4. O objetivo de aderência funcional previsto pelo plano é **40/40 obrigatórios e 13/13 diferenciais/Etapa 3**.

## Lacunas declaradas

- `[EXECUTADO PELO USUÁRIO · 2026-09-24]` freeze e release pública `v2.0.0` GREEN (`GATE.md`).
- Aderência medida na tag `v2.0.0` pela terceira auditoria: 97,5% dos obrigatórios e 96% dos diferenciais (`ADERENCIA-V2.0.0.md`). O único obrigatório corrigível (#40, exemplos no Swagger) foi fechado na patch 2.0.1 (F5-P1); o #9 (histórico) fica Parcial e declarado.
- Aderência medida pela segunda auditoria antes dos lotes C: 95% dos obrigatórios e 96% dos diferenciais (`ADERENCIA-FINAL.md`). Os lotes F5-C1 e F5-C2 fecharam as parciais de Docker (#2), métricas linha a linha (#29) e validação de tamanho (#32).
- Histórico de commits: F2-L1 a F3-L4 foram reconstruídos por lote depois dos gates (`docs/evidence/F3-L4/RECONSTRUCAO-HISTORICO.md`); declarado no README.
- GraphiQL exige o token CSRF à mão no painel Headers (roteiro no README); o Swagger UI envia automaticamente.
- A camada de IA permanece uma **proposta arquitetural**, não uma integração com LLM em produção; isso é expressamente aceito como opção da Etapa 5 do desafio.
- O frontend não possui tela administrativa de credenciais; o contrato existe em REST/GraphQL/Swagger e esse item não é requisito do desafio.
- O aviso de tamanho do bundle Vite permanece informativo; não é falha de build nem requisito funcional.
