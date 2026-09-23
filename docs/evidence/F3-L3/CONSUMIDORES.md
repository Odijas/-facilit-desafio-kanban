# F3-L3 — CONSUMIDORES

Data: 2026-09-23

- Nova `SecurityFilterChain` (`ActuatorSecurityConfiguration`, `@Order(1)`): consumidores são as requisições casadas por `EndpointRequest.toAnyEndpoint()` (endpoints expostos). A cadeia existente (`SecurityConfiguration.securityFilterChain`) não foi alterada e segue atendendo o restante, inclusive `/actuator/**` não exposto (401/403).
- `PasswordEncoder` (bean existente): passa a ser injetado também na cadeia do Actuator; nenhuma alteração de contrato.
- `AuthenticationManager` (bean existente): não é usado pela cadeia nova, que tem gerenciador próprio.
- Contextos de teste existentes: `SecurityApiIT` e `KanbanApiIT` (sem `app.metrics.password`, endpoint de métricas fechado; nenhuma asserção sobre `/actuator`); `HealthRestControllerTest` (`@WebMvcTest`) e `HealthGraphQlControllerTest` (`@GraphQlTest`) não carregam classes `@Configuration` de segurança por varredura, como já ocorria com `SecurityConfiguration`.
- `compose.yaml`: backend ganha `APP_METRICS_PASSWORD` (opcional), `LOGGING_STRUCTURED_FORMAT_CONSOLE` e `SPRING_MAIN_BANNER_MODE`; `db` e `frontend` sem alteração. Gates anteriores continuam válidos com o arquivo base.
- `.env.example`: dois campos novos vazios (`METRICS_PASSWORD`, `GRAFANA_ADMIN_PASSWORD`).
- `vite.config.ts`: sem alteração; o proxy continua só em `/api`, então o Actuator não é exposto pela origem do frontend.
- springdoc: `springdoc.show-actuator` não configurado (padrão falso); o contrato OpenAPI não muda.
