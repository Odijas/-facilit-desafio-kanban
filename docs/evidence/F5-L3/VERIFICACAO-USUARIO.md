# F5-L3 — VERIFICAÇÃO DO USUÁRIO

O gate prova o lote F5-L3 (camadas de teste completas e transação por caso de uso) na branch `feature/f5-l3-camadas-de-teste`, antes do commit. Ele cobre:

- pré-condições: F5-L2 commitado na base, arquivos alterados iguais aos do pacote, frontend intocado;
- build limpo do backend, com as classes de teste novas e a contagem exata de testes;
- Mockito carregado como agente (sem autoanexo dinâmico);
- checagens estáticas;
- subida com banco novo e migrations 1 a 7 (coluna `version`);
- ponta a ponta pela API: versão incrementada a cada gravação, recusas sem gravação e linhas da tabela com confirmação.

**Isolamento:**

- O gate usa um projeto Compose próprio (`facilit-kanban-f5l3`) e o remove no fim (`down -v` só desse projeto).
- O projeto padrão é apenas parado (`docker compose down`, sem `-v`).
- A porta 8080 precisa estar livre.

## 0. Pré-requisito: F5-L2 encerrado e mesclado na develop

O F5-L2 já foi encerrado, revalidado e promovido. A base esperada deste lote é `develop` em `bf56b61` (ou descendente apenas se explicitamente reconciliado antes de aplicar este pacote). Este pacote **não altera** arquivos de `docs/evidence/F5-L2/`.

## 1. Preparar a branch e aplicar o pacote

O bloco para no primeiro erro, mostra uma mensagem `PARE:` quando algo está fora do esperado e imprime `PREPARO_OK` só se tudo der certo. Serve para a primeira vez e para repetir (se já estiver na branch do lote, só reaplica o pacote).

```sh
cd ~/proj/facilit-desafio-kanban || exit 1
F=feature/f5-l3-camadas-de-teste
EXPECTED_BASE=bf56b61
PACKAGE="$HOME/Downloads/facilit-desafio-kanban-F5-L3-candidate-reconciliado.tar.gz"

[ -z "$(git status --porcelain=v1)" ] || { echo 'PARE: árvore com alterações'; exit 1; }
git switch develop
git pull --ff-only origin develop
[ "$(git rev-parse --short=7 HEAD)" = "$EXPECTED_BASE" ] || { echo "PARE: develop não está em $EXPECTED_BASE"; exit 1; }
[ "$(git rev-parse --short=7 gitlab/develop)" = "$EXPECTED_BASE" ] || { echo "PARE: gitlab/develop não está em $EXPECTED_BASE"; exit 1; }
if git show-ref --verify --quiet "refs/heads/$F"; then
  git switch "$F"
  [ "$(git merge-base "$F" develop)" = "$(git rev-parse develop)" ] || { echo 'PARE: branch F5-L3 não parte da develop atual'; exit 1; }
else
  git switch -c "$F"
fi
sha256sum "$PACKAGE"
tar -xzf "$PACKAGE" -C .
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5-L3/VERIFICACAO-USUARIO.md > /tmp/f5l3-gate.sh
bash -n /tmp/f5l3-gate.sh
echo PREPARO_OK
```

O `sha256sum` acima deve coincidir com o SHA-256 informado junto ao pacote reconciliado.

## 2. Rodar o gate

```sh
bash /tmp/f5l3-gate.sh 2>&1 | tee /tmp/saida-gate-f5l3.txt
```

Os logs longos ficam em `/tmp/f5l3-*.log`.

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
git rev-parse --is-inside-work-tree >/dev/null
rm -f /tmp/f5l3-*

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
  feature/f5-l3-*) ;;
  *) fail "branch atual: $BRANCH (esperado feature/f5-l3-camadas-de-teste)" ;;
esac
git cat-file -e HEAD:backend/src/main/java/br/com/facilit/kanban/delivery/rest/ApiErrorDocumentation.java 2>/dev/null \
  || fail 'o F5-L2 não está presente na base da branch'
git cat-file -e HEAD:docs/evidence/F5-L2/GATE.md 2>/dev/null || fail 'evidências do F5-L2 ausentes no commit base'
git status --porcelain=v1 -uall >/tmp/f5l3-status.txt
python3 - <<'PY'
import sys
expected = {
    'README.md',
    'backend/pom.xml',
    'backend/src/main/java/br/com/facilit/kanban/application/common/TransactionRunner.java',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java',
    'backend/src/main/java/br/com/facilit/kanban/application/responsible/ResponsibleCredentialService.java',
    'backend/src/main/java/br/com/facilit/kanban/application/responsible/ResponsibleService.java',
    'backend/src/main/java/br/com/facilit/kanban/application/secretariat/SecretariatService.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/graphql/GraphQlErrorHandler.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/rest/RestExceptionHandler.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/config/ApplicationBeans.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/config/SpringTransactionRunner.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaEntity.java',
    'backend/src/main/resources/db/migration/V7__add_project_version.sql',
    'backend/src/test/java/br/com/facilit/kanban/application/project/ProjectScheduleRefresherTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceMockitoTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleCredentialServiceTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/responsible/ResponsibleServiceTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/secretariat/SecretariatServiceTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/support/DirectTransactionRunner.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/graphql/ResponsibleGraphQlControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/graphql/SecretariatGraphQlControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsRestControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/rest/ProjectRestControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/rest/ResponsibleRestControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/rest/SecretariatRestControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/integration/ApiSession.java',
    'backend/src/test/java/br/com/facilit/kanban/integration/ProjectPersistenceAdapterIT.java',
    'backend/src/test/java/br/com/facilit/kanban/integration/StatusTransitionApiIT.java',
    'backend/src/test/java/br/com/facilit/kanban/integration/TransactionIT.java',
    'docs/evidence/F5-L3/CONSUMIDORES.md',
    'docs/evidence/F5-L3/DECISOES.md',
    'docs/evidence/F5-L3/EXECUCAO.md',
    'docs/evidence/F5-L3/FONTES-RAG.md',
    'docs/evidence/F5-L3/GATE.md',
    'docs/evidence/F5-L3/LEITURA.md',
    'docs/evidence/F5-L3/MATRIZ.md',
    'docs/evidence/F5-L3/RISCOS.md',
    'docs/evidence/F5-L3/VERIFICACAO-USUARIO.md',
}
actual = set()
for line in open('/tmp/f5l3-status.txt', encoding='utf-8'):
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
if any(path.startswith('frontend/') for path in actual):
    sys.exit('o lote não deveria alterar o frontend')
print(f'   {len(actual)} arquivos alterados, todos do pacote F5-L3; frontend intocado')
PY
echo "   branch $BRANCH sobre o F5-L2 commitado"
echo 'F5_L3_PRECONDITIONS_GREEN'

printf '%s\n' '=== BACKEND: CLEAN VERIFY ==='
(cd backend && run_logged /tmp/f5l3-backend.log mvn -B -ntp clean verify)
grep -q 'BUILD SUCCESS' /tmp/f5l3-backend.log
if grep -q '\[deprecation\]' /tmp/f5l3-backend.log; then grep '\[deprecation\]' /tmp/f5l3-backend.log >&2; fail 'uso de API depreciada no código do backend'; fi
if grep -Eqi 'self-attaching|loaded dynamically' /tmp/f5l3-backend.log; then
  grep -Ei 'self-attaching|loaded dynamically' /tmp/f5l3-backend.log | sort | uniq -c >&2
  fail 'o Mockito foi anexado dinamicamente (o agente do argLine não foi usado)'
fi
echo '   Mockito carregado como agente (sem autoanexo dinâmico)'
TOOL_DEPRECATIONS="$(grep -ci 'deprecat' /tmp/f5l3-backend.log || true)"
echo "   linhas com 'deprecat' no log do Maven (ferramentas, informativo): $TOOL_DEPRECATIONS"
python3 - <<'PY'
import glob
import xml.etree.ElementTree as ET
required = {
    'br.com.facilit.kanban.delivery.rest.ProjectRestControllerTest': 17,
    'br.com.facilit.kanban.delivery.rest.ResponsibleRestControllerTest': 9,
    'br.com.facilit.kanban.delivery.rest.SecretariatRestControllerTest': 6,
    'br.com.facilit.kanban.delivery.rest.ProjectIndicatorsRestControllerTest': 2,
    'br.com.facilit.kanban.delivery.graphql.ProjectGraphQlControllerTest': 5,
    'br.com.facilit.kanban.delivery.graphql.ResponsibleGraphQlControllerTest': 3,
    'br.com.facilit.kanban.delivery.graphql.SecretariatGraphQlControllerTest': 2,
    'br.com.facilit.kanban.application.project.ProjectServiceMockitoTest': 5,
    'br.com.facilit.kanban.application.project.ProjectServiceTest': 9,
    'br.com.facilit.kanban.domain.project.ProjectStatusTransitionTest': 24,
    'br.com.facilit.kanban.integration.ProjectPersistenceAdapterIT': 5,
    'br.com.facilit.kanban.integration.TransactionIT': 4,
    'br.com.facilit.kanban.integration.StatusTransitionApiIT': 16,
}
expected_totals = {'unitários': (23, 140), 'integração': (9, 45)}
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
    assert totals['failures'] == 0 and totals['errors'] == 0 and totals['skipped'] == 0, (kind, totals)
    print(f"   {kind}: {len(files)} classes, {totals['tests']} testes, {totals['failures']} falhas, {totals['errors']} erros, {totals['skipped']} ignorados")
    assert (len(files), totals['tests']) == expected_totals[kind], (kind, len(files), totals['tests'], expected_totals[kind])
for name, count in required.items():
    assert name in found, f'relatório ausente: {name}'
    assert found[name] == (count, 0, 0, 0), (name, found[name], count)
    print(f"   {name.rsplit('.', 1)[1]}: {count} testes OK")
PY
echo 'F5_L3_BACKEND_GREEN'

echo '=== ESTÁTICA ==='
git diff --check
python3 - <<'PY'
import os, re, sys
problems = []
names = [line[3:].rstrip('\n') for line in open('/tmp/f5l3-status.txt', encoding='utf-8') if line.strip()]
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
for text in ('### F5-L3', 'Flyway V1–V7', 'TransactionRunner', 'StatusTransitionApiIT', '@WebMvcTest', '@GraphQlTest',
             'F5-L2 — Contrato de erro, confirmações, Swagger e logs: GREEN'):
    if text not in readme:
        problems.append(f'README sem "{text}"')
if 'Flyway V1–V6' in readme:
    problems.append('README ainda cita Flyway V1–V6')
if '**GREEN**' not in open('docs/evidence/F5-L2/GATE.md', encoding='utf-8').read():
    problems.append('F5-L2 não promovido a GREEN')
if 'CANDIDATE' in open('docs/evidence/F5-L2/MATRIZ.md', encoding='utf-8').read():
    problems.append('matriz do F5-L2 ainda com CANDIDATE')
pom = open('backend/pom.xml', encoding='utf-8').read()
if pom.count('<argLine>-javaagent:${org.mockito:mockito-core:jar}</argLine>') != 2:
    problems.append('pom.xml sem o agente do Mockito no Surefire e no Failsafe')
migration = open('backend/src/main/resources/db/migration/V7__add_project_version.sql', encoding='utf-8').read()
if 'ADD COLUMN version BIGINT NOT NULL DEFAULT 0' not in migration:
    problems.append('V7 sem a coluna version')
entity = open('backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaEntity.java', encoding='utf-8').read()
if not re.search(r'@Version\s+@Column\(nullable = false\)\s+private Long version;', entity):
    problems.append('ProjectJpaEntity sem @Version Long version')
for root, _, files in os.walk('backend/src/test/java'):
    for file in files:
        text = open(os.path.join(root, file), encoding='utf-8').read()
        if 'org.springframework.boot.test.mock.mockito' in text:
            problems.append(f'@MockBean depreciado em {file}')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print(f'   {len(names)} arquivos do lote OK; README, F5-L2 GREEN, pom (agente do Mockito), V7 e @Version OK; sem @MockBean')
PY
echo 'F5_L3_STATIC_GREEN'

echo '=== DOCKER: BANCO NOVO + MIGRATIONS 1–7 ==='
RUN_ID="$(date +%s)-$$"
export POSTGRES_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export APP_ADMIN_EMAIL="f5l3-admin-${RUN_ID}@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export APP_DEMO_RESPONSIBLE_NAME='' APP_DEMO_RESPONSIBLE_EMAIL='' APP_DEMO_RESPONSIBLE_POSITION='' APP_DEMO_RESPONSIBLE_PASSWORD=''
export METRICS_PASSWORD='' SESSION_COOKIE_SECURE=false APP_TIME_ZONE=America/Sao_Paulo
GATE_PROJECT=facilit-kanban-f5l3
DC=(docker compose -p "$GATE_PROJECT" -f compose.yaml)

cleanup() {
  set +e
  "${DC[@]}" down -v --remove-orphans >/dev/null 2>&1
  rm -f /tmp/f5l3-*.json /tmp/f5l3-*.cookies
}
trap cleanup EXIT

docker compose down --remove-orphans >/dev/null 2>&1 || true
"${DC[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
run_logged /tmp/f5l3-compose.log "${DC[@]}" up --build -d db backend
for i in $(seq 1 60); do
  if curl -fsS http://localhost:8080/actuator/health >/tmp/f5l3-health.json 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 60 ]; then "${DC[@]}" logs --no-color backend | tail -80; fail 'backend não subiu'; fi
  sleep 2
done
MIGRATIONS="$(sql 'SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank;' | tr '\n' ' ' | sed 's/ *$//')"
test "$MIGRATIONS" = '1 2 3 4 5 6 7' || fail "migrations aplicadas: '$MIGRATIONS'"
VERSION_COLUMN="$(sql "SELECT data_type || ' ' || is_nullable || ' ' || column_default FROM information_schema.columns WHERE table_name = 'projects' AND column_name = 'version';")"
test "$VERSION_COLUMN" = 'bigint NO 0' || fail "coluna projects.version: '$VERSION_COLUMN'"
echo "   banco novo ($GATE_PROJECT); migrations V$MIGRATIONS; projects.version $VERSION_COLUMN"
echo 'F5_L3_DOCKER_GREEN'

echo '=== API: VERSÃO, RECUSAS SEM GRAVAÇÃO E TABELA COM CONFIRMAÇÃO ==='
BASE=http://localhost:8080
JAR=/tmp/f5l3-admin.cookies
SMOKE_ID="$(date +%s)"
TODAY="$(TZ=America/Sao_Paulo date +%F)"
day() { date -d "$TODAY $1 day" +%F; }
RESPONSIBLE_EMAIL="f5l3-$SMOKE_ID@example.invalid"
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
  test "$(curl -sS -o /tmp/f5l3-csrf.json -w '%{http_code}' -b "$JAR" -c "$JAR" "$BASE/api/v1/auth/csrf")" = '200'
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

version_of() {
  sql "SELECT version FROM projects WHERE id = '$1';"
}

csrf
check 'login ADMIN' '200' "$(call POST /api/v1/auth/login "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" /tmp/f5l3-a01.json)"
csrf
check 'criar responsável' '201' "$(call POST /api/v1/responsibles "{\"name\":\"Responsável F5-L3\",\"email\":\"$RESPONSIBLE_EMAIL\",\"position\":\"Analista\"}" /tmp/f5l3-a02.json)"
RESPONSIBLE_ID="$(python3 -c "import json; print(json.load(open('/tmp/f5l3-a02.json'))['id'])")"
PROJECT_BODY="\"responsibleIds\":[\"$RESPONSIBLE_ID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$(day +10)\""

check 'criar projeto A iniciar' '201' "$(call POST /api/v1/projects "{\"name\":\"F5L3 $SMOKE_ID\",$PROJECT_BODY}" /tmp/f5l3-a03.json)"
P1="$(python3 -c "import json; print(json.load(open('/tmp/f5l3-a03.json'))['id'])")"
expect_json /tmp/f5l3-a03.json status=NOT_STARTED
check 'versão ao criar' '0' "$(version_of "$P1")"
check 'editar projeto' '200' "$(call PUT "/api/v1/projects/$P1" "{\"name\":\"F5L3 $SMOKE_ID editado\",$PROJECT_BODY}" /tmp/f5l3-a04.json)"
check 'versão após editar' '1' "$(version_of "$P1")"
check 'linha 1: A iniciar → Em andamento' '200' "$(call PATCH "/api/v1/projects/$P1/status" '{"status":"IN_PROGRESS"}' /tmp/f5l3-a05.json)"
expect_json /tmp/f5l3-a05.json status=IN_PROGRESS "actualStart=$TODAY"
check 'versão após transição' '2' "$(version_of "$P1")"
check 'linha 5: Em andamento → Atrasado → 422' '422' "$(call PATCH "/api/v1/projects/$P1/status" '{"status":"OVERDUE"}' /tmp/f5l3-a06.json)"
expect_json /tmp/f5l3-a06.json code=TRANSITION_BLOCKED currentStatus=IN_PROGRESS requestedStatus=OVERDUE \
  'detail=^Em andamento → Atrasado bloqueado:'
check 'linha 4 sem confirm → 422' '422' "$(call PATCH "/api/v1/projects/$P1/status" '{"status":"NOT_STARTED"}' /tmp/f5l3-a07.json)"
expect_json /tmp/f5l3-a07.json code=CONFIRMATION_REQUIRED clearedField=actualStart
check 'recusas não gravam (versão igual)' '2' "$(version_of "$P1")"
check 'linha 6: Em andamento → Concluído' '200' "$(call PATCH "/api/v1/projects/$P1/status" '{"status":"COMPLETED"}' /tmp/f5l3-a08.json)"
expect_json /tmp/f5l3-a08.json status=COMPLETED "actualEnd=$TODAY"
check 'linha 12 com confirm, datas não dão Atrasado → 422' '422' "$(call PATCH "/api/v1/projects/$P1/status" '{"status":"OVERDUE","confirm":true}' /tmp/f5l3-a09.json)"
expect_json /tmp/f5l3-a09.json code=TRANSITION_BLOCKED currentStatus=COMPLETED requestedStatus=OVERDUE \
  'detail=^Concluído → Atrasado bloqueado:'
check 'linha 11 sem confirm → 422' '422' "$(call PATCH "/api/v1/projects/$P1/status" '{"status":"IN_PROGRESS"}' /tmp/f5l3-a10.json)"
expect_json /tmp/f5l3-a10.json code=CONFIRMATION_REQUIRED clearedField=actualEnd
check 'linha 11 com confirm → 200' '200' "$(call PATCH "/api/v1/projects/$P1/status" '{"status":"IN_PROGRESS","confirm":true}' /tmp/f5l3-a11.json)"
expect_json /tmp/f5l3-a11.json status=IN_PROGRESS actualEnd=
check 'versão final' '4' "$(version_of "$P1")"
check 'excluir projeto' '204' "$(call DELETE "/api/v1/projects/$P1" '' /tmp/f5l3-a12.json)"
check 'excluir responsável' '204' "$(call DELETE "/api/v1/responsibles/$RESPONSIBLE_ID" '' /tmp/f5l3-a13.json)"
test "$PASSED" -eq 18
echo 'F5_L3_API_GREEN'

printf '%s\n' '=== F5-L3 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige todos os marcadores `F5_L3_*_GREEN`, `=== F5-L3 GREEN ===` e `Resultado: exit code 0`. `[EXECUTADO PELO USUÁRIO · 2026-09-24]` esses marcadores foram obtidos com exit code 0; a saída deve ser persistida em `docs/evidence/F5-L3/SAIDA-GATE.txt` antes do commit documental.

## 3. Depois do GREEN: commits granulares e merge (Gitflow)

Os commits abaixo separam o lote por responsabilidade. O gate GREEN foi executado sobre o lote integral antes da criação dos commits; não se declara compilação isolada de cada commit sem execução específica.

```sh
cd ~/proj/facilit-desafio-kanban
B=backend/src/main/java/br/com/facilit/kanban
T=backend/src/test/java/br/com/facilit/kanban

git add $B/application/common/TransactionRunner.java $B/infrastructure/config/SpringTransactionRunner.java \
        $B/infrastructure/config/ApplicationBeans.java $B/application/project/ProjectService.java \
        $B/application/responsible/ResponsibleService.java $B/application/responsible/ResponsibleCredentialService.java \
        $B/application/secretariat/SecretariatService.java
git add $T/application/support/DirectTransactionRunner.java $T/application/project/ProjectServiceTest.java \
        $T/application/project/ProjectScheduleRefresherTest.java $T/application/responsible/ResponsibleServiceTest.java \
        $T/application/responsible/ResponsibleCredentialServiceTest.java $T/application/secretariat/SecretariatServiceTest.java
git commit -m "feat(backend): executa cada caso de uso de escrita numa transação (porta TransactionRunner)"

git add backend/src/main/resources/db/migration/V7__add_project_version.sql \
        $B/infrastructure/persistence/project/ProjectJpaEntity.java \
        $B/delivery/rest/RestExceptionHandler.java $B/delivery/graphql/GraphQlErrorHandler.java \
        $T/integration/TransactionIT.java
git commit -m "feat(backend): controle de concorrência otimista em projetos (@Version, 409 CONFLICT)"

git add backend/pom.xml $T/delivery $T/application/project/ProjectServiceMockitoTest.java
git commit -m "test(backend): controllers REST e GraphQL e ProjectService com Mockito"

git add $T/integration/ApiSession.java $T/integration/ProjectPersistenceAdapterIT.java \
        $T/integration/StatusTransitionApiIT.java
git commit -m "test(backend): repositório com PostgreSQL real e tabela de transição pela API"

git add README.md docs
git commit -m "docs(F5-L3): registra as evidências do F5-L3"

git status --short        # deve ficar vazio
git checkout develop
git merge --no-ff feature/f5-l3-camadas-de-teste -m "Merge branch 'feature/f5-l3-camadas-de-teste' into develop"
git push origin develop feature/f5-l3-camadas-de-teste
git push gitlab develop feature/f5-l3-camadas-de-teste
```
