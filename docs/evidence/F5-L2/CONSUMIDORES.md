# F5-L2 — CONSUMIDORES

Data: 2026-09-24

- **`ProjectStatusTransition.transition`:** ganhou o parâmetro `confirmed`. Único chamador em produção: `ProjectService.transition`. Testes: `ProjectStatusTransitionTest`.
- **`ProjectService.transition(id, status, confirmed, actor)`:** chamadores:
  - `ProjectRestController` (`PATCH /api/v1/projects/{id}/status`, campo `confirm`);
  - `ProjectGraphQlController` (mutation `transitionProject`, argumento `confirm`);
  - testes `ProjectServiceTest` e `ProjectScheduleRefresherTest`.
  - `[VERIFICADO: grep "\.transition("]`
- **Exceções de regra (antes `IllegalArgumentException`, 400; agora `BusinessRuleException` e subclasses, 422):**
  - `ProjectDates` (ordem das datas e data realizada futura), `ProjectScheduleCalculator` (término previsto obrigatório) e `ProjectStatusTransition`.
  - Consumidores:
    - `RestExceptionHandler` e `GraphQlErrorHandler` (novos tratadores);
    - `ProjectScheduleRefresher`, que captura `RuntimeException`, então continua igual;
    - frontend, que mostra `detail` e agora lê `code`;
    - coleção Postman.
- **Mensagens:** os testes que comparavam texto foram atualizados:
  - `ProjectServiceTest`, `ResponsibleServiceTest`, `ResponsibleCredentialServiceTest`, `SecretariatServiceTest`;
  - `ProjectDatesTest`, `ProjectScheduleCalculatorTest`, `ResponsibleTest`, `ProjectStatusTransitionTest`, `ProjectScheduleRefresherTest`;
  - `RestExceptionHandlerTest`, `SecurityApiIT`.
  - O frontend não compara mensagens do backend: `LoginPage` usa o status 401.
- **Códigos:**
  - `ApiErrorCode` ganhou `BUSINESS_RULE_VIOLATION`, `TRANSITION_BLOCKED` e `CONFIRMATION_REQUIRED`.
  - Os códigos existentes não mudam de significado. `INVALID_REQUEST` continua para entrada malformada: senha curta de credencial (`SecurityApiIT`) e data GraphQL inválida (`KanbanApiIT`).
- **Esquema GraphQL:** `transitionProject(id, status, confirm: Boolean = false)`. As chamadas existentes sem `confirm` continuam válidas.
- **`application.yml`:** `spring.web.locale*` afeta só a interpolação de mensagens do Bean Validation; `springdoc.override-with-generic-response` afeta só o `/api-docs`.
- **`ApiErrorDocumentation`:** bean `OpenApiCustomizer` lido pelo springdoc. Não altera respostas reais da API.
- **Frontend:**
  - `KanbanApiError` ganhou `code` (opcional, padrão `null`);
  - `transitionProject` ganhou `confirm` (padrão `false`, e o corpo sem `confirm` continua igual);
  - `KanbanBoard` passa a usar `ConfirmTransitionDialog`.
  - Os outros consumidores de `KanbanApiError`, os diálogos de projeto e responsável, só usam `message`.
- **Log de negócio:** logger novo (`br.com.facilit.kanban.business`). O gate de observabilidade do F3-L3 checa que nenhum segredo aparece no log, e estes eventos não carregam senha nem e-mail.
