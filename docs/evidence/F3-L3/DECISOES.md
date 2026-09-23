# F3-L3 — DECISÕES

Data: 2026-09-23

- `[VERIFICADO: REPLANEJAMENTO-F3.md §2]` F3-L3 = Observabilidade (antigo F3-L2); sem mudança de regra de negócio, de contrato REST/GraphQL ou de frontend.
- `[VERIFICADO: Spring Boot 3.5.16]` Actuator expõe só `health` e `prometheus` (o padrão expõe apenas `health`); descoberta (`/actuator`) desligada. `health` sem componentes nem detalhes, com grupos `liveness`/`readiness` para orquestradores.
- `[VERIFICADO: código-fonte Spring Boot 3.5.16, EndpointRequest]` cadeia de segurança própria com `securityMatcher(EndpointRequest.toAnyEndpoint())` e `@Order(1)`; `EndpointRequest.to("health")` cobre `/actuator/health/**`. A cadeia principal (sem `securityMatcher`) continua sendo a última.
- `[VERIFICADO]` `/actuator/prometheus` exige HTTP Basic com o papel `METRICS`, num `AuthenticationManager` próprio (`InMemoryUserDetailsManager` fora do contexto como bean, para não concorrer com o `DatabaseUserDetailsService`). A cadeia é stateless (`RequestAttributeSecurityContextRepository`), então sessão de ADMIN ou responsável não lê métricas, e credencial de ADMIN via Basic também não. Motivo: métricas revelam rotas, volumes e erros (OWASP Top 10 2025, A01 Broken Access Control e A02 Security Misconfiguration: negar por padrão, menor privilégio, sem expor diagnóstico).
- `[VERIFICADO]` CSRF desligado só nessa cadeia: requisições somente leitura, sem cookie e sem sessão.
- `[VERIFICADO]` fail closed: sem `APP_METRICS_PASSWORD`, nenhum usuário técnico existe e o endpoint responde 401; senha < 16 caracteres ou > 72 bytes (limite do bcrypt) impede a subida.
- `[VERIFICADO]` sem porta de gerenciamento separada: evita conflito de portas entre contextos de teste e mantém uma única superfície, protegida pela cadeia dedicada.
- `[VERIFICADO: Spring Boot 3.5.16, ElasticCommonSchemaStructuredLogFormatter]` logs JSON ECS ativados por variável de ambiente no Compose (`LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`) e banner desligado; a execução local via Maven mantém o log legível.
- `[VERIFICADO]` observabilidade em arquivo separado (`compose.observability.yaml`): o `docker compose up` base continua exigindo só as variáveis anteriores; as senhas de métricas e do Grafana são obrigatórias apenas quando a sobreposição é usada.
- `[VERIFICADO: docker/compose pkg/compose/secrets.go]` a senha de métricas chega ao Prometheus como secret do Compose com origem em variável de ambiente (arquivo `0444` em `/run/secrets`), porque o `prometheus.yml` não expande variáveis; o Grafana recebe a senha por variável obrigatória, no mesmo padrão já usado para o ADMIN.
- `[VERIFICADO: Docker Hub]` imagens fixadas: `prom/prometheus:v3.14.0` (tag ativa, 2026-08-18) e `grafana/grafana:13.1.3` (tag ativa, 2026-08-07); portas publicadas só em `127.0.0.1`; Grafana sem cadastro, sem anônimo e sem chamadas de atualização/telemetria.
- `[VERIFICADO]` painel com 6 visões: taxa HTTP, p95 por rota, 5xx, 4xx (401/403/404/409, útil para tentativa de acesso indevido), heap da JVM e pool HikariCP.
- `[VERIFICADO]` fora do lote, por custo/risco perto do freeze: tracing distribuído e métricas de negócio (projetos por status já existem nos indicadores do F3-L1).
- `[VERIFICADO]` dependências novas no backend, gerenciadas pelo parent do Boot: `spring-boot-starter-actuator` e `micrometer-registry-prometheus` (runtime). Frontend sem alteração.
- `[VERIFICADO]` gate: verificações negativas com `if … then exit 1` em vez de `! comando`, porque o `set -e` do bash ignora falha de comando invertido por `!`.
