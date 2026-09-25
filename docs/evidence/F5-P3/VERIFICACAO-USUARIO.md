# F5-P3 — VERIFICAÇÃO DO USUÁRIO (gate de freeze da patch release 2.0.1)

Este gate prova a entrega na branch `hotfix/2.0.1`, antes do merge na `main` e da tag. É o freeze da 2.0.0 (`docs/evidence/F5/VERIFICACAO-USUARIO.md`) adaptado à 2.0.1 e cobre:

- pré-condições: `hotfix/2.0.1` a partir da `v2.0.0`, versão 2.0.1, F5-P1 e F5-P2 GREEN, tag `v2.0.1` ainda inexistente;
- frontend completo e backend com `mvn clean verify`, com as contagens exatas (174 unitários/BDD e 51 de integração) e JaCoCo ≥ 95%;
- análise estática:
  - workflow, espaços e segredos no histórico;
  - Conventional Commits;
  - README, AI_USAGE, CHANGELOG `[2.0.1]`, ADR, auditorias e links;
- subida integral com **banco novo** e migrations V1–V7 do zero;
- UI, Swagger e contrato do OpenAPI:
  - exemplos em todas as 26 operações REST (regra do `OpenApiContractIT`);
  - CSRF no Swagger UI;
- REST e GraphQL com ADMIN, incluindo a busca literal do F5-P1;
- autenticação do responsável, revisão de segurança executável, Prometheus, Grafana e logs ECS;
- CI verde do commit da `hotfix/2.0.1`.

Rode depois dos passos 0 a 2 de `RELEASE.md` (commits do F5-P2, pacote do F5-P3 aplicado, commits e push da `hotfix/2.0.1`).

**Isolamento do banco:**

- O gate sobe um projeto Compose próprio (`facilit-kanban-f5p3`), com volumes novos, e o remove no fim (`down -v` só desse projeto).
- O projeto padrão é apenas parado (`docker compose down`, sem `-v`); o volume `postgres-data` dele não é tocado.
- As portas 8080, 5173, 9090 e 3000 precisam estar livres.

**Saída:** logs longos em `/tmp/f5p3-*.log`; a tela mostra os resumos e, em falha, o fim do log correspondente.

```sh
cd ~/proj/facilit-desafio-kanban
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5-P3/VERIFICACAO-USUARIO.md > /tmp/f5p3-gate.sh
bash -n /tmp/f5p3-gate.sh
bash -o pipefail -c 'bash /tmp/f5p3-gate.sh 2>&1 | tee /tmp/saida-gate-f5p3.txt'
```

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
git rev-parse --is-inside-work-tree >/dev/null
# Limpa a execução anterior, sem apagar o próprio gate (/tmp/f5p3-gate.sh), para ele poder ser repetido.
find /tmp -maxdepth 1 -name 'f5p3-*' ! -name 'f5p3-gate.sh' -exec rm -rf {} +

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

echo '=== PRÉ-CONDIÇÕES DA RELEASE 2.0.1 ==='
BRANCH="$(git branch --show-current)"
test "$BRANCH" = 'hotfix/2.0.1' || fail "branch atual: $BRANCH (esperado hotfix/2.0.1)"
if [ -n "$(git status --porcelain=v1)" ]; then git status --short >&2; fail 'área de trabalho com alterações sem commit'; fi
HEAD_SHA="$(git rev-parse HEAD)"
test "$(git ls-remote origin "refs/heads/$BRANCH" | cut -f1)" = "$HEAD_SHA" \
  || fail "hotfix/2.0.1 local difere da publicada no GitHub (faça git push origin hotfix/2.0.1)"
GITHUB_REPO="$(git remote get-url origin | sed -E 's#^(git@github\.com:|https://github\.com/)##; s#\.git$##')"
echo "   HEAD $HEAD_SHA · GitHub $GITHUB_REPO"
python3 - <<'PY'
import json, re
pom = open('backend/pom.xml', encoding='utf-8').read()
project = re.search(r'<artifactId>kanban</artifactId>\s*<version>([^<]+)</version>', pom)
assert project and project.group(1) == '2.0.1', project and project.group(1)
assert 'kanban-2.0.1.jar' in open('backend/Dockerfile', encoding='utf-8').read(), 'Dockerfile sem kanban-2.0.1.jar'
assert json.load(open('frontend/package.json', encoding='utf-8'))['version'] == '2.0.1', 'package.json fora da 2.0.1'
print('   versão 2.0.1 em pom.xml, Dockerfile e package.json')
PY
git rev-parse -q --verify 'v2.0.0^{commit}' >/dev/null || fail 'tag v2.0.0 ausente (git fetch origin --tags)'
git merge-base --is-ancestor 'v2.0.0^{commit}' HEAD || fail 'hotfix/2.0.1 não parte da v2.0.0'
if git rev-parse -q --verify 'refs/tags/v2.0.1' >/dev/null; then fail 'a tag v2.0.1 já existe (o freeze vem antes da tag)'; fi
python3 - <<'PY'
import sys
problems = []
read = lambda path: open(path, encoding='utf-8').read()
for lot in ('F5-P1', 'F5-P2'):
    if 'Estado: **GREEN**' not in read(f'docs/evidence/{lot}/GATE.md'):
        problems.append(f'{lot} não está GREEN')
    saida = read(f'docs/evidence/{lot}/SAIDA-GATE.txt')
    if f'=== {lot} GREEN ===' not in saida or 'Resultado: exit code 0' not in saida:
        problems.append(f'SAIDA-GATE do {lot} sem GREEN e exit code 0')
readme = read('README.md')
for text in ('F5-P1 — Swagger com exemplos em todas as operações', 'F5-P2 — Documentação coerente com a tag'):
    if text not in readme:
        problems.append(f'README sem "{text}"')
if '## 4. Decisões do usuário (2026-09-25, 7h50)' not in read('docs/governance/PLANO-CORRECAO-RELEASE-2.0.1.md'):
    problems.append('plano 2.0.1 sem as decisões do usuário')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print('   hotfix/2.0.1 a partir da v2.0.0; F5-P1 e F5-P2 GREEN com saída registrada; tag v2.0.1 ainda não existe')
PY
echo 'F5_P3_PRECONDITIONS_GREEN'

RUN_ID="$(date +%s)-$$"
export POSTGRES_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export APP_ADMIN_EMAIL="f5p3-admin-${RUN_ID}@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export APP_DEMO_RESPONSIBLE_NAME="Responsável Demo F5-P3"
export APP_DEMO_RESPONSIBLE_EMAIL="f5p3-demo-${RUN_ID}@example.invalid"
export APP_DEMO_RESPONSIBLE_POSITION="Analista"
export APP_DEMO_RESPONSIBLE_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export METRICS_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export GRAFANA_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export SESSION_COOKIE_SECURE=false
# Projeto Compose próprio do freeze: banco e volumes novos, sem tocar no volume do projeto padrão.
FREEZE_PROJECT=facilit-kanban-f5p3
DC=(docker compose -p "$FREEZE_PROJECT" -f compose.yaml -f compose.observability.yaml)

cleanup() {
  set +e
  "${DC[@]}" down -v --remove-orphans >/dev/null 2>&1
  rm -f /tmp/f5p3-*.json /tmp/f5p3-*.html /tmp/f5p3-*.cookies /tmp/f5p3-*.txt
}
trap cleanup EXIT

printf '%s\n' '=== FRONTEND: FORMAT + LINT + TYPECHECK + STRICT + TEST + BUILD ==='
(
  cd frontend
  run_logged /tmp/f5p3-frontend.log corepack enable
  run_logged /tmp/f5p3-frontend.log corepack prepare pnpm@12.5.1 --activate
  for step in "install --frozen-lockfile" format lint typecheck check:strict test build; do
    # shellcheck disable=SC2086
    run_logged /tmp/f5p3-frontend.log pnpm $step
  done
)
sed 's/\x1b\[[0-9;]*m//g' /tmp/f5p3-frontend.log | grep -E 'STRICT_TYPES_GREEN|Test Files|Tests +[0-9]' | sed 's/^ */   /' || true
if grep -qi 'deprecat' /tmp/f5p3-frontend.log; then grep -i 'deprecat' /tmp/f5p3-frontend.log >&2; fail 'aviso de depreciação no frontend'; fi
if [ -n "$(git status --porcelain=v1)" ]; then git status --short >&2; fail 'o format alterou arquivos (código não formatado na release)'; fi
(cd frontend && pnpm audit --prod >/tmp/f5p3-audit.txt 2>&1) || true
echo "   pnpm audit --prod (informativo): $(tail -1 /tmp/f5p3-audit.txt)"
echo 'F5_P3_FRONTEND_GREEN'

printf '%s\n' '=== BACKEND: CLEAN VERIFY ==='
(cd backend && run_logged /tmp/f5p3-backend.log mvn -B -ntp clean verify)
grep -q 'BUILD SUCCESS' /tmp/f5p3-backend.log
test -f backend/target/kanban-2.0.1.jar || fail 'jar kanban-2.0.1.jar não gerado'
if grep -q '\[deprecation\]' /tmp/f5p3-backend.log; then grep '\[deprecation\]' /tmp/f5p3-backend.log >&2; fail 'uso de API depreciada no código do backend'; fi
# Avisos de depreciação emitidos pelas ferramentas (Maven, JVM, bibliotecas de teste) são listados para análise, sem esconder.
TOOL_DEPRECATIONS="$(grep -ci 'deprecat' /tmp/f5p3-backend.log || true)"
echo "   linhas com 'deprecat' no log do Maven (ferramentas, informativo): $TOOL_DEPRECATIONS"
if [ "$TOOL_DEPRECATIONS" -gt 0 ]; then grep -i 'deprecat' /tmp/f5p3-backend.log | sort | uniq -c | head -10 | sed 's/^/   /'; fi
python3 - <<'PY'
import glob
import xml.etree.ElementTree as ET
for kind, pattern in (('unitários', 'backend/target/surefire-reports/TEST-*.xml'), ('integração', 'backend/target/failsafe-reports/TEST-*.xml')):
    totals = dict(tests=0, failures=0, errors=0, skipped=0)
    files = glob.glob(pattern)
    for name in files:
        root = ET.parse(name).getroot()
        for key in totals:
            totals[key] += int(root.get(key, 0))
    assert files and totals['tests'] > 0, (kind, files)
    assert totals['failures'] == 0 and totals['errors'] == 0 and totals['skipped'] == 0, (kind, totals)
    print(f"   {kind}: {len(files)} classes, {totals['tests']} testes, {totals['failures']} falhas, {totals['errors']} erros, {totals['skipped']} ignorados")
    expected = {'unitários': (25, 174), 'integração': (12, 51)}[kind]
    assert (len(files), totals['tests']) == expected, (kind, len(files), totals['tests'], expected)
PY
python3 - <<'PY'
import xml.etree.ElementTree as ET
root = ET.parse('backend/target/site/jacoco/jacoco.xml').getroot()
c = next(x for x in root.findall('counter') if x.attrib['type'] == 'LINE')
missed, covered = int(c.attrib['missed']), int(c.attrib['covered'])
ratio = covered / (covered + missed)
assert ratio >= 0.95, ratio
print(f'   JaCoCo linhas: {covered}/{covered + missed} = {ratio:.2%}')
bdd = ET.parse('backend/target/surefire-reports/TEST-br.com.facilit.kanban.bdd.StatusTransitionsBddTest.xml').getroot()
assert int(bdd.get('tests', 0)) == 12 and int(bdd.get('failures', 0)) == 0 and int(bdd.get('errors', 0)) == 0, bdd.attrib
print('   BDD: 12 cenários sem falhas/erros')
PY
echo 'F5_P3_JACOCO_BDD_GREEN'
echo 'F5_P3_BACKEND_GREEN'

echo '=== ANÁLISE ESTÁTICA: WORKFLOW, REPOSITÓRIO, HISTÓRICO E COMMITS ==='
docker run --rm -v "$PWD:/repo:ro" --workdir /repo rhysd/actionlint:1.7.12
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
echo '   workflow OK'
git ls-files -co --exclude-standard -z >/tmp/f5p3-files.txt
python3 - <<'PY'
import re, sys
names = [n for n in open('/tmp/f5p3-files.txt', 'rb').read().decode('utf-8').split('\0') if n]
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
git log --all --format='%H' --name-only --diff-filter=AM >/tmp/f5p3-history-files.txt
if grep -nE '(^|/)(\.env|id_rsa|id_ed25519)$|\.(pem|key|p12|jks)$' /tmp/f5p3-history-files.txt; then
  echo 'arquivo sensível encontrado no histórico' >&2
  exit 1
fi
git log --all -p --no-color --format='commit %H' >/tmp/f5p3-history-patch.txt
python3 - <<'PY'
import re, sys
patterns = {
    'chave privada': re.compile(r'-----BEGIN (?:RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----'),
    'token GitLab': re.compile(r'glpat-[0-9A-Za-z_-]{20,}'),
    'token GitHub': re.compile(r'\b(?:ghp|gho|ghu|ghs|ghr)_[0-9A-Za-z]{36}\b|github_pat_[0-9A-Za-z_]{40,}'),
    'chave AWS': re.compile(r'\bAKIA[0-9A-Z]{16}\b'),
}
environment_password = re.compile(
    r'^\+\s*(?:export\s+)?(?:POSTGRES_PASSWORD|APP_ADMIN_PASSWORD|APP_DEMO_RESPONSIBLE_PASSWORD|METRICS_PASSWORD|GRAFANA_ADMIN_PASSWORD|DB_PASSWORD)=(?!\s*$)(?!["\']?\$)(?!change-me-local-only\s*$)(?!["\']{2}\s*$)\S+'
)
commit = '?'
hits = []
with open('/tmp/f5p3-history-patch.txt', encoding='utf-8', errors='replace') as handle:
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
            if not current_file.startswith('docs/evidence/') and environment_password.search(line):
                hits.append(f'senha de ambiente com valor: commit {commit} arquivo {current_file}')
if hits:
    print('\n'.join(sorted(set(hits))), file=sys.stderr)
    sys.exit(1)
print('HISTORY_SECRETS_OK')
PY
echo '   repositório e histórico OK'
git log --no-merges --format='%h %s' 283ce5d..HEAD >/tmp/f5p3-commits.txt
python3 - <<'PY'
import re, sys
pattern = re.compile(r'^[0-9a-f]+ (feat|fix|docs|test|build|ci|chore|refactor|style|perf|revert)(\([^)]+\))?!?: \S.*$')
allowed_non_conventional = {
    '87fa002 Revert "docs(api): documenta os erros de cada operação no OpenAPI"',
}
lines = [line.rstrip('\n') for line in open('/tmp/f5p3-commits.txt', encoding='utf-8') if line.strip()]
bad = [line for line in lines if not pattern.match(line) and line not in allowed_non_conventional]
if bad:
    print('\n'.join(bad), file=sys.stderr)
    sys.exit(1)
print(f'   Conventional Commits: {len(lines)} commits (sem merges); 1 revert auditado legado aceito')
PY
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
need('AI_USAGE.md', ['Ferramentas', 'Como o trabalho foi estruturado', 'Sugestões da IA rejeitadas', 'Trecho de prompt representativo'])
adr = need('docs/adr/0001-camada-ia-agente-rag.md', ['Contexto', 'Decisão', 'Contrato proposto', 'Montagem de contexto',
                                                    'Falhas, prazos e degradação', 'Alternativas consideradas',
                                                    'trade-offs', 'Limitações e próximos passos'])
need('docs/evidence/F5/AUDITORIA.md', ['Obrigatórios do desafio', 'Diferenciais', 'Lacunas declaradas'])
need('CHANGELOG.md', ['2.0.1', '2.0.0', 'Breaking changes'])
need('docs/evidence/F4/REVISAO-SEGURANCA.md', ['A01:2025', 'A02:2025', 'A03:2025', 'A04:2025', 'A05:2025',
                                              'A06:2025', 'A07:2025', 'A08:2025', 'A09:2025', 'A10:2025'])
for path, text in (('README.md', readme), ('docs/adr/0001-camada-ia-agente-rag.md', adr)):
    if '```mermaid' not in text:
        problems.append(f'{path}: diagrama mermaid ausente')
for path in ('README.md', 'AI_USAGE.md', 'CHANGELOG.md', 'docs/adr/0001-camada-ia-agente-rag.md', 'docs/evidence/F5/AUDITORIA.md',
             'docs/evidence/F5/ADERENCIA-V2.0.0.md', 'docs/evidence/F4/REVISAO-SEGURANCA.md'):
    text = open(path, encoding='utf-8').read()
    base = os.path.dirname(path)
    for target in re.findall(r'\]\(([^)#\s]+)(?:#[^)]*)?\)', text):
        if re.match(r'[a-z]+://', target):
            continue
        if not os.path.exists(os.path.normpath(os.path.join(base, target))):
            problems.append(f'{path}: link quebrado {target}')
collection = json.load(open('docs/api/facilit-kanban.postman_collection.json', encoding='utf-8'))
for variable in collection['variable']:
    if variable['key'] in ('adminEmail', 'adminPassword') and variable['value']:
        problems.append(f'credencial versionada na coleção: {variable["key"]}')
first = re.search(r'^## \[([^\]]+)\] — (\S+)', open('CHANGELOG.md', encoding='utf-8').read(), re.M)
if not first or first.groups() != ('2.0.1', '2026-09-25'):
    problems.append(f'CHANGELOG: primeira entrada {first and first.groups()}')
for stale in ('a tag só é criada após o freeze GREEN', 'em todas as listagens'):
    if stale in readme:
        problems.append(f'README com frase desatualizada: {stale}')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print('   README, AI_USAGE, CHANGELOG 2.0.1, ADR, auditorias, revisão de segurança, diagramas, links e coleção OK')
PY
git diff --check
echo 'F5_P3_STATIC_GREEN'
printf '%s\n' '=== DOCKER: BANCO LIMPO + MIGRATIONS DO ZERO + OBSERVABILIDADE ==='
docker compose down --remove-orphans >/dev/null 2>&1 || true
"${DC[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
run_logged /tmp/f5p3-compose.log "${DC[@]}" up --build -d
for i in $(seq 1 60); do
  if curl -fsS http://localhost:8080/actuator/health >/tmp/f5p3-health.json 2>/dev/null \
    && curl -fsS http://localhost:5173/ >/tmp/f5p3-index.html 2>/dev/null \
    && curl -fsS http://127.0.0.1:3000/api/health >/tmp/f5p3-grafana-health.json 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 60 ]; then "${DC[@]}" logs --no-color backend frontend prometheus grafana | tail -80; fail 'serviços não subiram'; fi
  sleep 2
done
MIGRATIONS="$(sql 'SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank;' | tr '\n' ' ' | sed 's/ *$//')"
test "$MIGRATIONS" = '1 2 3 4 5 6 7' || fail "migrations aplicadas: '$MIGRATIONS'"
test "$(sql 'SELECT count(*) FROM flyway_schema_history WHERE NOT success;')" = '0' || fail 'migration com falha'
echo "   banco novo ($FREEZE_PROJECT); migrations V$MIGRATIONS aplicadas do zero"
python3 - <<'PY'
import json
data = json.load(open('/tmp/f5p3-health.json', encoding='utf-8'))
assert data.get('status') == 'UP' and set(data) <= {'status', 'groups'}, data
PY
echo 'F5_P3_DOCKER_CLEAN_DB_GREEN'

echo '=== UI + SWAGGER ==='
grep -q 'id="root"' /tmp/f5p3-index.html || fail 'index do frontend sem o elemento raiz'
test "$(curl -sS -o /tmp/f5p3-proxy.json -w '%{http_code}' http://localhost:5173/api/v1/health)" = '200' || fail 'proxy /api do frontend'
test "$(curl -sSL -o /tmp/f5p3-swagger.html -w '%{http_code}' http://localhost:8080/swagger-ui.html)" = '200' || fail 'Swagger UI'
grep -qi 'swagger' /tmp/f5p3-swagger.html || fail 'página do Swagger UI inesperada'
curl -fsS http://localhost:8080/api-docs >/tmp/f5p3-openapi.json
python3 - <<'PY'
import json
paths = json.load(open('/tmp/f5p3-openapi.json', encoding='utf-8'))['paths']
for path in ('/api/v1/projects', '/api/v1/projects/{id}', '/api/v1/projects/{id}/status', '/api/v1/responsibles',
             '/api/v1/responsibles/{id}/credentials', '/api/v1/secretariats', '/api/v1/indicators/projects', '/api/v1/indicators/projects/by-secretariat',
             '/api/v1/indicators/projects/by-responsible', '/api/v1/indicators/projects/deadlines', '/api/v1/auth/login'):
    assert path in paths, path
print(f'   OpenAPI com {len(paths)} caminhos')
PY
python3 - <<'PY'
import json, sys
docs = json.load(open('/tmp/f5p3-openapi.json', encoding='utf-8'))
schemas = docs.get('components', {}).get('schemas', {})
COMPOSITIONS = ('allOf', 'oneOf', 'anyOf')
def has_example(node):
    return isinstance(node, dict) and ('example' in node or 'examples' in node)
# Mesma regra do OpenApiContractIT (F5-P1): exemplo próprio, ou referência/lista/objeto com todas as partes exemplificadas.
def missing(schema, where, visiting):
    if not isinstance(schema, dict):
        return [where + ' sem schema']
    if has_example(schema):
        return []
    if '$ref' in schema:
        name = schema['$ref'].rsplit('/', 1)[-1]
        return [] if name in visiting else missing(schemas.get(name), f'{where} → {name}', visiting | {name})
    if 'items' in schema:
        return missing(schema['items'], where + '[]', visiting)
    result = []
    for composition in COMPOSITIONS:
        for part in schema.get(composition, []):
            result += missing(part, where, visiting)
    properties = schema.get('properties') or {}
    for key, value in properties.items():
        result += missing(value, f'{where}.{key}', visiting)
    if not properties and not any(c in schema for c in COMPOSITIONS):
        return [where + ' sem exemplo']
    return result
problems, operations = [], 0
for path, item in docs['paths'].items():
    if not path.startswith('/api/v1/'):
        continue
    for method in ('get', 'post', 'put', 'patch', 'delete'):
        if method not in item:
            continue
        operations += 1
        op, label = item[method], f'{method.upper()} {path}'
        for parameter in op.get('parameters', []):
            if not has_example(parameter):
                problems += [f"{label} parâmetro {parameter.get('name')}{m}" for m in missing(parameter.get('schema'), '', frozenset())]
        contents = [(label + ' corpo', op['requestBody'].get('content', {}))] if 'requestBody' in op else []
        contents += [(f'{label} resposta {code}', response['content']) for code, response in op.get('responses', {}).items()
                     if code.startswith('2') and response.get('content')]
        for where, content in contents:
            for media, value in content.items():
                if not has_example(value):
                    problems += [f'{where} ({media}){m}' for m in missing(value.get('schema'), '', frozenset())]
if operations != 26:
    problems.append(f'operações REST em /api/v1: {operations} (esperado 26)')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print(f'   exemplos em parâmetros, corpos e respostas de sucesso das {operations} operações REST')
PY
curl -fsS http://localhost:8080/swagger-ui/swagger-initializer.js -o /tmp/f5p3-initializer.js
grep -q 'requestInterceptor' /tmp/f5p3-initializer.js && grep -q 'X-XSRF-TOKEN' /tmp/f5p3-initializer.js \
  || fail 'Swagger UI sem o envio do token CSRF'
echo '   Swagger UI envia o token CSRF (requestInterceptor)'
echo 'F5_P3_UI_SWAGGER_GREEN'

echo '=== REST + GRAPHQL: SMOKE ADMIN + INDICADORES F5 ==='
BASE=http://localhost:8080
JAR=/tmp/f5p3-smoke.cookies
SMOKE_ID="$(date +%s)"
TODAY="$(TZ=America/Sao_Paulo date +%F)"
PLANNED_END="$(TZ=America/Sao_Paulo date -d '+10 days' +%F)"
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
  test "$(curl -sS -o /tmp/f5p3-csrf.json -w '%{http_code}' -b "$JAR" -c "$JAR" "$BASE/api/v1/auth/csrf")" = '200'
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

check 'health REST' '200' "$(curl -sS -o /tmp/f5p3-s01.json -w '%{http_code}' "$BASE/api/v1/health")"
test "$(json_get /tmp/f5p3-s01.json status)" = 'UP'
code="$(curl -sS -o /tmp/f5p3-s02.json -w '%{http_code}' "$BASE/api/v1/projects?page=0&size=20")"
check 'projetos sem sessão' '401' "$code"
test "$(json_get /tmp/f5p3-s02.json code)" = 'UNAUTHORIZED'
csrf; check 'CSRF antes do login' 'ok' 'ok'
check 'login ADMIN' '200' "$(call POST /api/v1/auth/login "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" /tmp/f5p3-s04.json)"
python3 -c "import json; assert 'ROLE_ADMIN' in json.load(open('/tmp/f5p3-s04.json'))['authorities']"
csrf; check 'CSRF renovado após login' 'ok' 'ok'
check 'sessão atual' '200' "$(call GET /api/v1/auth/me '' /tmp/f5p3-s06.json)"
test "$(json_get /tmp/f5p3-s06.json email)" = "$APP_ADMIN_EMAIL"
check 'criar secretaria' '201' "$(call POST /api/v1/secretariats "{\"name\":\"Secretaria Smoke $SMOKE_ID\"}" /tmp/f5p3-s07.json)"
SECRETARIAT_ID="$(json_get /tmp/f5p3-s07.json id)"
check 'criar responsável' '201' "$(call POST /api/v1/responsibles "{\"name\":\"Responsável Smoke\",\"email\":\"smoke-$SMOKE_ID@example.invalid\",\"position\":\"Analista\",\"secretariatId\":\"$SECRETARIAT_ID\"}" /tmp/f5p3-s08.json)"
RESPONSIBLE_ID="$(json_get /tmp/f5p3-s08.json id)"
check 'e-mail duplicado' '409' "$(call POST /api/v1/responsibles "{\"name\":\"Duplicado\",\"email\":\"smoke-$SMOKE_ID@example.invalid\",\"position\":\"Analista\"}" /tmp/f5p3-s09.json)"
test "$(json_get /tmp/f5p3-s09.json code)" = 'CONFLICT'
check 'criar projeto' '201' "$(call POST /api/v1/projects "{\"name\":\"Projeto Smoke $SMOKE_ID\",\"responsibleIds\":[\"$RESPONSIBLE_ID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$PLANNED_END\"}" /tmp/f5p3-s10.json)"
PROJECT_ID="$(json_get /tmp/f5p3-s10.json id)"
test "$(json_get /tmp/f5p3-s10.json status)" = 'NOT_STARTED'
check 'projeto sem responsável' '400' "$(call POST /api/v1/projects '{"name":"Inválido","responsibleIds":[]}' /tmp/f5p3-s11.json)"
test "$(json_get /tmp/f5p3-s11.json code)" = 'VALIDATION_ERROR'
check 'transição A iniciar → Em andamento' '200' "$(call PATCH "/api/v1/projects/$PROJECT_ID/status" '{"status":"IN_PROGRESS"}' /tmp/f5p3-s12.json)"
test "$(json_get /tmp/f5p3-s12.json status)" = 'IN_PROGRESS'
test "$(json_get /tmp/f5p3-s12.json actualStart)" = "$TODAY"
check 'transição bloqueada Em andamento → Atrasado' '422' "$(call PATCH "/api/v1/projects/$PROJECT_ID/status" '{"status":"OVERDUE"}' /tmp/f5p3-s13.json)"
test "$(json_get /tmp/f5p3-s13.json code)" = 'TRANSITION_BLOCKED'
check 'listar com filtros' '200' "$(call GET "/api/v1/projects?page=0&size=20&status=IN_PROGRESS&responsibleId=$RESPONSIBLE_ID&text=Smoke%20$SMOKE_ID" '' /tmp/f5p3-s14.json)"
python3 - "$PROJECT_ID" <<'PY'
import json, sys
with open('/tmp/f5p3-s14.json', encoding='utf-8') as handle:
    ids = [item['id'] for item in json.load(handle)['content']]
assert ids == [sys.argv[1]], ids
PY
check 'busca literal text=_ (nenhum nome com _)' '200' "$(call GET '/api/v1/projects?page=0&size=20&text=_' '' /tmp/f5p3-s14a.json)"
python3 -c "import json; c = json.load(open('/tmp/f5p3-s14a.json'))['content']; assert c == [], c"
check 'indicadores' '200' "$(call GET /api/v1/indicators/projects '' /tmp/f5p3-s15.json)"
python3 -c "import json; assert json.load(open('/tmp/f5p3-s15.json'))['totalProjects'] >= 1"
check 'indicadores por secretaria' '200' "$(call GET /api/v1/indicators/projects/by-secretariat '' /tmp/f5p3-s15a.json)"
check 'indicadores por responsável' '200' "$(call GET /api/v1/indicators/projects/by-responsible '' /tmp/f5p3-s15b.json)"
check 'prazos em 7 dias' '200' "$(call GET '/api/v1/indicators/projects/deadlines?withinDays=7' '' /tmp/f5p3-s15c.json)"
check 'withinDays inválido' '400' "$(call GET '/api/v1/indicators/projects/deadlines?withinDays=0' '' /tmp/f5p3-s15d.json)"
test "$(json_get /tmp/f5p3-s15d.json code)" = 'INVALID_REQUEST'
GRAPHQL_BODY="$(python3 -c 'import json, sys; print(json.dumps({"query": "query($id: ID!) { project(id: $id) { id status remainingTimePercentage delayDays } projectIndicators { totalProjects byStatus { status projectCount averageDelayDays } } }", "variables": {"id": sys.argv[1]}}))' "$PROJECT_ID")"
check 'GraphQL projeto e indicadores' '200' "$(call POST /graphql "$GRAPHQL_BODY" /tmp/f5p3-s16.json)"
python3 -c "import json; body = json.load(open('/tmp/f5p3-s16.json')); assert 'errors' not in body, body; assert body['data']['project']['status'] == 'IN_PROGRESS', body"
check 'excluir projeto' '204' "$(call DELETE "/api/v1/projects/$PROJECT_ID" '' /tmp/f5p3-s17.json)"
check 'excluir responsável' '204' "$(call DELETE "/api/v1/responsibles/$RESPONSIBLE_ID" '' /tmp/f5p3-s18.json)"
check 'excluir secretaria' '204' "$(call DELETE "/api/v1/secretariats/$SECRETARIAT_ID" '' /tmp/f5p3-s19.json)"
check 'logout' '204' "$(call POST /api/v1/auth/logout '' /tmp/f5p3-s20.json)"
check 'sessão encerrada' '401' "$(curl -sS -o /tmp/f5p3-s21.json -w '%{http_code}' -b "$JAR" "$BASE/api/v1/auth/me")"
test "$PASSED" -eq 26
echo 'F5_P3_API_GREEN'

echo '=== AUTENTICAÇÃO DO RESPONSÁVEL: LEGÍTIMO E ATAQUE ==='
JAR=/tmp/f5p3-admin2.cookies; rm -f "$JAR"; csrf
code="$(curl -sS -D /tmp/f5p3-login-headers.txt -o /tmp/f5p3-admin-login.json -w '%{http_code}' -X POST -b "$JAR" -c "$JAR" \
  -H 'Content-Type: application/json' -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  --data "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" "$BASE/api/v1/auth/login")"
check 'login ADMIN com cabeçalhos' '200' "$code"
csrf
check 'ADMIN cria outro responsável' '201' "$(call POST /api/v1/responsibles "{\"name\":\"Outro F5\",\"email\":\"f5-other-$SMOKE_ID@example.invalid\",\"position\":\"Gestor\"}" /tmp/f5p3-r1.json)"
OTHER_ID="$(json_get /tmp/f5p3-r1.json id)"
check 'ADMIN cria projeto alheio' '201' "$(call POST /api/v1/projects "{\"name\":\"Projeto Smoke alheio $SMOKE_ID\",\"responsibleIds\":[\"$OTHER_ID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$PLANNED_END\"}" /tmp/f5p3-r2.json)"
OTHER_PROJECT_ID="$(json_get /tmp/f5p3-r2.json id)"
ADMIN_JAR="$JAR"

JAR=/tmp/f5p3-demo.cookies; rm -f "$JAR"; csrf
check 'login do responsável de demonstração' '200' "$(call POST /api/v1/auth/login "{\"email\":\"$APP_DEMO_RESPONSIBLE_EMAIL\",\"password\":\"$APP_DEMO_RESPONSIBLE_PASSWORD\"}" /tmp/f5p3-r3.json)"
python3 -c "import json; d = json.load(open('/tmp/f5p3-r3.json')); assert d['authorities'] == ['ROLE_RESPONSIBLE'] and d['responsibleId'], d"
DEMO_ID="$(json_get /tmp/f5p3-r3.json responsibleId)"
csrf
check 'responsável cria o próprio projeto' '201' "$(call POST /api/v1/projects "{\"name\":\"Projeto Smoke próprio $SMOKE_ID\",\"responsibleIds\":[\"$DEMO_ID\"],\"plannedStart\":\"$TODAY\",\"plannedEnd\":\"$PLANNED_END\"}" /tmp/f5p3-r4.json)"
OWN_PROJECT_ID="$(json_get /tmp/f5p3-r4.json id)"
check 'responsável transiciona o próprio projeto' '200' "$(call PATCH "/api/v1/projects/$OWN_PROJECT_ID/status" '{"status":"IN_PROGRESS"}' /tmp/f5p3-r5.json)"
check 'responsável altera projeto alheio' '403' "$(call PATCH "/api/v1/projects/$OTHER_PROJECT_ID/status" '{"status":"IN_PROGRESS"}' /tmp/f5p3-r6.json)"
test "$(json_get /tmp/f5p3-r6.json code)" = 'FORBIDDEN'
check 'responsável cria secretaria' '403' "$(call POST /api/v1/secretariats '{"name":"Indevida F5"}' /tmp/f5p3-r7.json)"
check 'responsável define credencial' '403' "$(call PUT "/api/v1/responsibles/$OTHER_ID/credentials" '{"password":"tentativa-indevida-123"}' /tmp/f5p3-r8.json)"
check 'GraphQL do responsável bloqueado' '200' "$(call POST /graphql '{"query":"mutation { createSecretariat(input: {name: \"Indevida\"}) { id } }"}' /tmp/f5p3-r9.json)"
test "$(json_get /tmp/f5p3-r9.json errors.0.extensions.code)" = 'FORBIDDEN'
check 'responsável exclui o próprio projeto' '204' "$(call DELETE "/api/v1/projects/$OWN_PROJECT_ID" '' /tmp/f5p3-r10.json)"
JAR="$ADMIN_JAR"; csrf
check 'ADMIN exclui projeto alheio' '204' "$(call DELETE "/api/v1/projects/$OTHER_PROJECT_ID" '' /tmp/f5p3-r11.json)"
check 'ADMIN exclui outro responsável' '204' "$(call DELETE "/api/v1/responsibles/$OTHER_ID" '' /tmp/f5p3-r12.json)"
echo 'F5_P3_RESPONSIBLE_AUTH_GREEN'

echo '=== REVISÃO DE SEGURANÇA EXECUTÁVEL ==='
grep -i '^set-cookie: JSESSIONID=' /tmp/f5p3-login-headers.txt | grep -qi 'httponly' || fail 'cookie de sessão sem HttpOnly'
grep -i '^set-cookie: JSESSIONID=' /tmp/f5p3-login-headers.txt | grep -qi 'samesite=lax' || fail 'cookie de sessão sem SameSite=Lax'
curl -sS -D /tmp/f5p3-headers.txt -o /dev/null "$BASE/api/v1/health"
grep -qi '^x-content-type-options: nosniff' /tmp/f5p3-headers.txt || fail 'sem X-Content-Type-Options'
grep -qi '^x-frame-options: DENY' /tmp/f5p3-headers.txt || fail 'sem X-Frame-Options'
code="$(curl -sS -o /tmp/f5p3-sec1.json -w '%{http_code}' -X POST -b "$JAR" -H 'Content-Type: application/json' --data '{"name":"Sem CSRF"}' "$BASE/api/v1/secretariats")"
test "$code" = '403' && test "$(json_get /tmp/f5p3-sec1.json code)" = 'FORBIDDEN' || fail "POST sem CSRF devolveu $code"
code="$(call GET /api/v1/rota-inexistente '' /tmp/f5p3-sec2.json)"
test "$code" = '404' || fail "rota inexistente devolveu $code"
if grep -qiE '"trace"|"exception"|\bat [a-z]+\.[a-z]+\.' /tmp/f5p3-sec2.json; then fail 'resposta de erro com detalhe interno'; fi
test "$(sql "SELECT count(*) FROM app_users WHERE password_hash NOT LIKE '{bcrypt}%';")" = '0' || fail 'senha sem bcrypt'
test "$(sql 'SELECT count(*) FROM app_users;')" -ge 2 || fail 'usuários de bootstrap ausentes'
test "$(curl -sS -o /dev/null -w '%{http_code}' "$BASE/actuator/prometheus")" = '401' || fail 'métricas sem autenticação'
for path in /actuator/env /actuator/heapdump /actuator/beans /actuator/metrics; do
  case "$(curl -sS -o /dev/null -w '%{http_code}' "$BASE$path")" in 2*) fail "endpoint exposto: $path" ;; esac
done
test "$("${DC[@]}" port prometheus 9090)" = '127.0.0.1:9090' || fail 'Prometheus fora de 127.0.0.1'
test "$("${DC[@]}" port grafana 3000)" = '127.0.0.1:3000' || fail 'Grafana fora de 127.0.0.1'
# Portas publicadas lidas no próprio contêiner (o `docker compose port` não fica vazio para porta não publicada).
DB_CONTAINER="$("${DC[@]}" ps -q db)"
test -n "$DB_CONTAINER" || fail 'contêiner do banco não encontrado'
docker inspect -f '{"bindings": {{json .HostConfig.PortBindings}}, "ports": {{json .NetworkSettings.Ports}}}' "$DB_CONTAINER" >/tmp/f5p3-db-ports.json
python3 - <<'PY'
import json, sys
data = json.load(open('/tmp/f5p3-db-ports.json', encoding='utf-8'))
published = {port: hosts for section in data.values() for port, hosts in (section or {}).items() if hosts}
if published:
    sys.exit(f'FALHA: PostgreSQL publicado no host: {published}')
print(f"   banco sem porta no host (portas do contêiner: {sorted((data['ports'] or {}).keys())})")
PY
echo 'F5_P3_SECURITY_GREEN'

echo '=== PROMETHEUS + GRAFANA + LOGS ECS ==='
printf 'user = "prometheus:%s"\n' "$METRICS_PASSWORD" \
  | curl -sS -K - -o /tmp/f5p3-metrics.txt -w '%{http_code}' "$BASE/actuator/prometheus" | grep -qx 200 || fail 'métricas com a credencial técnica'
grep -q 'http_server_requests_seconds_bucket' /tmp/f5p3-metrics.txt || fail 'histograma HTTP ausente'
for i in $(seq 1 45); do
  if curl -fsS http://127.0.0.1:9090/api/v1/targets >/tmp/f5p3-targets.json 2>/dev/null && python3 - <<'PY'
import json, sys
targets = json.load(open('/tmp/f5p3-targets.json', encoding='utf-8'))['data']['activeTargets']
backend = [t for t in targets if t['labels'].get('job') == 'facilit-kanban-backend']
sys.exit(0 if len(backend) == 1 and backend[0]['health'] == 'up' else 1)
PY
  then break; fi
  if [ "$i" -eq 45 ]; then cat /tmp/f5p3-targets.json; fail 'alvo do Prometheus não ficou up'; fi
  sleep 2
done
printf 'user = "admin:%s"\n' "$GRAFANA_ADMIN_PASSWORD" | curl -fsS -K - -o /tmp/f5p3-grafana-ds.json http://127.0.0.1:3000/api/datasources/uid/prometheus/health
test "$(json_get /tmp/f5p3-grafana-ds.json status)" = 'OK' || fail 'datasource do Grafana'
printf 'user = "admin:%s"\n' "$GRAFANA_ADMIN_PASSWORD" | curl -fsS -K - -o /tmp/f5p3-grafana-dash.json http://127.0.0.1:3000/api/dashboards/uid/facilit-kanban
python3 -c "import json; d = json.load(open('/tmp/f5p3-grafana-dash.json'))['dashboard']; assert d['uid'] == 'facilit-kanban' and len(d['panels']) == 6, d.get('uid')"
"${DC[@]}" logs --no-color --no-log-prefix backend >/tmp/f5p3-backend-container.txt
python3 - <<'PY'
import json
records = []
for line in open('/tmp/f5p3-backend-container.txt', encoding='utf-8'):
    line = line.strip()
    if line.startswith('{'):
        try:
            records.append(json.loads(line))
        except json.JSONDecodeError:
            pass
started = [r for r in records if str(r.get('message', '')).startswith('Started ')]
assert started and '@timestamp' in started[0] and started[0].get('ecs', {}).get('version'), 'log ECS ausente'
print(f'   {len(records)} registros ECS no backend')
PY
for secret in "$POSTGRES_PASSWORD" "$APP_ADMIN_PASSWORD" "$APP_DEMO_RESPONSIBLE_PASSWORD" "$METRICS_PASSWORD" "$GRAFANA_ADMIN_PASSWORD"; do
  if grep -qF -- "$secret" /tmp/f5p3-backend-container.txt; then fail 'segredo no log do backend'; fi
done
echo 'F5_P3_OBSERVABILITY_GREEN'

echo '=== CI DO COMMIT DA hotfix/2.0.1 ==='
API="https://api.github.com/repos/$GITHUB_REPO"
RUN_STATE=""
for i in $(seq 1 35); do
  curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs?head_sha=$HEAD_SHA&per_page=50" >/tmp/f5p3-runs.json
  RUN_STATE="$(python3 - <<'PY'
import json
runs = [r for r in json.load(open('/tmp/f5p3-runs.json', encoding='utf-8'))['workflow_runs'] if r.get('path', '').startswith('.github/workflows/ci.yml')]
if not runs:
    print('none -')
else:
    run = max(runs, key=lambda r: r['run_attempt'] * 10**12 + r['id'])
    print(f"{run['status']}:{run['conclusion']} {run['id']} {run['html_url']}")
PY
)"
  echo "   [$i] $RUN_STATE"
  case "$RUN_STATE" in
    completed:success*) break ;;
    completed:*) fail 'pipeline da release terminou sem sucesso' ;;
  esac
  if [ "$i" -eq 35 ]; then fail 'pipeline da release não concluiu em 35 minutos'; fi
  sleep 60
done
RUN_ID_CI="$(echo "$RUN_STATE" | cut -d' ' -f2)"
curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs/$RUN_ID_CI/jobs?per_page=50" >/tmp/f5p3-jobs.json
python3 -c "import json; jobs = {j['name']: j['conclusion'] for j in json.load(open('/tmp/f5p3-jobs.json'))['jobs']}; print('  ', jobs); assert jobs == {'frontend': 'success', 'backend': 'success', 'repository': 'success'}, jobs"
echo 'F5_P3_CI_GREEN'

if [ -n "$(git status --porcelain=v1)" ]; then git status --short >&2; fail 'o gate deixou alterações na área de trabalho'; fi
printf '%s\n' '=== F5-P3 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
exit "$STATUS"
```

GREEN exige todos os marcadores `F5_P3_*_GREEN`, `=== F5-P3 GREEN ===` e `Resultado: exit code 0`. Depois do GREEN, siga o passo 4 de `RELEASE.md`.
