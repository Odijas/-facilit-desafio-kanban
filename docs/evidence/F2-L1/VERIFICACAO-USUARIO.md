# F2-L1 — VERIFICAÇÃO DO USUÁRIO

Execute no repositório local prevalente após aplicar o pacote F2-L1. O `set -euo pipefail` fica isolado em um `bash` filho para não encerrar o terminal interativo. O gate usa credenciais efêmeras geradas localmente; não imprime a senha e encerra o Compose ao final do caminho GREEN.

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban

export APP_ADMIN_EMAIL="f2-admin-$(date +%s)-$$@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export SESSION_COOKIE_SECURE=false

cookie_value() {
  local jar="$1"
  local name="$2"
  awk -v name="$name" '$6 == name { value=$7 } END { if (value == "") exit 1; print value }' "$jar"
}

printf '%s\n' '=== BACKEND: UNIT + INTEGRATION/API/SECURITY ==='
(
  cd backend
  mvn -B -ntp clean verify
)

KANBAN_REPORT=$(find backend/target/failsafe-reports -type f -name '*KanbanApiIT.txt' -print -quit)
SECURITY_REPORT=$(find backend/target/failsafe-reports -type f -name '*SecurityApiIT.txt' -print -quit)
test -n "$KANBAN_REPORT"
test -n "$SECURITY_REPORT"
grep -Fq 'Tests run: 4, Failures: 0, Errors: 0, Skipped: 0' "$KANBAN_REPORT"
grep -Fq 'Tests run: 3, Failures: 0, Errors: 0, Skipped: 0' "$SECURITY_REPORT"
echo 'F2_L1_SECURITY_IT_GREEN'

printf '%s\n' '=== FRONTEND REGRESSION ==='
(
  cd frontend
  corepack enable
  corepack prepare pnpm@12.5.1 --activate
  pnpm install --frozen-lockfile
  pnpm lint
  pnpm typecheck
  pnpm test
  pnpm build
)

printf '%s\n' '=== DOCKER + FLYWAY V4 ==='
docker compose down --remove-orphans
docker compose up --build -d

for i in $(seq 1 30); do
  if curl -fsS http://localhost:8080/api/v1/health >/tmp/f2l1-health.json 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 30 ]; then
    docker compose logs backend
    exit 1
  fi
  sleep 2
done
cat /tmp/f2l1-health.json
echo

docker compose exec -T db sh -lc \
  'psql -X -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "SELECT version || chr(58) || success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1"' \
  | grep -qx '4:true'
docker compose exec -T db sh -lc \
  'psql -X -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "SELECT count(*) FROM pg_indexes WHERE schemaname = '\''public'\'' AND indexname = '\''uq_app_users_email_lower'\''"' \
  | grep -qx '1'

echo 'F2_L1_FLYWAY_GREEN'

printf '%s\n' '=== ANÔNIMO: HEALTH PÚBLICO + NEGÓCIO PROTEGIDO ==='
HEALTH_STATUS=$(curl -sS -o /tmp/f2l1-health-public.json -w '%{http_code}' http://localhost:8080/api/v1/health)
test "$HEALTH_STATUS" = '200'

ANON_STATUS=$(curl -sS -o /tmp/f2l1-anon.json -w '%{http_code}' \
  'http://localhost:8080/api/v1/projects?page=0&size=20')
test "$ANON_STATUS" = '401'
python3 - <<'PY_ANON'
import json
with open('/tmp/f2l1-anon.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['code'] == 'UNAUTHORIZED', data
assert 'trace' not in data and 'stackTrace' not in data, data
PY_ANON

rm -f /tmp/f2l1-anon.cookies
curl -fsS -c /tmp/f2l1-anon.cookies http://localhost:8080/api/v1/auth/csrf >/tmp/f2l1-anon-csrf.json
ANON_XSRF=$(cookie_value /tmp/f2l1-anon.cookies XSRF-TOKEN)
GQL_ANON_STATUS=$(curl -sS -o /tmp/f2l1-gql-anon.json -w '%{http_code}' \
  -b /tmp/f2l1-anon.cookies \
  -H 'Content-Type: application/json' \
  -H "X-XSRF-TOKEN: $ANON_XSRF" \
  --data '{"query":"{ health { status } }"}' \
  http://localhost:8080/graphql)
test "$GQL_ANON_STATUS" = '401'
python3 -c 'import json; data=json.load(open("/tmp/f2l1-gql-anon.json")); assert data["code"] == "UNAUTHORIZED", data'

echo 'F2_L1_ANONYMOUS_BLOCKED'

printf '%s\n' '=== LOGIN INVÁLIDO: ERRO GENÉRICO ==='
rm -f /tmp/f2l1-bad.cookies
curl -fsS -c /tmp/f2l1-bad.cookies http://localhost:8080/api/v1/auth/csrf >/tmp/f2l1-bad-csrf.json
BAD_XSRF=$(cookie_value /tmp/f2l1-bad.cookies XSRF-TOKEN)
BAD_STATUS=$(curl -sS -o /tmp/f2l1-bad-login.json -w '%{http_code}' \
  -b /tmp/f2l1-bad.cookies -c /tmp/f2l1-bad.cookies \
  -H 'Content-Type: application/json' \
  -H "X-XSRF-TOKEN: $BAD_XSRF" \
  --data "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"wrong-password\"}" \
  http://localhost:8080/api/v1/auth/login)
test "$BAD_STATUS" = '401'
python3 - <<'PY_BAD'
import json
with open('/tmp/f2l1-bad-login.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['code'] == 'UNAUTHORIZED', data
assert data['detail'] == 'Invalid email or password', data
assert 'trace' not in data and 'stackTrace' not in data, data
PY_BAD

printf '%s\n' '=== LOGIN LEGÍTIMO + SESSÃO ==='
rm -f /tmp/f2l1-auth.cookies /tmp/f2l1-login.headers
curl -fsS -c /tmp/f2l1-auth.cookies http://localhost:8080/api/v1/auth/csrf >/tmp/f2l1-auth-csrf.json
LOGIN_XSRF=$(cookie_value /tmp/f2l1-auth.cookies XSRF-TOKEN)
LOGIN_STATUS=$(curl -sS -o /tmp/f2l1-login.json -D /tmp/f2l1-login.headers -w '%{http_code}' \
  -b /tmp/f2l1-auth.cookies -c /tmp/f2l1-auth.cookies \
  -H 'Content-Type: application/json' \
  -H "X-XSRF-TOKEN: $LOGIN_XSRF" \
  --data "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" \
  http://localhost:8080/api/v1/auth/login)
test "$LOGIN_STATUS" = '200'
python3 - "$APP_ADMIN_EMAIL" <<'PY_LOGIN'
import json, sys
with open('/tmp/f2l1-login.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['email'] == sys.argv[1], data
assert 'ROLE_ADMIN' in data['authorities'], data
PY_LOGIN
tr -d '\r' </tmp/f2l1-login.headers >/tmp/f2l1-login.headers.clean
grep -Eiq '^Set-Cookie: JSESSIONID=.*HttpOnly' /tmp/f2l1-login.headers.clean
grep -Eiq '^Set-Cookie: JSESSIONID=.*SameSite=Lax' /tmp/f2l1-login.headers.clean
SESSION_VALUE=$(cookie_value /tmp/f2l1-auth.cookies JSESSIONID)
test -n "$SESSION_VALUE"

ME_STATUS=$(curl -sS -o /tmp/f2l1-me.json -w '%{http_code}' \
  -b /tmp/f2l1-auth.cookies http://localhost:8080/api/v1/auth/me)
test "$ME_STATUS" = '200'
python3 - "$APP_ADMIN_EMAIL" <<'PY_ME'
import json, sys
with open('/tmp/f2l1-me.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['email'] == sys.argv[1], data
assert 'ROLE_ADMIN' in data['authorities'], data
PY_ME

echo 'F2_L1_SESSION_GREEN'

printf '%s\n' '=== CSRF: ATAQUE BLOQUEADO ==='
NO_CSRF_STATUS=$(curl -sS -o /tmp/f2l1-no-csrf.json -w '%{http_code}' \
  -b /tmp/f2l1-auth.cookies \
  -H 'Content-Type: application/json' \
  --data '{"name":"CSRF bloqueado","email":"csrf-blocked@example.invalid","position":"Analista","secretariatId":null}' \
  http://localhost:8080/api/v1/responsibles)
test "$NO_CSRF_STATUS" = '403'
python3 -c 'import json; data=json.load(open("/tmp/f2l1-no-csrf.json")); assert data["code"] == "FORBIDDEN", data'

printf '%s\n' '=== CSRF: CAMINHO LEGÍTIMO REST + GRAPHQL ==='
curl -fsS -b /tmp/f2l1-auth.cookies -c /tmp/f2l1-auth.cookies \
  http://localhost:8080/api/v1/auth/csrf >/tmp/f2l1-fresh-csrf.json
XSRF=$(cookie_value /tmp/f2l1-auth.cookies XSRF-TOKEN)

RESP_EMAIL="f2-security-$(date +%s)-$$@example.invalid"
RESP=$(curl -fsS \
  -b /tmp/f2l1-auth.cookies \
  -H 'Content-Type: application/json' \
  -H "X-XSRF-TOKEN: $XSRF" \
  --data "{\"name\":\"F2 Segurança\",\"email\":\"$RESP_EMAIL\",\"position\":\"Analista\",\"secretariatId\":null}" \
  http://localhost:8080/api/v1/responsibles)
RID=$(printf '%s' "$RESP" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')
test -n "$RID"

curl -fsS \
  -b /tmp/f2l1-auth.cookies \
  -H 'Content-Type: application/json' \
  -H "X-XSRF-TOKEN: $XSRF" \
  --data '{"query":"{ health { status } }"}' \
  http://localhost:8080/graphql \
  | python3 -c 'import json,sys; data=json.load(sys.stdin); assert not data.get("errors"), data; assert data["data"]["health"]["status"] == "UP", data'

echo 'F2_L1_CSRF_GREEN'

printf '%s\n' '=== HASH DA SENHA NO BANCO ==='
HASH=$(docker compose exec -T db sh -lc \
  "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -Atc \"SELECT password_hash FROM app_users WHERE email = '$APP_ADMIN_EMAIL'\"")
test -n "$HASH"
test "$HASH" != "$APP_ADMIN_PASSWORD"
case "$HASH" in
  '{bcrypt}$2'*) ;;
  *) echo 'Hash do administrador não está no formato delegado/bcrypt esperado'; exit 1 ;;
esac
echo 'PASSWORD_HASH_GREEN'

printf '%s\n' '=== LOGS: SEM CREDENCIAIS/PII TESTADAS ==='
docker compose logs backend >/tmp/f2l1-backend.log
if grep -Fq -- "$APP_ADMIN_PASSWORD" /tmp/f2l1-backend.log; then
  echo 'Senha encontrada nos logs'; exit 1
fi
if grep -Fq -- "$APP_ADMIN_EMAIL" /tmp/f2l1-backend.log; then
  echo 'E-mail administrativo encontrado nos logs'; exit 1
fi
if grep -Fq -- 'wrong-password' /tmp/f2l1-backend.log; then
  echo 'Credencial inválida encontrada nos logs'; exit 1
fi
echo 'NO_SECRET_LOG_GREEN'

printf '%s\n' '=== CLEANUP DO DADO FUNCIONAL + LOGOUT ==='
DELETE_STATUS=$(curl -sS -o /dev/null -w '%{http_code}' \
  -X DELETE \
  -b /tmp/f2l1-auth.cookies \
  -H "X-XSRF-TOKEN: $XSRF" \
  "http://localhost:8080/api/v1/responsibles/$RID")
test "$DELETE_STATUS" = '204'

OLD_SESSION_COOKIE="JSESSIONID=$SESSION_VALUE"
LOGOUT_STATUS=$(curl -sS -o /dev/null -w '%{http_code}' \
  -X POST \
  -b /tmp/f2l1-auth.cookies -c /tmp/f2l1-auth.cookies \
  -H "X-XSRF-TOKEN: $XSRF" \
  http://localhost:8080/api/v1/auth/logout)
test "$LOGOUT_STATUS" = '204'

AFTER_LOGOUT_STATUS=$(curl -sS -o /tmp/f2l1-after-logout.json -w '%{http_code}' \
  -H "Cookie: $OLD_SESSION_COOKIE" \
  http://localhost:8080/api/v1/auth/me)
test "$AFTER_LOGOUT_STATUS" = '401'
python3 -c 'import json; data=json.load(open("/tmp/f2l1-after-logout.json")); assert data["code"] == "UNAUTHORIZED", data'
echo 'F2_L1_LOGOUT_GREEN'

printf '%s\n' '=== REGRESSÃO PÚBLICA + GIT ==='
curl -fsS http://localhost:8080/api/v1/health
echo
curl -fsS -o /dev/null -w 'HTTP %{http_code}\n' http://localhost:5173/ | grep -qx 'HTTP 200'
git diff --check
docker compose ps

printf '%s\n' '=== LIMPEZA DO ADMIN EFÊMERO ==='
docker compose exec -T db sh -lc \
  "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM app_users WHERE email = '$APP_ADMIN_EMAIL'\"" \
  >/dev/null
docker compose down --remove-orphans
rm -f /tmp/f2l1-*.json /tmp/f2l1-*.headers /tmp/f2l1-*.headers.clean /tmp/f2l1-*.cookies /tmp/f2l1-backend.log

printf '%s\n' '=== F2-L1 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige, no mínimo, `F2_L1_SECURITY_IT_GREEN`, `F2_L1_ANONYMOUS_BLOCKED`, `F2_L1_SESSION_GREEN`, `F2_L1_CSRF_GREEN`, `PASSWORD_HASH_GREEN`, `NO_SECRET_LOG_GREEN`, `F2_L1_LOGOUT_GREEN`, `=== F2-L1 GREEN ===` e `Resultado: exit code 0`.
