# F5-Q1 — VERIFICAÇÃO DO USUÁRIO

Data: 2026-09-25

O gate valida o F5-Q1 antes de qualquer commit. A branch do lote deve nascer de `hotfix/2.0.2`, que por sua vez nasce da `v2.0.1` publicada. O pacote candidato contém código, testes e evidências; o GREEN só existe depois desta execução local.

## 0. Criar/atualizar a hotfix 2.0.2

```sh
cd ~/proj/facilit-desafio-kanban && \
{ [ -z "$(git status --porcelain=v1)" ] || { echo 'PARE: árvore com alterações'; false; }; } && \
git fetch origin --tags && git checkout main && git pull --ff-only origin main && \
{ [ "$(git rev-parse HEAD)" = "$(git rev-parse 'v2.0.1^{commit}')" ] || { echo 'PARE: main não está exatamente na v2.0.1'; false; }; } && \
{ if git rev-parse -q --verify refs/heads/hotfix/2.0.2 >/dev/null; then git checkout hotfix/2.0.2; else git checkout -b hotfix/2.0.2; fi; } && \
git push -u origin hotfix/2.0.2 && \
{ git push gitlab hotfix/2.0.2 || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'; } && \
echo HOTFIX_OK
```

## 1. Criar a bugfix e aplicar o pacote

Troque `<SHA256>` pelo hash informado junto do pacote.

```sh
cd ~/proj/facilit-desafio-kanban && F=bugfix/2.0.2-q1-code && \
if [ "$(git branch --show-current)" != "$F" ]; then \
  { [ -z "$(git status --porcelain=v1)" ] || { echo 'PARE: árvore com alterações'; false; }; } && \
  git checkout hotfix/2.0.2 && git pull --ff-only origin hotfix/2.0.2 && \
  { if git rev-parse -q --verify "refs/heads/$F" >/dev/null; then git checkout "$F"; else git checkout -b "$F"; fi; }; \
fi && \
{ [ "$(git rev-parse HEAD)" = "$(git rev-parse hotfix/2.0.2)" ] || { echo 'PARE: bugfix não está na base atual da hotfix/2.0.2'; false; }; } && \
(cd ~/Downloads && echo '<SHA256>  facilit-desafio-kanban-F5-Q1-candidate.tar.gz' | sha256sum -c -) && \
tar -xzf ~/Downloads/facilit-desafio-kanban-F5-Q1-candidate.tar.gz -C ~/proj && \
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5-Q1/VERIFICACAO-USUARIO.md > /tmp/f5q1-gate.sh && \
bash -n /tmp/f5q1-gate.sh && echo PREPARO_OK
```

## 2. Rodar o gate

```sh
bash -o pipefail -c 'bash /tmp/f5q1-gate.sh 2>&1 | tee /tmp/saida-gate-f5q1.txt'
```

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
export TERM="${TERM:-xterm}"

fail() { echo "FALHA: $*" >&2; exit 1; }
run_logged() {
  local log="$1"; shift
  if ! "$@" >>"$log" 2>&1; then
    tail -100 "$log" >&2
    return 1
  fi
}

printf '%s\n' '=== PRÉ-CONDIÇÕES E ESCOPO ==='
BRANCH="$(git branch --show-current)"
case "$BRANCH" in
  bugfix/2.0.2-*) ;;
  *) fail "branch atual: $BRANCH (esperado bugfix/2.0.2-*)" ;;
esac
[ "$(git rev-parse HEAD)" = "$(git rev-parse hotfix/2.0.2)" ] || fail 'HEAD da bugfix não é a base atual da hotfix/2.0.2'
git status --porcelain=v1 -uall >/tmp/f5q1-status.txt
python3 - <<'PY'
import sys
M='backend/src/main/java/br/com/facilit/kanban/'
T='backend/src/test/java/br/com/facilit/kanban/'
expected={
'backend/Dockerfile','backend/pom.xml','frontend/package.json',
M+'application/project/ProjectRepository.java',M+'application/project/ProjectService.java',
M+'delivery/rest/ApiErrorDocumentation.java',M+'domain/project/ProjectStatusTransition.java',
M+'infrastructure/persistence/project/ProjectJpaRepository.java',
M+'infrastructure/persistence/project/ProjectPersistenceAdapter.java',
T+'application/project/ProjectScheduleRefresherTest.java',
T+'application/project/ProjectServiceMockitoTest.java',T+'application/project/ProjectServiceTest.java',
T+'application/support/InMemoryProjectRepository.java',T+'domain/project/ProjectStatusTransitionTest.java',
T+'integration/OpenApiContractIT.java',T+'integration/ProjectPersistenceAdapterIT.java',
T+'integration/ScheduleFreshnessIT.java','docs/governance/PLANO-CORRECAO-RELEASE-2.0.2.md',
}
expected |= {f'docs/evidence/F5-Q1/{n}.md' for n in (
'CONSUMIDORES','DECISOES','EXECUCAO','FONTES-RAG','GATE','LEITURA','MATRIZ','RISCOS','VERIFICACAO-USUARIO')}
actual={line[3:].rstrip('\n') for line in open('/tmp/f5q1-status.txt',encoding='utf-8') if line.strip()}
for p in sorted(expected-actual): print('ausente:',p,file=sys.stderr)
for p in sorted(actual-expected): print('fora do lote:',p,file=sys.stderr)
if actual != expected: sys.exit(1)
print(f'   escopo exato: {len(actual)} arquivos')
PY
python3 - <<'PY'
import json,re
pom=open('backend/pom.xml',encoding='utf-8').read()
m=re.search(r'<artifactId>kanban</artifactId>\s*<version>([^<]+)</version>',pom)
assert m and m.group(1)=='2.0.2',m and m.group(1)
assert 'kanban-2.0.2.jar' in open('backend/Dockerfile',encoding='utf-8').read()
assert json.load(open('frontend/package.json',encoding='utf-8'))['version']=='2.0.2'
print('   versão 2.0.2 em pom.xml, Dockerfile e package.json')
PY
git diff --check
if grep -RInE '\b(listByStatus|findByStatus)\b|projectRepository\.findAll\(|service\.list\(|projectService\.list\(' \
  backend/src/main/java/br/com/facilit/kanban/application/project \
  backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project \
  backend/src/test/java/br/com/facilit/kanban/application/project \
  backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java \
  backend/src/test/java/br/com/facilit/kanban/integration/ProjectPersistenceAdapterIT.java \
  backend/src/test/java/br/com/facilit/kanban/integration/ScheduleFreshnessIT.java; then
  fail 'cadeia morta de listagem de projetos ainda presente'
fi
echo 'F5_Q1_SCOPE_GREEN'

printf '%s\n' '=== BACKEND: CLEAN VERIFY ==='
rm -f /tmp/f5q1-backend.log
(cd backend && run_logged /tmp/f5q1-backend.log mvn -B -ntp clean verify)
grep -q 'BUILD SUCCESS' /tmp/f5q1-backend.log || fail 'Maven sem BUILD SUCCESS'
test -f backend/target/kanban-2.0.2.jar || fail 'kanban-2.0.2.jar não gerado'
python3 - <<'PY'
import glob,xml.etree.ElementTree as ET
expected={'unitários/BDD':(25,177),'integração':(12,52)}
found={}
for kind,pattern in (('unitários/BDD','backend/target/surefire-reports/TEST-*.xml'),('integração','backend/target/failsafe-reports/TEST-*.xml')):
    files=glob.glob(pattern); totals={k:0 for k in ('tests','failures','errors','skipped')}
    for f in files:
        r=ET.parse(f).getroot(); found[r.get('name')]=r
        for k in totals: totals[k]+=int(r.get(k,0))
    assert (len(files),totals['tests'])==expected[kind],(kind,len(files),totals,expected[kind])
    assert totals['failures']==totals['errors']==totals['skipped']==0,(kind,totals)
    print(f"   {kind}: {len(files)} classes, {totals['tests']} testes, 0 falhas/erros/ignorados")
for clazz,name in (
('br.com.facilit.kanban.application.project.ProjectServiceTest','updateCanClearActualStartWithoutTransitionConfirmationAndRecalculatesStatus'),
('br.com.facilit.kanban.domain.project.ProjectStatusTransitionTest','line02WithMissingPlannedDatesExplainsWhatMustBeProvidedWithoutNullText'),
('br.com.facilit.kanban.domain.project.ProjectStatusTransitionTest','line11WhenRecalculatedAsNotStartedAsksForActualStart'),
('br.com.facilit.kanban.integration.OpenApiContractIT','errorExamplesMatchTheOperationResourceInputAndAuthorizationRule')):
    root=found[clazz]
    names={n.get('name') for n in root.findall('testcase')}
    assert name in names,(clazz,name,sorted(names))
    print('   teste presente e executado:',name)
PY
python3 - <<'PY'
import json,xml.etree.ElementTree as ET
root=ET.parse('backend/target/site/jacoco/jacoco.xml').getroot()
c=next(x for x in root.findall('counter') if x.get('type')=='LINE')
covered,missed=int(c.get('covered')),int(c.get('missed')); ratio=covered/(covered+missed)
assert ratio>=.95,ratio
print(f'   JaCoCo linhas: {covered}/{covered+missed} = {ratio:.2%}')
features=json.load(open('backend/target/cucumber.json',encoding='utf-8'))
scenarios=[e for f in features for e in f.get('elements',[]) if e.get('type')=='scenario']
steps=[s for sc in scenarios for s in sc.get('steps',[])]
assert len(scenarios)==12 and all(s['result']['status']=='passed' for s in steps),(len(scenarios),[s['name'] for s in steps if s['result']['status']!='passed'])
print(f'   BDD: {len(scenarios)} cenários GREEN')
PY
echo 'F5_Q1_BACKEND_GREEN'

printf '%s\n' '=== DOCKER: BANCO NOVO + API REAL ==='
RUN_ID="$(date +%s)-$$"
export POSTGRES_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export APP_ADMIN_EMAIL="f5q1-admin-${RUN_ID}@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export APP_DEMO_RESPONSIBLE_NAME=''
export APP_DEMO_RESPONSIBLE_EMAIL=''
export APP_DEMO_RESPONSIBLE_POSITION=''
export APP_DEMO_RESPONSIBLE_PASSWORD=''
export SESSION_COOKIE_SECURE=false
GATE_PROJECT=facilit-kanban-f5q1
DC=(docker compose -p "$GATE_PROJECT" -f compose.yaml)
cleanup(){ set +e; "${DC[@]}" down -v --remove-orphans >/dev/null 2>&1; }
trap cleanup EXIT
docker compose down --remove-orphans >/dev/null 2>&1 || true
"${DC[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
"${DC[@]}" up --build -d db backend >/tmp/f5q1-docker.log 2>&1 || { tail -100 /tmp/f5q1-docker.log >&2; exit 1; }
for i in $(seq 1 90); do curl -fsS http://localhost:8080/api/v1/health >/tmp/f5q1-health.json 2>/dev/null && break; sleep 2; done
curl -fsS http://localhost:8080/api/v1/health >/dev/null || fail 'backend não ficou saudável'
MIGRATIONS="$(printf '%s\n' 'SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank;' | "${DC[@]}" exec -T db sh -lc 'psql -X -tA -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' | tr '\n' ' ' | sed 's/ *$//')"
test "$MIGRATIONS" = '1 2 3 4 5 6 7' || fail "migrations: $MIGRATIONS"
echo "   migrations V$MIGRATIONS em banco novo"

BASE=http://localhost:8080
COOKIE=/tmp/f5q1.cookies
rm -f "$COOKIE"
cookie_token(){ awk '$6=="XSRF-TOKEN"{v=$7} END{print v}' "$COOKIE" 2>/dev/null || true; }
call(){
  local method="$1" path="$2" body="$3" out="$4" token
  local args=(-sS -o "$out" -w '%{http_code}' -X "$method" -b "$COOKIE" -c "$COOKIE" -H 'Content-Type: application/json')
  token="$(cookie_token)"; [ -z "$token" ] || args+=(-H "X-XSRF-TOKEN: $token")
  [ -z "$body" ] || args+=(--data "$body")
  curl "${args[@]}" "$BASE$path"
}
[ "$(call GET /api/v1/auth/csrf '' /tmp/f5q1-csrf.json)" = 200 ] || fail 'csrf inicial'
[ "$(call POST /api/v1/auth/login "{\"email\":\"$APP_ADMIN_EMAIL\",\"password\":\"$APP_ADMIN_PASSWORD\"}" /tmp/f5q1-login.json)" = 200 ] || fail 'login ADMIN'
[ "$(call GET /api/v1/auth/csrf '' /tmp/f5q1-csrf2.json)" = 200 ] || fail 'csrf após login'
[ "$(call GET /api-docs '' /tmp/f5q1-openapi.json)" = 200 ] || fail '/api-docs'
python3 - <<'PY'
import json
D=json.load(open('/tmp/f5q1-openapi.json',encoding='utf-8'))
P='application/problem+json'
def ex(path,method,status,code): return D['paths'][path][method]['responses'][status]['content'][P]['examples'][code]['value']
def validation_ex(path,method):
    media=D['paths'][path][method]['responses']['400']['content'][P]
    named=media.get('examples',{}).get('VALIDATION_ERROR',{}).get('value')
    if named is not None: return json.loads(named) if isinstance(named,str) else named
    direct=media.get('example')
    if direct is not None: return json.loads(direct) if isinstance(direct,str) else direct
    for item in media.get('examples',{}).values():
        value=item.get('value')
        if isinstance(value,str): value=json.loads(value)
        if isinstance(value,dict) and value.get('code')=='VALIDATION_ERROR': return value
    raise AssertionError((method,path,'VALIDATION_ERROR ausente'))
def schema_of(op):
    s=op['requestBody']['content']['application/json']['schema']
    if '$ref' in s: return D['components']['schemas'][s['$ref'].rsplit('/',1)[-1]]
    return s
for path,item in D['paths'].items():
    if not path.startswith('/api/v1/'): continue
    for method,op in item.items():
        if method not in {'get','post','put','patch','delete'}: continue
        if 'requestBody' in op:
            field=validation_ex(path,method)['violations'][0]['field']
            assert field in schema_of(op).get('properties',{}),(method,path,field)
        if '{id}' in path and '404' in op.get('responses',{}):
            detail=ex(path,method,'404','RESOURCE_NOT_FOUND')['detail']
            if path=='/api/v1/responsibles/{id}/credentials' and method=='delete': prefix='Credencial não encontrada para o responsável:'
            elif path.startswith('/api/v1/projects'): prefix='Projeto não encontrado:'
            elif path.startswith('/api/v1/responsibles'): prefix='Responsável não encontrado:'
            elif path.startswith('/api/v1/secretariats'): prefix='Secretaria não encontrada:'
            else: raise AssertionError((method,path))
            assert detail.startswith(prefix),(method,path,detail,prefix)
        if (path.startswith('/api/v1/responsibles') or path.startswith('/api/v1/secretariats')) and '403' in op.get('responses',{}):
            assert 'projeto' not in ex(path,method,'403','FORBIDDEN')['detail'].lower(),(method,path)
assert ex('/api/v1/indicators/projects/deadlines','get','400','INVALID_REQUEST')['detail']=='withinDays deve estar entre 1 e 90.'
assert ex('/api/v1/projects','get','400','INVALID_REQUEST')['detail']=='Tamanho de página inválido: size deve estar entre 1 e 100.'
print('   /api-docs: exemplos de 400/403/404 coerentes por operação')
PY
SID=10000000-0000-4000-8000-000000000001
[ "$(call GET "/api/v1/secretariats/$SID" '' /tmp/f5q1-secretariat404.json)" = 404 ] || fail '404 real de secretaria'
python3 - <<'PY'
import json
D=json.load(open('/tmp/f5q1-openapi.json',encoding='utf-8')); R=json.load(open('/tmp/f5q1-secretariat404.json',encoding='utf-8'))
e=D['paths']['/api/v1/secretariats/{id}']['get']['responses']['404']['content']['application/problem+json']['examples']['RESOURCE_NOT_FOUND']['value']['detail']
assert R['detail']==e,(R['detail'],e)
print('   404 real de secretaria = exemplo do OpenAPI')
PY
EMAIL="f5q1-${RUN_ID}@example.invalid"
[ "$(call POST /api/v1/responsibles "{\"name\":\"Q1\",\"email\":\"$EMAIL\",\"position\":\"Analista\"}" /tmp/f5q1-responsible.json)" = 201 ] || fail 'criação do responsável'
RID="$(python3 -c "import json; print(json.load(open('/tmp/f5q1-responsible.json'))['id'])")"
[ "$(call POST /api/v1/projects "{\"name\":\"Q1 sem datas\",\"responsibleIds\":[\"$RID\"]}" /tmp/f5q1-project.json)" = 201 ] || fail 'criação do projeto sem datas'
PID="$(python3 -c "import json; print(json.load(open('/tmp/f5q1-project.json'))['id'])")"
[ "$(call PATCH "/api/v1/projects/$PID/status" '{"status":"OVERDUE","confirm":false}' /tmp/f5q1-transition422.json)" = 422 ] || fail 'linha 2 deveria retornar 422'
python3 - <<'PY'
import json
p=json.load(open('/tmp/f5q1-transition422.json',encoding='utf-8')); d=p['detail']
assert p['code']=='TRANSITION_BLOCKED',p
assert 'null' not in d,d
assert 'plannedStart' in d and 'plannedEnd' in d,d
print('   422 linha 2 sem "null" e com orientação de plannedStart/plannedEnd')
PY
echo 'F5_Q1_DOCKER_API_GREEN'
printf '%s\n' '=== F5-Q1 GREEN ==='
echo 'Resultado: exit code 0'
VERIFY
```

GREEN exige `F5_Q1_SCOPE_GREEN`, `F5_Q1_BACKEND_GREEN`, `F5_Q1_DOCKER_API_GREEN`, `=== F5-Q1 GREEN ===` e `Resultado: exit code 0`.

## 3. Depois do GREEN — commits, fechamento e push

Há uma reconciliação obrigatória com o BASE: a migração dos testes existentes de D1 acompanha o `refactor(projetos)`, para que esse commit compile sozinho. Em `ProjectServiceTest.java`, use `git add -p`: inclua no refactor apenas o hunk da migração `list/listByStatus → search` e deixe o hunk `updateCanClearActualStart...` para o commit de teste.

```sh
cd ~/proj/facilit-desafio-kanban
B=backend/src/main/java/br/com/facilit/kanban
T=backend/src/test/java/br/com/facilit/kanban

git add backend/pom.xml backend/Dockerfile frontend/package.json
git commit -m "build(hotfix): define a versão 2.0.2"
(cd backend && mvn -B -ntp -DskipTests package)

git add "$B/delivery/rest/ApiErrorDocumentation.java" "$T/integration/OpenApiContractIT.java"
git commit -m "fix(api): contextualiza exemplos de erro por operação"
(cd backend && mvn -B -ntp -DskipTests package)

git add "$B/domain/project/ProjectStatusTransition.java" "$T/domain/project/ProjectStatusTransitionTest.java"
git commit -m "fix(dominio): corrige orientações de transições bloqueadas"
(cd backend && mvn -B -ntp -DskipTests package)

git add "$B/application/project/ProjectService.java" "$B/application/project/ProjectRepository.java" \
        "$B/infrastructure/persistence/project/ProjectPersistenceAdapter.java" \
        "$B/infrastructure/persistence/project/ProjectJpaRepository.java" \
        "$T/application/support/InMemoryProjectRepository.java" \
        "$T/application/project/ProjectServiceMockitoTest.java" \
        "$T/application/project/ProjectScheduleRefresherTest.java" \
        "$T/integration/ScheduleFreshnessIT.java" "$T/integration/ProjectPersistenceAdapterIT.java"
git add -p "$T/application/project/ProjectServiceTest.java"
git diff --cached -- "$T/application/project/ProjectServiceTest.java"
git commit -m "refactor(projetos): remove listagens sem uso e migra consumidores para search"
(cd backend && mvn -B -ntp -DskipTests package)

git add "$T/application/project/ProjectServiceTest.java"
git commit -m "test(projetos): prova update removendo início realizado sem confirmação"
(cd backend && mvn -B -ntp -DskipTests package)

git add docs/governance/PLANO-CORRECAO-RELEASE-2.0.2.md docs/evidence/F5-Q1
git commit -m "docs(F5-Q1): registra código testes e evidências da 2.0.2"

git status --short
# deve estar vazio

git checkout hotfix/2.0.2
git merge --no-ff bugfix/2.0.2-q1-code -m "Merge branch 'bugfix/2.0.2-q1-code' into hotfix/2.0.2"
git push origin hotfix/2.0.2 bugfix/2.0.2-q1-code
git push gitlab hotfix/2.0.2 bugfix/2.0.2-q1-code || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'

test "$(git rev-parse hotfix/2.0.2)" = "$(git ls-remote origin refs/heads/hotfix/2.0.2 | cut -f1)" && echo F5_Q1_GITFLOW_GREEN
```

Antes do F5-Q2, gere e envie um novo `tar.gz` da `hotfix/2.0.2` já fechada. Pelo protocolo do projeto, o Q2 não inicia sem essa nova evidência.
