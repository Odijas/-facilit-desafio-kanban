# F2-L3 — VERIFICAÇÃO DO USUÁRIO

Execute após aplicar o snapshot. O shell estrito fica isolado. O primeiro passo frontend executa `pnpm format`, cujo script usa `biome check --write .`, aplicando formatter e safe fixes/assist antes de lint, typecheck, testes e build.

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban

export APP_ADMIN_EMAIL="f2l3-admin-$(date +%s)-$$@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export SESSION_COOKIE_SECURE=false

RESPONSIBLE_EMAIL="f2l3-responsible-$(date +%s)-$$@example.invalid"
TODAY="$(date -I)"
PLANNED_END="$(date -I -d '+7 days')"
COOKIE_JAR=/tmp/f2l3.cookies
rm -f "$COOKIE_JAR" /tmp/f2l3-*.json /tmp/f2l3-*.html

cookie_value() {
  local jar="$1"
  local name="$2"
  awk -v name="$name" '$6 == name { value=$7 } END { if (value == "") exit 1; print value }' "$jar"
}

refresh_csrf() {
  curl -fsS -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
    http://localhost:5173/api/v1/auth/csrf >/tmp/f2l3-csrf.json
  CSRF_HEADER="$(python3 - <<'PY'
import json
with open('/tmp/f2l3-csrf.json', encoding='utf-8') as handle:
    print(json.load(handle)['headerName'])
PY
)"
  CSRF_COOKIE="$(python3 - <<'PY'
import json
with open('/tmp/f2l3-csrf.json', encoding='utf-8') as handle:
    print(json.load(handle)['cookieName'])
PY
)"
  CSRF_TOKEN="$(cookie_value "$COOKIE_JAR" "$CSRF_COOKIE")"
}

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
echo 'F2_L3_FRONTEND_GREEN'

printf '%s\n' '=== BACKEND REGRESSION ==='
(
  cd backend
  mvn -B -ntp clean verify
)
echo 'F2_L3_BACKEND_GREEN'

printf '%s\n' '=== DOCKER ==='
docker compose down --remove-orphans
docker compose up --build -d

for i in $(seq 1 30); do
  if curl -fsS http://localhost:8080/api/v1/health >/tmp/f2l3-health.json 2>/dev/null \
    && curl -fsS http://localhost:5173/ >/tmp/f2l3-index.html 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 30 ]; then
    docker compose logs backend frontend
    exit 1
  fi
  sleep 2
done
cat /tmp/f2l3-health.json
echo
grep -Fq '<div id="root"></div>' /tmp/f2l3-index.html

printf '%s\n' '=== LOGIN + CSRF VIA FRONTEND ==='
refresh_csrf
LOGIN_STATUS=$(curl -sS -o /tmp/f2l3-login.json -w '%{http_code}' \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" \
  http://localhost:5173/api/v1/auth/login)
test "$LOGIN_STATUS" = '200'
echo 'F2_L3_AUTH_GREEN'

printf '%s\n' '=== RESPONSÁVEL REAL VIA PROXY ==='
refresh_csrf
RESP_STATUS=$(curl -sS -o /tmp/f2l3-responsible.json -w '%{http_code}' \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data "{\"name\":\"Responsável F2-L3\",\"email\":\"$RESPONSIBLE_EMAIL\",\"position\":\"Analista\",\"secretariatId\":null}" \
  http://localhost:5173/api/v1/responsibles)
test "$RESP_STATUS" = '201'
RESPONSIBLE_ID="$(python3 - <<'PY'
import json
with open('/tmp/f2l3-responsible.json', encoding='utf-8') as handle:
    print(json.load(handle)['id'])
PY
)"
test -n "$RESPONSIBLE_ID"
echo 'F2_L3_RESPONSIBLE_GREEN'

printf '%s\n' '=== PROJETO REAL VIA PROXY ==='
refresh_csrf
PROJECT_STATUS=$(curl -sS -o /tmp/f2l3-project.json -w '%{http_code}' \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data "{\"name\":\"Projeto F2-L3\",\"responsibleIds\":[\"$RESPONSIBLE_ID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$PLANNED_END\",\"actualStart\":null,\"actualEnd\":null}" \
  http://localhost:5173/api/v1/projects)
test "$PROJECT_STATUS" = '201'
PROJECT_ID="$(python3 - <<'PY'
import json
with open('/tmp/f2l3-project.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['status'] == 'NOT_STARTED', data
print(data['id'])
PY
)"
test -n "$PROJECT_ID"

echo 'F2_L3_PROJECT_CREATE_GREEN'

printf '%s\n' '=== LISTAGEM ==='
curl -fsS -b "$COOKIE_JAR" \
  'http://localhost:5173/api/v1/projects?page=0&size=100' >/tmp/f2l3-projects.json
python3 - "$PROJECT_ID" <<'PY'
import json, sys
with open('/tmp/f2l3-projects.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert any(item['id'] == sys.argv[1] for item in data['content']), data
PY
echo 'F2_L3_LIST_GREEN'

printf '%s\n' '=== TRANSIÇÃO LEGÍTIMA NOT_STARTED -> IN_PROGRESS ==='
refresh_csrf
TRANSITION_STATUS=$(curl -sS -o /tmp/f2l3-transition.json -w '%{http_code}' \
  -X PATCH \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data '{"status":"IN_PROGRESS"}' \
  "http://localhost:5173/api/v1/projects/$PROJECT_ID/status")
test "$TRANSITION_STATUS" = '200'
python3 - "$TODAY" <<'PY'
import json, sys
with open('/tmp/f2l3-transition.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['status'] == 'IN_PROGRESS', data
assert data['actualStart'] == sys.argv[1], data
PY
echo 'F2_L3_TRANSITION_GREEN'

printf '%s\n' '=== BLOQUEIO DE DOMÍNIO IN_PROGRESS -> OVERDUE ==='
refresh_csrf
BLOCK_STATUS=$(curl -sS -o /tmp/f2l3-block.json -w '%{http_code}' \
  -X PATCH \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data '{"status":"OVERDUE"}' \
  "http://localhost:5173/api/v1/projects/$PROJECT_ID/status")
test "$BLOCK_STATUS" = '400'
python3 - <<'PY'
import json
with open('/tmp/f2l3-block.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['code'] == 'INVALID_REQUEST', data
assert 'Cannot transition IN_PROGRESS to OVERDUE' in data['detail'], data
PY
echo 'F2_L3_TRANSITION_BLOCK_GREEN'

printf '%s\n' '=== EDIÇÃO REAL ==='
refresh_csrf
UPDATE_STATUS=$(curl -sS -o /tmp/f2l3-update.json -w '%{http_code}' \
  -X PUT \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H 'Content-Type: application/json' \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  --data "{\"name\":\"Projeto F2-L3 atualizado\",\"responsibleIds\":[\"$RESPONSIBLE_ID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$PLANNED_END\",\"actualStart\":\"$TODAY\",\"actualEnd\":null}" \
  "http://localhost:5173/api/v1/projects/$PROJECT_ID")
test "$UPDATE_STATUS" = '200'
python3 - <<'PY'
import json
with open('/tmp/f2l3-update.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['name'] == 'Projeto F2-L3 atualizado', data
assert data['status'] == 'IN_PROGRESS', data
PY
echo 'F2_L3_UPDATE_GREEN'

printf '%s\n' '=== DELETE + CLEANUP FUNCIONAL ==='
refresh_csrf
DELETE_PROJECT=$(curl -sS -o /dev/null -w '%{http_code}' \
  -X DELETE -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  "http://localhost:5173/api/v1/projects/$PROJECT_ID")
test "$DELETE_PROJECT" = '204'

refresh_csrf
DELETE_RESPONSIBLE=$(curl -sS -o /dev/null -w '%{http_code}' \
  -X DELETE -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H "$CSRF_HEADER: $CSRF_TOKEN" \
  "http://localhost:5173/api/v1/responsibles/$RESPONSIBLE_ID")
test "$DELETE_RESPONSIBLE" = '204'
echo 'F2_L3_CRUD_GREEN'

printf '%s\n' '=== STATIC SAFETY + GIT ==='
! grep -RInE 'localStorage|sessionStorage' frontend/src
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
    if (entry.isDirectory()) {
      collect(absolute);
    } else if (entry.name.endsWith(".ts") || entry.name.endsWith(".tsx")) {
      sourceFiles.push(absolute);
    }
  }
}

collect(sourceRoot);
const violations = [];

for (const filename of sourceFiles) {
  const source = ts.createSourceFile(
    filename,
    fs.readFileSync(filename, "utf8"),
    ts.ScriptTarget.Latest,
    true,
    filename.endsWith(".tsx") ? ts.ScriptKind.TSX : ts.ScriptKind.TS,
  );

  function visit(node) {
    if (
      node.kind === ts.SyntaxKind.AnyKeyword ||
      node.kind === ts.SyntaxKind.AsExpression ||
      node.kind === ts.SyntaxKind.NonNullExpression
    ) {
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

console.log(`F2_L3_STRICT_TYPES_GREEN ${sourceFiles.length}`);
NODE
)
! test -e frontend/src/KanbanBoard.tsx
! test -e frontend/src/KanbanBoard.test.tsx
test -f frontend/src/features/kanban/KanbanBoard.tsx
test -f frontend/src/features/auth/LoginPage.tsx
test -f frontend/src/features/auth/ProtectedHome.tsx
test -f frontend/src/features/dashboard/DashboardPage.tsx
test "$(wc -l < frontend/src/App.tsx)" -le 40
git diff --check
docker compose ps

printf '%s\n' '=== CLEANUP ==='
docker compose exec -T db sh -lc \
  "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM app_users WHERE email = '$APP_ADMIN_EMAIL'\"" \
  >/dev/null
docker compose down --remove-orphans
rm -f "$COOKIE_JAR" /tmp/f2l3-*.json /tmp/f2l3-*.html

printf '%s\n' '=== F2-L3 / F2 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige `F2_L3_FRONTEND_GREEN`, `F2_L3_BACKEND_GREEN`, `F2_L3_AUTH_GREEN`, `F2_L3_RESPONSIBLE_GREEN`, `F2_L3_PROJECT_CREATE_GREEN`, `F2_L3_LIST_GREEN`, `F2_L3_TRANSITION_GREEN`, `F2_L3_TRANSITION_BLOCK_GREEN`, `F2_L3_UPDATE_GREEN`, `F2_L3_CRUD_GREEN`, `=== F2-L3 / F2 GREEN ===` e `Resultado: exit code 0`.
