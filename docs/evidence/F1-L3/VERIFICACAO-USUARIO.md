# F1-L3 — VERIFICAÇÃO DO USUÁRIO

Execute no repositório local prevalente após aplicar o pacote F1-L3. O `set -euo pipefail` fica isolado em um `bash` filho para não encerrar o terminal interativo.

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban

printf '%s\n' '=== BACKEND: UNIT + INTEGRATION/API/GRAPHQL ==='
(
  cd backend
  mvn -B -ntp clean verify
)

IT_REPORT=$(find backend/target/failsafe-reports -type f -name '*KanbanApiIT.txt' -print -quit)
test -n "$IT_REPORT"
grep -Fq 'Tests run: 4, Failures: 0, Errors: 0, Skipped: 0' "$IT_REPORT"
echo 'F1_L3_TESTCONTAINERS_GREEN'

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

printf '%s\n' '=== DOCKER ==='
docker compose down --remove-orphans
docker compose up --build -d

for i in $(seq 1 30); do
  if curl -fsS http://localhost:8080/api/v1/health >/tmp/f1l3-health.json 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 30 ]; then
    docker compose logs backend
    exit 1
  fi
  sleep 2
done
cat /tmp/f1l3-health.json
echo

printf '%s\n' '=== FLYWAY V3 + INDEXES NO COMPOSE ==='
docker compose exec -T db sh -lc \
  'psql -X -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "SELECT version || chr(58) || success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1"' \
  | grep -qx '3:true'
docker compose exec -T db sh -lc \
  'psql -X -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "SELECT count(*) FROM pg_indexes WHERE schemaname = '\''public'\'' AND indexname IN ('\''ix_projects_name_id'\'', '\''ix_projects_status_name_id'\'', '\''ix_responsibles_name_id'\'')"' \
  | grep -qx '3'

printf '%s\n' '=== OPENAPI + SWAGGER ==='
curl -fsS http://localhost:8080/api-docs > /tmp/f1l3-openapi.json
python3 - <<'PY_OPENAPI'
import json
with open('/tmp/f1l3-openapi.json', encoding='utf-8') as handle:
    data = json.load(handle)
assert data['openapi'].startswith('3.'), data.get('openapi')
assert '/api/v1/projects' in data['paths']
assert '/api/v1/responsibles' in data['paths']
assert data['components']['schemas']['ProjectRequest']['properties']['name']['example'] == 'Implantação do portal'
assert data['components']['schemas']['ResponsibleRequest']['properties']['email']['example'] == 'maria.silva@example.com'
print('OPENAPI_EXAMPLES_GREEN')
PY_OPENAPI
SWAGGER_STATUS=$(curl -sS -o /tmp/f1l3-swagger.html -w '%{http_code}' http://localhost:8080/swagger-ui.html)
case "$SWAGGER_STATUS" in
  200|301|302|303|307|308) ;;
  *) echo "Swagger HTTP inesperado: $SWAGGER_STATUS"; exit 1 ;;
esac

printf '%s\n' '=== REST: VALIDAÇÃO + ERROS + CAMINHO LEGÍTIMO ==='
INVALID_STATUS=$(curl -sS -o /tmp/f1l3-invalid.json -w '%{http_code}' \
  -X POST http://localhost:8080/api/v1/responsibles \
  -H 'Content-Type: application/json' \
  --data '{"name":"Inválido","email":"not-an-email","position":"Analista","secretariatId":null}')
test "$INVALID_STATUS" = '400'
python3 - <<'PY_INVALID'
import json
with open('/tmp/f1l3-invalid.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert data['code'] == 'VALIDATION_ERROR', data
assert any(item['field'] == 'email' for item in data['violations']), data
PY_INVALID

SUFFIX=$(date +%s)
EMAIL="f1l3-${SUFFIX}@example.com"
RESP=$(curl -fsS -X POST http://localhost:8080/api/v1/responsibles \
  -H 'Content-Type: application/json' \
  --data "{\"name\":\"Responsável F1-L3\",\"email\":\"$EMAIL\",\"position\":\"Analista\",\"secretariatId\":null}")
RID=$(printf '%s' "$RESP" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

DUP_STATUS=$(curl -sS -o /tmp/f1l3-dup.json -w '%{http_code}' \
  -X POST http://localhost:8080/api/v1/responsibles \
  -H 'Content-Type: application/json' \
  --data "{\"name\":\"Duplicado\",\"email\":\"${EMAIL^^}\",\"position\":\"Gestor\",\"secretariatId\":null}")
test "$DUP_STATUS" = '409'
python3 -c 'import json; data=json.load(open("/tmp/f1l3-dup.json")); assert data["code"] == "CONFLICT", data'

TODAY=$(date -u +%F)
FUTURE=$(date -u -d '+10 days' +%F)
PROJECT=$(curl -fsS -X POST http://localhost:8080/api/v1/projects \
  -H 'Content-Type: application/json' \
  --data "{\"name\":\"Projeto F1-L3\",\"responsibleIds\":[\"$RID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$FUTURE\",\"actualStart\":null,\"actualEnd\":null}")
PID=$(printf '%s' "$PROJECT" | python3 -c 'import json,sys; data=json.load(sys.stdin); assert data["status"] == "NOT_STARTED", data; print(data["id"])')

MISSING_ID=$(python3 -c 'import uuid; print(uuid.uuid4())')
MISSING_STATUS=$(curl -sS -o /tmp/f1l3-missing.json -w '%{http_code}' "http://localhost:8080/api/v1/projects/$MISSING_ID")
test "$MISSING_STATUS" = '404'
python3 -c 'import json; data=json.load(open("/tmp/f1l3-missing.json")); assert data["code"] == "RESOURCE_NOT_FOUND", data'

printf '%s\n' '=== GRAPHQL: REUSO + VALIDAÇÃO ==='
python3 - "$PID" <<'PY_GQL' > /tmp/f1l3-gql.json
import json, sys
print(json.dumps({
    'query': 'mutation($id: ID!, $status: ProjectStatus!) { transitionProject(id: $id, status: $status) { id status actualStart } }',
    'variables': {'id': sys.argv[1], 'status': 'IN_PROGRESS'}
}))
PY_GQL
curl -fsS -H 'Content-Type: application/json' --data-binary @/tmp/f1l3-gql.json http://localhost:8080/graphql \
  | python3 -c 'import json,sys; data=json.load(sys.stdin); assert not data.get("errors"), data; assert data["data"]["transitionProject"]["status"] == "IN_PROGRESS", data'

cat > /tmp/f1l3-gql-invalid.json <<'JSON_GQL'
{"query":"mutation($input: ResponsibleInput!) { createResponsible(input: $input) { id } }","variables":{"input":{"name":"Inválido","email":"invalid-email","position":"Analista"}}}
JSON_GQL
curl -fsS -H 'Content-Type: application/json' --data-binary @/tmp/f1l3-gql-invalid.json http://localhost:8080/graphql \
  | python3 -c 'import json,sys; data=json.load(sys.stdin); assert data["errors"][0]["extensions"]["code"] == "VALIDATION_ERROR", data'

printf '%s\n' '=== CLEANUP ==='
curl -fsS -o /dev/null -w '%{http_code}\n' -X DELETE "http://localhost:8080/api/v1/projects/$PID" | grep -qx '204'
curl -fsS -o /dev/null -w '%{http_code}\n' -X DELETE "http://localhost:8080/api/v1/responsibles/$RID" | grep -qx '204'

printf '%s\n' '=== REGRESSÃO HEALTH + FRONTEND ==='
curl -fsS http://localhost:8080/api/v1/health
echo
curl -fsS -H 'Content-Type: application/json' \
  --data '{"query":"{ health { status } }"}' \
  http://localhost:8080/graphql
echo
curl -fsS -o /dev/null -w 'HTTP %{http_code}\n' http://localhost:5173/

printf '%s\n' '=== GIT + CONTAINERS ==='
git diff --check
docker compose ps

printf '%s\n' '=== F1-L3 / F1 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige, no mínimo, `F1_L3_TESTCONTAINERS_GREEN`, `OPENAPI_EXAMPLES_GREEN`, `=== F1-L3 / F1 GREEN ===` e `Resultado: exit code 0`.
