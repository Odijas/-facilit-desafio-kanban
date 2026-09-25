# F5-C1 — VERIFICAÇÃO DO USUÁRIO

O gate prova o lote F5-C1 (entrega executável) na branch `bugfix/2.0.0-c1-entrega`, criada a partir da `release/2.0.0`, antes do commit. Ele cobre:

- pré-condições: base na `release/2.0.0` (`59051d0`), arquivos alterados iguais aos do pacote, frontend intocado;
- build limpo do backend, com JaCoCo ≥ 95% e a contagem exata de testes;
- checagens estáticas, incluindo a política do workflow que o freeze do F5-L5 aplica;
- **imagem do backend: o Dockerfile anterior (RED esperado) e o novo (GREEN obrigatório)**;
- subida com banco novo e migrations 1 a 7;
- Swagger UI: `requestInterceptor` de CSRF servido, mais o fluxo cookie → cabeçalho contra a API real.

**Isolamento:**

- O gate usa um projeto Compose próprio (`facilit-kanban-f5c1`) e o remove no fim (`down -v` só desse projeto).
- O projeto padrão é apenas parado (`docker compose down`, sem `-v`).
- A porta 8080 precisa estar livre.

## 0. Pré-requisito

A `release/2.0.0` local deve estar igual à do GitHub (`59051d0`) e sem alterações.

## 1. Preparar a branch e aplicar o pacote

O bloco para no primeiro erro, mostra uma mensagem `PARE:` quando algo está fora do esperado e imprime `PREPARO_OK` só se tudo der certo. Serve para a primeira vez e para repetir (se já estiver na branch do lote, só reaplica o pacote).

```sh
cd ~/proj/facilit-desafio-kanban && F=bugfix/2.0.0-c1-entrega && \
if [ "$(git branch --show-current)" != "$F" ]; then \
  { [ -z "$(git status --porcelain=v1)" ] || { echo 'PARE: árvore com alterações'; false; }; } && \
  git checkout release/2.0.0 && git pull --ff-only origin release/2.0.0 && \
  { if git rev-parse -q --verify "refs/heads/$F" >/dev/null; then git checkout "$F"; else git checkout -b "$F"; fi; }; \
fi && \
{ git merge-base --is-ancestor 59051d0 HEAD || { echo 'PARE: a branch não parte da release/2.0.0 (59051d0)'; false; }; } && \
(cd ~/Downloads && echo "<SHA256>  facilit-desafio-kanban-F5-C1-candidate.tar.gz" | sha256sum -c -) && \
tar -xzf ~/Downloads/facilit-desafio-kanban-F5-C1-candidate.tar.gz && \
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5-C1/VERIFICACAO-USUARIO.md > /tmp/f5c1-gate.sh && \
echo PREPARO_OK
```

Troque `<SHA256>` pelo hash informado com o pacote.

## 2. Rodar o gate

```sh
bash /tmp/f5c1-gate.sh 2>&1 | tee /tmp/saida-gate-f5c1.txt
```

Os logs longos ficam em `/tmp/f5c1-*.log`. As duas construções da imagem levam alguns minutos cada.

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
git rev-parse --is-inside-work-tree >/dev/null
rm -f /tmp/f5c1-*

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
  *) fail "branch atual: $BRANCH (esperado bugfix/2.0.0-c1-entrega)" ;;
esac
git merge-base --is-ancestor 59051d0 HEAD || fail 'a branch não parte da release/2.0.0 (59051d0)'
git status --porcelain=v1 -uall >/tmp/f5c1-status.txt
python3 - <<'PY'
import sys
expected = {
    '.github/workflows/ci.yml',
    'README.md',
    'backend/Dockerfile',
    'backend/src/main/resources/application.yml',
    'backend/src/test/java/br/com/facilit/kanban/integration/OpenApiContractIT.java',
    'docs/evidence/F5-C1/CONSUMIDORES.md',
    'docs/evidence/F5-C1/DECISOES.md',
    'docs/evidence/F5-C1/EXECUCAO.md',
    'docs/evidence/F5-C1/FONTES-RAG.md',
    'docs/evidence/F5-C1/GATE.md',
    'docs/evidence/F5-C1/LEITURA.md',
    'docs/evidence/F5-C1/MATRIZ.md',
    'docs/evidence/F5-C1/RISCOS.md',
    'docs/evidence/F5-C1/VERIFICACAO-USUARIO.md',
    'docs/governance/PLANO-CORRECAO-RELEASE-2.0.0.md',
}
actual = set()
for line in open('/tmp/f5c1-status.txt', encoding='utf-8'):
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
print(f'   {len(actual)} arquivos alterados, todos do pacote F5-C1; frontend intocado')
PY
echo "   branch $BRANCH sobre a release/2.0.0 (59051d0)"
echo 'F5_C1_PRECONDITIONS_GREEN'

printf '%s\n' '=== BACKEND: CLEAN VERIFY + JACOCO ==='
(cd backend && run_logged /tmp/f5c1-backend.log mvn -B -ntp clean verify)
grep -q 'BUILD SUCCESS' /tmp/f5c1-backend.log
if grep -q '\[deprecation\]' /tmp/f5c1-backend.log; then grep '\[deprecation\]' /tmp/f5c1-backend.log >&2; fail 'uso de API depreciada no código do backend'; fi
if grep -Eqi 'self-attaching|loaded dynamically' /tmp/f5c1-backend.log; then fail 'o Mockito foi anexado dinamicamente'; fi
python3 - <<'PY'
import glob
import xml.etree.ElementTree as ET
expected_totals = {'unitários/BDD': (24, 165), 'integração': (12, 49)}
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
name = 'br.com.facilit.kanban.integration.OpenApiContractIT'
assert found.get(name) == (4, 0, 0, 0), (name, found.get(name))
print('   OpenApiContractIT: 4 testes OK (inclui o CSRF do Swagger UI)')
report = ET.parse('backend/target/site/jacoco/jacoco.xml').getroot()
line = next(c for c in report.findall('counter') if c.get('type') == 'LINE')
covered, missed = int(line.get('covered')), int(line.get('missed'))
ratio = covered / (covered + missed)
print(f'   JaCoCo (unitários + integração): {ratio:.2%} de linhas ({covered}/{covered + missed})')
assert ratio >= 0.95, ratio
PY
echo 'F5_C1_BACKEND_GREEN'

echo '=== ESTÁTICA: DOCKERFILE, CI, SWAGGER, README E POLÍTICA DO FREEZE ==='
git diff --check
docker run --rm -v "$PWD:/repo:ro" --workdir /repo rhysd/actionlint:1.7.12
python3 - <<'PY'
import os, re, sys
problems = []
names = [line[3:].rstrip('\n') for line in open('/tmp/f5c1-status.txt', encoding='utf-8') if line.strip()]
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
dockerfile = open('backend/Dockerfile', encoding='utf-8').read()
if 'RUN mvn -B -ntp -DskipITs -Djacoco.skip=true verify' not in dockerfile:
    problems.append('Dockerfile sem -DskipITs -Djacoco.skip=true verify')
if 'kanban-2.0.0.jar' not in dockerfile:
    problems.append('Dockerfile sem kanban-2.0.0.jar (pré-condição do freeze)')
ci = open('.github/workflows/ci.yml', encoding='utf-8').read()
if 'run: docker build --pull -t facilit-kanban-backend:ci .' not in ci:
    problems.append('CI sem o passo docker build no job backend')
# Política do workflow aplicada pelo freeze (docs/evidence/F5/VERIFICACAO-USUARIO.md).
code = '\n'.join(line for line in ci.splitlines() if not line.lstrip().startswith('#'))
for ref, rest in re.findall(r'^\s*(?:-\s*)?uses:\s*(\S+)(.*)$', ci, re.M):
    if not re.fullmatch(r'[\w.-]+/[\w.-]+@[0-9a-f]{40}', ref) or not re.search(r'#\s*v\d+\.\d+\.\d+', rest):
        problems.append(f'action fora da política: {ref}')
if not re.search(r'^permissions:\n  contents: read\n(?!  )', ci, re.M):
    problems.append('permissions de topo diferente de contents: read')
for forbidden in ('pull_request_target', 'self-hosted', 'secrets.', 'write-all', 'contents: write'):
    if forbidden in code:
        problems.append(f'termo proibido no workflow: {forbidden}')
if ci.count('persist-credentials: false') != ci.count('actions/checkout@'):
    problems.append('checkout sem persist-credentials: false')
jobs = set(re.findall(r'^  (\w[\w-]*):\n    name:', ci, re.M))
if jobs != {'frontend', 'backend', 'repository'}:
    problems.append(f'jobs inesperados: {sorted(jobs)}')
config = open('backend/src/main/resources/application.yml', encoding='utf-8').read()
if not re.search(r'^  swagger-ui:\n    path: /swagger-ui\.html\n(?:    #.*\n)?    csrf:\n      enabled: true\n', config, re.M):
    problems.append('application.yml sem springdoc.swagger-ui.csrf.enabled: true')
readme = open('README.md', encoding='utf-8').read()
for text in ('Usar a API pelo Swagger UI', 'springdoc.swagger-ui.csrf.enabled', 'No GraphiQL', 'F5-C1 — Entrega executável'):
    if text not in readme:
        problems.append(f'README sem "{text}"')
for heading in ('Visão geral', 'Como rodar', 'Como testar', 'Swagger', 'Limitações e próximos passos'):
    if not re.search(r'^#{1,3} .*' + re.escape(heading), readme, re.M | re.I):
        problems.append(f'README sem a seção exigida pelo freeze: {heading}')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print(f'   {len(names)} arquivos do lote OK; Dockerfile, CI (3 jobs, política do freeze), Swagger CSRF e README OK')
PY
echo 'F5_C1_STATIC_GREEN'

echo '=== IMAGEM DO BACKEND: DOCKERFILE ANTERIOR (RED) × NOVO (GREEN) ==='
RUN_ID="$(date +%s)-$$"
export POSTGRES_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export APP_ADMIN_EMAIL="f5c1-admin-${RUN_ID}@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export APP_DEMO_RESPONSIBLE_NAME='' APP_DEMO_RESPONSIBLE_EMAIL='' APP_DEMO_RESPONSIBLE_POSITION='' APP_DEMO_RESPONSIBLE_PASSWORD=''
export METRICS_PASSWORD='' SESSION_COOKIE_SECURE=false APP_TIME_ZONE=America/Sao_Paulo
GATE_PROJECT=facilit-kanban-f5c1
DC=(docker compose -p "$GATE_PROJECT" -f compose.yaml)

cleanup() {
  set +e
  "${DC[@]}" down -v --remove-orphans >/dev/null 2>&1
  docker image rm -f facilit-kanban-f5c1-red >/dev/null 2>&1
  rm -f /tmp/f5c1-*.json /tmp/f5c1-*.cookies /tmp/f5c1-Dockerfile.anterior
}
trap cleanup EXIT

docker compose down --remove-orphans >/dev/null 2>&1 || true
"${DC[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
run_logged /tmp/f5c1-image.log "${DC[@]}" build --no-cache backend
grep -Eo 'Tests run: [0-9]+, Failures: 0, Errors: 0, Skipped: 0$' /tmp/f5c1-image.log | tail -1 | sed 's/^/   imagem nova, unitários no build: /' || true
grep -q 'Skipping JaCoCo execution' /tmp/f5c1-image.log && echo '   imagem nova: JaCoCo desligado no build da imagem'
echo '   imagem nova: build OK'
echo 'F5_C1_DOCKER_IMAGE_GREEN'

git show HEAD:backend/Dockerfile >/tmp/f5c1-Dockerfile.anterior
if docker build -f /tmp/f5c1-Dockerfile.anterior -t facilit-kanban-f5c1-red backend >/tmp/f5c1-image-red.log 2>&1; then
  echo '   [INFORMATIVO] RED não reproduzido: a imagem com o Dockerfile anterior também foi construída (lacuna 1 era falso positivo)'
else
  grep -E 'lines covered ratio|Coverage checks have not been met' /tmp/f5c1-image-red.log | sed 's/^.*\(Rule violated\|Coverage checks\)/\1/' | head -2 | sed 's/^/   RED reproduzido (Dockerfile anterior): /'
  grep -q 'Coverage checks have not been met' /tmp/f5c1-image-red.log \
    || { tail -40 /tmp/f5c1-image-red.log >&2; fail 'o Dockerfile anterior falhou por outro motivo que não o JaCoCo'; }
fi

echo '=== DOCKER: BANCO NOVO + MIGRATIONS 1–7 ==='
run_logged /tmp/f5c1-compose.log "${DC[@]}" up -d db backend
for i in $(seq 1 60); do
  if curl -fsS http://localhost:8080/actuator/health >/tmp/f5c1-health.json 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 60 ]; then "${DC[@]}" logs --no-color backend | tail -80; fail 'backend não subiu'; fi
  sleep 2
done
MIGRATIONS="$(sql 'SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank;' | tr '\n' ' ' | sed 's/ *$//')"
test "$MIGRATIONS" = '1 2 3 4 5 6 7' || fail "migrations aplicadas: '$MIGRATIONS'"
echo "   banco novo ($GATE_PROJECT); migrations V$MIGRATIONS; backend UP com a imagem nova"
echo 'F5_C1_DOCKER_GREEN'

echo '=== SWAGGER UI: CSRF ==='
BASE=http://localhost:8080
JAR=/tmp/f5c1-swagger.cookies
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

# O que o requestInterceptor do Swagger UI faz: lê o cookie XSRF-TOKEN no momento da chamada e o envia em X-XSRF-TOKEN.
cookie_token() {
  if [ -f "$JAR" ]; then awk '$6 == "XSRF-TOKEN" { value=$7 } END { print value }' "$JAR"; fi
}

as_swagger() {
  local method="$1" path="$2" body="$3" out="$4" token
  local args=(-sS -o "$out" -w '%{http_code}' -X "$method" -b "$JAR" -c "$JAR" -H 'Content-Type: application/json')
  token="$(cookie_token)"
  if [ -n "$token" ]; then args+=(-H "X-XSRF-TOKEN: $token"); fi
  if [ -n "$body" ]; then args+=(--data "$body"); fi
  curl "${args[@]}" "$BASE$path"
}

check 'Swagger UI publicado' '200' "$(curl -sSL -o /tmp/f5c1-swagger.html -w '%{http_code}' "$BASE/swagger-ui.html")"
check 'swagger-initializer.js publicado' '200' "$(curl -sS -o /tmp/f5c1-initializer.js -w '%{http_code}' "$BASE/swagger-ui/swagger-initializer.js")"
python3 - <<'PY'
text = open('/tmp/f5c1-initializer.js', encoding='utf-8').read()
for needle in ('requestInterceptor', 'XSRF-TOKEN=', "request.headers['X-XSRF-TOKEN']", 'isSameOrigin'):
    assert needle in text, needle
print('   swagger-initializer.js com requestInterceptor: cookie XSRF-TOKEN → cabeçalho X-XSRF-TOKEN (mesma origem)')
PY
check 'login sem o cabeçalho (antes da correção) → 403' '403' "$(curl -sS -o /tmp/f5c1-s01.json -w '%{http_code}' -X POST -H 'Content-Type: application/json' --data "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" "$BASE/api/v1/auth/login")"
check 'passo 1: GET /api/v1/auth/csrf' '200' "$(as_swagger GET /api/v1/auth/csrf '' /tmp/f5c1-s02.json)"
test -n "$(cookie_token)" || fail 'cookie XSRF-TOKEN ausente depois do passo 1'
check 'passo 2: login com o cabeçalho do interceptor' '200' "$(as_swagger POST /api/v1/auth/login "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" /tmp/f5c1-s03.json)"
check 'passo 3: GET /api/v1/auth/csrf (token novo após o login)' '200' "$(as_swagger GET /api/v1/auth/csrf '' /tmp/f5c1-s04.json)"
check 'passo 4: escrita autenticada (criar secretaria)' '201' "$(as_swagger POST /api/v1/secretariats "{\"name\":\"Secretaria F5-C1 $RUN_ID\"}" /tmp/f5c1-s05.json)"
SECRETARIAT_ID="$(python3 -c "import json; print(json.load(open('/tmp/f5c1-s05.json'))['id'])")"
check 'excluir secretaria' '204' "$(as_swagger DELETE "/api/v1/secretariats/$SECRETARIAT_ID" '' /tmp/f5c1-s06.json)"
test "$PASSED" -eq 8
echo 'F5_C1_SWAGGER_GREEN'

printf '%s\n' '=== F5-C1 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige todos os marcadores `F5_C1_*_GREEN`, `=== F5-C1 GREEN ===` e `Resultado: exit code 0`. Traga a saída (`/tmp/saida-gate-f5c1.txt`).

## 3. Conferência no navegador (2 minutos)

O script prova tudo o que é observável sem navegador. Falta o clique no Swagger UI:

```sh
cd ~/proj/facilit-desafio-kanban
docker compose up --build -d db backend
```

No navegador, em `http://localhost:8080/swagger-ui.html`, siga o roteiro do README (seção "Como rodar"):

1. `GET /api/v1/auth/csrf`;
2. `POST /api/v1/auth/login` com o administrador do seu `.env`;
3. `GET /api/v1/auth/csrf`;
4. `GET /api/v1/projects`.

Responda "Swagger OK" (com o status do passo 4) ou mande o status em que parou. Depois: `docker compose down`.

## 4. Depois do GREEN: commits granulares, merge na release e CI

Cada commit compila sozinho, na ordem abaixo.

```sh
cd ~/proj/facilit-desafio-kanban

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

CI da `release/2.0.0`, com o passo novo. Espera até 35 minutos e imprime `F5_C1_CI_GREEN`:

```sh
cd ~/proj/facilit-desafio-kanban && bash <<'CI'
set -euo pipefail
HEAD_SHA="$(git rev-parse release/2.0.0)"
REPO="$(git remote get-url origin | sed -E 's#^(git@github\.com:|https://github\.com/)##; s#\.git$##')"
API="https://api.github.com/repos/$REPO"
for i in $(seq 1 35); do
  curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs?head_sha=$HEAD_SHA&per_page=50" >/tmp/f5c1-runs.json
  STATE="$(python3 -c "
import json
runs = [r for r in json.load(open('/tmp/f5c1-runs.json'))['workflow_runs'] if r.get('path', '').startswith('.github/workflows/ci.yml')]
run = max(runs, key=lambda r: r['run_attempt'] * 10**12 + r['id']) if runs else None
print(f\"{run['status']}:{run['conclusion']} {run['id']}\" if run else 'none -')
")"
  echo "   [$i] $STATE"
  case "$STATE" in
    completed:success*) break ;;
    completed:*) echo 'FALHA: CI da release terminou sem sucesso' >&2; exit 1 ;;
  esac
  [ "$i" -lt 35 ] || { echo 'FALHA: CI não concluiu em 35 minutos' >&2; exit 1; }
  sleep 60
done
RUN_ID_CI="$(echo "$STATE" | cut -d' ' -f2)"
curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs/$RUN_ID_CI/jobs?per_page=50" >/tmp/f5c1-jobs.json
python3 - <<'PY'
import json
jobs = {j['name']: j for j in json.load(open('/tmp/f5c1-jobs.json'))['jobs']}
assert {name: job['conclusion'] for name, job in jobs.items()} == {'frontend': 'success', 'backend': 'success', 'repository': 'success'}, jobs.keys()
step = next(s for s in jobs['backend']['steps'] if s['name'].startswith('Imagem Docker do backend'))
assert step['conclusion'] == 'success', step
print(f"   jobs frontend/backend/repository: success · passo '{step['name']}': success · {jobs['backend']['html_url']}")
PY
echo 'F5_C1_CI_GREEN'
CI
```

Traga as saídas do gate, da conferência no navegador e do CI.
