# F1-L2 — VERIFICAÇÃO DO USUÁRIO

Execute o bloco abaixo no repositório local prevalente após aplicar o pacote F1-L2. O terminal permanece aberto em caso de falha porque o `set -euo pipefail` roda dentro de um `bash` filho.

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban

printf '%s\n' '=== BACKEND TESTS ==='
(
  cd backend
  mvn -B -ntp clean verify
)

printf '%s\n' '=== DOCKER ==='
docker compose down --remove-orphans
docker compose up --build -d

for i in $(seq 1 30); do
  if curl -fsS http://localhost:8080/api/v1/health >/tmp/f1l2-health.json 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 30 ]; then
    docker compose logs backend
    exit 1
  fi
  sleep 2
done
cat /tmp/f1l2-health.json
echo

gql() {
  local query="$1"
  local variables="$2"
  python3 - "$query" "$variables" <<'PY' | curl -fsS \
    -H 'Content-Type: application/json' \
    --data-binary @- \
    http://localhost:8080/graphql
import json
import sys
print(json.dumps({"query": sys.argv[1], "variables": json.loads(sys.argv[2])}))
PY
}

assert_gql_clean() {
  python3 -c 'import json,sys; data=json.load(sys.stdin); assert not data.get("errors"), data'
}

SUFFIX=$(date +%s)
TODAY=$(date +%F)
FUTURE_START=$(date -d '+2 days' +%F)
FUTURE_END=$(date -d '+10 days' +%F)
EMAIL="f1l2-${SUFFIX}@example.com"

printf '%s\n' '=== PREPARA RESPONSÁVEL ==='
RESP=$(curl -fsS -X POST http://localhost:8080/api/v1/responsibles \
  -H 'Content-Type: application/json' \
  --data "{\"name\":\"Responsável F1-L2\",\"email\":\"$EMAIL\",\"position\":\"Analista\",\"secretariatId\":null}")
RID=$(printf '%s' "$RESP" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

printf '%s\n' '=== REST: TRANSIÇÃO LEGÍTIMA ==='
PROJECT_PAYLOAD=$(printf '{"name":"Projeto REST F1-L2","responsibleIds":["%s"],"plannedStart":"%s","plannedEnd":"%s","actualStart":null,"actualEnd":null}' "$RID" "$TODAY" "$FUTURE_END")
PROJECT=$(curl -fsS -X POST http://localhost:8080/api/v1/projects \
  -H 'Content-Type: application/json' \
  --data "$PROJECT_PAYLOAD")
PID=$(printf '%s' "$PROJECT" | python3 -c 'import json,sys; data=json.load(sys.stdin); assert data["status"] == "NOT_STARTED"; print(data["id"])')

TRANSITIONED=$(curl -fsS -X PATCH "http://localhost:8080/api/v1/projects/$PID/status" \
  -H 'Content-Type: application/json' \
  --data '{"status":"IN_PROGRESS"}')
printf '%s' "$TRANSITIONED" | python3 -c 'import json,sys; data=json.load(sys.stdin); assert data["status"] == "IN_PROGRESS", data; assert data["actualStart"] == sys.argv[1], data' "$TODAY"

printf '%s\n' '=== REST: LISTAGEM POR STATUS ==='
curl -fsS 'http://localhost:8080/api/v1/projects?status=IN_PROGRESS&page=0&size=20' \
  | python3 -c 'import json,sys; data=json.load(sys.stdin); assert any(item["id"] == sys.argv[1] and item["status"] == "IN_PROGRESS" for item in data["content"]), data' "$PID"

printf '%s\n' '=== REST: BLOQUEIO COM MENSAGEM CLARA ==='
BLOCK_STATUS=$(curl -sS -o /tmp/f1l2-block.json -w '%{http_code}' \
  -X PATCH "http://localhost:8080/api/v1/projects/$PID/status" \
  -H 'Content-Type: application/json' \
  --data '{"status":"OVERDUE"}')
test "$BLOCK_STATUS" = '400'
python3 - <<'PY'
import json
with open('/tmp/f1l2-block.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert 'remove actualStart' in data.get('detail', ''), data
PY

printf '%s\n' '=== REST: NÃO PODE MARCAR ATRASADO ANTES DO INÍCIO ==='
FUTURE_PAYLOAD=$(printf '{"name":"Projeto Futuro F1-L2","responsibleIds":["%s"],"plannedStart":"%s","plannedEnd":"%s","actualStart":null,"actualEnd":null}' "$RID" "$FUTURE_START" "$FUTURE_END")
FUTURE_PROJECT=$(curl -fsS -X POST http://localhost:8080/api/v1/projects \
  -H 'Content-Type: application/json' \
  --data "$FUTURE_PAYLOAD")
FUTURE_PID=$(printf '%s' "$FUTURE_PROJECT" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')
EARLY_STATUS=$(curl -sS -o /tmp/f1l2-early.json -w '%{http_code}' \
  -X PATCH "http://localhost:8080/api/v1/projects/$FUTURE_PID/status" \
  -H 'Content-Type: application/json' \
  --data '{"status":"OVERDUE"}')
test "$EARLY_STATUS" = '400'
python3 - <<'PY'
import json
with open('/tmp/f1l2-early.json', encoding='utf-8') as handle:
    data=json.load(handle)
assert 'before plannedStart' in data.get('detail', ''), data
PY

printf '%s\n' '=== GRAPHQL: TRANSIÇÃO + FILTRO ==='
CREATE_QUERY='mutation($input: ProjectInput!) { createProject(input: $input) { id status actualStart actualEnd } }'
CREATE_VARS=$(python3 - "$RID" "$TODAY" "$FUTURE_END" <<'PY'
import json
import sys
print(json.dumps({"input": {
    "name": "Projeto GraphQL F1-L2",
    "responsibleIds": [sys.argv[1]],
    "plannedStart": sys.argv[2],
    "plannedEnd": sys.argv[3],
    "actualStart": sys.argv[2],
    "actualEnd": sys.argv[2],
}}))
PY
)
GQL_CREATED=$(gql "$CREATE_QUERY" "$CREATE_VARS")
printf '%s' "$GQL_CREATED" | assert_gql_clean
GQL_PID=$(printf '%s' "$GQL_CREATED" | python3 -c 'import json,sys; data=json.load(sys.stdin)["data"]["createProject"]; assert data["status"] == "COMPLETED"; print(data["id"])')

TRANSITION_QUERY='mutation($id: ID!, $status: ProjectStatus!) { transitionProject(id: $id, status: $status) { id status actualEnd } }'
GQL_TRANSITIONED=$(gql "$TRANSITION_QUERY" "{\"id\":\"$GQL_PID\",\"status\":\"IN_PROGRESS\"}")
printf '%s' "$GQL_TRANSITIONED" | assert_gql_clean
printf '%s' "$GQL_TRANSITIONED" | python3 -c 'import json,sys; data=json.load(sys.stdin)["data"]["transitionProject"]; assert data["status"] == "IN_PROGRESS" and data["actualEnd"] is None, data'

FILTER_QUERY='query { projects(status: IN_PROGRESS, page: 0, size: 20) { content { id status } page size } }'
GQL_FILTER=$(gql "$FILTER_QUERY" '{}')
printf '%s' "$GQL_FILTER" | assert_gql_clean
printf '%s' "$GQL_FILTER" | python3 -c 'import json,sys; data=json.load(sys.stdin)["data"]["projects"]; assert any(item["id"] == sys.argv[1] and item["status"] == "IN_PROGRESS" for item in data["content"]), data' "$GQL_PID"

printf '%s\n' '=== FINALIZA TRANSIÇÃO REST ==='
COMPLETED=$(curl -fsS -X PATCH "http://localhost:8080/api/v1/projects/$PID/status" \
  -H 'Content-Type: application/json' \
  --data '{"status":"COMPLETED"}')
printf '%s' "$COMPLETED" | python3 -c 'import json,sys; assert json.load(sys.stdin)["status"] == "COMPLETED"'

printf '%s\n' '=== CLEANUP ==='
for PROJECT_ID in "$PID" "$FUTURE_PID" "$GQL_PID"; do
  curl -fsS -o /dev/null -w '%{http_code}\n' -X DELETE "http://localhost:8080/api/v1/projects/$PROJECT_ID" | grep -qx '204'
done
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

printf '%s\n' '=== F1-L2 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige `=== F1-L2 GREEN ===` e `Resultado: exit code 0`.
