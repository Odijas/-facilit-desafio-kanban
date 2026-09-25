# F5-C2 — VERIFICAÇÃO DO USUÁRIO

O gate prova o lote F5-C2 (rigor de testes e validação) na branch `bugfix/2.0.0-c2-testes-validacao`, criada da `release/2.0.0` já com o F5-C1, antes do commit. Ele cobre:

- pré-condições: F5-C1 commitado na base, arquivos alterados iguais aos do pacote, frontend intocado;
- build limpo do backend: contagem exata, BDD com o passo de métricas nas 12 linhas e JaCoCo ≥ 95%;
- checagens estáticas;
- subida com banco novo (a imagem é construída de novo, com o Dockerfile do F5-C1);
- limites de entrada na API real (REST e GraphQL) e no OpenAPI;
- CI da `release/2.0.0` com o F5-C1 (`F5_C1_CI_GREEN`).

**Isolamento:**

- O gate usa um projeto Compose próprio (`facilit-kanban-f5c2`) e o remove no fim (`down -v` só desse projeto).
- O projeto padrão é apenas parado (`docker compose down`, sem `-v`).
- A porta 8080 precisa estar livre.

## 0. Estágio Gitflow do F5-C1 (se ainda não foi feito)

Commits semânticos do F5-C1, merge `--no-ff` na `release/2.0.0` e push nos dois remotos. É a seção 4 de `docs/evidence/F5-C1/VERIFICACAO-USUARIO.md`.

```sh
cd ~/proj/facilit-desafio-kanban
git checkout bugfix/2.0.0-c1-entrega

git add backend/Dockerfile
git commit -m "fix(docker): constrói a imagem do backend sem o limite de cobertura medido com testes de integração"

git add .github/workflows/ci.yml
git commit -m "ci(backend): constrói a imagem Docker do backend no CI"

git add backend/src/main/resources/application.yml backend/src/test/java/br/com/facilit/kanban/integration/OpenApiContractIT.java
git commit -m "fix(api): Swagger UI envia o token CSRF exigido pela API"

git add README.md docs
git commit -m "docs(F5-C1): plano de correção da release, roteiro do Swagger e evidências"

git status --short        # deve ficar vazio
git checkout release/2.0.0
git merge --no-ff bugfix/2.0.0-c1-entrega -m "Merge branch 'bugfix/2.0.0-c1-entrega' into release/2.0.0"
git push origin release/2.0.0 bugfix/2.0.0-c1-entrega
git push gitlab release/2.0.0 bugfix/2.0.0-c1-entrega
```

O push da `release/2.0.0` dispara o CI. O gate deste lote confere o resultado no fim (`F5_C1_CI_GREEN`).

## 1. Preparar a branch e aplicar o pacote

O bloco para no primeiro erro, mostra uma mensagem `PARE:` quando algo está fora do esperado e imprime `PREPARO_OK` só se tudo der certo. Serve para a primeira vez e para repetir (se já estiver na branch do lote, só reaplica o pacote).

```sh
cd ~/proj/facilit-desafio-kanban && F=bugfix/2.0.0-c2-testes-validacao && \
if [ "$(git branch --show-current)" != "$F" ]; then \
  { [ -z "$(git status --porcelain=v1)" ] || { echo 'PARE: árvore com alterações (o F5-C1 foi commitado? seção 0)'; false; }; } && \
  git checkout release/2.0.0 && git pull --ff-only origin release/2.0.0 && \
  { if git rev-parse -q --verify "refs/heads/$F" >/dev/null; then git checkout "$F"; else git checkout -b "$F"; fi; }; \
fi && \
{ git cat-file -p HEAD:backend/Dockerfile | grep -q 'jacoco.skip' && git cat-file -e HEAD:docs/evidence/F5-C1/GATE.md \
  || { echo 'PARE: a base da branch não tem o F5-C1 commitado (seção 0)'; false; }; } && \
(cd ~/Downloads && echo "<SHA256>  facilit-desafio-kanban-F5-C2-candidate.tar.gz" | sha256sum -c -) && \
tar -xzf ~/Downloads/facilit-desafio-kanban-F5-C2-candidate.tar.gz && \
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5-C2/VERIFICACAO-USUARIO.md > /tmp/f5c2-gate.sh && \
echo PREPARO_OK
```

Troque `<SHA256>` pelo hash informado com o pacote.

## 2. Rodar o gate

```sh
bash /tmp/f5c2-gate.sh 2>&1 | tee /tmp/saida-gate-f5c2.txt
```

Os logs longos ficam em `/tmp/f5c2-*.log`. No fim, o gate espera o CI da `release/2.0.0` terminar (até 35 minutos).

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
git rev-parse --is-inside-work-tree >/dev/null
rm -f /tmp/f5c2-*

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
  bugfix/2.0.0-*) ;;
  *) fail "branch atual: $BRANCH (esperado bugfix/2.0.0-c2-testes-validacao)" ;;
esac
git cat-file -p HEAD:backend/Dockerfile | grep -q 'RUN mvn -B -ntp -DskipITs -Djacoco.skip=true verify' \
  || fail 'a base da branch não tem o F5-C1 commitado (seção 0)'
git cat-file -e HEAD:docs/evidence/F5-C1/GATE.md 2>/dev/null || fail 'evidências do F5-C1 ausentes no commit base'
git status --porcelain=v1 -uall >/tmp/f5c2-status.txt
python3 - <<'PY'
import sys
expected = {
    'README.md',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectFilter.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/common/InputLimits.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ResponsibleGraphQlController.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/graphql/SecretariatGraphQlController.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRequest.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/rest/ResponsibleRequest.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/rest/SecretariatRequest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/project/ProjectFilterTest.java',
    'backend/src/test/java/br/com/facilit/kanban/bdd/StatusTransitionSteps.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/rest/ProjectRestControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/rest/ResponsibleRestControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/rest/SecretariatRestControllerTest.java',
    'backend/src/test/resources/features/transicoes.feature',
    'docs/evidence/F5-C1/GATE.md',
    'docs/evidence/F5-C1/MATRIZ.md',
    'docs/evidence/F5-C1/SAIDA-GATE.txt',
    'docs/evidence/F5-C2/CONSUMIDORES.md',
    'docs/evidence/F5-C2/DECISOES.md',
    'docs/evidence/F5-C2/EXECUCAO.md',
    'docs/evidence/F5-C2/FONTES-RAG.md',
    'docs/evidence/F5-C2/GATE.md',
    'docs/evidence/F5-C2/LEITURA.md',
    'docs/evidence/F5-C2/MATRIZ.md',
    'docs/evidence/F5-C2/RISCOS.md',
    'docs/evidence/F5-C2/VERIFICACAO-USUARIO.md',
}
actual = set()
for line in open('/tmp/f5c2-status.txt', encoding='utf-8'):
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
print(f'   {len(actual)} arquivos alterados, todos do pacote F5-C2; frontend intocado')
PY
echo "   branch $BRANCH sobre a release/2.0.0 com o F5-C1 ($(git rev-parse --short HEAD))"
echo 'F5_C2_PRECONDITIONS_GREEN'

printf '%s\n' '=== BACKEND: CLEAN VERIFY + BDD + JACOCO ==='
(cd backend && run_logged /tmp/f5c2-backend.log mvn -B -ntp clean verify)
grep -q 'BUILD SUCCESS' /tmp/f5c2-backend.log
if grep -q '\[deprecation\]' /tmp/f5c2-backend.log; then grep '\[deprecation\]' /tmp/f5c2-backend.log >&2; fail 'uso de API depreciada no código do backend'; fi
if grep -Eqi 'self-attaching|loaded dynamically' /tmp/f5c2-backend.log; then fail 'o Mockito foi anexado dinamicamente'; fi
python3 - <<'PY'
import glob, json
import xml.etree.ElementTree as ET
expected_totals = {'unitários/BDD': (25, 174), 'integração': (12, 49)}
required = {
    'br.com.facilit.kanban.delivery.rest.ProjectRestControllerTest': 20,
    'br.com.facilit.kanban.delivery.rest.ResponsibleRestControllerTest': 10,
    'br.com.facilit.kanban.delivery.rest.SecretariatRestControllerTest': 7,
    'br.com.facilit.kanban.delivery.graphql.ProjectGraphQlControllerTest': 10,
    'br.com.facilit.kanban.application.project.ProjectFilterTest': 3,
    'br.com.facilit.kanban.integration.OpenApiContractIT': 4,
}
found = {}
for kind, pattern in (('unitários/BDD', 'backend/target/surefire-reports/TEST-*.xml'), ('integração', 'backend/target/failsafe-reports/TEST-*.xml')):
    totals = dict(tests=0, failures=0, errors=0, skipped=0)
    files = glob.glob(pattern)
    for name in files:
        root = ET.parse(name).getroot()
        for key in totals:
            totals[key] += int(root.get(key, 0))
        found[root.get('name')] = tuple(int(root.get(key, 0)) for key in ('tests', 'failures', 'errors', 'skipped'))
    assert totals['failures'] == 0 and totals['errors'] == 0 and totals['skipped'] == 0, (kind, totals)
    print(f"   {kind}: {len(files)} relatórios, {totals['tests']} testes, 0 falhas/erros/ignorados")
    assert (len(files), totals['tests']) == expected_totals[kind], (kind, len(files), totals['tests'], expected_totals[kind])
for name, count in required.items():
    assert found.get(name) == (count, 0, 0, 0), (name, found.get(name), count)
    print(f"   {name.rsplit('.', 1)[1]}: {count} testes OK")
features = json.load(open('backend/target/cucumber.json', encoding='utf-8'))
scenarios = [element for feature in features for element in feature.get('elements', []) if element.get('type') == 'scenario']
steps = [step for scenario in scenarios for step in scenario.get('steps', [])]
assert len(scenarios) == 12, len(scenarios)
assert all(step['result']['status'] == 'passed' for step in steps), [s['name'] for s in steps if s['result']['status'] != 'passed']
assert len(steps) == 48, len(steps)
metric_steps = [step for step in steps if step['name'].startswith('as métricas depois da transição são atraso')]
assert len(metric_steps) == 12, len(metric_steps)
print(f'   BDD: {len(scenarios)} cenários, {len(steps)} passos aprovados; métricas conferidas nas 12 linhas')
report = ET.parse('backend/target/site/jacoco/jacoco.xml').getroot()
line = next(c for c in report.findall('counter') if c.get('type') == 'LINE')
covered, missed = int(line.get('covered')), int(line.get('missed'))
ratio = covered / (covered + missed)
print(f'   JaCoCo (unitários + integração): {ratio:.2%} de linhas ({covered}/{covered + missed})')
assert ratio >= 0.95, ratio
PY
echo 'F5_C2_BACKEND_GREEN'

echo '=== ESTÁTICA ==='
git diff --check
python3 - <<'PY'
import os, re, sys
problems = []
names = [line[3:].rstrip('\n') for line in open('/tmp/f5c2-status.txt', encoding='utf-8') if line.strip()]
for name in names:
    if not os.path.isfile(name):
        continue
    data = open(name, 'rb').read()
    for index, text in enumerate(data.decode('utf-8').split('\n'), 1):
        if text != text.rstrip(' \t'):
            problems.append(f'espaço no fim da linha: {name}:{index}')
            break
    if not data.endswith(b'\n') or data.endswith(b'\n\n'):
        problems.append(f'fim de arquivo sem exatamente uma quebra de linha: {name}')
feature = open('backend/src/test/resources/features/transicoes.feature', encoding='utf-8').read()
if '| atraso | percentual |' not in feature or feature.count('| BLOCKED      | -      | -          |') != 5:
    problems.append('transicoes.feature sem as colunas de métricas')
limits = open('backend/src/main/java/br/com/facilit/kanban/delivery/common/InputLimits.java', encoding='utf-8').read()
for text in ('NAME_MAX_LENGTH = 200', 'EMAIL_MAX_LENGTH = 254', 'RESPONSIBLES_MAX = 50'):
    if text not in limits:
        problems.append(f'InputLimits sem {text}')
if 'TEXT_MAX_LENGTH = 100' not in open('backend/src/main/java/br/com/facilit/kanban/application/project/ProjectFilter.java', encoding='utf-8').read():
    problems.append('ProjectFilter sem TEXT_MAX_LENGTH = 100')
readme = open('README.md', encoding='utf-8').read()
for text in ('Limites de entrada, iguais em REST e GraphQL', 'F5-C1 — Entrega executável', 'GREEN em 2026-09-24.\n- F5-C2'):
    if text not in readme:
        problems.append(f'README sem "{text.splitlines()[0]}"')
for heading in ('Visão geral', 'Como rodar', 'Como testar', 'Swagger', 'Limitações e próximos passos'):
    if not re.search(r'^#{1,3} .*' + re.escape(heading), readme, re.M | re.I):
        problems.append(f'README sem a seção exigida pelo freeze: {heading}')
if '**GREEN**' not in open('docs/evidence/F5-C1/GATE.md', encoding='utf-8').read():
    problems.append('F5-C1 não promovido a GREEN')
if 'CANDIDATE' in open('docs/evidence/F5-C1/MATRIZ.md', encoding='utf-8').read():
    problems.append('matriz do F5-C1 ainda com CANDIDATE')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print(f'   {len(names)} arquivos do lote OK; BDD com métricas, limites, README e F5-C1 GREEN')
PY
echo 'F5_C2_STATIC_GREEN'

echo '=== DOCKER: BANCO NOVO + MIGRATIONS 1–7 ==='
RUN_ID="$(date +%s)"
export POSTGRES_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export APP_ADMIN_EMAIL="f5c2-admin-${RUN_ID}@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export APP_DEMO_RESPONSIBLE_NAME='' APP_DEMO_RESPONSIBLE_EMAIL='' APP_DEMO_RESPONSIBLE_POSITION='' APP_DEMO_RESPONSIBLE_PASSWORD=''
export METRICS_PASSWORD='' SESSION_COOKIE_SECURE=false APP_TIME_ZONE=America/Sao_Paulo
GATE_PROJECT=facilit-kanban-f5c2
DC=(docker compose -p "$GATE_PROJECT" -f compose.yaml)

cleanup() {
  set +e
  "${DC[@]}" down -v --remove-orphans >/dev/null 2>&1
  rm -f /tmp/f5c2-*.json /tmp/f5c2-*.cookies
}
trap cleanup EXIT

docker compose down --remove-orphans >/dev/null 2>&1 || true
"${DC[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
run_logged /tmp/f5c2-compose.log "${DC[@]}" up --build -d db backend
for i in $(seq 1 60); do
  if curl -fsS http://localhost:8080/actuator/health >/tmp/f5c2-health.json 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 60 ]; then "${DC[@]}" logs --no-color backend | tail -80; fail 'backend não subiu'; fi
  sleep 2
done
MIGRATIONS="$(sql 'SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank;' | tr '\n' ' ' | sed 's/ *$//')"
test "$MIGRATIONS" = '1 2 3 4 5 6 7' || fail "migrations aplicadas: '$MIGRATIONS'"
echo "   banco novo ($GATE_PROJECT); migrations V$MIGRATIONS; imagem construída com o Dockerfile do F5-C1"
echo 'F5_C2_DOCKER_GREEN'

echo '=== API: LIMITES DE ENTRADA (REST, GRAPHQL E OPENAPI) ==='
BASE=http://localhost:8080
JAR=/tmp/f5c2-admin.cookies
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

cookie_token() {
  if [ -f "$JAR" ]; then awk '$6 == "XSRF-TOKEN" { value=$7 } END { print value }' "$JAR"; fi
}

call() {
  local method="$1" path="$2" body="$3" out="$4" token
  local args=(-sS -o "$out" -w '%{http_code}' -X "$method" -b "$JAR" -c "$JAR" -H 'Content-Type: application/json')
  token="$(cookie_token)"
  if [ -n "$token" ]; then args+=(-H "X-XSRF-TOKEN: $token"); fi
  if [ -n "$body" ]; then args+=(--data "$body"); fi
  curl "${args[@]}" "$BASE$path"
}

# Textos de teste: e-mails bem formados com 254 e 255 caracteres (parte local 64, rótulos de domínio ≤ 63).
python3 - "$RUN_ID" <<'PY' >/tmp/f5c2-values.env
import shlex, sys
run = sys.argv[1]
local = ('f5c2' + run).ljust(64, 'a')
values = {
    'EMAIL254': local + '@' + 'b' * 63 + '.' + 'c' * 63 + '.' + 'd' * 57 + '.com',
    'EMAIL255': local + '@' + 'b' * 63 + '.' + 'c' * 63 + '.' + 'd' * 58 + '.com',
    'NAME200': 'n' * 200,
    'NAME201': 'n' * 201,
    'TEXT101': 't' * 101,
    'IDS51': ','.join(f'"{i:08d}-0000-4000-8000-000000000000"' for i in range(51)),
}
assert (len(values['EMAIL254']), len(values['EMAIL255'])) == (254, 255)
for key, value in values.items():
    print(f'{key}={shlex.quote(value)}')
PY
# shellcheck disable=SC1091
. /tmp/f5c2-values.env

check 'passo 1: GET /api/v1/auth/csrf' '200' "$(call GET /api/v1/auth/csrf '' /tmp/f5c2-a01.json)"
check 'login ADMIN' '200' "$(call POST /api/v1/auth/login "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" /tmp/f5c2-a02.json)"
check 'token novo após o login' '200' "$(call GET /api/v1/auth/csrf '' /tmp/f5c2-a03.json)"

check 'responsável com e-mail de 255 → 400' '400' "$(call POST /api/v1/responsibles "{\"name\":\"Limite\",\"email\":\"$EMAIL255\",\"position\":\"Analista\"}" /tmp/f5c2-a04.json)"
expect_json /tmp/f5c2-a04.json code=VALIDATION_ERROR violations.0.field=email 'violations.0.message=tamanho deve ser entre 0 e 254'
check 'responsável com nome de 201 → 400' '400' "$(call POST /api/v1/responsibles "{\"name\":\"$NAME201\",\"email\":\"n201-$RUN_ID@example.invalid\",\"position\":\"Analista\"}" /tmp/f5c2-a05.json)"
expect_json /tmp/f5c2-a05.json code=VALIDATION_ERROR violations.0.field=name 'violations.0.message=tamanho deve ser entre 0 e 200'
check 'responsável no limite (nome 200, e-mail 254) → 201' '201' "$(call POST /api/v1/responsibles "{\"name\":\"$NAME200\",\"email\":\"$EMAIL254\",\"position\":\"Analista\"}" /tmp/f5c2-a06.json)"
RESPONSIBLE_ID="$(python3 -c "import json; print(json.load(open('/tmp/f5c2-a06.json'))['id'])")"

check 'projeto com nome de 201 → 400' '400' "$(call POST /api/v1/projects "{\"name\":\"$NAME201\",\"responsibleIds\":[\"$RESPONSIBLE_ID\"]}" /tmp/f5c2-a07.json)"
expect_json /tmp/f5c2-a07.json code=VALIDATION_ERROR violations.0.field=name
check 'projeto com 51 responsáveis → 400' '400' "$(call POST /api/v1/projects "{\"name\":\"Limite\",\"responsibleIds\":[$IDS51]}" /tmp/f5c2-a08.json)"
expect_json /tmp/f5c2-a08.json code=VALIDATION_ERROR violations.0.field=responsibleIds 'violations.0.message=tamanho deve ser entre 0 e 50'
check 'projeto no limite (nome 200) → 201' '201' "$(call POST /api/v1/projects "{\"name\":\"$NAME200\",\"responsibleIds\":[\"$RESPONSIBLE_ID\"]}" /tmp/f5c2-a09.json)"
PROJECT_ID="$(python3 -c "import json; print(json.load(open('/tmp/f5c2-a09.json'))['id'])")"
check 'busca com texto de 101 → 400' '400' "$(call GET "/api/v1/projects?text=$TEXT101" '' /tmp/f5c2-a10.json)"
expect_json /tmp/f5c2-a10.json code=INVALID_REQUEST 'detail=Texto de busca muito longo: use no máximo 100 caracteres (text).'

python3 -c 'import json, sys; print(json.dumps({"query": "mutation($input: SecretariatInput!) { createSecretariat(input: $input) { id } }", "variables": {"input": {"name": sys.argv[1]}}}))' "$NAME201" >/tmp/f5c2-gql.json
check 'GraphQL secretaria com nome de 201' '200' "$(call POST /graphql "$(cat /tmp/f5c2-gql.json)" /tmp/f5c2-a11.json)"
expect_json /tmp/f5c2-a11.json errors.0.extensions.code=VALIDATION_ERROR

check 'OpenAPI' '200' "$(call GET /api-docs '' /tmp/f5c2-a12.json)"
python3 - <<'PY'
import json
schemas = json.load(open('/tmp/f5c2-a12.json', encoding='utf-8'))['components']['schemas']
assert schemas['ProjectRequest']['properties']['name']['maxLength'] == 200, schemas['ProjectRequest']['properties']['name']
assert schemas['ProjectRequest']['properties']['responsibleIds']['maxItems'] == 50, schemas['ProjectRequest']['properties']['responsibleIds']
assert schemas['ResponsibleRequest']['properties']['email']['maxLength'] == 254, schemas['ResponsibleRequest']['properties']['email']
assert schemas['SecretariatRequest']['properties']['name']['maxLength'] == 200, schemas['SecretariatRequest']['properties']['name']
print('   OpenAPI com maxLength 200/254 e maxItems 50')
PY
check 'excluir projeto' '204' "$(call DELETE "/api/v1/projects/$PROJECT_ID" '' /tmp/f5c2-a13.json)"
check 'excluir responsável' '204' "$(call DELETE "/api/v1/responsibles/$RESPONSIBLE_ID" '' /tmp/f5c2-a14.json)"
test "$PASSED" -eq 14
echo 'F5_C2_API_GREEN'

echo '=== CI DA release/2.0.0 COM O F5-C1 ==='
git fetch -q origin release/2.0.0
CI_SHA="$(git rev-parse origin/release/2.0.0)"
git cat-file -p "$CI_SHA:backend/Dockerfile" | grep -q 'jacoco.skip' || fail 'origin/release/2.0.0 ainda sem o F5-C1 (faça o push da seção 0)'
REPO="$(git remote get-url origin | sed -E 's#^(git@github\.com:|https://github\.com/)##; s#\.git$##')"
API="https://api.github.com/repos/$REPO"
STATE=''
for i in $(seq 1 35); do
  curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs?head_sha=$CI_SHA&per_page=50" >/tmp/f5c2-runs.json
  STATE="$(python3 -c "
import json
runs = [r for r in json.load(open('/tmp/f5c2-runs.json'))['workflow_runs'] if r.get('path', '').startswith('.github/workflows/ci.yml')]
run = max(runs, key=lambda r: r['run_attempt'] * 10**12 + r['id']) if runs else None
print(f\"{run['status']}:{run['conclusion']} {run['id']}\" if run else 'none -')
")"
  echo "   [$i] ${CI_SHA:0:7} $STATE"
  case "$STATE" in
    completed:success*) break ;;
    completed:*) fail 'CI da release terminou sem sucesso' ;;
  esac
  [ "$i" -lt 35 ] || fail 'CI não concluiu em 35 minutos'
  sleep 60
done
RUN_ID_CI="$(echo "$STATE" | cut -d' ' -f2)"
curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs/$RUN_ID_CI/jobs?per_page=50" >/tmp/f5c2-jobs.json
python3 - <<'PY'
import json
jobs = {j['name']: j for j in json.load(open('/tmp/f5c2-jobs.json'))['jobs']}
assert {name: job['conclusion'] for name, job in jobs.items()} == {'frontend': 'success', 'backend': 'success', 'repository': 'success'}, sorted(jobs)
step = next(s for s in jobs['backend']['steps'] if s['name'].startswith('Imagem Docker do backend'))
assert step['conclusion'] == 'success', step
print(f"   jobs frontend/backend/repository: success · passo '{step['name']}': success · {jobs['backend']['html_url']}")
PY
echo 'F5_C1_CI_GREEN'

printf '%s\n' '=== F5-C2 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige todos os marcadores `F5_C2_*_GREEN`, `F5_C1_CI_GREEN`, `=== F5-C2 GREEN ===` e `Resultado: exit code 0`. Traga a saída (`/tmp/saida-gate-f5c2.txt`) e, se fez, o resultado da conferência do Swagger no navegador (seção 3 do F5-C1).

## 3. Depois do GREEN: commits semânticos, merge na release e push (Gitflow)

Cada commit compila sozinho, na ordem abaixo.

```sh
cd ~/proj/facilit-desafio-kanban
B=backend/src/main/java/br/com/facilit/kanban
T=backend/src/test/java/br/com/facilit/kanban

git add $B/delivery/common/InputLimits.java $B/delivery/rest/ProjectRequest.java $B/delivery/rest/ResponsibleRequest.java \
        $B/delivery/rest/SecretariatRequest.java $B/delivery/graphql/ProjectGraphQlController.java \
        $B/delivery/graphql/ResponsibleGraphQlController.java $B/delivery/graphql/SecretariatGraphQlController.java \
        $B/application/project/ProjectFilter.java \
        $T/application/project/ProjectFilterTest.java $T/delivery/rest/ProjectRestControllerTest.java \
        $T/delivery/rest/ResponsibleRestControllerTest.java $T/delivery/rest/SecretariatRestControllerTest.java \
        $T/delivery/graphql/ProjectGraphQlControllerTest.java
git commit -m "fix(api): limita o tamanho das entradas de texto e da lista de responsáveis"

git add backend/src/test/resources/features/transicoes.feature $T/bdd/StatusTransitionSteps.java
git commit -m "test(bdd): confere atraso e percentual restante em cada linha da tabela de transição"

git add README.md docs
git commit -m "docs(F5-C2): promove o F5-C1 e registra as evidências do F5-C2"

git status --short        # deve ficar vazio
git checkout release/2.0.0
git merge --no-ff bugfix/2.0.0-c2-testes-validacao -m "Merge branch 'bugfix/2.0.0-c2-testes-validacao' into release/2.0.0"
git push origin release/2.0.0 bugfix/2.0.0-c2-testes-validacao
git push gitlab release/2.0.0 bugfix/2.0.0-c2-testes-validacao
```

O CI desta `release/2.0.0` é conferido no gate do F5-C3.
