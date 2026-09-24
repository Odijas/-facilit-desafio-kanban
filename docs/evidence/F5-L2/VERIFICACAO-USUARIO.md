# F5-L2 — VERIFICAÇÃO DO USUÁRIO

O gate prova o lote F5-L2 (contrato de erro, confirmações, Swagger e logs) na branch `feature/f5-l2-contrato-de-erro`, antes do commit. Ele cobre:

- frontend completo (format, lint, typecheck, tipagem estrita, testes e build);
- build limpo do backend, com os testes do lote;
- checagens estáticas;
- subida com banco novo;
- ponta a ponta pela API:
  - códigos 422 por tipo de regra;
  - confirmação obrigatória em REST e GraphQL;
  - Bean Validation, 404 e 409 em pt-BR;
- erros documentados no OpenAPI;
- eventos de negócio no log real do contêiner, sem dado pessoal.

**Isolamento:**

- O gate usa um projeto Compose próprio (`facilit-kanban-f5l2`) e o remove no fim (`down -v` só desse projeto).
- O projeto padrão é apenas parado (`docker compose down`, sem `-v`).
- A porta 8080 precisa estar livre.

## 0. Pré-requisito: F5-L1 commitado e mesclado na develop

Se ainda não foi feito, execute a seção 3 de `docs/evidence/F5-L1/VERIFICACAO-USUARIO.md`: commits, merge `--no-ff` na `develop` e push nos dois remotos.

## 1. Preparar a branch e aplicar o pacote

O bloco para no primeiro erro e imprime `PREPARO_OK` só se tudo der certo. Serve para a primeira vez e para repetir.

```sh
cd ~/proj/facilit-desafio-kanban && \
git fetch --tags origin && \
F=feature/f5-l2-contrato-de-erro && \
if git rev-parse -q --verify "refs/heads/$F" >/dev/null; then \
  [ "$(git rev-parse develop)" = "$(git rev-parse "$F")" ] && git checkout "$F"; \
else \
  [ -z "$(git status --porcelain=v1)" ] && git checkout develop && git pull --ff-only origin develop && git checkout -b "$F"; \
fi && \
[ "$(git branch --show-current)" = "$F" ] && \
(cd ~/Downloads && echo "<SHA256>  facilit-desafio-kanban-F5-L2-candidate.tar.gz" | sha256sum -c -) && \
tar -xzf ~/Downloads/facilit-desafio-kanban-F5-L2-candidate.tar.gz && \
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5-L2/VERIFICACAO-USUARIO.md > /tmp/f5l2-gate.sh && \
echo PREPARO_OK
```

Troque `<SHA256>` pelo hash informado com o pacote.

## 2. Rodar o gate

```sh
bash /tmp/f5l2-gate.sh 2>&1 | tee /tmp/saida-gate-f5l2.txt
```

Os logs longos ficam em `/tmp/f5l2-*.log`.

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
git rev-parse --is-inside-work-tree >/dev/null
rm -f /tmp/f5l2-*

fail() { echo "FALHA: $*" >&2; exit 1; }

run_logged() {
  local log="$1"; shift
  if ! "$@" >>"$log" 2>&1; then
    tail -80 "$log" >&2
    return 1
  fi
}

sql() {
  printf '%s\n' "$1" | "${DC[@]}" exec -T db sh -lc 'psql -X -tA -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
}

echo '=== PRÉ-CONDIÇÕES ==='
BRANCH="$(git branch --show-current)"
case "$BRANCH" in
  feature/f5-l2-*) ;;
  *) fail "branch atual: $BRANCH (esperado feature/f5-l2-contrato-de-erro)" ;;
esac
git cat-file -e HEAD:backend/src/main/resources/db/migration/V6__track_project_schedule_calculation_date.sql 2>/dev/null \
  || fail 'o F5-L1 não está commitado na base da branch (execute a seção 3 do docs/evidence/F5-L1/VERIFICACAO-USUARIO.md)'
git cat-file -e HEAD:docs/evidence/F5-L1/GATE.md 2>/dev/null || fail 'evidências do F5-L1 ausentes no commit base'
git status --porcelain=v1 -uall >/tmp/f5l2-status.txt
python3 - <<'PY'
import sys
expected = {
    'README.md',
    'backend/src/main/java/br/com/facilit/kanban/application/common/Actor.java',
    'backend/src/main/java/br/com/facilit/kanban/application/common/BusinessLog.java',
    'backend/src/main/java/br/com/facilit/kanban/application/common/PageQuery.java',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectFilter.java',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectScheduleRefresher.java',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java',
    'backend/src/main/java/br/com/facilit/kanban/application/responsible/ResponsibleCredentialService.java',
    'backend/src/main/java/br/com/facilit/kanban/application/responsible/ResponsibleService.java',
    'backend/src/main/java/br/com/facilit/kanban/application/secretariat/SecretariatService.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/common/ApiErrorCode.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/rest/ApiErrorDocumentation.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectStatusRequest.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/rest/ResponsibleRestController.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java',
    'backend/src/main/java/br/com/facilit/kanban/domain/common/BusinessRuleException.java',
    'backend/src/main/java/br/com/facilit/kanban/domain/project/ConfirmationRequiredException.java',
    'backend/src/main/java/br/com/facilit/kanban/domain/project/Project.java',
    'backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectDates.java',
    'backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculator.java',
    'backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatus.java',
    'backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java',
    'backend/src/main/java/br/com/facilit/kanban/domain/project/TransitionBlockedException.java',
    'backend/src/main/java/br/com/facilit/kanban/domain/responsible/Responsible.java',
    'backend/src/main/java/br/com/facilit/kanban/domain/secretariat/Secretariat.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/security/AuthenticatedActorResolver.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/security/SecurityConfiguration.java',
    'backend/src/main/resources/application.yml',
    'backend/src/main/resources/graphql/kanban.graphqls',
    'backend/src/test/java/br/com/facilit/kanban/application/project/ProjectScheduleRefresherTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleCredentialServiceTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleServiceTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/secretariat/SecretariatServiceTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/support/CapturedBusinessLog.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandlerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectDatesTest.java',
    'backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectScheduleCalculatorTest.java',
    'backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java',
    'backend/src/test/java/br/com/facilit/kanban/domain/responsible/ResponsibleTest.java',
    'backend/src/test/java/br/com/facilit/kanban/integration/OpenApiContractIT.java',
    'backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java',
    'docs/api/facilit-kanban.postman_collection.json',
    'docs/evidence/F5-L1/GATE.md',
    'docs/evidence/F5-L1/MATRIZ.md',
    'docs/evidence/F5-L2/CONSUMIDORES.md',
    'docs/evidence/F5-L2/DECISOES.md',
    'docs/evidence/F5-L2/EXECUCAO.md',
    'docs/evidence/F5-L2/FONTES-RAG.md',
    'docs/evidence/F5-L2/GATE.md',
    'docs/evidence/F5-L2/LEITURA.md',
    'docs/evidence/F5-L2/MATRIZ.md',
    'docs/evidence/F5-L2/RISCOS.md',
    'docs/evidence/F5-L2/VERIFICACAO-USUARIO.md',
    'frontend/src/api/kanban.test.ts',
    'frontend/src/api/kanban.ts',
    'frontend/src/features/kanban/ConfirmTransitionDialog.tsx',
    'frontend/src/features/kanban/KanbanBoard.test.tsx',
    'frontend/src/features/kanban/KanbanBoard.tsx',
}
actual = set()
for line in open('/tmp/f5l2-status.txt', encoding='utf-8'):
    line = line.rstrip('\n')
    if line:
        actual.add(line[3:])
missing = sorted(expected - actual)
extra = sorted(actual - expected)
if missing or extra:
    for path in missing:
        print(f'ausente (pacote não aplicado?): {path}', file=sys.stderr)
    for path in extra:
        print(f'alteração fora do pacote: {path}', file=sys.stderr)
    sys.exit(1)
print(f'   {len(actual)} arquivos alterados, todos do pacote F5-L2')
PY
echo "   branch $BRANCH sobre o F5-L1 commitado"
echo 'F5_L2_PRECONDITIONS_GREEN'

printf '%s\n' '=== FRONTEND: FORMAT + LINT + TYPECHECK + STRICT + TEST + BUILD ==='
(
  cd frontend
  run_logged /tmp/f5l2-frontend.log corepack enable
  run_logged /tmp/f5l2-frontend.log corepack prepare pnpm@12.5.1 --activate
  for step in "install --frozen-lockfile" format lint typecheck check:strict test build; do
    # shellcheck disable=SC2086
    run_logged /tmp/f5l2-frontend.log pnpm $step
  done
)
sed 's/\x1b\[[0-9;]*m//g' /tmp/f5l2-frontend.log | grep -E 'STRICT_TYPES_GREEN|Test Files|Tests +[0-9]' | sed 's/^ */   /' || true
if grep -qi 'deprecat' /tmp/f5l2-frontend.log; then grep -i 'deprecat' /tmp/f5l2-frontend.log >&2; fail 'aviso de depreciação no frontend'; fi
git status --porcelain=v1 -uall >/tmp/f5l2-status-after-format.txt
cmp -s /tmp/f5l2-status.txt /tmp/f5l2-status-after-format.txt || { diff /tmp/f5l2-status.txt /tmp/f5l2-status-after-format.txt >&2; fail 'o format alterou arquivos'; }
TESTS="$(sed 's/\x1b\[[0-9;]*m//g' /tmp/f5l2-frontend.log | grep -Eo 'Tests +[0-9]+ passed' | grep -Eo '[0-9]+' | tail -1)"
test "${TESTS:-0}" -ge 33 || fail "testes do frontend: ${TESTS:-0} (esperado ≥ 33)"
echo 'F5_L2_FRONTEND_GREEN'

printf '%s\n' '=== BACKEND: CLEAN VERIFY ==='
(cd backend && run_logged /tmp/f5l2-backend.log mvn -B -ntp clean verify)
grep -q 'BUILD SUCCESS' /tmp/f5l2-backend.log
if grep -q '\[deprecation\]' /tmp/f5l2-backend.log; then grep '\[deprecation\]' /tmp/f5l2-backend.log >&2; fail 'uso de API depreciada no código do backend'; fi
TOOL_DEPRECATIONS="$(grep -ci 'deprecat' /tmp/f5l2-backend.log || true)"
echo "   linhas com 'deprecat' no log do Maven (ferramentas, informativo): $TOOL_DEPRECATIONS"
python3 - <<'PY'
import glob
import xml.etree.ElementTree as ET
required = {
    'br.com.facilit.kanban.domain.project.ProjectStatusTransitionTest': 24,
    'br.com.facilit.kanban.delivery.rest.RestExceptionHandlerTest': 7,
    'br.com.facilit.kanban.application.project.ProjectServiceTest': 9,
    'br.com.facilit.kanban.integration.OpenApiContractIT': 3,
}
found = {}
for kind, pattern in (('unitários', 'backend/target/surefire-reports/TEST-*.xml'), ('integração', 'backend/target/failsafe-reports/TEST-*.xml')):
    totals = dict(tests=0, failures=0, errors=0, skipped=0)
    files = glob.glob(pattern)
    for name in files:
        root = ET.parse(name).getroot()
        for key in totals:
            totals[key] += int(root.get(key, 0))
        found[root.get('name')] = tuple(int(root.get(key, 0)) for key in ('tests', 'failures', 'errors', 'skipped'))
    assert files and totals['tests'] > 0, (kind, files)
    assert totals['failures'] == 0 and totals['errors'] == 0, (kind, totals)
    print(f"   {kind}: {len(files)} classes, {totals['tests']} testes, {totals['failures']} falhas, {totals['errors']} erros, {totals['skipped']} ignorados")
for name, count in required.items():
    assert name in found, f'relatório ausente: {name}'
    assert found[name] == (count, 0, 0, 0), (name, found[name], count)
    print(f"   {name.rsplit('.', 1)[1]}: {count} testes OK")
PY
echo 'F5_L2_BACKEND_GREEN'

echo '=== ESTÁTICA ==='
git diff --check
python3 - <<'PY'
import json, os, re, sys
problems = []
names = [line[3:].rstrip('\n') for line in open('/tmp/f5l2-status.txt', encoding='utf-8') if line.strip()]
for name in names:
    if not os.path.isfile(name):
        continue
    data = open(name, 'rb').read()
    for index, line in enumerate(data.decode('utf-8').split('\n'), 1):
        if line != line.rstrip(' \t'):
            problems.append(f'espaço no fim da linha: {name}:{index}')
            break
    if not data.endswith(b'\n') or data.endswith(b'\n\n'):
        problems.append(f'fim de arquivo sem exatamente uma quebra de linha: {name}')
readme = open('README.md', encoding='utf-8').read()
for text in ('TRANSITION_BLOCKED', 'CONFIRMATION_REQUIRED', 'BUSINESS_RULE_VIOLATION', '### F5-L2',
             'F5-L1 — Regras sempre corretas (status de hoje, fuso, datas realizadas): GREEN'):
    if text not in readme:
        problems.append(f'README sem "{text}"')
if 'Erros de transição respondem 400' in readme:
    problems.append('README ainda descreve bloqueio como 400')
if '**GREEN**' not in open('docs/evidence/F5-L1/GATE.md', encoding='utf-8').read():
    problems.append('F5-L1 não promovido a GREEN')
collection = open('docs/api/facilit-kanban.postman_collection.json', encoding='utf-8').read()
json.loads(collection)
for text in ('TRANSITION_BLOCKED', 'CONFIRMATION_REQUIRED', '\\"confirm\\": true'):
    if text not in collection:
        problems.append(f'coleção sem {text}')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print(f'   {len(names)} arquivos do lote OK; README, F5-L1 GREEN e coleção OK')
PY
echo 'F5_L2_STATIC_GREEN'

echo '=== DOCKER: BANCO NOVO ==='
RUN_ID="$(date +%s)-$$"
export POSTGRES_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export APP_ADMIN_EMAIL="f5l2-admin-${RUN_ID}@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export APP_DEMO_RESPONSIBLE_NAME='' APP_DEMO_RESPONSIBLE_EMAIL='' APP_DEMO_RESPONSIBLE_POSITION='' APP_DEMO_RESPONSIBLE_PASSWORD=''
export METRICS_PASSWORD='' SESSION_COOKIE_SECURE=false APP_TIME_ZONE=America/Sao_Paulo
GATE_PROJECT=facilit-kanban-f5l2
DC=(docker compose -p "$GATE_PROJECT" -f compose.yaml)

cleanup() {
  set +e
  "${DC[@]}" down -v --remove-orphans >/dev/null 2>&1
  rm -f /tmp/f5l2-*.json /tmp/f5l2-*.cookies
}
trap cleanup EXIT

docker compose down --remove-orphans >/dev/null 2>&1 || true
"${DC[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
run_logged /tmp/f5l2-compose.log "${DC[@]}" up --build -d db backend
for i in $(seq 1 60); do
  if curl -fsS http://localhost:8080/actuator/health >/tmp/f5l2-health.json 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 60 ]; then "${DC[@]}" logs --no-color backend | tail -80; fail 'backend não subiu'; fi
  sleep 2
done
MIGRATIONS="$(sql 'SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank;' | tr '\n' ' ' | sed 's/ *$//')"
test "$MIGRATIONS" = '1 2 3 4 5 6' || fail "migrations aplicadas: '$MIGRATIONS'"
echo "   banco novo ($GATE_PROJECT); migrations V$MIGRATIONS"
echo 'F5_L2_DOCKER_GREEN'

echo '=== API: CONTRATO DE ERRO E CONFIRMAÇÕES ==='
BASE=http://localhost:8080
JAR=/tmp/f5l2-admin.cookies
SMOKE_ID="$(date +%s)"
TODAY="$(TZ=America/Sao_Paulo date +%F)"
day() { date -d "$TODAY $1 day" +%F; }
RESPONSIBLE_EMAIL="f5l2-$SMOKE_ID@example.invalid"
rm -f "$JAR"
PASSED=0

check() {
  local name="$1" expected="$2" actual="$3"
  if [ "$actual" != "$expected" ]; then
    echo "FALHA: $name — esperado $expected, obtido $actual" >&2
    exit 1
  fi
  PASSED=$((PASSED + 1))
  echo "  ok $PASSED $name"
}

# Confere campos do JSON: pares caminho=valor; valor começando com ^ confere prefixo.
expect_json() {
  python3 - "$@" <<'PY'
import json, sys
path, *checks = sys.argv[1:]
body = json.load(open(path, encoding='utf-8'))
for check in checks:
    key, expected = check.split('=', 1)
    value = body
    for part in key.split('.'):
        value = value[int(part)] if isinstance(value, list) else value[part]
    value = '' if value is None else str(value)
    ok = value.startswith(expected[1:]) if expected.startswith('^') else value == expected
    if not ok:
        sys.exit(f'{key}: esperado {expected!r}, obtido {value!r}')
PY
}

csrf() {
  test "$(curl -sS -o /tmp/f5l2-csrf.json -w '%{http_code}' -b "$JAR" -c "$JAR" "$BASE/api/v1/auth/csrf")" = '200'
  CSRF_TOKEN="$(awk '$6 == "XSRF-TOKEN" { value=$7 } END { if (value == "") exit 1; print value }' "$JAR")"
  test -n "$CSRF_TOKEN"
}

call() {
  local method="$1" path="$2" body="$3" out="$4"
  if [ -n "$body" ]; then
    curl -sS -o "$out" -w '%{http_code}' -X "$method" -b "$JAR" -c "$JAR" \
      -H 'Content-Type: application/json' -H "X-XSRF-TOKEN: $CSRF_TOKEN" --data "$body" "$BASE$path"
  else
    curl -sS -o "$out" -w '%{http_code}' -X "$method" -b "$JAR" -c "$JAR" \
      -H "X-XSRF-TOKEN: $CSRF_TOKEN" "$BASE$path"
  fi
}

graphql() {
  python3 -c 'import json, sys; print(json.dumps({"query": sys.argv[1], "variables": json.loads(sys.argv[2])}))' "$1" "$2"
}

csrf
check 'login ADMIN' '200' "$(call POST /api/v1/auth/login "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" /tmp/f5l2-a01.json)"
csrf
check 'criar responsável' '201' "$(call POST /api/v1/responsibles "{\"name\":\"Responsável F5-L2\",\"email\":\"$RESPONSIBLE_EMAIL\",\"position\":\"Analista\"}" /tmp/f5l2-a02.json)"
RESPONSIBLE_ID="$(python3 -c "import json; print(json.load(open('/tmp/f5l2-a02.json'))['id'])")"
check 'e-mail duplicado → 409' '409' "$(call POST /api/v1/responsibles "{\"name\":\"Duplicado\",\"email\":\"$RESPONSIBLE_EMAIL\",\"position\":\"Analista\"}" /tmp/f5l2-a03.json)"
expect_json /tmp/f5l2-a03.json code=CONFLICT 'detail=Já existe responsável com este e-mail.'
check 'e-mail inválido → 400' '400' "$(call POST /api/v1/responsibles '{"name":"Inválido","email":"sem-arroba","position":"Analista"}' /tmp/f5l2-a04.json)"
expect_json /tmp/f5l2-a04.json code=VALIDATION_ERROR 'detail=Dados de entrada inválidos.' violations.0.field=email
python3 - <<'PY'
import json
message = json.load(open('/tmp/f5l2-a04.json', encoding='utf-8'))['violations'][0]['message']
assert 'must' not in message and 'well-formed' not in message, message
print(f'   mensagem de validação: {message}')
PY

PROJECT_BODY="\"responsibleIds\":[\"$RESPONSIBLE_ID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$(day +10)\""
check 'criar projeto' '201' "$(call POST /api/v1/projects "{\"name\":\"F5L2 $SMOKE_ID\",$PROJECT_BODY}" /tmp/f5l2-a05.json)"
P1="$(python3 -c "import json; print(json.load(open('/tmp/f5l2-a05.json'))['id'])")"
check 'A iniciar → Em andamento' '200' "$(call PATCH "/api/v1/projects/$P1/status" '{"status":"IN_PROGRESS"}' /tmp/f5l2-a06.json)"
check 'Em andamento → Atrasado → 422' '422' "$(call PATCH "/api/v1/projects/$P1/status" '{"status":"OVERDUE"}' /tmp/f5l2-a07.json)"
expect_json /tmp/f5l2-a07.json code=TRANSITION_BLOCKED currentStatus=IN_PROGRESS requestedStatus=OVERDUE \
  'detail=^Em andamento → Atrasado bloqueado: com as datas atuais o projeto não fica Atrasado.'
check 'Em andamento → A iniciar sem confirm → 422' '422' "$(call PATCH "/api/v1/projects/$P1/status" '{"status":"NOT_STARTED"}' /tmp/f5l2-a08.json)"
expect_json /tmp/f5l2-a08.json code=CONFIRMATION_REQUIRED clearedField=actualStart \
  "detail=Confirme Em andamento → A iniciar: o início realizado ($TODAY) será apagado. Reenvie com confirm = true."
check 'nada gravado sem confirmação' '200' "$(call GET "/api/v1/projects/$P1" '' /tmp/f5l2-a09.json)"
expect_json /tmp/f5l2-a09.json status=IN_PROGRESS "actualStart=$TODAY"
check 'Em andamento → A iniciar com confirm → 200' '200' "$(call PATCH "/api/v1/projects/$P1/status" '{"status":"NOT_STARTED","confirm":true}' /tmp/f5l2-a10.json)"
expect_json /tmp/f5l2-a10.json status=NOT_STARTED actualStart=

MUTATION='mutation($id: ID!, $status: ProjectStatus!, $confirm: Boolean) { transitionProject(id: $id, status: $status, confirm: $confirm) { status actualStart } }'
check 'GraphQL A iniciar → Em andamento' '200' "$(call POST /graphql "$(graphql "$MUTATION" "{\"id\":\"$P1\",\"status\":\"IN_PROGRESS\"}")" /tmp/f5l2-a11.json)"
expect_json /tmp/f5l2-a11.json data.transitionProject.status=IN_PROGRESS
check 'GraphQL sem confirm' '200' "$(call POST /graphql "$(graphql "$MUTATION" "{\"id\":\"$P1\",\"status\":\"NOT_STARTED\"}")" /tmp/f5l2-a12.json)"
expect_json /tmp/f5l2-a12.json errors.0.extensions.code=CONFIRMATION_REQUIRED errors.0.extensions.clearedField=actualStart
check 'GraphQL com confirm' '200' "$(call POST /graphql "$(graphql "$MUTATION" "{\"id\":\"$P1\",\"status\":\"NOT_STARTED\",\"confirm\":true}")" /tmp/f5l2-a13.json)"
expect_json /tmp/f5l2-a13.json data.transitionProject.status=NOT_STARTED

check 'início realizado futuro → 422' '422' "$(call POST /api/v1/projects "{\"name\":\"F5L2 $SMOKE_ID futuro\",$PROJECT_BODY,\"actualStart\":\"$(day +1)\"}" /tmp/f5l2-a14.json)"
expect_json /tmp/f5l2-a14.json code=BUSINESS_RULE_VIOLATION "detail=^Início realizado ($(day +1)) não pode ser posterior a hoje ($TODAY)"
check 'término previsto antes do início → 422' '422' "$(call POST /api/v1/projects "{\"name\":\"F5L2 $SMOKE_ID datas\",\"responsibleIds\":[\"$RESPONSIBLE_ID\"],\"plannedStart\":\"$(day +5)\",\"plannedEnd\":\"$TODAY\"}" /tmp/f5l2-a15.json)"
expect_json /tmp/f5l2-a15.json code=BUSINESS_RULE_VIOLATION 'detail=^Término previsto (plannedEnd = '
MISSING_ID=30000000-0000-4000-8000-00000000f5f5
check 'projeto inexistente → 404' '404' "$(call GET "/api/v1/projects/$MISSING_ID" '' /tmp/f5l2-a16.json)"
expect_json /tmp/f5l2-a16.json code=RESOURCE_NOT_FOUND "detail=Projeto não encontrado: $MISSING_ID"
echo 'F5_L2_API_GREEN'

echo '=== OPENAPI: ERROS DOCUMENTADOS ==='
curl -fsS "$BASE/api-docs" >/tmp/f5l2-openapi.json
python3 - <<'PY'
import json, sys
docs = json.load(open('/tmp/f5l2-openapi.json', encoding='utf-8'))
public = {'/api/v1/health', '/api/v1/auth/csrf', '/api/v1/auth/login'}
problems, operations = [], 0
for path, item in docs['paths'].items():
    if not path.startswith('/api/v1/'):
        continue
    for method, operation in item.items():
        if method not in ('get', 'post', 'put', 'patch', 'delete'):
            continue
        operations += 1
        required = ['500'] + ([] if path in public else ['401', '403'] + (['409'] if method != 'get' else []) + (['404'] if '{id}' in path else []))
        for code in required:
            content = operation.get('responses', {}).get(code, {}).get('content', {}).get('application/problem+json')
            if not content or not (content.get('examples') or content.get('example')):
                problems.append(f'{method.upper()} {path}: {code} sem problem+json com exemplo')
transition = docs['paths']['/api/v1/projects/{id}/status']['patch']['responses']['422']['content']['application/problem+json']['examples']
for name in ('TRANSITION_BLOCKED', 'CONFIRMATION_REQUIRED', 'BUSINESS_RULE_VIOLATION'):
    if name not in transition:
        problems.append(f'transição sem exemplo {name}')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print(f'   {operations} operações /api/v1 com erros documentados em application/problem+json')
PY
echo 'F5_L2_OPENAPI_GREEN'

echo '=== LOGS DE NEGÓCIO (LOG ECS DO CONTÊINER) ==='
for id in "$P1"; do
  check "excluir projeto $id" '204' "$(call DELETE "/api/v1/projects/$id" '' /tmp/f5l2-a17.json)"
done
check 'excluir responsável' '204' "$(call DELETE "/api/v1/responsibles/$RESPONSIBLE_ID" '' /tmp/f5l2-a18.json)"
sleep 1
"${DC[@]}" logs --no-color --no-log-prefix backend >/tmp/f5l2-backend-run.log 2>&1
python3 - "$P1" "$RESPONSIBLE_ID" "$RESPONSIBLE_EMAIL" "$APP_ADMIN_PASSWORD" <<'PY'
import json, sys
project, responsible, email, password = sys.argv[1:]
records = []
raw = open('/tmp/f5l2-backend-run.log', encoding='utf-8').read()
for line in raw.splitlines():
    line = line.strip()
    if line.startswith('{'):
        try:
            records.append(json.loads(line))
        except json.JSONDecodeError:
            pass
business = [r for r in records if str(r.get('message', '')).startswith('evento=')]
messages = [r['message'] for r in business]
expected = [
    f'evento=projeto.criado id={project} status=NOT_STARTED ator=ADMIN',
    f'evento=projeto.transicao id={project} de=NOT_STARTED para=IN_PROGRESS confirmado=false ator=ADMIN',
    f'evento=projeto.transicao.recusada id={project} de=IN_PROGRESS para=OVERDUE motivo=TransitionBlockedException ator=ADMIN',
    f'evento=projeto.transicao.recusada id={project} de=IN_PROGRESS para=NOT_STARTED motivo=ConfirmationRequiredException ator=ADMIN',
    f'evento=projeto.transicao id={project} de=IN_PROGRESS para=NOT_STARTED confirmado=true ator=ADMIN',
    f'evento=projeto.excluido id={project} ator=ADMIN',
    f'evento=responsavel.criado id={responsible} ator=ADMIN',
    f'evento=responsavel.excluido id={responsible} ator=ADMIN',
]
missing = [event for event in expected if event not in messages]
assert not missing, ('eventos ausentes no log ECS', missing, messages[-15:])
sample = next(r for r in business if r['message'] == expected[0])
assert sample.get('ecs', {}).get('version'), sample
assert sample.get('log', {}).get('logger') == 'br.com.facilit.kanban.business', sample
assert email not in raw, 'e-mail do responsável apareceu no log'
assert password not in raw, 'senha apareceu no log'
print(f'   {len(business)} eventos de negócio no log ECS (logger br.com.facilit.kanban.business); sem e-mail nem senha')
PY
test "$PASSED" -eq 18
echo 'F5_L2_BUSINESS_LOGS_GREEN'

printf '%s\n' '=== F5-L2 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige todos os marcadores `F5_L2_*_GREEN`, `=== F5-L2 GREEN ===` e `Resultado: exit code 0`. Traga a saída (`/tmp/saida-gate-f5l2.txt`).

## 3. Depois do GREEN: commits granulares e merge (Gitflow)

Cada commit compila sozinho, na ordem abaixo.

```sh
cd ~/proj/facilit-desafio-kanban
R=backend/src/main/java/br/com/facilit/kanban/delivery/rest
I=backend/src/test/java/br/com/facilit/kanban/integration

git add backend/src ':!'$R/ApiErrorDocumentation.java ':!'$I/OpenApiContractIT.java
git commit -m "feat(backend)!: erros de negócio 422, confirmação obrigatória, mensagens em pt-BR e logs de negócio" \
  -m "BREAKING CHANGE: transição bloqueada responde 422 TRANSITION_BLOCKED (antes 400 INVALID_REQUEST); Em andamento → A iniciar e Concluído → Em andamento/Atrasado exigem confirm = true (422 CONFIRMATION_REQUIRED)."

git add $R/ApiErrorDocumentation.java $I/OpenApiContractIT.java
git commit -m "docs(api): documenta os erros de cada operação no OpenAPI"

git add frontend
git commit -m "feat(frontend): pede confirmação quando a transição apaga uma data registrada"

git add docs/api/facilit-kanban.postman_collection.json
git commit -m "test(api): atualiza a coleção para 422 e confirmação obrigatória"

git add README.md docs
git commit -m "docs(F5-L2): promove o F5-L1 e registra as evidências do F5-L2"

git status --short        # deve ficar vazio
git checkout develop
git merge --no-ff feature/f5-l2-contrato-de-erro -m "Merge branch 'feature/f5-l2-contrato-de-erro' into develop"
git push origin develop feature/f5-l2-contrato-de-erro
git push gitlab develop feature/f5-l2-contrato-de-erro
```
