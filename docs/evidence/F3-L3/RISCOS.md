# F3-L3 — RISCOS E LACUNAS

Data: 2026-09-23

- `[DESCONHECIDO]` compilação Spring, `MetricsCredentialsTest`, `ObservabilityIT` e demais testes não executados neste ambiente (Maven Central 403, sem JDK 25). Resolve: gate local (`F3_L3_BACKEND_GREEN`).
- `[DESCONHECIDO]` runtime Docker (backend com Actuator, Prometheus e Grafana reais) não executado neste ambiente (Docker Hub bloqueado). Resolve: gate local.
- `[HIPÓTESE]` a versão local do Docker Compose suporta secrets com origem `environment` (confirmado no código atual do Compose, não na versão instalada do usuário). O gate falha em `F3_L3_COMPOSE_REQUIRED_SECRETS_GREEN` ou `F3_L3_PROMETHEUS_GREEN` se não suportar.
- `[HIPÓTESE]` Grafana 13.1.3 carrega o painel clássico (`schemaVersion` 41) pelo provider de arquivos e responde nas rotas legadas `/api/dashboards/uid/:uid` e `/api/datasources/uid/:uid/health` (rotas confirmadas no código v13.1.3; formato `status: "OK"` da saúde do datasource não confirmado nesta versão). Resolve: gate `F3_L3_GRAFANA_GREEN`.
- `[VERIFICADO]` Prometheus e Grafana ficam em `127.0.0.1`; a UI do Prometheus não tem autenticação própria e por isso não é publicada em outras interfaces. As portas 9090 e 3000 precisam estar livres para o gate.
- `[VERIFICADO]` a senha do Grafana vale na criação do contêiner; trocar `GRAFANA_ADMIN_PASSWORD` exige recriar o serviço (documentado no README).
- `[VERIFICADO]` cada coleta faz uma verificação bcrypt (Basic), a cada 15 s; custo aceitável para o ambiente local.
- `[VERIFICADO]` com `METRICS_PASSWORD` preenchido e curto no `.env`, o backend não sobe nem no Compose base (fail-fast intencional).
- `[VERIFICADO]` achado próprio: nos gates anteriores, a verificação `! grep -RInE 'localStorage|sessionStorage' frontend/src` não bloquearia o gate (o `set -e` ignora comando invertido por `!`). As execuções anteriores não tinham ocorrência, então o resultado não muda; o gate do F3-L3 usa `if grep …; then exit 1; fi`.
- `[VERIFICADO]` fora do escopo: tracing distribuído, alertas do Prometheus e métricas de negócio.
- `[VERIFICADO]` aviso não bloqueante mantido: bundle Vite acima de 500 kB.
- `[VERIFICADO]` rev1 RED por asserção do gate (corpo exato do health). A raiz do health expõe só os nomes dos grupos `liveness`/`readiness`, sem componentes, banco ou versões; não há vazamento de detalhe. Corrigido no rev2 apenas no gate.
