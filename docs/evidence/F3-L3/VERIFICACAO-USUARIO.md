# F3-L3 — VERIFICAÇÃO DO USUÁRIO

Execute após aplicar o pacote. O shell estrito fica isolado. O frontend executa `pnpm format` (`biome check --write .`) antes de lint, typecheck, testes e build. O gate sobe o Compose com a sobreposição `compose.observability.yaml`; as portas `127.0.0.1:9090` (Prometheus) e `127.0.0.1:3000` (Grafana) precisam estar livres.

## Aplicação do pacote (antes do gate)

```sh
cd ~/proj/facilit-desafio-kanban
tar -xzf ~/Downloads/facilit-desafio-kanban-F3-L3-candidate-rev2.tar.gz
```

O F3-L3 não remove nem move arquivos; apenas adiciona e altera.

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
git rev-parse --is-inside-work-tree >/dev/null

RUN_ID="$(date +%s)-$$"
export APP_ADMIN_EMAIL="f3l3-admin-${RUN_ID}@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export APP_DEMO_RESPONSIBLE_NAME=""
export APP_DEMO_RESPONSIBLE_EMAIL=""
export APP_DEMO_RESPONSIBLE_POSITION=""
export APP_DEMO_RESPONSIBLE_PASSWORD=""
export SESSION_COOKIE_SECURE=false
export METRICS_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export GRAFANA_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"

DC=(docker compose -f compose.yaml -f compose.observability.yaml)
JAR_ADMIN=/tmp/f3l3-admin.cookies
NEG_DIR="$(mktemp -d /tmp/f3l3-compose.XXXXXX)"
rm -f /tmp/f3l3-*.json /tmp/f3l3-*.html /tmp/f3l3-*.txt /tmp/f3l3-*.log /tmp/f3l3-*.cookies

json_field() {
  python3 - "$1" "$2" <<'PY'
import json, sys
with open(sys.argv[1], encoding='utf-8') as handle:
    data = json.load(handle)
value = data
for part in sys.argv[2].split('.'):
    value = value[int(part)] if isinstance(value, list) else value[part]
print('' if value is None else value)
PY
}

refresh_csrf() {
  local jar="$1"
  curl -fsS -b "$jar" -c "$jar" http://localhost:5173/api/v1/auth/csrf >/tmp/f3l3-csrf.json
  CSRF_HEADER="$(json_field /tmp/f3l3-csrf.json headerName)"
  local cookie_name
  cookie_name="$(json_field /tmp/f3l3-csrf.json cookieName)"
  CSRF_TOKEN="$(awk -v name="$cookie_name" '$6 == name { value=$7 } END { if (value == "") exit 1; print value }' "$jar")"
}

login_admin() {
  rm -f "$JAR_ADMIN"
  refresh_csrf "$JAR_ADMIN"
  curl -sS -o /tmp/f3l3-login.json -w '%{http_code}' -X POST -b "$JAR_ADMIN" -c "$JAR_ADMIN" \
    -H 'Content-Type: application/json' -H "$CSRF_HEADER: $CSRF_TOKEN" \
    --data "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" \
    http://localhost:5173/api/v1/auth/login
}

# Credenciais via stdin (curl -K -), fora da lista de processos.
metrics_get() {
  local user="$1" password="$2" out="$3"
  printf 'user = "%s:%s"\n' "$user" "$password" \
    | curl -sS -K - -o "$out" -w '%{http_code}' http://localhost:8080/actuator/prometheus
}

grafana_get() {
  local path="$1" out="$2"
  printf 'user = "admin:%s"\n' "$GRAFANA_ADMIN_PASSWORD" \
    | curl -fsS -K - -o "$out" "http://127.0.0.1:3000$path"
}

prom_query() {
  curl -fsS --get --data-urlencode "query=$1" http://127.0.0.1:9090/api/v1/query >"$2"
}

cleanup() {
  set +e
  if "${DC[@]}" ps -q db >/dev/null 2>&1; then
    "${DC[@]}" exec -T db sh -lc \
      "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM app_users WHERE email = '$APP_ADMIN_EMAIL'\"" >/dev/null 2>&1
  fi
  "${DC[@]}" down --remove-orphans >/dev/null 2>&1
  rm -rf "$NEG_DIR"
  rm -f /tmp/f3l3-*.json /tmp/f3l3-*.html /tmp/f3l3-*.txt /tmp/f3l3-*.log /tmp/f3l3-*.cookies
}

# Renderiza as duas composições num diretório sem .env, com ambiente controlado.
compose_config() {
  (
    cd "$NEG_DIR"
    env -u METRICS_PASSWORD -u GRAFANA_ADMIN_PASSWORD -u POSTGRES_PASSWORD "$@" \
      docker compose -f compose.yaml -f compose.observability.yaml config --quiet
  )
}
trap cleanup EXIT

printf '%s\n' '=== FRONTEND: BIOME WRITE + LINT + TYPECHECK + TEST + BUILD ==='
(
  cd frontend
  corepack enable
  corepack prepare pnpm@12.5.1 --activate
  pnpm install --frozen-lockfile
  pnpm format
  pnpm lint
  pnpm typecheck
  pnpm test
  pnpm build
)
echo 'F3_L3_FRONTEND_GREEN'

printf '%s\n' '=== BACKEND: VERIFY (inclui MetricsCredentialsTest e ObservabilityIT) ==='
(
  cd backend
  mvn -B -ntp clean verify
)
test -f backend/target/failsafe-reports/TEST-br.com.facilit.kanban.integration.ObservabilityIT.xml
test -f backend/target/surefire-reports/TEST-br.com.facilit.kanban.infrastructure.security.MetricsCredentialsTest.xml
echo 'F3_L3_BACKEND_GREEN'

echo '=== COMPOSE: SENHAS OBRIGATÓRIAS NA SOBREPOSIÇÃO ==='
cp compose.yaml compose.observability.yaml "$NEG_DIR/"
mkdir -p "$NEG_DIR/backend" "$NEG_DIR/frontend" "$NEG_DIR/observability"
compose_config POSTGRES_PASSWORD=control-only METRICS_PASSWORD=control-only-metrics GRAFANA_ADMIN_PASSWORD=control-only
if compose_config POSTGRES_PASSWORD=control-only GRAFANA_ADMIN_PASSWORD=control-only >/tmp/f3l3-neg-metrics.txt 2>&1; then
  echo 'compose aceitou a sobreposição sem METRICS_PASSWORD' >&2
  exit 1
fi
grep -q 'METRICS_PASSWORD' /tmp/f3l3-neg-metrics.txt
if compose_config POSTGRES_PASSWORD=control-only METRICS_PASSWORD=control-only-metrics >/tmp/f3l3-neg-grafana.txt 2>&1; then
  echo 'compose aceitou a sobreposição sem GRAFANA_ADMIN_PASSWORD' >&2
  exit 1
fi
grep -q 'GRAFANA_ADMIN_PASSWORD' /tmp/f3l3-neg-grafana.txt
echo 'F3_L3_COMPOSE_REQUIRED_SECRETS_GREEN'

printf '%s\n' '=== DOCKER (base + observabilidade) ==='
"${DC[@]}" down --remove-orphans
"${DC[@]}" up --build -d
for i in $(seq 1 45); do
  if curl -fsS http://localhost:8080/actuator/health >/tmp/f3l3-health.json 2>/dev/null \
    && curl -fsS http://localhost:5173/ >/tmp/f3l3-index.html 2>/dev/null \
    && curl -fsS http://127.0.0.1:3000/api/health >/tmp/f3l3-grafana-health.json 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 45 ]; then
    "${DC[@]}" logs backend frontend prometheus grafana
    exit 1
  fi
  sleep 2
done
cat /tmp/f3l3-health.json
echo
test "$("${DC[@]}" port prometheus 9090)" = '127.0.0.1:9090'
test "$("${DC[@]}" port grafana 3000)" = '127.0.0.1:3000'
echo 'F3_L3_DOCKER_GREEN'

echo '=== ACTUATOR: HEALTH PÚBLICO SEM DETALHES ==='
python3 - <<'PY'
import json
with open('/tmp/f3l3-health.json', encoding='utf-8') as handle:
    data = json.load(handle)
# Com grupos de health (probes), o Boot lista os nomes dos grupos na raiz (SystemHealth.groups).
assert data.get('status') == 'UP', data
assert set(data) <= {'status', 'groups'}, data
assert 'components' not in data and 'details' not in data, data
assert set(data.get('groups', [])) <= {'liveness', 'readiness'}, data
PY
test "$(curl -sS -o /tmp/f3l3-liveness.json -w '%{http_code}' http://localhost:8080/actuator/health/liveness)" = '200'
test "$(curl -sS -o /tmp/f3l3-readiness.json -w '%{http_code}' http://localhost:8080/actuator/health/readiness)" = '200'
echo 'F3_L3_HEALTH_GREEN'

echo '=== ACTUATOR: MÉTRICAS SÓ COM A CREDENCIAL TÉCNICA ==='
test "$(curl -sS -o /tmp/f3l3-prom-anon.txt -w '%{http_code}' http://localhost:8080/actuator/prometheus)" = '401'
test "$(metrics_get prometheus wrong-metrics-password-000 /tmp/f3l3-prom-wrong.txt)" = '401'
test "$(metrics_get "$APP_ADMIN_EMAIL" "$APP_ADMIN_PASSWORD" /tmp/f3l3-prom-admin-basic.txt)" = '401'
test "$(login_admin)" = '200'
test "$(curl -sS -o /tmp/f3l3-prom-admin-session.txt -w '%{http_code}' -b "$JAR_ADMIN" http://localhost:8080/actuator/prometheus)" = '401'
for path in /actuator /actuator/env /actuator/heapdump /actuator/metrics; do
  code="$(printf 'user = "prometheus:%s"\n' "$METRICS_PASSWORD" | curl -sS -K - -o /tmp/f3l3-other.txt -w '%{http_code}' "http://localhost:8080$path")"
  case "$code" in
    2*) echo "endpoint indevidamente disponível: $path ($code)" >&2; exit 1 ;;
  esac
done
test "$(metrics_get prometheus "$METRICS_PASSWORD" /tmp/f3l3-prom-ok.txt)" = '200'
grep -q 'http_server_requests_seconds_bucket' /tmp/f3l3-prom-ok.txt
grep -q 'application="facilit-kanban"' /tmp/f3l3-prom-ok.txt
grep -q 'jvm_memory_used_bytes' /tmp/f3l3-prom-ok.txt
grep -q 'hikaricp_connections_active' /tmp/f3l3-prom-ok.txt
echo 'F3_L3_METRICS_SECURED_GREEN'

echo '=== PROMETHEUS: ALVO UP E SÉRIES COLETADAS ==='
for i in 1 2 3 4 5; do
  curl -fsS http://localhost:5173/api/v1/health >/dev/null
done
for i in $(seq 1 45); do
  if curl -fsS http://127.0.0.1:9090/api/v1/targets >/tmp/f3l3-targets.json 2>/dev/null \
    && prom_query 'sum(http_server_requests_seconds_count{application="facilit-kanban", uri="/api/v1/health"})' /tmp/f3l3-q-requests.json 2>/dev/null \
    && python3 - <<'PY'
import json, sys
with open('/tmp/f3l3-targets.json', encoding='utf-8') as handle:
    targets = json.load(handle)['data']['activeTargets']
with open('/tmp/f3l3-q-requests.json', encoding='utf-8') as handle:
    result = json.load(handle)['data']['result']
backend = [t for t in targets if t['labels'].get('job') == 'facilit-kanban-backend']
ok = len(backend) == 1 and backend[0]['health'] == 'up' and result and float(result[0]['value'][1]) >= 5
sys.exit(0 if ok else 1)
PY
  then
    break
  fi
  if [ "$i" -eq 45 ]; then
    cat /tmp/f3l3-targets.json
    "${DC[@]}" logs prometheus
    exit 1
  fi
  sleep 2
done
prom_query 'histogram_quantile(0.95, sum by (le, uri) (rate(http_server_requests_seconds_bucket{application="facilit-kanban"}[5m])))' /tmp/f3l3-q-p95.json
prom_query 'sum(jvm_memory_used_bytes{application="facilit-kanban", area="heap"})' /tmp/f3l3-q-heap.json
prom_query 'sum(hikaricp_connections_active{application="facilit-kanban"})' /tmp/f3l3-q-hikari.json
for file in /tmp/f3l3-q-p95.json /tmp/f3l3-q-heap.json /tmp/f3l3-q-hikari.json; do
  test "$(json_field "$file" status)" = 'success'
done
python3 - <<'PY'
import json
with open('/tmp/f3l3-q-heap.json', encoding='utf-8') as handle:
    result = json.load(handle)['data']['result']
assert result and float(result[0]['value'][1]) > 0, result
PY
echo 'F3_L3_PROMETHEUS_GREEN'

echo '=== GRAFANA: ACESSO, DATASOURCE E PAINEL PROVISIONADOS ==='
test "$(curl -sS -o /tmp/f3l3-grafana-anon.json -w '%{http_code}' http://127.0.0.1:3000/api/search)" = '401'
test "$(printf 'user = "admin:admin"\n' | curl -sS -K - -o /tmp/f3l3-grafana-default.json -w '%{http_code}' http://127.0.0.1:3000/api/search)" = '401'
grafana_get /api/datasources/uid/prometheus /tmp/f3l3-grafana-ds.json
test "$(json_field /tmp/f3l3-grafana-ds.json type)" = 'prometheus'
test "$(json_field /tmp/f3l3-grafana-ds.json url)" = 'http://prometheus:9090'
grafana_get /api/datasources/uid/prometheus/health /tmp/f3l3-grafana-ds-health.json
test "$(json_field /tmp/f3l3-grafana-ds-health.json status)" = 'OK'
grafana_get /api/dashboards/uid/facilit-kanban /tmp/f3l3-grafana-dash.json
python3 - <<'PY'
import json
with open('/tmp/f3l3-grafana-dash.json', encoding='utf-8') as handle:
    dashboard = json.load(handle)['dashboard']
assert dashboard['uid'] == 'facilit-kanban', dashboard.get('uid')
assert len(dashboard['panels']) == 6, len(dashboard['panels'])
PY
echo 'F3_L3_GRAFANA_GREEN'

echo '=== LOGS ESTRUTURADOS (ECS) SEM SEGREDOS ==='
"${DC[@]}" logs --no-color --no-log-prefix backend >/tmp/f3l3-backend.log
python3 - <<'PY'
import json
records = []
with open('/tmp/f3l3-backend.log', encoding='utf-8') as handle:
    for line in handle:
        line = line.strip()
        if line.startswith('{'):
            try:
                records.append(json.loads(line))
            except json.JSONDecodeError:
                pass
assert records, 'nenhuma linha JSON no log do backend'
started = [r for r in records if str(r.get('message', '')).startswith('Started ')]
assert started, 'linha de inicialização não está em JSON'
sample = started[0]
assert '@timestamp' in sample, sample
assert sample.get('log', {}).get('level') == 'INFO', sample
assert sample.get('ecs', {}).get('version'), sample
print(f'ECS_RECORDS {len(records)}')
PY
for secret in "$METRICS_PASSWORD" "$GRAFANA_ADMIN_PASSWORD" "$APP_ADMIN_PASSWORD"; do
  if grep -qF -- "$secret" /tmp/f3l3-backend.log; then
    echo 'segredo encontrado no log do backend' >&2
    exit 1
  fi
done
echo 'F3_L3_STRUCTURED_LOGS_GREEN'

echo '=== STATIC SAFETY + GIT ==='
(
  cd frontend
  node --input-type=module <<'NODE'
import fs from "node:fs";
import path from "node:path";
import ts from "typescript";
const sourceRoot = path.resolve("src");
const sourceFiles = [];
function collect(directory) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const absolute = path.join(directory, entry.name);
    if (entry.isDirectory()) collect(absolute);
    else if (entry.name.endsWith(".ts") || entry.name.endsWith(".tsx")) sourceFiles.push(absolute);
  }
}
collect(sourceRoot);
const violations = [];
for (const filename of sourceFiles) {
  const source = ts.createSourceFile(filename, fs.readFileSync(filename, "utf8"), ts.ScriptTarget.Latest, true, filename.endsWith(".tsx") ? ts.ScriptKind.TSX : ts.ScriptKind.TS);
  function visit(node) {
    if (node.kind === ts.SyntaxKind.AnyKeyword || node.kind === ts.SyntaxKind.AsExpression || node.kind === ts.SyntaxKind.NonNullExpression) {
      const position = source.getLineAndCharacterOfPosition(node.getStart(source));
      violations.push(`${path.relative(sourceRoot, filename)}:${position.line + 1}`);
    }
    ts.forEachChild(node, visit);
  }
  visit(source);
}
if (violations.length > 0) {
  console.error(violations.join("\n"));
  process.exit(1);
}
console.log(`F3_L3_STRICT_TYPES_GREEN ${sourceFiles.length}`);
NODE
)
if grep -RInE 'localStorage|sessionStorage' frontend/src; then
  exit 1
fi
if grep -RInE '(PASSWORD|password)[:=][[:space:]]*[^-?$[:space:]{]' compose.yaml compose.observability.yaml observability; then
  echo 'senha literal em arquivo de Compose/observabilidade' >&2
  exit 1
fi
git diff --check
"${DC[@]}" ps
echo 'F3_L3_STATIC_GREEN'

printf '%s\n' '=== F3-L3 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige todos os marcadores `F3_L3_*_GREEN`, `=== F3-L3 GREEN ===` e `Resultado: exit code 0`.
