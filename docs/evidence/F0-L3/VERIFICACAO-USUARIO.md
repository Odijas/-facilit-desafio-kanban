# F0-L3 — VERIFICAÇÃO DO USUÁRIO

Execute na raiz do projeto. O Bash estrito roda em processo filho e não altera o shell interativo.

**Atenção:** por ser o fechamento greenfield da F0, o gate remove o volume local `postgres-data` deste Compose para provar que a migration cria o banco a partir do zero. Não execute este reset se tiver colocado dados que precise preservar.

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban

echo "=== FRONTEND ==="
cd frontend
corepack enable
corepack prepare pnpm@12.5.1 --activate
pnpm install --frozen-lockfile
pnpm lint
pnpm typecheck
pnpm test
pnpm build
cd ..

echo "=== BACKEND ==="
cd backend
mvn -B -ntp clean verify
cd ..

echo "=== DOCKER + MIGRATION DO ZERO ==="
docker compose down -v --remove-orphans
docker compose up --build -d

for tentativa in $(seq 1 30); do
  if curl -fsS http://localhost:8080/api/v1/health >/tmp/kanban-health.json 2>/dev/null; then
    cat /tmp/kanban-health.json
    echo
    break
  fi
  if [ "$tentativa" -eq 30 ]; then
    docker compose logs --no-color db backend
    exit 1
  fi
  sleep 2
done

echo "=== FLYWAY + SCHEMA ==="
MIGRATION_STATUS="$(docker compose exec -T db sh -lc 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -At' <<'SQL'
SELECT version || ':' || success::text
FROM flyway_schema_history
WHERE version = '1';
SQL
)"
[ "$MIGRATION_STATUS" = "1:true" ] || {
  echo "Migration V1 não confirmada: $MIGRATION_STATUS"
  exit 1
}
echo "$MIGRATION_STATUS"

TABLE_COUNT="$(docker compose exec -T db sh -lc 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -At' <<'SQL'
SELECT count(*)
FROM pg_tables
WHERE schemaname = 'public'
  AND tablename IN ('projects', 'responsibles', 'secretariats', 'project_responsibles');
SQL
)"
[ "$TABLE_COUNT" = "4" ] || {
  echo "Quantidade inesperada de tabelas core: $TABLE_COUNT"
  exit 1
}
echo "core_tables=$TABLE_COUNT"

INDEX_COUNT="$(docker compose exec -T db sh -lc 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -At' <<'SQL'
SELECT count(*)
FROM pg_indexes
WHERE schemaname = 'public'
  AND indexname IN (
    'ix_responsibles_secretariat_id',
    'ix_projects_status',
    'ix_projects_planned_start',
    'ix_projects_planned_end',
    'ix_project_responsibles_responsible_id'
  );
SQL
)"
[ "$INDEX_COUNT" = "5" ] || {
  echo "Quantidade inesperada de índices explícitos: $INDEX_COUNT"
  exit 1
}
echo "explicit_indexes=$INDEX_COUNT"

echo "=== PERSISTÊNCIA: CAMINHO LEGÍTIMO ==="
docker compose exec -T db sh -lc 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' <<'SQL'
BEGIN;
INSERT INTO secretariats (id, name, created_at, updated_at)
VALUES ('10000000-0000-4000-8000-000000000001', 'Tecnologia', now(), now());
INSERT INTO responsibles (id, name, email, position, secretariat_id, created_at, updated_at)
VALUES (
  '20000000-0000-4000-8000-000000000001',
  'Ana',
  'ana@example.com',
  'Analista',
  '10000000-0000-4000-8000-000000000001',
  now(),
  now()
);
INSERT INTO projects (
  id, name, status, planned_start, planned_end, actual_start, actual_end,
  delay_days, remaining_time_percentage, created_at, updated_at
) VALUES (
  '30000000-0000-4000-8000-000000000001',
  'Portal',
  'NOT_STARTED',
  DATE '2026-09-22',
  DATE '2026-09-30',
  NULL,
  NULL,
  0,
  100,
  now(),
  now()
);
INSERT INTO project_responsibles (project_id, responsible_id)
VALUES (
  '30000000-0000-4000-8000-000000000001',
  '20000000-0000-4000-8000-000000000001'
);
ROLLBACK;
SQL
echo "DB_LEGITIMATE_PATH_GREEN"

echo "=== PERSISTÊNCIA: CAMINHO DE ATAQUE ==="
if docker compose exec -T db sh -lc 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' <<'SQL'
BEGIN;
INSERT INTO projects (
  id, name, status, delay_days, remaining_time_percentage, created_at, updated_at
) VALUES (
  '30000000-0000-4000-8000-000000000099',
  'Inválido',
  'NOT_STARTED',
  -1,
  101,
  now(),
  now()
);
ROLLBACK;
SQL
then
  echo "ERRO: banco aceitou métricas inválidas"
  exit 1
else
  echo "DB_CONSTRAINT_ATTACK_BLOCKED"
fi

echo "=== REST ==="
curl -fsS http://localhost:8080/api/v1/health
echo

echo "=== GRAPHQL ==="
curl -fsS \
  -H 'Content-Type: application/json' \
  --data '{"query":"{ health { status } }"}' \
  http://localhost:8080/graphql
echo

echo "=== FRONTEND HTTP ==="
curl -fsS -o /dev/null -w 'HTTP %{http_code}\n' http://localhost:5173/

echo "=== CONTAINERS ==="
docker compose ps

echo "=== GIT ==="
git diff --check
git status --short

echo "=== F0-L3 / F0 GREEN ==="
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige: frontend lint/typecheck/test/build sem erro; Maven `BUILD SUCCESS` com 18 testes e nenhuma falha; migration `1:true`; quatro tabelas core; cinco índices explícitos; caminho legítimo aceito; ataque de métricas inválidas bloqueado; REST/GraphQL `UP`; frontend HTTP 200; `git diff --check` limpo; exit code 0.
