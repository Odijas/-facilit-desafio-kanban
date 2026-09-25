# F5-P1 — VERIFICAÇÃO DO USUÁRIO

O gate prova o lote F5-P1 (Swagger com exemplos completos, filtro de texto literal e versão 2.0.1) na branch `bugfix/2.0.1-p1-swagger`, criada da `hotfix/2.0.1`, antes do commit. Ele cobre:

- pré-condições: a `hotfix/2.0.1` igual à tag `v2.0.0`, arquivos alterados iguais aos do pacote, frontend só com a versão;
- build limpo do backend: contagem exata, BDD intacto e JaCoCo ≥ 95%;
- checagens estáticas;
- subida com banco novo e imagem `kanban-2.0.1.jar`;
- na API real:
  - exemplo em todo parâmetro, corpo e resposta de sucesso das 26 operações REST (`/api-docs`);
  - `%` e `_` como caracteres literais na busca (REST e GraphQL);
  - regressões do CSRF no Swagger UI e dos limites de entrada.

O CI deste lote é conferido no gate do F5-P2, depois do push (mesmo esquema dos lotes F5-C).

**Isolamento:**

- O gate usa um projeto Compose próprio (`facilit-kanban-f5p1`) e o remove no fim (`down -v` só desse projeto).
- O projeto padrão é apenas parado (`docker compose down`, sem `-v`).
- A porta 8080 precisa estar livre.

## 0. Criar a `hotfix/2.0.1` (Gitflow, uma vez)

A `hotfix/2.0.1` nasce da `main`, que precisa estar exatamente na tag `v2.0.0`. O bloco para no primeiro erro e imprime `HOTFIX_OK` no fim. O GitLab é espelho: se falhar, o bloco avisa e segue.

```sh
cd ~/proj/facilit-desafio-kanban && \
{ [ -z "$(git status --porcelain=v1)" ] || { echo 'PARE: árvore com alterações'; false; }; } && \
git fetch origin --tags && git checkout main && git pull --ff-only origin main && \
{ [ "$(git rev-parse HEAD)" = "$(git rev-parse 'v2.0.0^{commit}')" ] || { echo 'PARE: a main não está na v2.0.0'; false; }; } && \
{ if git rev-parse -q --verify refs/heads/hotfix/2.0.1 >/dev/null; then git checkout hotfix/2.0.1; else git checkout -b hotfix/2.0.1; fi; } && \
git push -u origin hotfix/2.0.1 && \
{ git push gitlab hotfix/2.0.1 || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'; } && \
echo HOTFIX_OK
```

## 1. Preparar a branch e aplicar o pacote

O bloco para no primeiro erro, mostra uma mensagem `PARE:` quando algo está fora do esperado e imprime `PREPARO_OK` só se tudo der certo. Serve para a primeira vez e para repetir (se já estiver na branch do lote, só reaplica o pacote).

```sh
cd ~/proj/facilit-desafio-kanban && F=bugfix/2.0.1-p1-swagger && \
if [ "$(git branch --show-current)" != "$F" ]; then \
  { [ -z "$(git status --porcelain=v1)" ] || { echo 'PARE: árvore com alterações'; false; }; } && \
  git checkout hotfix/2.0.1 && git pull --ff-only origin hotfix/2.0.1 && \
  { if git rev-parse -q --verify "refs/heads/$F" >/dev/null; then git checkout "$F"; else git checkout -b "$F"; fi; }; \
fi && \
{ [ -z "$(git diff --name-only v2.0.0 HEAD)" ] || { echo 'PARE: a base da branch não é a v2.0.0 (seção 0)'; false; }; } && \
(cd ~/Downloads && echo "<SHA256>  facilit-desafio-kanban-F5-P1-candidate-rev2.tar.gz" | sha256sum -c -) && \
tar -xzf ~/Downloads/facilit-desafio-kanban-F5-P1-candidate-rev2.tar.gz && \
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5-P1/VERIFICACAO-USUARIO.md > /tmp/f5p1-gate.sh && \
echo PREPARO_OK
```

Troque `<SHA256>` pelo hash informado com o pacote.

## 2. Rodar o gate

```sh
bash /tmp/f5p1-gate.sh 2>&1 | tee /tmp/saida-gate-f5p1.txt
```

Os logs longos ficam em `/tmp/f5p1-*.log`.

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
git rev-parse --is-inside-work-tree >/dev/null
# Limpa a execução anterior, sem apagar o próprio gate (/tmp/f5p1-gate.sh), para ele poder ser repetido.
find /tmp -maxdepth 1 -name 'f5p1-*' ! -name 'f5p1-gate.sh' -exec rm -rf {} +

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
  bugfix/2.0.1-*) ;;
  *) fail "branch atual: $BRANCH (esperado bugfix/2.0.1-p1-swagger)" ;;
esac
git rev-parse -q --verify 'v2.0.0^{commit}' >/dev/null || fail 'tag v2.0.0 ausente (git fetch origin --tags)'
test -z "$(git diff --name-only v2.0.0 HEAD)" || fail 'o commit base da branch não é a v2.0.0 (seção 0)'
git status --porcelain=v1 -uall >/tmp/f5p1-status.txt
python3 - <<'PY'
import sys
main = 'backend/src/main/java/br/com/facilit/kanban/'
test = 'backend/src/test/java/br/com/facilit/kanban/'
expected = {
    'backend/pom.xml',
    'backend/Dockerfile',
    'frontend/package.json',
    main + 'delivery/common/ApiExamples.java',
    main + 'delivery/auth/AuthRestController.java',
    main + 'infrastructure/persistence/project/ProjectPersistenceAdapter.java',
    test + 'integration/OpenApiContractIT.java',
    test + 'integration/ProjectPersistenceAdapterIT.java',
    'docs/governance/PLANO-CORRECAO-RELEASE-2.0.1.md',
}
expected |= {main + 'delivery/rest/' + name + '.java' for name in (
    'ApiListExampleDocumentation', 'HealthRestController', 'PageResponse', 'ProjectDeadlinesResponse',
    'ProjectGroupIndicatorResponse', 'ProjectIndicatorsResponse', 'ProjectIndicatorsRestController', 'ProjectResponse',
    'ProjectRestController', 'ResponsibleCredentialsRequest', 'ResponsibleResponse', 'ResponsibleRestController',
    'SecretariatRequest', 'SecretariatResponse', 'SecretariatRestController')}
expected |= {f'docs/evidence/F5-P1/{name}.md' for name in (
    'CONSUMIDORES', 'DECISOES', 'EXECUCAO', 'FONTES-RAG', 'GATE', 'LEITURA', 'MATRIZ', 'RISCOS', 'VERIFICACAO-USUARIO')}
actual = set()
for line in open('/tmp/f5p1-status.txt', encoding='utf-8'):
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
print(f'   {len(actual)} arquivos alterados, todos do pacote F5-P1; no frontend, só o package.json')
PY
echo "   branch $BRANCH sobre a hotfix/2.0.1 = v2.0.0 ($(git rev-parse --short HEAD))"
echo 'F5_P1_PRECONDITIONS_GREEN'

printf '%s\n' '=== BACKEND: CLEAN VERIFY + BDD + JACOCO ==='
(cd backend && run_logged /tmp/f5p1-backend.log mvn -B -ntp clean verify)
grep -q 'BUILD SUCCESS' /tmp/f5p1-backend.log
if grep -q '\[deprecation\]' /tmp/f5p1-backend.log; then grep '\[deprecation\]' /tmp/f5p1-backend.log >&2; fail 'uso de API depreciada no código do backend'; fi
if grep -Eqi 'self-attaching|loaded dynamically' /tmp/f5p1-backend.log; then fail 'o Mockito foi anexado dinamicamente'; fi
test -f backend/target/kanban-2.0.1.jar || fail 'jar kanban-2.0.1.jar não gerado'
python3 - <<'PY'
import glob, json
import xml.etree.ElementTree as ET
expected_totals = {'unitários/BDD': (25, 174), 'integração': (12, 51)}
required = {
    'br.com.facilit.kanban.integration.OpenApiContractIT': 5,
    'br.com.facilit.kanban.integration.ProjectPersistenceAdapterIT': 6,
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
assert len(scenarios) == 12 and len(steps) == 48, (len(scenarios), len(steps))
assert all(step['result']['status'] == 'passed' for step in steps), [s['name'] for s in steps if s['result']['status'] != 'passed']
print(f'   BDD: {len(scenarios)} cenários, {len(steps)} passos aprovados')
report = ET.parse('backend/target/site/jacoco/jacoco.xml').getroot()
line = next(c for c in report.findall('counter') if c.get('type') == 'LINE')
covered, missed = int(line.get('covered')), int(line.get('missed'))
ratio = covered / (covered + missed)
print(f'   JaCoCo (unitários + integração): {ratio:.2%} de linhas ({covered}/{covered + missed})')
assert ratio >= 0.95, ratio
PY
echo 'F5_P1_BACKEND_GREEN'

echo '=== ESTÁTICA ==='
git diff --check
git diff --quiet v2.0.0 -- .github README.md CHANGELOG.md AI_USAGE.md backend/src/main/resources \
  || fail 'o F5-P1 não altera workflow, README, CHANGELOG, AI_USAGE nem configuração (isso é do F5-P2)'
python3 - <<'PY'
import json, os, re, subprocess, sys
problems = []
names = [line[3:].rstrip('\n') for line in open('/tmp/f5p1-status.txt', encoding='utf-8') if line.strip()]
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
pom = open('backend/pom.xml', encoding='utf-8').read()
project = re.search(r'<artifactId>kanban</artifactId>\s*<version>([^<]+)</version>', pom)
if not project or project.group(1) != '2.0.1':
    problems.append(f'pom.xml fora da 2.0.1: {project and project.group(1)}')
if 'COPY --from=build /workspace/target/kanban-2.0.1.jar app.jar' not in open('backend/Dockerfile', encoding='utf-8').read():
    problems.append('Dockerfile sem kanban-2.0.1.jar')
if json.load(open('frontend/package.json', encoding='utf-8'))['version'] != '2.0.1':
    problems.append('frontend/package.json fora da 2.0.1')
diff = subprocess.run(['git', 'diff', '--numstat', 'v2.0.0', '--', 'frontend/package.json'], capture_output=True, text=True, check=True).stdout.split()
if diff[:2] != ['1', '1']:
    problems.append(f'frontend/package.json com mudança além da versão: {diff}')
adapter = open('backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java', encoding='utf-8').read()
for text in ("LIKE_ESCAPE = '\\\\'", 'containsPattern(filter.text())', 'LIKE_ESCAPE));'):
    if text not in adapter:
        problems.append(f'ProjectPersistenceAdapter sem {text}')
if 'REST_OPERATIONS = 26' not in open('backend/src/test/java/br/com/facilit/kanban/integration/OpenApiContractIT.java', encoding='utf-8').read():
    problems.append('OpenApiContractIT sem o teste de exemplos')
for root, _, files in os.walk('backend/src/main/java/br/com/facilit/kanban/application'):
    for file in files:
        if 'io.swagger' in open(os.path.join(root, file), encoding='utf-8').read():
            problems.append(f'camada de aplicação dependendo do OpenAPI: {file}')
plan = open('docs/governance/PLANO-CORRECAO-RELEASE-2.0.1.md', encoding='utf-8').read()
if '## 4. Decisões do usuário (2026-09-25, 7h50)' not in plan or plan.count('**Aprovado.**') != 2:
    problems.append('plano 2.0.1 sem as duas decisões aprovadas')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print(f'   {len(names)} arquivos do lote OK; versão 2.0.1; LIKE com ESCAPE; aplicação sem OpenAPI; plano com as decisões')
PY
echo 'F5_P1_STATIC_GREEN'

echo '=== DOCKER: BANCO NOVO + MIGRATIONS 1–7 + IMAGEM 2.0.1 ==='
RUN_ID="$(date +%s)"
export POSTGRES_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export APP_ADMIN_EMAIL="f5p1-admin-${RUN_ID}@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export APP_DEMO_RESPONSIBLE_NAME='' APP_DEMO_RESPONSIBLE_EMAIL='' APP_DEMO_RESPONSIBLE_POSITION='' APP_DEMO_RESPONSIBLE_PASSWORD=''
export METRICS_PASSWORD='' SESSION_COOKIE_SECURE=false APP_TIME_ZONE=America/Sao_Paulo
GATE_PROJECT=facilit-kanban-f5p1
DC=(docker compose -p "$GATE_PROJECT" -f compose.yaml)

cleanup() {
  set +e
  "${DC[@]}" down -v --remove-orphans >/dev/null 2>&1
  rm -f /tmp/f5p1-*.json /tmp/f5p1-*.cookies /tmp/f5p1-*.js
}
trap cleanup EXIT

docker compose down --remove-orphans >/dev/null 2>&1 || true
"${DC[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
run_logged /tmp/f5p1-compose.log "${DC[@]}" up --build -d db backend
for i in $(seq 1 60); do
  if curl -fsS http://localhost:8080/actuator/health >/tmp/f5p1-health.json 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 60 ]; then "${DC[@]}" logs --no-color backend | tail -80; fail 'backend não subiu'; fi
  sleep 2
done
MIGRATIONS="$(sql 'SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank;' | tr '\n' ' ' | sed 's/ *$//')"
test "$MIGRATIONS" = '1 2 3 4 5 6 7' || fail "migrations aplicadas: '$MIGRATIONS'"
echo "   banco novo ($GATE_PROJECT); migrations V$MIGRATIONS; imagem construída com kanban-2.0.1.jar"
echo 'F5_P1_DOCKER_GREEN'

echo '=== API: EXEMPLOS NO OPENAPI, FILTRO LITERAL E REGRESSÕES ==='
BASE=http://localhost:8080
JAR=/tmp/f5p1-admin.cookies
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

names_are() {
  python3 - "$@" <<'PY'
import json, sys
path, pointer, *expected = sys.argv[1:]
value = json.load(open(path, encoding='utf-8'))
for part in pointer.split('.'):
    value = value[part]
names = sorted(item['name'] for item in value)
if names != sorted(expected):
    sys.exit(f'nomes: esperado {sorted(expected)}, obtido {names}')
PY
}

check 'GET /api/v1/auth/csrf' '200' "$(call GET /api/v1/auth/csrf '' /tmp/f5p1-a01.json)"
check 'login ADMIN' '200' "$(call POST /api/v1/auth/login "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" /tmp/f5p1-a02.json)"
check 'token novo após o login' '200' "$(call GET /api/v1/auth/csrf '' /tmp/f5p1-a03.json)"

check 'OpenAPI' '200' "$(call GET /api-docs '' /tmp/f5p1-a04.json)"
python3 - <<'PY'
import json, sys
docs = json.load(open('/tmp/f5p1-a04.json', encoding='utf-8'))
schemas = docs.get('components', {}).get('schemas', {})
METHODS = ('get', 'post', 'put', 'patch', 'delete')
COMPOSITIONS = ('allOf', 'oneOf', 'anyOf')

def has_example(node):
    return isinstance(node, dict) and ('example' in node or 'examples' in node)

# Mesma regra do OpenApiContractIT: exemplo próprio, ou referência/lista/objeto com todas as partes exemplificadas.
def missing(schema, where, visiting):
    if not isinstance(schema, dict):
        return [where + ' sem schema']
    if has_example(schema):
        return []
    if '$ref' in schema:
        name = schema['$ref'].rsplit('/', 1)[-1]
        if name in visiting:
            return []
        return missing(schemas.get(name), f'{where} → {name}', visiting | {name})
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

problems, operations, counts = [], 0, dict(parâmetros=0, corpos=0, respostas=0)
for path, item in docs['paths'].items():
    if not path.startswith('/api/v1/'):
        continue
    for method in METHODS:
        if method not in item:
            continue
        operations += 1
        op = item[method]
        label = f'{method.upper()} {path}'
        for parameter in op.get('parameters', []):
            counts['parâmetros'] += 1
            if not has_example(parameter):
                problems += [f"{label} parâmetro {parameter.get('name')}{m}" for m in missing(parameter.get('schema'), '', frozenset())]
        contents = []
        if 'requestBody' in op:
            counts['corpos'] += 1
            contents.append((label + ' corpo', op['requestBody'].get('content', {})))
        for code, response in op.get('responses', {}).items():
            if code.startswith('2') and response.get('content'):
                counts['respostas'] += 1
                contents.append((f'{label} resposta {code}', response['content']))
        for where, content in contents:
            for media, value in content.items():
                if not has_example(value):
                    problems += [f'{where} ({media}){m}' for m in missing(value.get('schema'), '', frozenset())]
csrf = docs['paths']['/api/v1/auth/csrf']['get']
if csrf.get('parameters'):
    problems.append(f"GET /api/v1/auth/csrf documenta parâmetros: {[p.get('name') for p in csrf['parameters']]}")
ids = schemas['ProjectRequest']['properties']['responsibleIds']
if ids.get('maxItems') != 50 or not (has_example(ids) or has_example(ids.get('items'))):
    problems.append(f'ProjectRequest.responsibleIds sem maxItems 50 ou sem exemplo (na lista ou no item): {ids}')
if schemas['ProjectRequest']['properties']['name'].get('maxLength') != 200:
    problems.append('ProjectRequest.name sem maxLength 200 (regressão do F5-C2)')
if operations != 26:
    problems.append(f'operações REST em /api/v1: {operations} (esperado 26)')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print(f"   OpenAPI: {operations} operações; {counts['parâmetros']} parâmetros, {counts['corpos']} corpos e "
      f"{counts['respostas']} respostas de sucesso com exemplo; 0 faltas; /auth/csrf sem parâmetro; maxItems/maxLength mantidos")
PY
check 'exemplos em todas as operações do OpenAPI' 'ok' 'ok'
curl -fsS "$BASE/swagger-ui/swagger-initializer.js" -o /tmp/f5p1-initializer.js
grep -q 'requestInterceptor' /tmp/f5p1-initializer.js && grep -q "X-XSRF-TOKEN" /tmp/f5p1-initializer.js \
  || fail 'Swagger UI sem o envio do CSRF (regressão do F5-C1)'
check 'Swagger UI continua enviando o CSRF' 'ok' 'ok'

check 'responsável para os projetos' '201' "$(call POST /api/v1/responsibles "{\"name\":\"Filtro\",\"email\":\"f5p1-$RUN_ID@example.invalid\",\"position\":\"Analista\"}" /tmp/f5p1-a05.json)"
RESPONSIBLE_ID="$(python3 -c "import json; print(json.load(open('/tmp/f5p1-a05.json'))['id'])")"
N=6
for NAME in 'Meta 100%' 'Meta 1000' 'Lote_A' 'LoteXA'; do
  check "projeto \"$NAME\"" '201' "$(call POST /api/v1/projects "{\"name\":\"$NAME\",\"responsibleIds\":[\"$RESPONSIBLE_ID\"]}" "/tmp/f5p1-a0$N.json")"
  N=$((N + 1))
done
check 'busca text=100% (REST)' '200' "$(call GET '/api/v1/projects?text=100%25' '' /tmp/f5p1-b01.json)"
names_are /tmp/f5p1-b01.json content 'Meta 100%'
check 'busca text=% (REST)' '200' "$(call GET '/api/v1/projects?text=%25' '' /tmp/f5p1-b02.json)"
names_are /tmp/f5p1-b02.json content 'Meta 100%'
check 'busca text=_ (REST)' '200' "$(call GET '/api/v1/projects?text=_' '' /tmp/f5p1-b03.json)"
names_are /tmp/f5p1-b03.json content 'Lote_A'
check 'busca text=lote_a (REST)' '200' "$(call GET '/api/v1/projects?text=lote_a' '' /tmp/f5p1-b04.json)"
names_are /tmp/f5p1-b04.json content 'Lote_A'
check 'busca text=meta (REST)' '200' "$(call GET '/api/v1/projects?text=meta' '' /tmp/f5p1-b05.json)"
names_are /tmp/f5p1-b05.json content 'Meta 100%' 'Meta 1000'
check 'busca text=_ (GraphQL)' '200' "$(call POST /graphql '{"query":"{ projects(page: 0, size: 20, text: \"_\") { content { name } } }"}' /tmp/f5p1-b06.json)"
names_are /tmp/f5p1-b06.json data.projects.content 'Lote_A'
test "$PASSED" -eq 17 || fail "verificações da API: $PASSED (esperado 17)"
echo 'F5_P1_API_GREEN'

printf '%s\n' '=== F5-P1 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige todos os marcadores `F5_P1_*_GREEN`, `=== F5-P1 GREEN ===` e `Resultado: exit code 0`. Traga a saída (`/tmp/saida-gate-f5p1.txt`) e o resultado da seção 3.

## 3. Conferência no navegador (2 minutos)

O script prova a regra dos exemplos no `/api-docs`. Falta ver o Swagger UI mostrando os exemplos:

```sh
cd ~/proj/facilit-desafio-kanban
docker compose up --build -d db backend
```

No navegador, em `http://localhost:8080/swagger-ui.html`:

1. abra `GET /api/v1/projects`: a seção **Parameters** mostra os exemplos (`0`, `20`, `IN_PROGRESS`, `portal`…), e **Responses → 200 → Example Value** mostra uma página com um projeto;
2. abra `GET /api/v1/indicators/projects/deadlines`: **Example Value** mostra `withinDays`, `from`, `to` e um projeto em `projects`;
3. abra `POST /api/v1/secretariats`: o **Request body** mostra `{"name": "Secretaria de Saúde"}`.

Responda "Swagger OK" ou diga o que não apareceu. Depois: `docker compose down`.

## 4. Depois do GREEN: commits semânticos, merge na hotfix e push (Gitflow)

Cada commit compila sozinho, na ordem abaixo.

```sh
cd ~/proj/facilit-desafio-kanban
B=backend/src/main/java/br/com/facilit/kanban
T=backend/src/test/java/br/com/facilit/kanban

git add backend/pom.xml backend/Dockerfile frontend/package.json
git commit -m "build(hotfix): define a versão 2.0.1"

git add $B/delivery/common/ApiExamples.java $B/delivery/rest $B/delivery/auth/AuthRestController.java \
        $T/integration/OpenApiContractIT.java
git commit -m "fix(api): documenta exemplos de parâmetros, corpos e respostas em todas as operações do Swagger"

git add $B/infrastructure/persistence/project/ProjectPersistenceAdapter.java $T/integration/ProjectPersistenceAdapterIT.java
git commit -m "fix(filtros): trata %, _ e \\ do texto de busca como caracteres literais"

git add docs/governance/PLANO-CORRECAO-RELEASE-2.0.1.md docs/evidence/F5-P1
git commit -m "docs(F5-P1): plano da patch release 2.0.1 e evidências do lote"

git status --short        # deve ficar vazio
git checkout hotfix/2.0.1
git merge --no-ff bugfix/2.0.1-p1-swagger -m "Merge branch 'bugfix/2.0.1-p1-swagger' into hotfix/2.0.1"
git push origin hotfix/2.0.1 bugfix/2.0.1-p1-swagger
git push gitlab hotfix/2.0.1 bugfix/2.0.1-p1-swagger || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'
```

O push da `hotfix/2.0.1` dispara o CI. O resultado é conferido no gate do F5-P2 (`F5_P1_CI_GREEN`).
