# F3-L3 — GATE

Data: 2026-09-23

Estado: **CANDIDATE rev2 / aguardando execução local** (rev1 RED: asserção do gate exigia corpo exato `{"status":"UP"}` no health).

Escopo: Observabilidade (`docs/governance/REPLANEJAMENTO-F3.md`, §2 e §6), antigo F3-L2 do plano inicial.

Critérios:

- `F3_L3_FRONTEND_GREEN`: Biome write, lint, typecheck, 30 testes e build (frontend sem alteração neste lote);
- `F3_L3_BACKEND_GREEN`: `mvn clean verify` com unitários e ITs verdes, incluindo `MetricsCredentialsTest` e `ObservabilityIT` (relatórios presentes);
- `F3_L3_COMPOSE_REQUIRED_SECRETS_GREEN`: controle positivo; sobreposição recusada sem `METRICS_PASSWORD` e sem `GRAFANA_ADMIN_PASSWORD`;
- `F3_L3_DOCKER_GREEN`: Compose base + observabilidade no ar; Prometheus e Grafana publicados só em `127.0.0.1`;
- `F3_L3_HEALTH_GREEN`: `/actuator/health` público com `status = UP`, apenas as chaves `status` e `groups` (nomes `liveness`/`readiness`), sem `components` nem `details`; liveness e readiness 200;
- `F3_L3_METRICS_SECURED_GREEN`: `/actuator/prometheus` 401 anônimo, com senha errada, com credencial do ADMIN (Basic) e com sessão do ADMIN; 200 com a credencial técnica; `env`, `heapdump`, `metrics` e raiz do Actuator indisponíveis; séries HTTP (histograma), JVM e HikariCP com a tag `application`;
- `F3_L3_PROMETHEUS_GREEN`: alvo `facilit-kanban-backend` `up`; contagem de requisições coletada; consultas p95, heap e Hikari válidas;
- `F3_L3_GRAFANA_GREEN`: API 401 anônima e com a senha padrão `admin`; datasource provisionado e saudável; painel `facilit-kanban` com 6 painéis;
- `F3_L3_STRUCTURED_LOGS_GREEN`: log do backend em JSON ECS (`@timestamp`, `log.level`, `ecs.version`), inclusive a linha de inicialização; nenhuma das senhas do gate aparece no log;
- `F3_L3_STRICT_TYPES_GREEN`, ausência de armazenamento de navegador, ausência de senha literal nos arquivos de Compose/observabilidade, `git diff --check` e `F3_L3_STATIC_GREEN`;
- marcador final `=== F3-L3 GREEN ===` e exit code 0.
