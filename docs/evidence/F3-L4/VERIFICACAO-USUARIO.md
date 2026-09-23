# F3-L4 — VERIFICAÇÃO DO USUÁRIO (etapa 1 de 4: pré-publicação local)

O F3-L4 fecha em quatro etapas, nesta ordem:

1. **Este gate local.** Build, testes, CI validado estaticamente, smoke de API contra o Compose (os 21 cenários da coleção), documentação e varredura de segredos no histórico. Marcador: `=== F3-L4 LOCAL GREEN ===`.
2. **Reconstrução do histórico por lote**, em Gitflow, sem publicar. Roteiro em `RECONSTRUCAO-HISTORICO.md`. Marcador: `=== F3-L4 HISTORICO GREEN ===`.
3. **Migração para o GitHub.** Roteiro em `MIGRACAO-GITHUB.md`, executado por você. A publicação é irreversível, por isso não está automatizada.
4. **Gate do pipeline.** `VERIFICACAO-CI.md` confere, pela API pública do GitHub, se o primeiro pipeline do commit publicado terminou verde. Marcador: `=== F3-L4 GREEN ===`.

O shell estrito fica isolado. O frontend executa `pnpm format` (`biome check --write .`) antes das verificações. O smoke de API usa só `curl` e `python3`, sem ferramenta baixada na hora.

## Aplicação do pacote (antes do gate)

```sh
cd ~/proj/facilit-desafio-kanban
tar -xzf ~/Downloads/facilit-desafio-kanban-F3-L4-candidate-rev3.tar.gz
```

O F3-L4 não remove nem move arquivos; apenas adiciona e altera.

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
git rev-parse --is-inside-work-tree >/dev/null

RUN_ID="$(date +%s)-$$"
export APP_ADMIN_EMAIL="f3l4-admin-${RUN_ID}@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export APP_DEMO_RESPONSIBLE_NAME=""
export APP_DEMO_RESPONSIBLE_EMAIL=""
export APP_DEMO_RESPONSIBLE_POSITION=""
export APP_DEMO_RESPONSIBLE_PASSWORD=""
export METRICS_PASSWORD=""
export SESSION_COOKIE_SECURE=false
rm -f /tmp/f3l4-*.json /tmp/f3l4-*.html /tmp/f3l4-*.txt /tmp/f3l4-*.cookies

cleanup() {
  set +e
  if docker compose ps -q db >/dev/null 2>&1; then
    docker compose exec -T db sh -lc \
      "psql -X -v ON_ERROR_STOP=1 -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM app_users WHERE email = '$APP_ADMIN_EMAIL'\"" >/dev/null 2>&1
    # Sobras do smoke, caso ele pare no meio (nomes e e-mails exclusivos do smoke).
    docker compose exec -T db sh -lc \
      "psql -X -U \"\$POSTGRES_USER\" -d \"\$POSTGRES_DB\" -c \"DELETE FROM projects WHERE name LIKE 'Projeto Smoke %'\" -c \"DELETE FROM responsibles WHERE email LIKE 'smoke-%@example.invalid'\" -c \"DELETE FROM secretariats WHERE name LIKE 'Secretaria Smoke %'\"" >/dev/null 2>&1
  fi
  docker compose down --remove-orphans >/dev/null 2>&1
  rm -f /tmp/f3l4-*.json /tmp/f3l4-*.html /tmp/f3l4-*.txt /tmp/f3l4-*.cookies
}
trap cleanup EXIT

printf '%s\n' '=== FRONTEND: BIOME WRITE + LINT + TYPECHECK + STRICT + TEST + BUILD ==='
(
  cd frontend
  corepack enable
  corepack prepare pnpm@12.5.1 --activate
  pnpm install --frozen-lockfile
  pnpm format
  pnpm lint
  pnpm typecheck
  pnpm check:strict
  pnpm test
  pnpm build
)
echo 'F3_L4_FRONTEND_GREEN'

printf '%s\n' '=== BACKEND: VERIFY ==='
(
  cd backend
  mvn -B -ntp clean verify
)
echo 'F3_L4_BACKEND_GREEN'

echo '=== CI: ACTIONLINT + POLÍTICA DE SEGURANÇA DO WORKFLOW ==='
docker run --rm -v "$PWD:/repo:ro" --workdir /repo rhysd/actionlint:1.7.12 -color
python3 - <<'PY'
import re, sys
path = '.github/workflows/ci.yml'
text = open(path, encoding='utf-8').read()
code = '\n'.join(line for line in text.splitlines() if not line.lstrip().startswith('#'))
problems = []
uses = re.findall(r'^\s*(?:-\s*)?uses:\s*(\S+)(.*)$', text, re.M)
if not uses:
    problems.append('nenhum uses: encontrado')
for ref, rest in uses:
    if not re.fullmatch(r'[\w.-]+/[\w.-]+@[0-9a-f]{40}', ref):
        problems.append(f'action sem SHA completo: {ref}')
    if not re.search(r'#\s*v\d+\.\d+\.\d+', rest):
        problems.append(f'action sem comentário de versão: {ref}')
if not re.search(r'^permissions:\n  contents: read\n(?!  )', text, re.M):
    problems.append('permissions de topo diferente de contents: read')
for forbidden in ('pull_request_target', 'self-hosted', 'secrets.', 'write-all', 'contents: write'):
    if forbidden in code:
        problems.append(f'termo proibido: {forbidden}')
checkouts = text.count('actions/checkout@')
if text.count('persist-credentials: false') != checkouts:
    problems.append('checkout sem persist-credentials: false')
jobs = set(re.findall(r'^  (\w[\w-]*):\n    name:', text, re.M))
if jobs != {'frontend', 'backend', 'repository'}:
    problems.append(f'jobs inesperados: {sorted(jobs)}')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print(f'WORKFLOW_POLICY_OK actions={len(uses)} jobs={sorted(jobs)}')
PY
echo 'F3_L4_WORKFLOW_GREEN'

echo '=== REPOSITÓRIO: ESPAÇOS, ARQUIVOS SENSÍVEIS E HISTÓRICO ==='
git ls-files -co --exclude-standard -z >/tmp/f3l4-files.txt
python3 - <<'PY'
import re, sys
names = [n for n in open('/tmp/f3l4-files.txt', 'rb').read().decode('utf-8').split('\0') if n]
problems = []
sensitive = re.compile(r'(^|/)(\.env|id_rsa|id_ed25519)$|\.(pem|key|p12|jks)$')
for name in names:
    if sensitive.search(name):
        problems.append(f'arquivo sensível no repositório: {name}')
        continue
    try:
        data = open(name, 'rb').read()
    except FileNotFoundError:
        continue
    if b'\0' in data:
        continue
    lines = data.decode('utf-8', errors='replace').split('\n')
    for index, line in enumerate(lines, 1):
        if line.rstrip('\r') != line.rstrip('\r').rstrip(' \t'):
            problems.append(f'espaço no fim da linha: {name}:{index}')
            break
    if data.endswith(b'\n\n'):
        problems.append(f'linha em branco no fim do arquivo: {name}')
if problems:
    print('\n'.join(problems[:50]), file=sys.stderr)
    sys.exit(1)
print(f'WORKTREE_OK files={len(names)}')
PY
if grep -RInE 'localStorage|sessionStorage' frontend/src; then
  exit 1
fi
git log --all --format='%H' --name-only --diff-filter=AM >/tmp/f3l4-history-files.txt
if grep -nE '(^|/)(\.env|id_rsa|id_ed25519)$|\.(pem|key|p12|jks)$' /tmp/f3l4-history-files.txt; then
  echo 'arquivo sensível encontrado no histórico' >&2
  exit 1
fi
git log --all -p --no-color --format='commit %H' >/tmp/f3l4-history-patch.txt
python3 - <<'PY'
import re, sys
patterns = {
    'chave privada': re.compile(r'-----BEGIN (?:RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----'),
    'token GitLab': re.compile(r'glpat-[0-9A-Za-z_-]{20,}'),
    'token GitHub': re.compile(r'\b(?:ghp|gho|ghu|ghs|ghr)_[0-9A-Za-z]{36}\b|github_pat_[0-9A-Za-z_]{40,}'),
    'chave AWS': re.compile(r'\bAKIA[0-9A-Z]{16}\b'),
    'senha de ambiente com valor': re.compile(
        r'^\+\s*(?:export\s+)?(?:POSTGRES_PASSWORD|APP_ADMIN_PASSWORD|APP_DEMO_RESPONSIBLE_PASSWORD|METRICS_PASSWORD|GRAFANA_ADMIN_PASSWORD|DB_PASSWORD)=(?!\s*$)(?!["\']?\$)(?!change-me-local-only\s*$)(?!["\']{2}\s*$)\S+'),
}
commit = '?'
hits = []
with open('/tmp/f3l4-history-patch.txt', encoding='utf-8', errors='replace') as handle:
    current_file = '?'
    for line in handle:
        if line.startswith('commit '):
            commit = line.split()[1][:12]
        elif line.startswith('+++ b/'):
            current_file = line[6:].strip()
        elif line.startswith('+') and not line.startswith('+++'):
            for label, pattern in patterns.items():
                if pattern.search(line):
                    hits.append(f'{label}: commit {commit} arquivo {current_file}')
if hits:
    print('\n'.join(sorted(set(hits))), file=sys.stderr)
    sys.exit(1)
print('HISTORY_SECRETS_OK')
PY
echo 'F3_L4_REPOSITORY_GREEN'

echo '=== DOCUMENTAÇÃO: README, AI_USAGE, ADR, COLEÇÃO E LINKS ==='
python3 - <<'PY'
import json, os, re, sys
problems = []
def need(path, headings):
    text = open(path, encoding='utf-8').read()
    for heading in headings:
        if not re.search(r'^#{1,3} .*' + re.escape(heading), text, re.M | re.I):
            problems.append(f'{path}: seção ausente "{heading}"')
    return text
readme = need('README.md', ['Visão geral', 'Arquitetura e decisões técnicas', 'Regras de negócio', 'Como rodar',
                            'Como testar', 'Swagger', 'Segurança', 'Observabilidade', 'CI (GitHub Actions)',
                            'Estrutura de pastas e convenções', 'Uso de IA', 'Limitações e próximos passos'])
ai = need('AI_USAGE.md', ['Ferramentas', 'Como o trabalho foi estruturado', 'Sugestões da IA rejeitadas',
                          'Trecho de prompt representativo'])
adr = need('docs/adr/0001-camada-ia-agente-rag.md', ['Contexto', 'Decisão', 'Contrato proposto',
                                                    'Montagem de contexto', 'Falhas, prazos e degradação',
                                                    'Alternativas consideradas', 'trade-offs', 'Limitações e próximos passos'])
for path, text in (('README.md', readme), ('docs/adr/0001-camada-ia-agente-rag.md', adr)):
    if '```mermaid' not in text:
        problems.append(f'{path}: diagrama mermaid ausente')
if len(re.findall(r'^\d\. \*\*', ai.split('## 4.')[1].split('## 5.')[0], re.M)) < 1:
    problems.append('AI_USAGE.md: nenhuma sugestão rejeitada listada')
for path in ('README.md', 'AI_USAGE.md', 'docs/adr/0001-camada-ia-agente-rag.md'):
    text = open(path, encoding='utf-8').read()
    base = os.path.dirname(path)
    for target in re.findall(r'\]\(([^)#\s]+)(?:#[^)]*)?\)', text):
        if re.match(r'[a-z]+://', target):
            continue
        if not os.path.exists(os.path.normpath(os.path.join(base, target))):
            problems.append(f'{path}: link quebrado {target}')
collection = json.load(open('docs/api/facilit-kanban.postman_collection.json', encoding='utf-8'))
if collection['info']['schema'] != 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json':
    problems.append('coleção fora do schema v2.1')
for variable in collection['variable']:
    if variable['key'] in ('adminEmail', 'adminPassword') and variable['value']:
        problems.append(f'credencial versionada na coleção: {variable["key"]}')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print('DOCS_OK')
PY
echo 'F3_L4_DOCS_GREEN'

printf '%s\n' '=== DOCKER + SMOKE DE API (MESMOS 21 CENÁRIOS DA COLEÇÃO) ==='
docker compose down --remove-orphans
docker compose up --build -d
for i in $(seq 1 45); do
  if curl -fsS http://localhost:8080/actuator/health >/tmp/f3l4-health.json 2>/dev/null \
    && curl -fsS http://localhost:5173/ >/tmp/f3l4-index.html 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 45 ]; then
    docker compose logs backend frontend
    exit 1
  fi
  sleep 2
done
cat /tmp/f3l4-health.json
echo

BASE=http://localhost:8080
JAR=/tmp/f3l4-smoke.cookies
SMOKE_ID="$(date +%s)"
TODAY="$(date -u +%F)"
PLANNED_END="$(date -u -d '+10 days' +%F)"
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
  test "$(curl -sS -o /tmp/f3l4-csrf.json -w '%{http_code}' -b "$JAR" -c "$JAR" "$BASE/api/v1/auth/csrf")" = '200'
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

check 'health REST' '200' "$(curl -sS -o /tmp/f3l4-s01.json -w '%{http_code}' "$BASE/api/v1/health")"
test "$(json_get /tmp/f3l4-s01.json status)" = 'UP'
code="$(curl -sS -o /tmp/f3l4-s02.json -w '%{http_code}' "$BASE/api/v1/projects?page=0&size=20")"
check 'projetos sem sessão' '401' "$code"
test "$(json_get /tmp/f3l4-s02.json code)" = 'UNAUTHORIZED'
csrf; check 'CSRF antes do login' 'ok' 'ok'
check 'login ADMIN' '200' "$(call POST /api/v1/auth/login "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" /tmp/f3l4-s04.json)"
python3 -c "import json; assert 'ROLE_ADMIN' in json.load(open('/tmp/f3l4-s04.json'))['authorities']"
csrf; check 'CSRF renovado após login' 'ok' 'ok'
check 'sessão atual' '200' "$(call GET /api/v1/auth/me '' /tmp/f3l4-s06.json)"
test "$(json_get /tmp/f3l4-s06.json email)" = "$APP_ADMIN_EMAIL"
check 'criar secretaria' '201' "$(call POST /api/v1/secretariats "{\"name\":\"Secretaria Smoke $SMOKE_ID\"}" /tmp/f3l4-s07.json)"
SECRETARIAT_ID="$(json_get /tmp/f3l4-s07.json id)"
check 'criar responsável' '201' "$(call POST /api/v1/responsibles "{\"name\":\"Responsável Smoke\",\"email\":\"smoke-$SMOKE_ID@example.invalid\",\"position\":\"Analista\",\"secretariatId\":\"$SECRETARIAT_ID\"}" /tmp/f3l4-s08.json)"
RESPONSIBLE_ID="$(json_get /tmp/f3l4-s08.json id)"
check 'e-mail duplicado' '409' "$(call POST /api/v1/responsibles "{\"name\":\"Duplicado\",\"email\":\"smoke-$SMOKE_ID@example.invalid\",\"position\":\"Analista\"}" /tmp/f3l4-s09.json)"
test "$(json_get /tmp/f3l4-s09.json code)" = 'CONFLICT'
check 'criar projeto' '201' "$(call POST /api/v1/projects "{\"name\":\"Projeto Smoke $SMOKE_ID\",\"responsibleIds\":[\"$RESPONSIBLE_ID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$PLANNED_END\"}" /tmp/f3l4-s10.json)"
PROJECT_ID="$(json_get /tmp/f3l4-s10.json id)"
test "$(json_get /tmp/f3l4-s10.json status)" = 'NOT_STARTED'
check 'projeto sem responsável' '400' "$(call POST /api/v1/projects '{"name":"Inválido","responsibleIds":[]}' /tmp/f3l4-s11.json)"
test "$(json_get /tmp/f3l4-s11.json code)" = 'VALIDATION_ERROR'
check 'transição A iniciar → Em andamento' '200' "$(call PATCH "/api/v1/projects/$PROJECT_ID/status" '{"status":"IN_PROGRESS"}' /tmp/f3l4-s12.json)"
test "$(json_get /tmp/f3l4-s12.json status)" = 'IN_PROGRESS'
test "$(json_get /tmp/f3l4-s12.json actualStart)" = "$TODAY"
check 'transição bloqueada Em andamento → Atrasado' '400' "$(call PATCH "/api/v1/projects/$PROJECT_ID/status" '{"status":"OVERDUE"}' /tmp/f3l4-s13.json)"
test "$(json_get /tmp/f3l4-s13.json code)" = 'INVALID_REQUEST'
check 'listar com filtros' '200' "$(call GET "/api/v1/projects?page=0&size=20&status=IN_PROGRESS&responsibleId=$RESPONSIBLE_ID&text=Smoke%20$SMOKE_ID" '' /tmp/f3l4-s14.json)"
python3 - "$PROJECT_ID" <<'PY'
import json, sys
with open('/tmp/f3l4-s14.json', encoding='utf-8') as handle:
    ids = [item['id'] for item in json.load(handle)['content']]
assert ids == [sys.argv[1]], ids
PY
check 'indicadores' '200' "$(call GET /api/v1/indicators/projects '' /tmp/f3l4-s15.json)"
python3 -c "import json; assert json.load(open('/tmp/f3l4-s15.json'))['totalProjects'] >= 1"
GRAPHQL_BODY="$(python3 -c 'import json, sys; print(json.dumps({"query": "query($id: ID!) { project(id: $id) { id status remainingTimePercentage delayDays } projectIndicators { totalProjects byStatus { status projectCount averageDelayDays } } }", "variables": {"id": sys.argv[1]}}))' "$PROJECT_ID")"
check 'GraphQL projeto e indicadores' '200' "$(call POST /graphql "$GRAPHQL_BODY" /tmp/f3l4-s16.json)"
python3 -c "import json; body = json.load(open('/tmp/f3l4-s16.json')); assert 'errors' not in body, body; assert body['data']['project']['status'] == 'IN_PROGRESS', body"
check 'excluir projeto' '204' "$(call DELETE "/api/v1/projects/$PROJECT_ID" '' /tmp/f3l4-s17.json)"
check 'excluir responsável' '204' "$(call DELETE "/api/v1/responsibles/$RESPONSIBLE_ID" '' /tmp/f3l4-s18.json)"
check 'excluir secretaria' '204' "$(call DELETE "/api/v1/secretariats/$SECRETARIAT_ID" '' /tmp/f3l4-s19.json)"
check 'logout' '204' "$(call POST /api/v1/auth/logout '' /tmp/f3l4-s20.json)"
check 'sessão encerrada' '401' "$(curl -sS -o /tmp/f3l4-s21.json -w '%{http_code}' -b "$JAR" "$BASE/api/v1/auth/me")"
test "$PASSED" -eq 21
echo 'F3_L4_API_SMOKE_GREEN'

echo '=== OPENAPI + GIT ==='
curl -fsS http://localhost:8080/api-docs >/tmp/f3l4-openapi.json
python3 - <<'PY'
import json
with open('/tmp/f3l4-openapi.json', encoding='utf-8') as handle:
    paths = json.load(handle)['paths']
for path in ('/api/v1/projects', '/api/v1/projects/{id}/status', '/api/v1/indicators/projects', '/api/v1/secretariats'):
    assert path in paths, path
PY
git diff --check
docker compose ps
echo 'F3_L4_STATIC_GREEN'

printf '%s\n' '=== F3-L4 LOCAL GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN local exige todos os marcadores `F3_L4_*_GREEN`, `=== F3-L4 LOCAL GREEN ===` e `Resultado: exit code 0`. Depois dele, siga `RECONSTRUCAO-HISTORICO.md`, `MIGRACAO-GITHUB.md` e, por fim, `VERIFICACAO-CI.md`.
