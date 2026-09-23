# F1-L1 — Verificação obrigatória no ambiente do usuário

Este gate recria o volume PostgreSQL para provar V1 + V2 do zero. Não execute se houver dados locais que precisem ser preservados.

Execute a partir da raiz do projeto. O `set -euo pipefail` roda em um Bash filho e não fecha o terminal interativo.

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban

gql() {
  local query="$1"
  local variables="$2"
  local payload
  payload=$(python3 - "$query" "$variables" <<'PY'
import json
import sys
print(json.dumps({"query": sys.argv[1], "variables": json.loads(sys.argv[2])}))
PY
)
  curl -fsS \
    -H 'Content-Type: application/json' \
    --data "$payload" \
    http://localhost:8080/graphql
}

assert_gql_clean() {
  python3 -c 'import json,sys; data=json.load(sys.stdin); assert "errors" not in data, data.get("errors")'
}

echo '=== FRONTEND REGRESSION ==='
cd frontend
corepack enable
corepack prepare pnpm@12.5.1 --activate
pnpm install --frozen-lockfile
pnpm lint
pnpm typecheck
pnpm test
pnpm build
cd ..

echo '=== BACKEND ==='
cd backend
mvn -B -ntp dependency:tree \
  -Dincludes=org.springframework.data:spring-data-jpa,org.springframework.graphql:spring-graphql,org.springframework:spring-webmvc
mvn -B -ntp clean verify
cd ..

echo '=== DOCKER + MIGRATIONS DO ZERO ==='
docker compose down -v --remove-orphans
docker compose up --build -d

for tentativa in $(seq 1 30); do
  if curl -fsS http://localhost:8080/api/v1/health >/tmp/f1l1-health.json 2>/dev/null; then
    cat /tmp/f1l1-health.json
    echo
    break
  fi
  if [ "$tentativa" -eq 30 ]; then
    docker compose logs --no-color backend db
    exit 1
  fi
  sleep 2
done

DB_USER=$(docker compose exec -T db sh -lc 'printf %s "$POSTGRES_USER"')
DB_NAME=$(docker compose exec -T db sh -lc 'printf %s "$POSTGRES_DB"')
FLYWAY=$(docker compose exec -T db psql -U "$DB_USER" -d "$DB_NAME" -Atc   "SELECT version || ':' || success::text FROM flyway_schema_history WHERE version IN ('1','2') ORDER BY installed_rank;")
printf '%s\n' "$FLYWAY"
printf '%s\n' "$FLYWAY" | grep -qx '1:true'
printf '%s\n' "$FLYWAY" | grep -qx '2:true'

echo '=== REST RESPONSÁVEL: CAMINHO LEGÍTIMO ==='
RESP=$(curl -fsS -X POST http://localhost:8080/api/v1/responsibles \
  -H 'Content-Type: application/json' \
  --data '{"name":"Ana Silva","email":"ANA.SILVA@EXAMPLE.COM","position":"Analista","secretariatId":null}')
echo "$RESP"
RID=$(printf '%s' "$RESP" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')
EMAIL=$(printf '%s' "$RESP" | python3 -c 'import json,sys; print(json.load(sys.stdin)["email"])')
test "$EMAIL" = 'ana.silva@example.com'

curl -fsS "http://localhost:8080/api/v1/responsibles/$RID" >/tmp/f1l1-responsible-get.json
curl -fsS 'http://localhost:8080/api/v1/responsibles?page=0&size=10' >/tmp/f1l1-responsible-page.json
python3 -c 'import json,sys; data=json.load(sys.stdin); assert any(item["id"] == sys.argv[1] for item in data["content"])' \
  "$RID" </tmp/f1l1-responsible-page.json

UPDATED_RESP=$(curl -fsS -X PUT "http://localhost:8080/api/v1/responsibles/$RID" \
  -H 'Content-Type: application/json' \
  --data '{"name":"Ana Souza","email":"ana.silva@example.com","position":"Gestora","secretariatId":null}')
printf '%s' "$UPDATED_RESP" | python3 -c 'import json,sys; assert json.load(sys.stdin)["name"] == "Ana Souza"'

echo '=== REST RESPONSÁVEL: CAMINHO DE ATAQUE ==='
DUPLICATE_STATUS=$(curl -sS -o /tmp/f1l1-duplicate.json -w '%{http_code}' \
  -X POST http://localhost:8080/api/v1/responsibles \
  -H 'Content-Type: application/json' \
  --data '{"name":"Duplicada","email":"ANA.SILVA@EXAMPLE.COM","position":"Analista","secretariatId":null}')
test "$DUPLICATE_STATUS" = '409'
cat /tmp/f1l1-duplicate.json
echo

INVALID_EMAIL_STATUS=$(curl -sS -o /tmp/f1l1-invalid-email.json -w '%{http_code}' \
  -X POST http://localhost:8080/api/v1/responsibles \
  -H 'Content-Type: application/json' \
  --data '{"name":"Inválida","email":"not-an-email","position":"Analista","secretariatId":null}')
test "$INVALID_EMAIL_STATUS" = '400'
cat /tmp/f1l1-invalid-email.json
echo

echo '=== REST PROJETO ==='
TODAY=$(date +%F)
END_DATE=$(date -d '+10 days' +%F)
PROJECT_PAYLOAD=$(printf '{"name":"Portal de Serviços","responsibleIds":["%s"],"plannedStart":"%s","plannedEnd":"%s","actualStart":null,"actualEnd":null}' "$RID" "$TODAY" "$END_DATE")
PROJECT=$(curl -fsS -X POST http://localhost:8080/api/v1/projects \
  -H 'Content-Type: application/json' \
  --data "$PROJECT_PAYLOAD")
echo "$PROJECT"
PID=$(printf '%s' "$PROJECT" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')
printf '%s' "$PROJECT" | python3 -c 'import json,sys; assert json.load(sys.stdin)["status"] == "NOT_STARTED"'

PROJECT_UPDATE=$(printf '{"name":"Portal de Serviços","responsibleIds":["%s"],"plannedStart":"%s","plannedEnd":"%s","actualStart":"%s","actualEnd":null}' "$RID" "$TODAY" "$END_DATE" "$TODAY")
UPDATED_PROJECT=$(curl -fsS -X PUT "http://localhost:8080/api/v1/projects/$PID" \
  -H 'Content-Type: application/json' \
  --data "$PROJECT_UPDATE")
printf '%s' "$UPDATED_PROJECT" | python3 -c 'import json,sys; assert json.load(sys.stdin)["status"] == "IN_PROGRESS"'

curl -fsS "http://localhost:8080/api/v1/projects/$PID" >/tmp/f1l1-project-get.json
curl -fsS 'http://localhost:8080/api/v1/projects?page=0&size=10' >/tmp/f1l1-project-page.json
python3 -c 'import json,sys; data=json.load(sys.stdin); assert any(item["id"] == sys.argv[1] for item in data["content"])' \
  "$PID" </tmp/f1l1-project-page.json

IN_USE_STATUS=$(curl -sS -o /tmp/f1l1-in-use.json -w '%{http_code}' \
  -X DELETE "http://localhost:8080/api/v1/responsibles/$RID")
test "$IN_USE_STATUS" = '409'

curl -fsS -o /dev/null -w '%{http_code}' -X DELETE "http://localhost:8080/api/v1/projects/$PID" | grep -qx '204'
curl -fsS -o /dev/null -w '%{http_code}' -X DELETE "http://localhost:8080/api/v1/responsibles/$RID" | grep -qx '204'

echo '=== GRAPHQL CRUD ==='
CREATE_R_QUERY='mutation($input: ResponsibleInput!) { createResponsible(input: $input) { id email } }'
CREATE_R_VARS='{"input":{"name":"Bruno Lima","email":"BRUNO@EXAMPLE.COM","position":"Analista","secretariatId":null}}'
GQL_R=$(gql "$CREATE_R_QUERY" "$CREATE_R_VARS")
printf '%s' "$GQL_R" | assert_gql_clean
GQL_RID=$(printf '%s' "$GQL_R" | python3 -c 'import json,sys; print(json.load(sys.stdin)["data"]["createResponsible"]["id"])')

GQL_DUPLICATE=$(gql "$CREATE_R_QUERY" '{"input":{"name":"Duplicado GraphQL","email":"bruno@example.com","position":"Analista","secretariatId":null}}')
printf '%s' "$GQL_DUPLICATE" | python3 -c 'import json,sys; data=json.load(sys.stdin); assert "errors" in data and data["errors"], data'

CREATE_P_QUERY='mutation($input: ProjectInput!) { createProject(input: $input) { id status } }'
CREATE_P_VARS=$(python3 - "$GQL_RID" "$TODAY" "$END_DATE" <<'PY'
import json
import sys
print(json.dumps({"input": {
    "name": "Projeto GraphQL",
    "responsibleIds": [sys.argv[1]],
    "plannedStart": sys.argv[2],
    "plannedEnd": sys.argv[3],
    "actualStart": None,
    "actualEnd": None,
}}))
PY
)
GQL_P=$(gql "$CREATE_P_QUERY" "$CREATE_P_VARS")
printf '%s' "$GQL_P" | assert_gql_clean
GQL_PID=$(printf '%s' "$GQL_P" | python3 -c 'import json,sys; print(json.load(sys.stdin)["data"]["createProject"]["id"])')

LIST_QUERY='query { responsibles(page: 0, size: 10) { content { id } page size } projects(page: 0, size: 10) { content { id status } page size } }'
GQL_LIST=$(gql "$LIST_QUERY" '{}')
printf '%s' "$GQL_LIST" | assert_gql_clean
printf '%s' "$GQL_LIST" | python3 -c 'import json,sys; data=json.load(sys.stdin)["data"]; assert any(item["id"] == sys.argv[1] for item in data["responsibles"]["content"]); assert any(item["id"] == sys.argv[2] for item in data["projects"]["content"])' \
  "$GQL_RID" "$GQL_PID"

UPDATE_P_QUERY='mutation($id: ID!, $input: ProjectInput!) { updateProject(id: $id, input: $input) { id status } }'
UPDATE_P_VARS=$(python3 - "$GQL_PID" "$GQL_RID" "$TODAY" "$END_DATE" <<'PY'
import json
import sys
print(json.dumps({"id": sys.argv[1], "input": {
    "name": "Projeto GraphQL",
    "responsibleIds": [sys.argv[2]],
    "plannedStart": sys.argv[3],
    "plannedEnd": sys.argv[4],
    "actualStart": sys.argv[3],
    "actualEnd": None,
}}))
PY
)
GQL_P_UPDATED=$(gql "$UPDATE_P_QUERY" "$UPDATE_P_VARS")
printf '%s' "$GQL_P_UPDATED" | assert_gql_clean
printf '%s' "$GQL_P_UPDATED" | python3 -c 'import json,sys; assert json.load(sys.stdin)["data"]["updateProject"]["status"] == "IN_PROGRESS"'

DELETE_P_QUERY='mutation($id: ID!) { deleteProject(id: $id) }'
GQL_DELETE_P=$(gql "$DELETE_P_QUERY" "{\"id\":\"$GQL_PID\"}")
printf '%s' "$GQL_DELETE_P" | assert_gql_clean
printf '%s' "$GQL_DELETE_P" | python3 -c 'import json,sys; assert json.load(sys.stdin)["data"]["deleteProject"] is True'

UPDATE_R_QUERY='mutation($id: ID!, $input: ResponsibleInput!) { updateResponsible(id: $id, input: $input) { id name email } }'
UPDATE_R_VARS=$(python3 - "$GQL_RID" <<'PY'
import json
import sys
print(json.dumps({"id": sys.argv[1], "input": {
    "name": "Bruno Souza",
    "email": "bruno@example.com",
    "position": "Gestor",
    "secretariatId": None,
}}))
PY
)
GQL_R_UPDATED=$(gql "$UPDATE_R_QUERY" "$UPDATE_R_VARS")
printf '%s' "$GQL_R_UPDATED" | assert_gql_clean
printf '%s' "$GQL_R_UPDATED" | python3 -c 'import json,sys; assert json.load(sys.stdin)["data"]["updateResponsible"]["name"] == "Bruno Souza"'

DELETE_R_QUERY='mutation($id: ID!) { deleteResponsible(id: $id) }'
GQL_DELETE_R=$(gql "$DELETE_R_QUERY" "{\"id\":\"$GQL_RID\"}")
printf '%s' "$GQL_DELETE_R" | assert_gql_clean
printf '%s' "$GQL_DELETE_R" | python3 -c 'import json,sys; assert json.load(sys.stdin)["data"]["deleteResponsible"] is True'

echo '=== BANCO ==='
docker compose exec -T db psql -U "$DB_USER" -d "$DB_NAME" -Atc \
  "SELECT indexname FROM pg_indexes WHERE tablename = 'responsibles' AND indexname = 'uq_responsibles_email_ci';" \
  | grep -qx 'uq_responsibles_email_ci'

echo '=== REGRESSÃO HEALTH + FRONTEND ==='
curl -fsS http://localhost:8080/api/v1/health
echo
curl -fsS -H 'Content-Type: application/json' \
  --data '{"query":"{ health { status } }"}' \
  http://localhost:8080/graphql
echo
curl -fsS -o /dev/null -w 'HTTP %{http_code}\n' http://localhost:5173/

echo '=== GIT + CONTAINERS ==='
git diff --check
docker compose ps

echo '=== F1-L1 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige `=== F1-L1 GREEN ===` e `Resultado: exit code 0`.
