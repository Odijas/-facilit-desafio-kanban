# F0-L1 — VERIFICAÇÃO DO USUÁRIO

**Estado: executada e GREEN em 2026-09-21.**

O gate final comprovou REST, GraphQL, frontend, PostgreSQL e integridade do diff.

## Comando reproduzível

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban

docker compose down --remove-orphans
docker compose up --build -d

for tentativa in $(seq 1 30); do
  if curl -fsS http://localhost:8080/api/v1/health >/tmp/kanban-health.json 2>/dev/null; then
    cat /tmp/kanban-health.json
    echo
    break
  fi

  if [ "$tentativa" -eq 30 ]; then
    docker compose logs --no-color backend
    exit 1
  fi

  sleep 2
done

curl -fsS \
  -H 'Content-Type: application/json' \
  --data '{"query":"{ health { status } }"}' \
  http://localhost:8080/graphql
echo

curl -fsS -o /dev/null -w 'HTTP %{http_code}\n' \
  http://localhost:5173/

docker compose ps
git diff --check

echo "=== F0-L1 GREEN ==="
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

## Evidência observada

```text
REST: {"status":"UP"}
GraphQL: {"data":{"health":{"status":"UP"}}}
Frontend: HTTP 200
PostgreSQL: healthy
F0-L1 GREEN
Resultado: exit code 0
```
