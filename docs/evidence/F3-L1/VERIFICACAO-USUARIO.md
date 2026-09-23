# F3-L1 — VERIFICAÇÃO DO USUÁRIO

Execute após aplicar o snapshot. O shell estrito fica isolado. O frontend executa `pnpm format` (`biome check --write .`) antes de lint, typecheck, testes e build.

## Aplicação do pacote rev4 (antes do gate)

O `tar -x` não remove arquivos. O F3-L1 move `SecretariatRepository` de `application/responsible` para `application/secretariat`; o arquivo antigo precisa ser removido explicitamente.

```sh
cd ~/proj/facilit-desafio-kanban
tar -xzf ~/Downloads/facilit-desafio-kanban-F3-L1-candidate-rev4.tar.gz
rm -f backend/src/main/java/br/com/facilit/kanban/application/responsible/SecretariatRepository.java
```

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban

printf '%s\n' '=== RECONCILIAÇÃO F3-L1 ==='
ORPHAN=backend/src/main/java/br/com/facilit/kanban/application/responsible/SecretariatRepository.java
if [ -e "$ORPHAN" ]; then
  echo "Arquivo órfão presente; remova antes do gate: $ORPHAN" >&2
  exit 1
fi
test -f backend/src/main/java/br/com/facilit/kanban/application/secretariat/SecretariatRepository.java
git rev-parse --is-inside-work-tree >/dev/null
echo 'F3_L1_RECONCILIATION_GREEN'

export APP_ADMIN_EMAIL="f3l1-admin-$(date +%s)-$$@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export SESSION_COOKIE_SECURE=false

RESPONSIBLE_EMAIL="f3l1-responsible-$(date +%s)-$$@example.invalid"
TODAY="$(date -I)"
PLANNED_END="$(date -I -d '+10 days')"
OTHER_START="$(date -I -d '+30 days')"
OTHER_END="$(date -I -d '+40 days')"
FILTER_FROM="$(date -I -d '-1 day')"
FILTER_TO="$(date -I -d '+20 days')"
INVALID_FROM="$(date -I -d '+2 days')"
COOKIE_JAR=/tmp/f3l1.cookies
SECRETARIAT_ID=""
RESPONSIBLE_ID=""
PROJECT_ID=""
OTHER_PROJECT_ID=""
rm -f "$COOKIE_JAR" /tmp/f3l1-*.json /tmp/f3l1-*.html

cookie_value() {
  local jar="$1"
  local name="$2"
  awk -v name="$name" '$6 == name { value=$7 } END { if (value == "") exit 1; print value }' "$jar"
}

refresh_csrf() {
  curl -fsS -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
    http://localhost:5173/api/v1/auth/csrf >/tmp/f3l1-csrf.json
  CSRF_HEADER="$(python3 - <<'PY'
import json
with open('/tmp/f3l1-csrf.json', encoding='utf-8') as handle:
    print(json.load(handle)['headerName'])
PY
)"
  CSRF_COOKIE="$(python3 - <<'PY'
import json
with open('/tmp/f3l1-csrf.json', encoding='utf-8') as handle:
    print(json.load(handle)['cookieName'])
PY
)"
  CSRF_TOKEN="$(cookie_value "$COOKIE_JAR" "$CSRF_COOKIE")"
}

cleanup() {
  set +e
  if docker compose ps -q db >/dev/null 2>&1; then
    if [ -n "$PROJECT_ID" ]; then
      docker compose exec -T db sh -lc \
        "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM projects WHERE id = '$PROJECT_ID'::uuid\"" >/dev/null 2>&1
    fi
    if [ -n "$OTHER_PROJECT_ID" ]; then
      docker compose exec -T db sh -lc \
        "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM projects WHERE id = '$OTHER_PROJECT_ID'::uuid\"" >/dev/null 2>&1
    fi
    if [ -n "$RESPONSIBLE_ID" ]; then
      docker compose exec -T db sh -lc \
        "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM responsibles WHERE id = '$RESPONSIBLE_ID'::uuid\"" >/dev/null 2>&1
    fi
    if [ -n "$SECRETARIAT_ID" ]; then
      docker compose exec -T db sh -lc \
        "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM secretariats WHERE id = '$SECRETARIAT_ID'::uuid\"" >/dev/null 2>&1
    fi
    docker compose exec -T db sh -lc \
      "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM app_users WHERE email = '$APP_ADMIN_EMAIL'\"" >/dev/null 2>&1
  fi
  docker compose down --remove-orphans >/dev/null 2>&1
  rm -f "$COOKIE_JAR" /tmp/f3l1-*.json /tmp/f3l1-*.html
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
echo 'F3_L1_FRONTEND_GREEN'

printf '%s\n' '=== BACKEND: VERIFY ==='
(
  cd backend
  mvn -B -ntp clean verify
)
echo 'F3_L1_BACKEND_GREEN'

printf '%s\n' '=== DOCKER ==='
docker compose down --remove-orphans
docker compose up --build -d
for i in $(seq 1 30); do
  if curl -fsS http://localhost:8080/api/v1/health >/tmp/f3l1-health.json 2>/dev/null \
    && curl -fsS http://localhost:5173/ >/tmp/f3l1-index.html 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 30 ]; then
    docker compose logs backend frontend
    exit 1
  fi
  sleep 2
done
cat /tmp/f3l1-health.json
echo
grep -Fq '<div id="root"></div>' /tmp/f3l1-index.html

echo '=== LOGIN + CSRF VIA FRONTEND ==='
refresh_csrf
LOGIN_STATUS=$(curl -sS -o /tmp/f3l1-login.json -w '%{http_code}' \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" \
  http://localhost:5173/api/v1/auth/login)
test "$LOGIN_STATUS" = '200'
echo 'F3_L1_AUTH_GREEN'

echo '=== SECRETARIA REAL ==='
refresh_csrf
STATUS=$(curl -sS -o /tmp/f3l1-secretariat.json -w '%{http_code}' \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data '{"name":"Secretaria F3-L1"}' \
  http://localhost:5173/api/v1/secretariats)
test "$STATUS" = '201'
SECRETARIAT_ID="$(python3 - <<'PY'
import json
with open('/tmp/f3l1-secretariat.json', encoding='utf-8') as handle:
    print(json.load(handle)['id'])
PY
)"
test -n "$SECRETARIAT_ID"
echo 'F3_L1_SECRETARIAT_CREATE_GREEN'

echo '=== RESPONSÁVEL VINCULADO ==='
refresh_csrf
STATUS=$(curl -sS -o /tmp/f3l1-responsible.json -w '%{http_code}' \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data "{\"name\":\"Responsável F3-L1\",\"email\":\"$RESPONSIBLE_EMAIL\",\"position\":\"Gestor\",\"secretariatId\":\"$SECRETARIAT_ID\"}" \
  http://localhost:5173/api/v1/responsibles)
test "$STATUS" = '201'
RESPONSIBLE_ID="$(python3 - <<'PY'
import json
with open('/tmp/f3l1-responsible.json', encoding='utf-8') as handle:
    print(json.load(handle)['id'])
PY
)"
test -n "$RESPONSIBLE_ID"
echo 'F3_L1_RESPONSIBLE_GREEN'

echo '=== DOIS PROJETOS PARA FILTRO ==='
refresh_csrf
STATUS=$(curl -sS -o /tmp/f3l1-project.json -w '%{http_code}' \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data "{\"name\":\"Portal F3-L1\",\"responsibleIds\":[\"$RESPONSIBLE_ID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$PLANNED_END\",\"actualStart\":null,\"actualEnd\":null}" \
  http://localhost:5173/api/v1/projects)
test "$STATUS" = '201'
PROJECT_ID="$(python3 - <<'PY'
import json
with open('/tmp/f3l1-project.json', encoding='utf-8') as handle:
    print(json.load(handle)['id'])
PY
)"

refresh_csrf
STATUS=$(curl -sS -o /tmp/f3l1-other-project.json -w '%{http_code}' \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data "{\"name\":\"Administrativo F3-L1\",\"responsibleIds\":[\"$RESPONSIBLE_ID\"],\"plannedStart\":\"$OTHER_START\",\"plannedEnd\":\"$OTHER_END\",\"actualStart\":null,\"actualEnd\":null}" \
  http://localhost:5173/api/v1/projects)
test "$STATUS" = '201'
OTHER_PROJECT_ID="$(python3 - <<'PY'
import json
with open('/tmp/f3l1-other-project.json', encoding='utf-8') as handle:
    print(json.load(handle)['id'])
PY
)"
echo 'F3_L1_PROJECT_FIXTURES_GREEN'

echo '=== LISTAGEM SEM FILTROS E FILTRO ISOLADO ==='
curl -fsS -b "$COOKIE_JAR" -G \
  --data-urlencode 'page=0' \
  --data-urlencode 'size=20' \
  http://localhost:5173/api/v1/projects >/tmp/f3l1-unfiltered.json
python3 - <<'PY'
import json
with open('/tmp/f3l1-unfiltered.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['totalElements'] >= 2, data
PY
curl -fsS -b "$COOKIE_JAR" -G \
  --data-urlencode 'page=0' \
  --data-urlencode 'size=20' \
  --data-urlencode 'text=administrativo f3-l1' \
  http://localhost:5173/api/v1/projects >/tmp/f3l1-text-only.json
python3 - "$OTHER_PROJECT_ID" <<'PY'
import json, sys
with open('/tmp/f3l1-text-only.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert [item['id'] for item in data['content']] == [sys.argv[1]], data
PY
echo 'F3_L1_LIST_UNFILTERED_GREEN'

echo '=== FILTROS REST COMBINADOS ==='
curl -fsS -b "$COOKIE_JAR" -G \
  --data-urlencode 'page=0' \
  --data-urlencode 'size=20' \
  --data-urlencode "secretariatId=$SECRETARIAT_ID" \
  --data-urlencode "responsibleId=$RESPONSIBLE_ID" \
  --data-urlencode "plannedFrom=$FILTER_FROM" \
  --data-urlencode "plannedTo=$FILTER_TO" \
  --data-urlencode 'text=Portal' \
  http://localhost:5173/api/v1/projects >/tmp/f3l1-filtered.json
python3 - "$PROJECT_ID" <<'PY'
import json, sys
with open('/tmp/f3l1-filtered.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert len(data['content']) == 1, data
assert data['content'][0]['id'] == sys.argv[1], data
PY
echo 'F3_L1_FILTERS_REST_GREEN'

echo '=== PERÍODO INVÁLIDO BLOQUEADO ==='
STATUS=$(curl -sS -o /tmp/f3l1-invalid-period.json -w '%{http_code}' -b "$COOKIE_JAR" -G \
  --data-urlencode "plannedFrom=$INVALID_FROM" \
  --data-urlencode "plannedTo=$TODAY" \
  http://localhost:5173/api/v1/projects)
test "$STATUS" = '400'
python3 - <<'PY'
import json
with open('/tmp/f3l1-invalid-period.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['code'] == 'INVALID_REQUEST', data
PY
echo 'F3_L1_FILTER_VALIDATION_GREEN'

echo '=== INDICADORES REST ==='
curl -fsS -b "$COOKIE_JAR" \
  http://localhost:5173/api/v1/indicators/projects >/tmp/f3l1-indicators.json
python3 - <<'PY'
import json
with open('/tmp/f3l1-indicators.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['totalProjects'] >= 2, data
assert data['delayedProjects'] >= 0, data
rows={item['status']: item for item in data['byStatus']}
assert set(rows) == {'NOT_STARTED','IN_PROGRESS','OVERDUE','COMPLETED'}, rows
assert rows['NOT_STARTED']['projectCount'] >= 2, rows
assert rows['NOT_STARTED']['averageDelayDays'] >= 0, rows
PY
echo 'F3_L1_INDICATORS_REST_GREEN'

echo '=== GRAPHQL: FILTROS + INDICADORES + SECRETARIA ==='
python3 - "$SECRETARIAT_ID" "$RESPONSIBLE_ID" "$FILTER_FROM" "$FILTER_TO" <<'PY'
import json, sys
query='''query($secretariatId: ID!, $responsibleId: ID, $plannedFrom: String, $plannedTo: String, $text: String) {
  projects(page: 0, size: 20, secretariatId: $secretariatId, responsibleId: $responsibleId, plannedFrom: $plannedFrom, plannedTo: $plannedTo, text: $text) { content { id name } }
  projectIndicators { totalProjects delayedProjects byStatus { status projectCount averageDelayDays } }
  secretariat(id: $secretariatId) { id name }
}'''
body={'query': query, 'variables': {'secretariatId': sys.argv[1], 'responsibleId': sys.argv[2], 'plannedFrom': sys.argv[3], 'plannedTo': sys.argv[4], 'text': 'Portal'}}
with open('/tmp/f3l1-gql-request.json','w',encoding='utf-8') as handle: json.dump(body,handle)
PY
refresh_csrf
curl -fsS -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data-binary @/tmp/f3l1-gql-request.json \
  http://localhost:8080/graphql >/tmp/f3l1-gql.json
python3 - "$PROJECT_ID" "$SECRETARIAT_ID" <<'PY'
import json, sys
with open('/tmp/f3l1-gql.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert 'errors' not in data, data
assert len(data['data']['projects']['content']) == 1, data
assert data['data']['projects']['content'][0]['id'] == sys.argv[1], data
assert data['data']['projectIndicators']['totalProjects'] >= 2, data
assert data['data']['secretariat']['id'] == sys.argv[2], data
PY
echo 'F3_L1_GRAPHQL_GREEN'

echo '=== UPDATE SECRETARIA VIA GRAPHQL ==='
python3 - "$SECRETARIAT_ID" <<'PY'
import json, sys
body={'query':'mutation($id: ID!, $input: SecretariatInput!) { updateSecretariat(id: $id, input: $input) { id name } }','variables':{'id':sys.argv[1],'input':{'name':'Secretaria F3-L1 Atualizada'}}}
with open('/tmp/f3l1-gql-update.json','w',encoding='utf-8') as handle: json.dump(body,handle)
PY
refresh_csrf
curl -fsS -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data-binary @/tmp/f3l1-gql-update.json \
  http://localhost:8080/graphql >/tmp/f3l1-gql-update-response.json
python3 - <<'PY'
import json
with open('/tmp/f3l1-gql-update-response.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert 'errors' not in data, data
assert data['data']['updateSecretariat']['name'] == 'Secretaria F3-L1 Atualizada', data
PY
echo 'F3_L1_SECRETARIAT_UPDATE_GREEN'

echo '=== BLOQUEIO DE EXCLUSÃO DA SECRETARIA REFERENCIADA ==='
refresh_csrf
STATUS=$(curl -sS -o /tmp/f3l1-secretariat-block.json -w '%{http_code}' \
  -X DELETE -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  "http://localhost:5173/api/v1/secretariats/$SECRETARIAT_ID")
test "$STATUS" = '409'
python3 - <<'PY'
import json
with open('/tmp/f3l1-secretariat-block.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['code'] == 'CONFLICT', data
PY
echo 'F3_L1_SECRETARIAT_BLOCK_GREEN'

echo '=== CLEANUP FUNCIONAL ==='
for id in "$PROJECT_ID" "$OTHER_PROJECT_ID"; do
  refresh_csrf
  STATUS=$(curl -sS -o /dev/null -w '%{http_code}' -X DELETE \
    -b "$COOKIE_JAR" -c "$COOKIE_JAR" -H "$CSRF_HEADER: $CSRF_TOKEN" \
    "http://localhost:5173/api/v1/projects/$id")
  test "$STATUS" = '204'
done
PROJECT_ID=""
OTHER_PROJECT_ID=""

refresh_csrf
STATUS=$(curl -sS -o /dev/null -w '%{http_code}' -X DELETE \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" -H "$CSRF_HEADER: $CSRF_TOKEN" \
  "http://localhost:5173/api/v1/responsibles/$RESPONSIBLE_ID")
test "$STATUS" = '204'
RESPONSIBLE_ID=""

refresh_csrf
STATUS=$(curl -sS -o /dev/null -w '%{http_code}' -X DELETE \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" -H "$CSRF_HEADER: $CSRF_TOKEN" \
  "http://localhost:5173/api/v1/secretariats/$SECRETARIAT_ID")
test "$STATUS" = '204'
SECRETARIAT_ID=""
echo 'F3_L1_CRUD_GREEN'

echo '=== OPENAPI + STATIC SAFETY + GIT ==='
curl -fsS http://localhost:8080/api-docs >/tmp/f3l1-openapi.json
python3 - <<'PY'
import json
with open('/tmp/f3l1-openapi.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert '/api/v1/secretariats' in data['paths'], data['paths'].keys()
assert '/api/v1/indicators/projects' in data['paths'], data['paths'].keys()
PY
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
console.log(`F3_L1_STRICT_TYPES_GREEN ${sourceFiles.length}`);
NODE
)
! grep -RInE 'localStorage|sessionStorage' frontend/src
git diff --check
docker compose ps
echo 'F3_L1_STATIC_GREEN'

printf '%s\n' '=== F3-L1 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige todos os marcadores `F3_L1_*_GREEN`, `=== F3-L1 GREEN ===` e `Resultado: exit code 0`.
