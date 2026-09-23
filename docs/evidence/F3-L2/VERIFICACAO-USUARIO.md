# F3-L2 — VERIFICAÇÃO DO USUÁRIO

Execute após aplicar o pacote. O shell estrito fica isolado. O frontend executa `pnpm format` (`biome check --write .`) antes de lint, typecheck, testes e build.

## Aplicação do pacote (antes do gate)

```sh
cd ~/proj/facilit-desafio-kanban
tar -xzf ~/Downloads/facilit-desafio-kanban-F3-L2-candidate.tar.gz
```

O F3-L2 não remove nem move arquivos; apenas adiciona e altera.

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
git rev-parse --is-inside-work-tree >/dev/null

RUN_ID="$(date +%s)-$$"
export APP_ADMIN_EMAIL="f3l2-admin-${RUN_ID}@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export APP_DEMO_RESPONSIBLE_NAME="Responsável Demo F3-L2"
export APP_DEMO_RESPONSIBLE_EMAIL="f3l2-demo-${RUN_ID}@example.invalid"
export APP_DEMO_RESPONSIBLE_POSITION="Analista"
export APP_DEMO_RESPONSIBLE_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export SESSION_COOKIE_SECURE=false

OTHER_EMAIL="f3l2-other-${RUN_ID}@example.invalid"
OTHER_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
TODAY="$(date -I)"
PLANNED_END="$(date -I -d '+10 days')"
JAR_ADMIN=/tmp/f3l2-admin.cookies
JAR_DEMO=/tmp/f3l2-demo.cookies
JAR_OTHER=/tmp/f3l2-other.cookies
OTHER_ID=""
OWN_PROJECT_ID=""
OTHER_PROJECT_ID=""
rm -f /tmp/f3l2-*.json /tmp/f3l2-*.html /tmp/f3l2-*.cookies

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
  curl -fsS -b "$jar" -c "$jar" http://localhost:5173/api/v1/auth/csrf >/tmp/f3l2-csrf.json
  CSRF_HEADER="$(json_field /tmp/f3l2-csrf.json headerName)"
  local cookie_name
  cookie_name="$(json_field /tmp/f3l2-csrf.json cookieName)"
  CSRF_TOKEN="$(awk -v name="$cookie_name" '$6 == name { value=$7 } END { if (value == "") exit 1; print value }' "$jar")"
}

api() {
  local jar="$1" method="$2" path="$3" body="$4" out="$5"
  refresh_csrf "$jar"
  if [ -n "$body" ]; then
    curl -sS -o "$out" -w '%{http_code}' -X "$method" -b "$jar" -c "$jar" \
      -H 'Content-Type: application/json' -H "$CSRF_HEADER: $CSRF_TOKEN" \
      --data "$body" "http://localhost:5173$path"
  else
    curl -sS -o "$out" -w '%{http_code}' -X "$method" -b "$jar" -c "$jar" \
      -H "$CSRF_HEADER: $CSRF_TOKEN" "http://localhost:5173$path"
  fi
}

login() {
  local jar="$1" email="$2" password="$3"
  rm -f "$jar"
  api "$jar" POST /api/v1/auth/login "{\"email\":\"$email\",\"password\":\"$password\"}" /tmp/f3l2-login.json
}

expect_forbidden() {
  local status="$1" file="$2"
  test "$status" = '403'
  test "$(json_field "$file" code)" = 'FORBIDDEN'
}

cleanup() {
  set +e
  if docker compose ps -q db >/dev/null 2>&1; then
    for id in "$OWN_PROJECT_ID" "$OTHER_PROJECT_ID"; do
      if [ -n "$id" ]; then
        docker compose exec -T db sh -lc \
          "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM projects WHERE id = '$id'::uuid\"" >/dev/null 2>&1
      fi
    done
    docker compose exec -T db sh -lc \
      "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM responsibles WHERE email IN ('$OTHER_EMAIL', '$APP_DEMO_RESPONSIBLE_EMAIL')\"" >/dev/null 2>&1
    docker compose exec -T db sh -lc \
      "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM app_users WHERE email = '$APP_ADMIN_EMAIL'\"" >/dev/null 2>&1
  fi
  docker compose down --remove-orphans >/dev/null 2>&1
  rm -f /tmp/f3l2-*.json /tmp/f3l2-*.html /tmp/f3l2-*.cookies
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
echo 'F3_L2_FRONTEND_GREEN'

printf '%s\n' '=== BACKEND: VERIFY ==='
(
  cd backend
  mvn -B -ntp clean verify
)
echo 'F3_L2_BACKEND_GREEN'

printf '%s\n' '=== DOCKER ==='
docker compose down --remove-orphans
docker compose up --build -d
for i in $(seq 1 30); do
  if curl -fsS http://localhost:8080/api/v1/health >/tmp/f3l2-health.json 2>/dev/null \
    && curl -fsS http://localhost:5173/ >/tmp/f3l2-index.html 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 30 ]; then
    docker compose logs backend frontend
    exit 1
  fi
  sleep 2
done
cat /tmp/f3l2-health.json
echo
echo 'F3_L2_DOCKER_GREEN'

echo '=== ADMIN E RESPONSÁVEL DE DEMONSTRAÇÃO ==='
test "$(login "$JAR_ADMIN" "$APP_ADMIN_EMAIL" "$APP_ADMIN_PASSWORD")" = '200'
python3 - <<'PY'
import json
with open('/tmp/f3l2-login.json', encoding='utf-8') as handle:
    data = json.load(handle)
assert 'ROLE_ADMIN' in data['authorities'], data
assert data['responsibleId'] is None, data
PY
test "$(login "$JAR_DEMO" "$APP_DEMO_RESPONSIBLE_EMAIL" "$APP_DEMO_RESPONSIBLE_PASSWORD")" = '200'
DEMO_ID="$(json_field /tmp/f3l2-login.json responsibleId)"
python3 - <<'PY'
import json
with open('/tmp/f3l2-login.json', encoding='utf-8') as handle:
    data = json.load(handle)
assert data['authorities'] == ['ROLE_RESPONSIBLE'], data
assert data['responsibleId'], data
PY
echo 'F3_L2_DEMO_BOOTSTRAP_GREEN'

echo '=== OUTRO RESPONSÁVEL E PROJETO ALHEIO (ADMIN) ==='
test "$(api "$JAR_ADMIN" POST /api/v1/responsibles "{\"name\":\"Outro F3-L2\",\"email\":\"$OTHER_EMAIL\",\"position\":\"Gestor\"}" /tmp/f3l2-other.json)" = '201'
OTHER_ID="$(json_field /tmp/f3l2-other.json id)"
test "$(api "$JAR_ADMIN" POST /api/v1/projects "{\"name\":\"Alheio F3-L2\",\"responsibleIds\":[\"$OTHER_ID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$PLANNED_END\"}" /tmp/f3l2-other-project.json)" = '201'
OTHER_PROJECT_ID="$(json_field /tmp/f3l2-other-project.json id)"
echo 'F3_L2_FIXTURES_GREEN'

echo '=== RESPONSÁVEL: CAMINHO LEGÍTIMO ==='
test "$(api "$JAR_DEMO" POST /api/v1/projects "{\"name\":\"Próprio F3-L2\",\"responsibleIds\":[\"$DEMO_ID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$PLANNED_END\"}" /tmp/f3l2-own-project.json)" = '201'
OWN_PROJECT_ID="$(json_field /tmp/f3l2-own-project.json id)"
test "$(api "$JAR_DEMO" PATCH "/api/v1/projects/$OWN_PROJECT_ID/status" '{"status":"IN_PROGRESS"}' /tmp/f3l2-own-transition.json)" = '200'
test "$(json_field /tmp/f3l2-own-transition.json status)" = 'IN_PROGRESS'
test "$(api "$JAR_DEMO" GET '/api/v1/projects?page=0&size=100&text=F3-L2' '' /tmp/f3l2-board.json)" = '200'
python3 - "$OWN_PROJECT_ID" "$OTHER_PROJECT_ID" <<'PY'
import json, sys
with open('/tmp/f3l2-board.json', encoding='utf-8') as handle:
    ids = {item['id'] for item in json.load(handle)['content']}
assert sys.argv[1] in ids and sys.argv[2] in ids, ids
PY
echo 'F3_L2_RESPONSIBLE_LEGIT_GREEN'

echo '=== RESPONSÁVEL: CAMINHO DE ATAQUE ==='
expect_forbidden "$(api "$JAR_DEMO" PATCH "/api/v1/projects/$OTHER_PROJECT_ID/status" '{"status":"IN_PROGRESS"}' /tmp/f3l2-deny-1.json)" /tmp/f3l2-deny-1.json
expect_forbidden "$(api "$JAR_DEMO" DELETE "/api/v1/projects/$OTHER_PROJECT_ID" '' /tmp/f3l2-deny-2.json)" /tmp/f3l2-deny-2.json
expect_forbidden "$(api "$JAR_DEMO" POST /api/v1/responsibles '{"name":"Indevido","email":"indevido-f3l2@example.invalid","position":"Analista"}' /tmp/f3l2-deny-3.json)" /tmp/f3l2-deny-3.json
expect_forbidden "$(api "$JAR_DEMO" PUT "/api/v1/responsibles/$OTHER_ID" "{\"name\":\"Outro F3-L2\",\"email\":\"takeover-f3l2@example.invalid\",\"position\":\"Gestor\"}" /tmp/f3l2-deny-4.json)" /tmp/f3l2-deny-4.json
expect_forbidden "$(api "$JAR_DEMO" POST /api/v1/secretariats '{"name":"Indevida"}' /tmp/f3l2-deny-5.json)" /tmp/f3l2-deny-5.json
expect_forbidden "$(api "$JAR_DEMO" PUT "/api/v1/responsibles/$OTHER_ID/credentials" '{"password":"tentativa-invasao-123"}' /tmp/f3l2-deny-6.json)" /tmp/f3l2-deny-6.json
refresh_csrf "$JAR_DEMO"
curl -fsS -b "$JAR_DEMO" -c "$JAR_DEMO" \
  -H 'Content-Type: application/json' -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data '{"query":"mutation { createSecretariat(input: {name: \"Indevida\"}) { id } }"}' \
  http://localhost:8080/graphql >/tmp/f3l2-deny-7.json
test "$(json_field /tmp/f3l2-deny-7.json errors.0.extensions.code)" = 'FORBIDDEN'
test "$(api "$JAR_ADMIN" GET "/api/v1/projects/$OTHER_PROJECT_ID" '' /tmp/f3l2-other-after.json)" = '200'
test "$(json_field /tmp/f3l2-other-after.json status)" = "$(json_field /tmp/f3l2-other-project.json status)"
echo 'F3_L2_RESPONSIBLE_ATTACK_BLOCKED_GREEN'

echo '=== CREDENCIAIS (ADMIN) ==='
test "$(api "$JAR_ADMIN" PUT "/api/v1/responsibles/$OTHER_ID/credentials" '{"password":"curta"}' /tmp/f3l2-weak.json)" = '400'
test "$(json_field /tmp/f3l2-weak.json code)" = 'INVALID_REQUEST'
test "$(api "$JAR_ADMIN" PUT "/api/v1/responsibles/$OTHER_ID/credentials" "{\"password\":\"$OTHER_PASSWORD\"}" /tmp/f3l2-set.json)" = '204'
test "$(login "$JAR_OTHER" "$OTHER_EMAIL" "$OTHER_PASSWORD")" = '200'
test "$(json_field /tmp/f3l2-login.json responsibleId)" = "$OTHER_ID"
test "$(api "$JAR_ADMIN" DELETE "/api/v1/responsibles/$OTHER_ID/credentials" '' /tmp/f3l2-revoke.json)" = '204'
test "$(login "$JAR_OTHER" "$OTHER_EMAIL" "$OTHER_PASSWORD")" = '401'
echo 'F3_L2_CREDENTIALS_GREEN'

echo '=== ERRO SEGURO ==='
test "$(api "$JAR_ADMIN" GET /api/v1/unknown-resource '' /tmp/f3l2-unknown.json)" = '404'
echo 'F3_L2_SAFE_ERROR_GREEN'

echo '=== CLEANUP FUNCIONAL ==='
test "$(api "$JAR_DEMO" DELETE "/api/v1/projects/$OWN_PROJECT_ID" '' /tmp/f3l2-del-own.json)" = '204'
OWN_PROJECT_ID=""
test "$(api "$JAR_ADMIN" DELETE "/api/v1/projects/$OTHER_PROJECT_ID" '' /tmp/f3l2-del-other.json)" = '204'
OTHER_PROJECT_ID=""
test "$(api "$JAR_ADMIN" DELETE "/api/v1/responsibles/$OTHER_ID" '' /tmp/f3l2-del-resp.json)" = '204'
OTHER_ID=""
echo 'F3_L2_CLEANUP_GREEN'

echo '=== OPENAPI + STATIC SAFETY + GIT ==='
curl -fsS http://localhost:8080/api-docs >/tmp/f3l2-openapi.json
python3 - <<'PY'
import json
with open('/tmp/f3l2-openapi.json', encoding='utf-8') as handle:
    paths = json.load(handle)['paths']
assert '/api/v1/responsibles/{id}/credentials' in paths, paths.keys()
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
console.log(`F3_L2_STRICT_TYPES_GREEN ${sourceFiles.length}`);
NODE
)
! grep -RInE 'localStorage|sessionStorage' frontend/src
git diff --check
docker compose ps
echo 'F3_L2_STATIC_GREEN'

printf '%s\n' '=== F3-L2 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige todos os marcadores `F3_L2_*_GREEN`, `=== F3-L2 GREEN ===` e `Resultado: exit code 0`.
