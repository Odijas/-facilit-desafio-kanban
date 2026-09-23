# F3-L3 — GATE

Data: 2026-09-23

Estado: **GREEN** (rev2, gate local em 2026-09-23). Histórico: rev1 RED (asserção do gate exigia corpo exato `{"status":"UP"}` no health).

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

Evidência final recebida do usuário:

```text
[EXECUTADO PELO USUÁRIO · 2026-09-23] gate F3-L3 rev2 (trecho recebido, a partir da subida do Compose):
  Image facilit-kanban-backend Built · Image facilit-kanban-frontend Built
  Container facilit-kanban-db-1 Healthy · backend-1, prometheus-1, frontend-1, grafana-1 Started
  {"status":"UP","groups":["liveness","readiness"]}
  F3_L3_DOCKER_GREEN
  F3_L3_HEALTH_GREEN
  F3_L3_METRICS_SECURED_GREEN
  F3_L3_PROMETHEUS_GREEN
  F3_L3_GRAFANA_GREEN
  ECS_RECORDS 37
  F3_L3_STRUCTURED_LOGS_GREEN
  F3_L3_STRICT_TYPES_GREEN 31
  docker compose ps: grafana 127.0.0.1:3000->3000/tcp; prometheus 127.0.0.1:9090->9090/tcp; db healthy
  F3_L3_STATIC_GREEN
  === F3-L3 GREEN ===
  Resultado: exit code 0
[VERIFICADO: script com `set -euo pipefail`] F3_L3_FRONTEND_GREEN, F3_L3_BACKEND_GREEN (com relatórios de ObservabilityIT e MetricsCredentialsTest) e F3_L3_COMPOSE_REQUIRED_SECRETS_GREEN precedem a subida do Compose; o exit code 0 final só ocorre se todas as etapas terminarem com sucesso. Esses três marcadores não constam do trecho recebido.
```

Conclusão: F3-L3 promovido para GREEN em 2026-09-23.
