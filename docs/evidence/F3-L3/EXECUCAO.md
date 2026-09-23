# F3-L3 — EXECUÇÃO

Data: 2026-09-23

## Base

```text
[EXECUTADO PELO USUÁRIO · 2026-09-23] F3-L2 GREEN (exit code 0).
[VERIFICADO] base do F3-L3 = conteúdo do F3-L2 GREEN + promoção documental do F3-L2.
```

## Executado neste ambiente

```text
[EXECUTADO · 2026-09-23] Docker Hub (API de tags): prom/prometheus:v3.14.0 active (2026-08-18); grafana/grafana:13.1.3 active (2026-08-07); ambas com amd64 e arm64.
[EXECUTADO · 2026-09-23] binário oficial Prometheus 3.14.0 (GitHub releases):
  promtool check config observability/prometheus/prometheus.yml → SUCCESS
  servidor Prometheus real + alvo HTTP simulado exigindo Basic (senha lida de password_file com modo 0444) → alvo facilit-kanban-backend "up"
  9 consultas do painel Grafana contra o Prometheus → status success, todas com série retornada
[EXECUTADO · 2026-09-23] YAML (PyYAML): compose.yaml, compose.observability.yaml, prometheus.yml, provisioning de datasource e de painéis, application.yml → válidos; painel JSON → 6 painéis.
[EXECUTADO · 2026-09-23] javac (JDK 21) sem dependências sobre todo backend/src: nenhum erro envolvendo símbolo do próprio projeto; os únicos erros fora de "pacote/símbolo ausente" são 6 preexistentes (@Override e referência de método cujo supertipo vem do Spring ausente), nenhum nos arquivos novos.
[EXECUTADO · 2026-09-23] APIs usadas conferidas no código-fonte das tags exatas (Spring Boot v3.5.16, Spring Security 6.5.11): nenhuma depreciada (o build usa -Xlint:all -Werror).
[EXECUTADO · 2026-09-23] frontend (Node 22.22.2, pnpm 12.5.1), sem alteração de código neste lote:
  pnpm format: Checked 38 files. No fixes applied.
  pnpm lint: No fixes applied · exit 0
  pnpm typecheck: exit 0
  pnpm test: Test Files 6 passed (6); Tests 30 passed (30)
  pnpm build: built · exit 0
[EXECUTADO · 2026-09-23] trecho de validação ECS do gate contra log de amostra (linha de aviso da JVM + linha ECS de inicialização) → exit 0, ECS_RECORDS 1.
[EXECUTADO · 2026-09-23] regex de senha literal do gate: 0 ocorrências nos arquivos do projeto; 1 ocorrência em arquivo de controle com senha literal.
[EXECUTADO · 2026-09-23] gate extraído por awk e `bash -n`: GATE_F3L3_BASH_SYNTAX_GREEN; 10 marcadores F3_L3_*_GREEN emitidos por echo + STRICT_TYPES pelo node.
[EXECUTADO · 2026-09-23] `git diff --no-index --check` contra a árvore do F3-L2: sem diagnóstico.
```

## Correções próprias antes do empacotamento

```text
[VERIFICADO] rascunho do gate usava `! grep` para segredos no log e armazenamento de navegador; sob `set -e` isso não bloqueia. Trocado por `if grep …; then exit 1; fi`.
[VERIFICADO] rascunho do gate consultava o Prometheus fora de condição; uma falha de conexão antes da subida encerraria o gate. Consultas movidas para dentro do `if` do laço de espera.
[VERIFICADO] rascunho da regex de senha literal acusava `${VAR:?…}` e `${VAR:-}`; classe de caracteres ajustada e testada com controle positivo.
[VERIFICADO] verificação negativa do Compose passou a rodar em diretório temporário sem `.env`, com controle positivo, para não depender do `.env` local.
```

## rev1 — RED no gate do usuário (2026-09-23)

```text
[EXECUTADO PELO USUÁRIO · 2026-09-23] rev1: F3_L3_FRONTEND_GREEN, F3_L3_BACKEND_GREEN (inclui ObservabilityIT e MetricsCredentialsTest), F3_L3_COMPOSE_REQUIRED_SECRETS_GREEN e F3_L3_DOCKER_GREEN; falha em HEALTH:
  {"status":"UP","groups":["liveness","readiness"]}
  AssertionError: {'status': 'UP', 'groups': ['liveness', 'readiness']}
  Resultado: exit code 1
[VERIFICADO: Spring Boot v3.5.16, HealthEndpointSupport.java:109 e 198–199; SystemHealth.getGroups com @JsonInclude(NON_EMPTY)] com grupos de health configurados (probes), a raiz do health lista os nomes dos grupos, independentemente de show-details/show-components.
[ERRO PRÓPRIO] a asserção exigia o corpo exato sem verificar no código-fonte como o Boot monta a resposta.
[CORREÇÃO rev2] somente o gate: status UP; chaves ⊆ {status, groups}; sem components/details; grupos ⊆ {liveness, readiness}. Código de produção e testes sem alteração.
[EXECUTADO · 2026-09-23] nova asserção contra o corpo real do rev1 → aceita; contra corpo com "components" → rejeita; gate rev2 `bash -n` → GREEN.
```

## Não executado neste ambiente

```text
[DESCONHECIDO] mvn clean verify (MetricsCredentialsTest, ObservabilityIT e regressão), Docker Compose com Actuator/Prometheus/Grafana reais e logs ECS reais. Resolve: gate local.
[DESCONHECIDO] frontend em Node 24. Resolve: gate local.
```
