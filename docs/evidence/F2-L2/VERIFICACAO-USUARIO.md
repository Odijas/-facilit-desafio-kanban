# F2-L2 — VERIFICAÇÃO DO USUÁRIO

Execute no repositório local após aplicar o pacote. O `set -euo pipefail` fica isolado em um shell filho.

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban

export APP_ADMIN_EMAIL="f2l2-admin-$(date +%s)-$$@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export SESSION_COOKIE_SECURE=false

cookie_value() {
  local jar="$1"
  local name="$2"
  awk -v name="$name" '$6 == name { value=$7 } END { if (value == "") exit 1; print value }' "$jar"
}

printf '%s\n' '=== FRONTEND: LINT + TYPECHECK + TEST + BUILD ==='
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

echo 'F2_L2_FRONTEND_GREEN'

printf '%s\n' '=== BACKEND REGRESSION ==='
(
  cd backend
  mvn -B -ntp clean verify
)

echo 'F2_L2_BACKEND_GREEN'

printf '%s\n' '=== DOCKER + FRONTEND PROXY ==='
docker compose down --remove-orphans
docker compose up --build -d

for i in $(seq 1 30); do
  if curl -fsS http://localhost:8080/api/v1/health >/tmp/f2l2-health.json 2>/dev/null \
    && curl -fsS http://localhost:5173/ >/tmp/f2l2-index.html 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 30 ]; then
    docker compose logs backend frontend
    exit 1
  fi
  sleep 2
done

cat /tmp/f2l2-health.json
echo

grep -Fq '<div id="root"></div>' /tmp/f2l2-index.html

printf '%s\n' '=== LOGIN REAL ATRAVÉS DA ORIGEM DO FRONTEND ==='
rm -f /tmp/f2l2.cookies
curl -fsS -c /tmp/f2l2.cookies \
  http://localhost:5173/api/v1/auth/csrf >/tmp/f2l2-csrf.json
XSRF=$(cookie_value /tmp/f2l2.cookies XSRF-TOKEN)

LOGIN_STATUS=$(curl -sS -o /tmp/f2l2-login.json -w '%{http_code}' \
  -b /tmp/f2l2.cookies -c /tmp/f2l2.cookies \
  -H 'Content-Type: application/json' \
  -H "X-XSRF-TOKEN: $XSRF" \
  --data "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" \
  http://localhost:5173/api/v1/auth/login)
test "$LOGIN_STATUS" = '200'

ME_STATUS=$(curl -sS -o /tmp/f2l2-me.json -w '%{http_code}' \
  -b /tmp/f2l2.cookies http://localhost:5173/api/v1/auth/me)
test "$ME_STATUS" = '200'
python3 - "$APP_ADMIN_EMAIL" <<'PY_ME'
import json, sys
with open('/tmp/f2l2-me.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['email'] == sys.argv[1], data
assert 'ROLE_ADMIN' in data['authorities'], data
PY_ME

echo 'F2_L2_PROXY_AUTH_GREEN'

printf '%s\n' '=== LOGOUT REAL ATRAVÉS DA ORIGEM DO FRONTEND ==='
curl -fsS -b /tmp/f2l2.cookies -c /tmp/f2l2.cookies \
  http://localhost:5173/api/v1/auth/csrf >/tmp/f2l2-csrf-logout.json
XSRF=$(cookie_value /tmp/f2l2.cookies XSRF-TOKEN)
LOGOUT_STATUS=$(curl -sS -o /dev/null -w '%{http_code}' \
  -X POST \
  -b /tmp/f2l2.cookies -c /tmp/f2l2.cookies \
  -H "X-XSRF-TOKEN: $XSRF" \
  http://localhost:5173/api/v1/auth/logout)
test "$LOGOUT_STATUS" = '204'

AFTER_LOGOUT=$(curl -sS -o /tmp/f2l2-after-logout.json -w '%{http_code}' \
  -b /tmp/f2l2.cookies http://localhost:5173/api/v1/auth/me)
test "$AFTER_LOGOUT" = '401'

echo 'F2_L2_PROXY_LOGOUT_GREEN'

printf '%s\n' '=== STATIC SAFETY + GIT ==='
! grep -RInE 'localStorage|sessionStorage' frontend/src
! grep -RInE '\bany\b|\bas\b' frontend/src/api/auth.ts frontend/src/App.tsx
git diff --check
docker compose ps

printf '%s\n' '=== CLEANUP ==='
docker compose exec -T db sh -lc \
  "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM app_users WHERE email = '$APP_ADMIN_EMAIL'\"" \
  >/dev/null
docker compose down --remove-orphans
rm -f /tmp/f2l2*.json /tmp/f2l2*.html /tmp/f2l2.cookies

printf '%s\n' '=== F2-L2 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige `F2_L2_FRONTEND_GREEN`, `F2_L2_BACKEND_GREEN`, `F2_L2_PROXY_AUTH_GREEN`, `F2_L2_PROXY_LOGOUT_GREEN`, `=== F2-L2 GREEN ===` e `Resultado: exit code 0`.
