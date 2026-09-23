# F3-L3 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-23

| Requisito | Implementação | Teste/verificação | Estado |
|---|---|---|---|
| Actuator com exposição mínima | `pom.xml` (starter-actuator), `application.yml` `management.endpoints.web.exposure.include: health,prometheus`, discovery desligado | `ObservabilityIT.nonExposedActuatorEndpointsAreUnavailable`, gate `METRICS_SECURED` | GREEN |
| Health público sem detalhes + probes | `show-details`/`show-components: never`, `probes.enabled` | `ObservabilityIT.healthAndProbesArePublicWithoutDetails`, gate `HEALTH` | GREEN |
| Métricas só para a credencial técnica | `ActuatorSecurityConfiguration` (cadeia `@Order(1)`, Basic, stateless, `AuthenticationManager` próprio) | `ObservabilityIT.prometheusRequiresTheMetricsCredential`, gate `METRICS_SECURED` | GREEN |
| Fail closed e política da senha de métricas | `MetricsCredentials` (vazia → sem usuário; < 16 caracteres ou > 72 bytes → falha na subida; hash bcrypt) | `MetricsCredentialsTest` (5 casos) | GREEN |
| Métricas HTTP, JVM e pool com tag da aplicação | `management.metrics.tags.application`, histograma de `http.server.requests`, `micrometer-registry-prometheus` | `ObservabilityIT`, gate `METRICS_SECURED`/`PROMETHEUS` | GREEN |
| Prometheus coletando com autenticação | `compose.observability.yaml`, `observability/prometheus/prometheus.yml` (`basic_auth.password_file` via secret) | promtool + Prometheus 3.14.0 real com alvo simulado (neste ambiente), gate `PROMETHEUS` | GREEN |
| Grafana provisionado | datasource `prometheus`, provider de painéis, `facilit-kanban.json` (6 painéis) | consultas do painel executadas no Prometheus 3.14.0 (neste ambiente), gate `GRAFANA` | GREEN |
| Senhas obrigatórias, sem padrão, fora do versionamento | `${METRICS_PASSWORD:?}`, `${GRAFANA_ADMIN_PASSWORD:?}`, `.env.example` vazio | gate `COMPOSE_REQUIRED_SECRETS`, `STATIC` | GREEN |
| Portas de observabilidade só locais | `127.0.0.1:9090`, `127.0.0.1:3000` | gate `DOCKER` (`docker compose port`) | GREEN |
| Logs estruturados | `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`, banner desligado no Compose | gate `STRUCTURED_LOGS` | GREEN |
| Segredos fora dos logs | nenhuma senha registrada pelo código | gate `STRUCTURED_LOGS` | GREEN |
| Promoção documental do F3-L2 | `docs/evidence/F3-L2`, README, `REPLANEJAMENTO-F3.md` §2 | evidência do gate do usuário | GREEN |
