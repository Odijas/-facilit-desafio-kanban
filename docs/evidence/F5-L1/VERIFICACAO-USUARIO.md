# F5-L1 — VERIFICAÇÃO DO USUÁRIO

O gate prova o lote F5-L1 (regras sempre corretas) na branch `feature/f5-l1-regras-sempre-corretas`, antes do commit. Ele cobre:

- build limpo e testes completos, com os testes novos do lote;
- checagens estáticas dos arquivos do lote;
- subida com **banco novo** e migrations 1 a 6;
- ponta a ponta pela API:
  - projeto "envelhecido" 10 dias no banco aparece Atrasado, com 8 dias de atraso e 0%, no GET, no GraphQL, na listagem por status e nos indicadores;
  - `updated_at` intacto;
  - transição a partir do status de hoje;
  - "hoje" em São Paulo;
  - data realizada futura recusada.

**Isolamento:**

- O gate usa um projeto Compose próprio (`facilit-kanban-f5l1`) e o remove no fim (`down -v` só desse projeto).
- O projeto padrão é apenas parado (`docker compose down`, sem `-v`); o volume dele não é tocado.
- A porta 8080 precisa estar livre.
- O frontend não muda neste lote e não é executado; o CI roda o job `frontend` no push.

**Horário:** o gate recusa rodar entre 23h50 e 0h10 (horário de Brasília), porque a virada do dia invalida as asserções de data.

## 1. Preparar a branch e aplicar o pacote

O bloco para no primeiro erro e imprime `PREPARO_OK` só se tudo der certo. Ele serve para a primeira vez e para repetir:

- cria a branch a partir da `develop` limpa, ou entra nela se já existir no mesmo commit da `develop`;
- confere o SHA-256 antes de extrair o pacote.

Troque `<SHA256>` pelo hash informado com o pacote.

```sh
cd ~/proj/facilit-desafio-kanban && \
git fetch --tags origin && \
F=feature/f5-l1-regras-sempre-corretas && \
if git rev-parse -q --verify "refs/heads/$F" >/dev/null; then \
  [ "$(git rev-parse develop)" = "$(git rev-parse "$F")" ] && git checkout "$F"; \
else \
  [ -z "$(git status --porcelain=v1)" ] && git checkout develop && git pull --ff-only origin develop && git checkout -b "$F"; \
fi && \
[ "$(git branch --show-current)" = "$F" ] && \
(cd ~/Downloads && echo "<SHA256>  facilit-desafio-kanban-F5-L1-candidate-rev2.tar.gz" | sha256sum -c -) && \
tar -xzf ~/Downloads/facilit-desafio-kanban-F5-L1-candidate-rev2.tar.gz && \
echo PREPARO_OK
```

## 2. Rodar o gate

```sh
cd ~/proj/facilit-desafio-kanban
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5-L1/VERIFICACAO-USUARIO.md > /tmp/f5l1-gate.sh
bash /tmp/f5l1-gate.sh 2>&1 | tee /tmp/saida-gate-f5l1.txt
```

Os logs longos (Maven e Compose) ficam em `/tmp/f5l1-*.log`. A tela mostra só os resumos e, em falha, o fim do log.

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
git rev-parse --is-inside-work-tree >/dev/null
rm -f /tmp/f5l1-*

fail() { echo "FALHA: $*" >&2; exit 1; }

# Executa um comando guardando a saída em log; em falha mostra o fim do log.
run_logged() {
  local log="$1"; shift
  if ! "$@" >>"$log" 2>&1; then
    tail -80 "$log" >&2
    return 1
  fi
}

# SQL pelo stdin do psql do contêiner do banco (evita aspas aninhadas).
sql() {
  printf '%s\n' "$1" | "${DC[@]}" exec -T db sh -lc 'psql -X -tA -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
}

echo '=== PRÉ-CONDIÇÕES ==='
NOW_SP="$(TZ=America/Sao_Paulo date +%H%M)"
if [ "$NOW_SP" -ge 2350 ] || [ "$NOW_SP" -lt 10 ]; then
  fail 'rode fora da virada do dia (entre 0h10 e 23h50, horário de Brasília)'
fi
BRANCH="$(git branch --show-current)"
case "$BRANCH" in
  feature/f5-l1-*) ;;
  *) fail "branch atual: $BRANCH (esperado feature/f5-l1-regras-sempre-corretas)" ;;
esac
git rev-parse -q --verify refs/tags/v1.0.0 >/dev/null || fail 'tag v1.0.0 ausente (git fetch --tags origin)'
# Gitflow: a tag fica no merge da release na main; a develop recebe a branch release (2º pai desse merge), não a main.
RELEASE_COMMIT="$(git rev-parse 'v1.0.0^{commit}')"
if git merge-base --is-ancestor "$RELEASE_COMMIT" HEAD; then
  RELEASE_BASE="$RELEASE_COMMIT"
elif git rev-parse -q --verify "${RELEASE_COMMIT}^2" >/dev/null && git merge-base --is-ancestor "${RELEASE_COMMIT}^2" HEAD; then
  RELEASE_BASE="$(git rev-parse "${RELEASE_COMMIT}^2")"
else
  fail 'a branch não contém a release v1.0.0 (nem a tag nem a branch release mesclada)'
fi
git diff --quiet "$RELEASE_COMMIT" "$RELEASE_BASE" -- || fail 'a release mesclada na develop difere do conteúdo da tag v1.0.0'
git status --porcelain=v1 -uall >/tmp/f5l1-status.txt
python3 - <<'PY'
import sys
expected = {
    '.env.example',
    'README.md',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectRepository.java',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectScheduleRefresher.java',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectScheduleSnapshot.java',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectScheduleUpdate.java',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRequest.java',
    'backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectDates.java',
    'backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatusTransition.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ProjectScheduleRefreshJob.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/config/SchedulingConfiguration.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaEntity.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaRepository.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java',
    'backend/src/main/resources/application.yml',
    'backend/src/main/resources/db/migration/V6__track_project_schedule_calculation_date.sql',
    'backend/src/test/java/br/com/facilit/kanban/application/project/ProjectScheduleRefresherTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleServiceTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java',
    'backend/src/test/java/br/com/facilit/kanban/application/support/MutableClock.java',
    'backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectDatesTest.java',
    'backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java',
    'backend/src/test/java/br/com/facilit/kanban/integration/KanbanApiIT.java',
    'backend/src/test/java/br/com/facilit/kanban/integration/ScheduleCalculationDateMigrationIT.java',
    'backend/src/test/java/br/com/facilit/kanban/integration/ScheduleFreshnessIT.java',
    'backend/src/test/java/br/com/facilit/kanban/integration/SecurityApiIT.java',
    'compose.yaml',
    'docs/adr/0002-status-sempre-atual.md',
    'docs/api/facilit-kanban.postman_collection.json',
    'docs/evidence/F5-L1/CONSUMIDORES.md',
    'docs/evidence/F5-L1/DECISOES.md',
    'docs/evidence/F5-L1/EXECUCAO.md',
    'docs/evidence/F5-L1/FONTES-RAG.md',
    'docs/evidence/F5-L1/GATE.md',
    'docs/evidence/F5-L1/LEITURA.md',
    'docs/evidence/F5-L1/MATRIZ.md',
    'docs/evidence/F5-L1/RISCOS.md',
    'docs/evidence/F5-L1/VERIFICACAO-USUARIO.md',
}
# O plano pode já ter sido colocado (e até commitado) antes do pacote.
optional = {'docs/governance/PLANO-CONFORMIDADE-F5.md'}
actual = set()
for line in open('/tmp/f5l1-status.txt', encoding='utf-8'):
    line = line.rstrip('\n')
    if line:
        actual.add(line[3:])
missing = sorted(expected - actual)
extra = sorted(actual - expected - optional)
if missing or extra:
    for path in missing:
        print(f'ausente (pacote não aplicado?): {path}', file=sys.stderr)
    for path in extra:
        print(f'alteração fora do pacote: {path}', file=sys.stderr)
    sys.exit(1)
print(f'   {len(actual)} arquivos alterados, todos do pacote F5-L1; frontend intocado')
PY
test -f docs/governance/PLANO-CONFORMIDADE-F5.md || fail 'plano docs/governance/PLANO-CONFORMIDADE-F5.md ausente'
echo "   branch $BRANCH contém a release v1.0.0 (${RELEASE_BASE:0:7}, mesmo conteúdo da tag)"
echo 'F5_L1_PRECONDITIONS_GREEN'

printf '%s\n' '=== BACKEND: CLEAN VERIFY ==='
(cd backend && run_logged /tmp/f5l1-backend.log mvn -B -ntp clean verify)
grep -q 'BUILD SUCCESS' /tmp/f5l1-backend.log
if grep -q '\[deprecation\]' /tmp/f5l1-backend.log; then grep '\[deprecation\]' /tmp/f5l1-backend.log >&2; fail 'uso de API depreciada no código do backend'; fi
TOOL_DEPRECATIONS="$(grep -ci 'deprecat' /tmp/f5l1-backend.log || true)"
echo "   linhas com 'deprecat' no log do Maven (ferramentas, informativo): $TOOL_DEPRECATIONS"
if [ "$TOOL_DEPRECATIONS" -gt 0 ]; then grep -i 'deprecat' /tmp/f5l1-backend.log | sort | uniq -c | head -10 | sed 's/^/   /'; fi
python3 - <<'PY'
import glob
import xml.etree.ElementTree as ET
required = {
    'br.com.facilit.kanban.application.project.ProjectScheduleRefresherTest': 8,
    'br.com.facilit.kanban.domain.project.ProjectDatesTest': 4,
    'br.com.facilit.kanban.domain.project.ProjectStatusTransitionTest': 19,
    'br.com.facilit.kanban.integration.ScheduleFreshnessIT': 3,
    'br.com.facilit.kanban.integration.ScheduleCalculationDateMigrationIT': 1,
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
    tests, failures, errors, skipped = found[name]
    assert (tests, failures, errors, skipped) == (count, 0, 0, 0), (name, found[name], count)
    print(f"   {name.rsplit('.', 1)[1]}: {tests} testes OK")
PY
echo 'F5_L1_BACKEND_GREEN'

echo '=== ESTÁTICA: ARQUIVOS DO LOTE, README, ADR E COLEÇÃO ==='
git diff --check
python3 - <<'PY'
import json, os, re, sys
problems = []
names = [line[3:].rstrip('\n') for line in open('/tmp/f5l1-status.txt', encoding='utf-8') if line.strip()]
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
def need(path, headings):
    text = open(path, encoding='utf-8').read()
    for heading in headings:
        if not re.search(r'^#{1,3} .*' + re.escape(heading), text, re.M | re.I):
            problems.append(f'{path}: seção ausente "{heading}"')
    return text
readme = need('README.md', ['Regras de negócio', 'Interpretações do enunciado', 'Como rodar', 'Como testar',
                            'Limitações e próximos passos', 'F5-L1'])
adr = need('docs/adr/0002-status-sempre-atual.md', ['1. Contexto', '2. Decisão', '3. Alternativas consideradas',
                                                    '4. Consequências e trade-offs', '5. Testes'])
if '```mermaid' not in adr:
    problems.append('ADR 0002 sem diagrama mermaid')
if 'término previsto futuro' in readme:
    problems.append('README ainda descreve "término previsto futuro" (contradiz o código)')
if 'V1–V5' in readme:
    problems.append('README ainda cita Flyway V1–V5')
for path in ('README.md', 'docs/adr/0002-status-sempre-atual.md', 'docs/governance/PLANO-CONFORMIDADE-F5.md'):
    text = open(path, encoding='utf-8').read()
    base = os.path.dirname(path)
    for target in re.findall(r'\]\(([^)#\s]+)(?:#[^)]*)?\)', text):
        if re.match(r'[a-z]+://', target):
            continue
        if not os.path.exists(os.path.normpath(os.path.join(base, target))):
            problems.append(f'{path}: link quebrado {target}')
collection_text = open('docs/api/facilit-kanban.postman_collection.json', encoding='utf-8').read()
json.loads(collection_text)
if 'Date.now() - 3 * 60 * 60 * 1000' not in collection_text:
    problems.append('coleção Postman sem "hoje" em UTC-3')
yml = open('backend/src/main/resources/application.yml', encoding='utf-8').read()
for key in ('time-zone: ${APP_TIME_ZONE:America/Sao_Paulo}', 'cron: "${APP_SCHEDULE_REFRESH_CRON:0 0 0 * * *}"'):
    if key not in yml:
        problems.append(f'application.yml sem {key}')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print(f'   {len(names)} arquivos do lote sem espaço sobrando; README, ADR 0002, links, coleção e application.yml OK')
PY
echo 'F5_L1_STATIC_GREEN'

echo '=== DOCKER: BANCO NOVO + MIGRATIONS 1–6 ==='
RUN_ID="$(date +%s)-$$"
export POSTGRES_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export APP_ADMIN_EMAIL="f5l1-admin-${RUN_ID}@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export APP_DEMO_RESPONSIBLE_NAME='' APP_DEMO_RESPONSIBLE_EMAIL='' APP_DEMO_RESPONSIBLE_POSITION='' APP_DEMO_RESPONSIBLE_PASSWORD=''
export METRICS_PASSWORD='' SESSION_COOKIE_SECURE=false APP_TIME_ZONE=America/Sao_Paulo
GATE_PROJECT=facilit-kanban-f5l1
DC=(docker compose -p "$GATE_PROJECT" -f compose.yaml)

cleanup() {
  set +e
  "${DC[@]}" down -v --remove-orphans >/dev/null 2>&1
  rm -f /tmp/f5l1-*.json /tmp/f5l1-*.cookies
}
trap cleanup EXIT

docker compose down --remove-orphans >/dev/null 2>&1 || true
"${DC[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
run_logged /tmp/f5l1-compose.log "${DC[@]}" up --build -d db backend
for i in $(seq 1 60); do
  if curl -fsS http://localhost:8080/actuator/health >/tmp/f5l1-health.json 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 60 ]; then "${DC[@]}" logs --no-color backend | tail -80; fail 'backend não subiu'; fi
  sleep 2
done
MIGRATIONS="$(sql 'SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank;' | tr '\n' ' ' | sed 's/ *$//')"
test "$MIGRATIONS" = '1 2 3 4 5 6' || fail "migrations aplicadas: '$MIGRATIONS'"
test "$(sql "SELECT is_nullable FROM information_schema.columns WHERE table_name = 'projects' AND column_name = 'schedule_calculated_on';")" = 'NO' \
  || fail 'coluna schedule_calculated_on ausente ou anulável'
for i in $(seq 1 15); do
  "${DC[@]}" logs --no-color backend >/tmp/f5l1-backend-run.log 2>&1
  if grep -q 'gatilho=startup' /tmp/f5l1-backend-run.log; then break; fi
  if [ "$i" -eq 15 ]; then tail -40 /tmp/f5l1-backend-run.log >&2; fail 'recálculo da subida não registrado no log'; fi
  sleep 1
done
echo "   banco novo ($GATE_PROJECT); migrations V$MIGRATIONS; schedule_calculated_on NOT NULL; recálculo na subida registrado"
echo 'F5_L1_DOCKER_CLEAN_DB_GREEN'

echo '=== API: STATUS DE HOJE, FUSO E DATAS REALIZADAS ==='
BASE=http://localhost:8080
JAR=/tmp/f5l1-admin.cookies
SMOKE_ID="$(date +%s)"
TODAY="$(TZ=America/Sao_Paulo date +%F)"
day() { date -d "$TODAY $1 day" +%F; }
echo "   hoje em America/Sao_Paulo: $TODAY · data UTC agora: $(date -u +%F)"
rm -f "$JAR"
PASSED=0

json_get() {
  python3 - "$1" "$2" <<'PY'
import json, sys
with open(sys.argv[1], encoding='utf-8') as handle:
    value = json.load(handle)
for part in sys.argv[2].split('.'):
    value = value[int(part)] if isinstance(value, list) else value[part]
print('' if value is None else value)
PY
}

check() {
  local name="$1" expected="$2" actual="$3"
  if [ "$actual" != "$expected" ]; then
    echo "FALHA: $name — esperado $expected, obtido $actual" >&2
    exit 1
  fi
  PASSED=$((PASSED + 1))
  echo "  ok $PASSED $name"
}

csrf() {
  test "$(curl -sS -o /tmp/f5l1-csrf.json -w '%{http_code}' -b "$JAR" -c "$JAR" "$BASE/api/v1/auth/csrf")" = '200'
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

# Simula dias sem edição: datas e data do cálculo recuam juntas, como se o projeto tivesse sido gravado há N dias.
age_project() {
  test "$(sql "WITH aged AS (UPDATE projects SET planned_start = planned_start - $2, planned_end = planned_end - $2, actual_start = actual_start - $2, actual_end = actual_end - $2, schedule_calculated_on = schedule_calculated_on - $2 WHERE id = '$1' RETURNING 1) SELECT count(*) FROM aged;")" = '1' \
    || fail "não envelheceu o projeto $1"
}

csrf
check 'login ADMIN' '200' "$(call POST /api/v1/auth/login "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" /tmp/f5l1-a01.json)"
csrf
check 'criar responsável' '201' "$(call POST /api/v1/responsibles "{\"name\":\"Responsável F5-L1\",\"email\":\"f5l1-$SMOKE_ID@example.invalid\",\"position\":\"Analista\"}" /tmp/f5l1-a02.json)"
RESPONSIBLE_ID="$(json_get /tmp/f5l1-a02.json id)"

IN_PROGRESS_BODY="\"responsibleIds\":[\"$RESPONSIBLE_ID\"],\"plannedStart\":\"$(day -2)\",\"plannedEnd\":\"$(day +2)\",\"actualStart\":\"$(day -2)\""
check 'criar projeto Em andamento' '201' "$(call POST /api/v1/projects "{\"name\":\"F5L1 $SMOKE_ID vencido\",$IN_PROGRESS_BODY}" /tmp/f5l1-a03.json)"
P1="$(json_get /tmp/f5l1-a03.json id)"
check 'status ao criar' 'IN_PROGRESS 0 50' "$(json_get /tmp/f5l1-a03.json status) $(json_get /tmp/f5l1-a03.json delayDays) $(json_get /tmp/f5l1-a03.json remainingTimePercentage)"
check 'data do cálculo ao criar = hoje' "$TODAY" "$(sql "SELECT schedule_calculated_on FROM projects WHERE id = '$P1';")"

age_project "$P1" 10
UPDATED_AT_BEFORE="$(sql "SELECT updated_at FROM projects WHERE id = '$P1';")"
check 'GET após 10 dias sem edição' '200' "$(call GET "/api/v1/projects/$P1" '' /tmp/f5l1-a04.json)"
check 'status, atraso e % de hoje' 'OVERDUE 8 0' "$(json_get /tmp/f5l1-a04.json status) $(json_get /tmp/f5l1-a04.json delayDays) $(json_get /tmp/f5l1-a04.json remainingTimePercentage)"
check 'data do cálculo gravada = hoje' "$TODAY" "$(sql "SELECT schedule_calculated_on FROM projects WHERE id = '$P1';")"
check 'updated_at intacto (recálculo não é edição)' "$UPDATED_AT_BEFORE" "$(sql "SELECT updated_at FROM projects WHERE id = '$P1';")"

check 'listar Atrasado' '200' "$(call GET "/api/v1/projects?page=0&size=100&status=OVERDUE&text=F5L1%20$SMOKE_ID" '' /tmp/f5l1-a05.json)"
python3 - "$P1" <<'PY'
import json, sys
ids = [item['id'] for item in json.load(open('/tmp/f5l1-a05.json', encoding='utf-8'))['content']]
assert sys.argv[1] in ids, ids
PY
check 'listar Em andamento' '200' "$(call GET "/api/v1/projects?page=0&size=100&status=IN_PROGRESS&text=F5L1%20$SMOKE_ID" '' /tmp/f5l1-a06.json)"
python3 - "$P1" <<'PY'
import json, sys
ids = [item['id'] for item in json.load(open('/tmp/f5l1-a06.json', encoding='utf-8'))['content']]
assert sys.argv[1] not in ids, ids
PY
check 'indicadores' '200' "$(call GET /api/v1/indicators/projects '' /tmp/f5l1-a07.json)"
python3 - <<'PY'
import json
body = json.load(open('/tmp/f5l1-a07.json', encoding='utf-8'))
overdue = next(item for item in body['byStatus'] if item['status'] == 'OVERDUE')
assert overdue['projectCount'] >= 1 and overdue['averageDelayDays'] > 0, body
assert body['delayedProjects'] >= 1, body
PY
GRAPHQL_BODY="$(python3 -c 'import json, sys; print(json.dumps({"query": "query($id: ID!) { project(id: $id) { status delayDays remainingTimePercentage } }", "variables": {"id": sys.argv[1]}}))' "$P1")"
check 'GraphQL do mesmo projeto' '200' "$(call POST /graphql "$GRAPHQL_BODY" /tmp/f5l1-a08.json)"
python3 -c "import json; b = json.load(open('/tmp/f5l1-a08.json')); assert 'errors' not in b, b; p = b['data']['project']; assert (p['status'], p['delayDays'], p['remainingTimePercentage']) == ('OVERDUE', 8, 0), p"

check 'criar segundo projeto Em andamento' '201' "$(call POST /api/v1/projects "{\"name\":\"F5L1 $SMOKE_ID transição\",$IN_PROGRESS_BODY}" /tmp/f5l1-a09.json)"
P2="$(json_get /tmp/f5l1-a09.json id)"
age_project "$P2" 10
check 'Em andamento desatualizado → Atrasado recusado (v1.0.0 aceitava)' '400' "$(call PATCH "/api/v1/projects/$P2/status" '{"status":"OVERDUE"}' /tmp/f5l1-a10.json)"
check 'motivo: já está Atrasado hoje' 'INVALID_REQUEST|Project is already in status OVERDUE' "$(json_get /tmp/f5l1-a10.json code)|$(json_get /tmp/f5l1-a10.json detail)"

check 'criar projeto A iniciar hoje' '201' "$(call POST /api/v1/projects "{\"name\":\"F5L1 $SMOKE_ID fuso\",\"responsibleIds\":[\"$RESPONSIBLE_ID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$(day +10)\"}" /tmp/f5l1-a11.json)"
P3="$(json_get /tmp/f5l1-a11.json id)"
check 'A iniciar → Em andamento' '200' "$(call PATCH "/api/v1/projects/$P3/status" '{"status":"IN_PROGRESS"}' /tmp/f5l1-a12.json)"
check 'início realizado = hoje em São Paulo' "$TODAY" "$(json_get /tmp/f5l1-a12.json actualStart)"

check 'início realizado no futuro recusado' '400' "$(call POST /api/v1/projects "{\"name\":\"F5L1 $SMOKE_ID futuro\",\"responsibleIds\":[\"$RESPONSIBLE_ID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$(day +10)\",\"actualStart\":\"$(day +1)\"}" /tmp/f5l1-a13.json)"
python3 - "$(day +1)" "$TODAY" <<'PY'
import json, sys
body = json.load(open('/tmp/f5l1-a13.json', encoding='utf-8'))
expected = f'Início realizado ({sys.argv[1]}) não pode ser posterior a hoje ({sys.argv[2]})'
assert body['code'] == 'INVALID_REQUEST' and body['detail'].startswith(expected), body
PY

for id in "$P1" "$P2" "$P3"; do
  check "excluir projeto $id" '204' "$(call DELETE "/api/v1/projects/$id" '' /tmp/f5l1-a14.json)"
done
check 'excluir responsável' '204' "$(call DELETE "/api/v1/responsibles/$RESPONSIBLE_ID" '' /tmp/f5l1-a15.json)"
test "$PASSED" -eq 24
echo 'F5_L1_API_GREEN'

printf '%s\n' '=== F5-L1 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige todos os marcadores `F5_L1_*_GREEN`, `=== F5-L1 GREEN ===` e `Resultado: exit code 0`. Traga a saída (`/tmp/saida-gate-f5l1.txt`) para a promoção.

## 3. Depois do GREEN: commits granulares e merge (Gitflow)

Só depois do GREEN. Cada commit compila sozinho, na ordem abaixo.

```sh
cd ~/proj/facilit-desafio-kanban
B=backend/src/main/java/br/com/facilit/kanban
T=backend/src/test/java/br/com/facilit/kanban

git add $B/domain/project/ProjectDates.java $B/domain/project/ProjectStatusTransition.java \
        $T/domain/project/ProjectDatesTest.java $T/domain/project/ProjectStatusTransitionTest.java
git commit -m "fix(domain): parte a transição do status de hoje e recusa data realizada futura"

git add $B/infrastructure/config/ApplicationBeans.java backend/src/main/resources/application.yml \
        compose.yaml .env.example $T/integration/KanbanApiIT.java $T/integration/SecurityApiIT.java \
        docs/api/facilit-kanban.postman_collection.json
git commit -m "fix(config): define hoje pelo fuso de negócio (APP_TIME_ZONE)"

git add backend/src
git commit -m "fix(backend): recalcula status e métricas quando o dia muda"

git add README.md docs
git commit -m "docs(F5-L1): registra ADR 0002, interpretações do enunciado e evidências"

git status --short        # deve ficar vazio
git checkout develop
git merge --no-ff feature/f5-l1-regras-sempre-corretas -m "Merge branch 'feature/f5-l1-regras-sempre-corretas' into develop"
git push origin develop feature/f5-l1-regras-sempre-corretas
git push gitlab develop feature/f5-l1-regras-sempre-corretas
```

O push dispara o CI no GitHub (jobs `frontend`, `backend` e `repository`).
