# F5-Q2 — VERIFICAÇÃO DO USUÁRIO

O gate prova o F5-Q2 na branch `bugfix/2.0.2-q2-docs`, criada da `hotfix/2.0.2` após o fechamento GREEN do Q1.

Este lote altera **somente documentação/evidências**. Não há mudança em backend, frontend, migrations, CI ou contrato; por isso Maven/Docker não são repetidos aqui. O código do Q1 já passou no gate integral e será novamente coberto pelo freeze F5-Q3.

## 1. Preparar a branch e aplicar o pacote

```sh
cd ~/proj/facilit-desafio-kanban

test -z "$(git status --porcelain=v1)" || { echo 'PARE: árvore com alterações'; exit 1; }
git fetch origin --tags
git checkout hotfix/2.0.2
git pull --ff-only origin hotfix/2.0.2

test "$(git rev-parse HEAD)" = "$(git rev-parse origin/hotfix/2.0.2)" || {
  echo 'PARE: hotfix local difere da origin/hotfix/2.0.2'
  exit 1
}

git checkout -b bugfix/2.0.2-q2-docs

cd ~/Downloads
echo '<SHA256>  facilit-desafio-kanban-F5-Q2-candidate.tar.gz' | sha256sum -c -

cd ~/proj
tar -xzf ~/Downloads/facilit-desafio-kanban-F5-Q2-candidate.tar.gz
cd ~/proj/facilit-desafio-kanban

awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' \
  docs/evidence/F5-Q2/VERIFICACAO-USUARIO.md > /tmp/f5q2-gate.sh
bash -n /tmp/f5q2-gate.sh
```

Troque `<SHA256>` pelo hash informado com o pacote.

## 2. Rodar o gate

```sh
bash -o pipefail -c \
  'bash /tmp/f5q2-gate.sh 2>&1 | tee /tmp/saida-gate-f5q2.txt'
```

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
fail(){ echo "FALHA: $*" >&2; exit 1; }

printf '%s\n' '=== PRÉ-CONDIÇÕES E ESCOPO ==='
BRANCH="$(git branch --show-current)"
test "$BRANCH" = 'bugfix/2.0.2-q2-docs' || fail "branch atual: $BRANCH"
git fetch -q origin hotfix/2.0.2
BASE="$(git rev-parse HEAD)"
ORIGIN="$(git rev-parse origin/hotfix/2.0.2)"
test "$BASE" = "$ORIGIN" || fail "base $BASE difere de origin/hotfix/2.0.2 $ORIGIN"
test "$BASE" = '9ab3bbd81dcf06f6251651cb4dc50aad1a8cf1f5' || fail "base Q1 inesperada: $BASE"
git status --porcelain=v1 -uall >/tmp/f5q2-status.txt
python3 - <<'PY'
import sys
expected = {
    'README.md', 'AI_USAGE.md', 'CHANGELOG.md', 'docs/adr/0002-status-sempre-atual.md',
    'docs/evidence/F5/ADERENCIA-V2.0.1.md',
    'docs/evidence/F5-Q1/EXECUCAO.md', 'docs/evidence/F5-Q1/FECHAMENTO.txt',
    'docs/evidence/F5-Q1/GATE.md', 'docs/evidence/F5-Q1/MATRIZ.md',
    'docs/evidence/F5-Q1/RISCOS.md', 'docs/evidence/F5-Q1/SAIDA-GATE.txt',
}
expected |= {f'docs/evidence/F5-Q2/{name}.md' for name in (
    'CONSUMIDORES','DECISOES','EXECUCAO','FONTES-RAG','GATE','LEITURA','MATRIZ','RISCOS','VERIFICACAO-USUARIO')}
actual=set()
for line in open('/tmp/f5q2-status.txt', encoding='utf-8'):
    line=line.rstrip('\n')
    if line:
        actual.add(line[3:])
missing=sorted(expected-actual); extra=sorted(actual-expected)
if missing or extra:
    for p in missing: print(f'ausente: {p}', file=sys.stderr)
    for p in extra: print(f'fora do escopo: {p}', file=sys.stderr)
    sys.exit(1)
for p in actual:
    if p.startswith(('backend/','frontend/','.github/','observability/')):
        sys.exit(f'Q2 não altera código/CI/observabilidade: {p}')
print(f'   escopo exato: {len(actual)} arquivos documentais')
PY
echo 'F5_Q2_PRECONDITIONS_GREEN'

printf '%s\n' '=== DOCUMENTAÇÃO ==='
git diff --check
python3 - <<'PY'
from pathlib import Path
import hashlib, os, re, sys
problems=[]
status=[line[3:].rstrip('\n') for line in open('/tmp/f5q2-status.txt',encoding='utf-8') if line.strip()]
for name in status:
    p=Path(name); data=p.read_bytes()
    if not data.endswith(b'\n') or data.endswith(b'\n\n'):
        problems.append(f'{name}: fim de arquivo deve ter exatamente uma quebra')
    for i,line in enumerate(data.decode('utf-8').splitlines(),1):
        if line != line.rstrip(' \t'):
            problems.append(f'{name}:{i}: whitespace final')
            break

def read(path): return Path(path).read_text(encoding='utf-8')
readme=read('README.md'); ai=read('AI_USAGE.md'); changelog=read('CHANGELOG.md'); adr=read('docs/adr/0002-status-sempre-atual.md')
old=[
 'ProjectStatusTransitionTest.doesNotLetStaleInProgressBypassTheInProgressToOverdueBlock',
 'ProjectStatusTransitionTest.blocksNotStartedToOverdueBeforePlannedStart',
 '`blocksNotStartedToOverdueOnPlannedStartBecauseDatesStillClassifyAsNotStarted`',
 '`treatsStaleNotStartedAsOverdueOnceThePlannedStartHasPassed`',
 'ProjectServiceTest.createsReadsListsUpdatesAndDeletesProjectWithRecalculatedMetrics',
]
for s in old:
    if s in readme: problems.append(f'README ainda contém referência antiga: {s}')
required=[
 'ProjectStatusTransitionTest.line05DoesNotLetStaleInProgressBypassTheInProgressToOverdueBlock',
 'ProjectStatusTransitionTest.line02BlocksNotStartedToOverdueBeforePlannedStart',
 '`line02BlocksNotStartedToOverdueOnPlannedStartBecauseDatesStillClassifyAsNotStarted`',
 '`line02TreatsStaleNotStartedAsOverdueOnceThePlannedStartHasPassed`',
 'ProjectServiceTest.updateCanClearActualStartWithoutTransitionConfirmationAndRecalculatesStatus',
 'F5-Q1 — Código e testes da patch `2.0.2`',
 'docs/evidence/F5/ADERENCIA-V2.0.1.md',
]
for s in required:
    if s not in readme: problems.append(f'README sem: {s}')
if '`get`, `list`, `listByStatus`, `search` e `indicators`' in adr:
    problems.append('ADR 0002 ainda cita list/listByStatus')
if '`get`, `search` e `indicators`' not in adr:
    problems.append('ADR 0002 sem pontos de recálculo pós-D1')
for stale in ('ProjectStatusTransitionTest.doesNotLetStaleInProgressBypassTheInProgressToOverdueBlock','`appliesOverdueRowWhenStoredInProgressIsStale`','`treatsStaleNotStartedAsOverdueOnceThePlannedStartHasPassed`'):
    if stale in adr: problems.append(f'ADR 0002 ainda com referência de teste antiga: {stale}')
if not re.search(r'^## \[2\.0\.2\] — 2026-09-25$', changelog, re.M):
    problems.append('CHANGELOG sem [2.0.2] como entrada')
first=re.search(r'^## \[([^]]+)\]', changelog, re.M)
if not first or first.group(1)!='2.0.2': problems.append('CHANGELOG: 2.0.2 não é a primeira versão')
for s in ('quatro referências a testes', 'exemplos de 400/403/404', 'Código morto de listagem de projetos'):
    if s not in changelog: problems.append(f'CHANGELOG sem: {s}')
for s in ('PLANO-CORRECAO-RELEASE-2.0.2.md', 'v2.0.1: referências documentais e exemplos de erro inconsistentes', 'F5-Q1 RED 1: helper do teste OpenAPI rígido demais'):
    if s not in ai: problems.append(f'AI_USAGE sem: {s}')
# Auditoria original, byte a byte pelo hash fornecido.
aud=Path('docs/evidence/F5/ADERENCIA-V2.0.1.md').read_bytes()
if hashlib.sha256(aud).hexdigest() != '493ce0cf43b9d0637315c819fa845940af7a43c914f20f1a7a83f441d173af74':
    problems.append('ADERENCIA-V2.0.1.md difere do arquivo original')
# Q1 promovido.
q1gate=read('docs/evidence/F5-Q1/GATE.md'); q1out=read('docs/evidence/F5-Q1/SAIDA-GATE.txt'); close=read('docs/evidence/F5-Q1/FECHAMENTO.txt')
if 'Estado: **GREEN**' not in q1gate: problems.append('F5-Q1/GATE não está GREEN')
for marker in ('F5_Q1_SCOPE_GREEN','F5_Q1_BACKEND_GREEN','F5_Q1_DOCKER_API_GREEN','=== F5-Q1 GREEN ===','Resultado: exit code 0'):
    if marker not in q1out: problems.append(f'F5-Q1/SAIDA-GATE sem {marker}')
if 'HEAD=9ab3bbd81dcf06f6251651cb4dc50aad1a8cf1f5' not in close or 'ORIGIN=9ab3bbd81dcf06f6251651cb4dc50aad1a8cf1f5' not in close:
    problems.append('F5-Q1/FECHAMENTO sem igualdade HEAD/ORIGIN esperada')
# Referências explícitas de testes nas interpretações do README.
checks={
 'ProjectScheduleRefresherTest.java':['usesTheBusinessTimeZoneToDecideWhatTodayIs'],
 'ProjectScheduleCalculatorTest.java':['keepsProjectInProgressOnItsPlannedEndDate','classifiesProjectWithActualEndAsCompleted','classifiesMissedPlannedStartAsOverdue','rejectsStartedProjectWithoutPlannedEndWhenItCannotBeClassified','capsRemainingPercentageAtOneHundredBeforePlannedStart'],
 'ProjectServiceTest.java':['rejectsActualDatesAfterTodayOnCreateAndUpdate','clearsRecordedActualStartOnlyWithExplicitConfirmation','updateCanClearActualStartWithoutTransitionConfirmationAndRecalculatesStatus'],
 'ProjectStatusTransitionTest.java':['line05DoesNotLetStaleInProgressBypassTheInProgressToOverdueBlock','line05BlocksInProgressToOverdueWhenDatesDoNotRecalculateAsOverdue','line02BlocksNotStartedToOverdueBeforePlannedStart','line02BlocksNotStartedToOverdueOnPlannedStartBecauseDatesStillClassifyAsNotStarted','line02TreatsStaleNotStartedAsOverdueOnceThePlannedStartHasPassed','line01RequiresPlannedEndToClassifyTheStartedProject'],
}
for filename, methods in checks.items():
    found=list(Path('backend/src/test').rglob(filename))
    if len(found)!=1:
        problems.append(f'{filename}: esperado 1 arquivo, encontrados {len(found)}'); continue
    src=found[0].read_text(encoding='utf-8')
    for method in methods:
        if not re.search(r'\bvoid\s+'+re.escape(method)+r'\s*\(',src): problems.append(f'{filename}: método ausente {method}')
for cls in ('ProjectDatesTest.java','RestExceptionHandlerTest.java','ScheduleFreshnessIT.java','OpenApiContractIT.java'):
    if len(list(Path('backend/src/test').rglob(cls)))!=1: problems.append(f'classe de teste ausente: {cls}')
# Wildcards line04/11/12 continuam sustentados.
pt=list(Path('backend/src/test').rglob('ProjectStatusTransitionTest.java'))[0].read_text(encoding='utf-8')
for prefix in ('line04','line11','line12'):
    if not re.search(r'\bvoid\s+'+prefix+r'\w*\s*\(',pt): problems.append(f'ProjectStatusTransitionTest sem método {prefix}*')
# Links Markdown relativos nos documentos principais.
for doc in ('README.md','AI_USAGE.md','docs/adr/0002-status-sempre-atual.md'):
    text=read(doc); base=Path(doc).parent
    for target in re.findall(r'\]\(([^)#\s]+)(?:#[^)]*)?\)', text):
        if re.match(r'[a-z]+://',target): continue
        if not (base/target).resolve().exists(): problems.append(f'{doc}: link quebrado {target}')
if problems:
    print('\n'.join(problems),file=sys.stderr); sys.exit(1)
print(f'   {len(status)} arquivos: conteúdo, referências, testes, auditoria, promoção Q1 e links OK')
PY
echo 'F5_Q2_DOCS_GREEN'

printf '%s\n' '=== HISTÓRICO Q1 ==='
git log --no-merges --format='%h %s' 'v2.0.1..HEAD' >/tmp/f5q2-commits.txt
python3 - <<'PY'
import re,sys
pat=re.compile(r'^[0-9a-f]+ (feat|fix|docs|test|build|ci|chore|refactor|style|perf|revert)(\([^)]+\))?!?: \S.*$')
lines=[x.rstrip() for x in open('/tmp/f5q2-commits.txt',encoding='utf-8') if x.strip()]
bad=[x for x in lines if not pat.match(x)]
if bad or len(lines)<5:
    print('\n'.join(bad) or f'apenas {len(lines)} commits sem merge desde v2.0.1',file=sys.stderr); sys.exit(1)
for x in lines: print('   '+x)
print(f'   {len(lines)} commits sem merge desde v2.0.1; Conventional Commits OK')
PY
echo 'F5_Q2_HISTORY_GREEN'

printf '%s\n' '=== CI DA hotfix/2.0.2 COM O Q1 ==='
CI_SHA="$ORIGIN"
REPO="$(git remote get-url origin | sed -E 's#^(git@github\.com:|https://github\.com/)##; s#\.git$##')"
API="https://api.github.com/repos/$REPO"
STATE=''
for i in $(seq 1 30); do
  curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs?head_sha=$CI_SHA&per_page=50" >/tmp/f5q2-runs.json
  STATE="$(python3 - <<'PY'
import json
runs=[r for r in json.load(open('/tmp/f5q2-runs.json'))['workflow_runs'] if r.get('path','').startswith('.github/workflows/ci.yml')]
r=max(runs,key=lambda x:(x.get('run_attempt',0),x['id'])) if runs else None
print(f"{r['status']}:{r['conclusion']} {r['id']}" if r else 'none -')
PY
)"
  echo "   [$i] ${CI_SHA:0:7} $STATE"
  case "$STATE" in
    completed:success*) break ;;
    completed:*) fail 'CI da hotfix terminou sem sucesso' ;;
  esac
  [ "$i" -lt 30 ] || fail 'CI não concluiu em 30 minutos'
  sleep 60
done
RUN_ID="$(echo "$STATE" | awk '{print $2}')"
curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs/$RUN_ID/jobs?per_page=50" >/tmp/f5q2-jobs.json
python3 - <<'PY'
import json,sys
jobs={j['name']:j for j in json.load(open('/tmp/f5q2-jobs.json'))['jobs']}
for name in ('frontend','backend','repository'):
    if name not in jobs or jobs[name]['conclusion']!='success': sys.exit(f'job {name} não está success: {jobs.get(name)}')
steps=jobs['backend'].get('steps',[])
docker=[s for s in steps if s['name'].startswith('Imagem Docker do backend')]
if not docker or docker[0]['conclusion']!='success': sys.exit('passo Imagem Docker do backend não está success')
print(f"   frontend/backend/repository: success · {jobs['backend']['html_url']}")
PY
echo 'F5_Q1_CI_GREEN'

printf '%s\n' '=== F5-Q2 GREEN ==='
echo 'Resultado: exit code 0'
VERIFY
```

GREEN exige:

```text
F5_Q2_PRECONDITIONS_GREEN
F5_Q2_DOCS_GREEN
F5_Q2_HISTORY_GREEN
F5_Q1_CI_GREEN
=== F5-Q2 GREEN ===
Resultado: exit code 0
```

## 3. Depois do GREEN — registrar saída, commits e fechamento

```sh
cd ~/proj/facilit-desafio-kanban

cp /tmp/saida-gate-f5q2.txt docs/evidence/F5-Q2/SAIDA-GATE.txt

git add README.md
git commit -m "docs(readme): corrige referências de testes e registra o F5-Q1"

git add docs/adr/0002-status-sempre-atual.md
git commit -m "docs(adr): remove pontos de recálculo eliminados no F5-Q1"

git add CHANGELOG.md
git commit -m "docs(changelog): registra a versão 2.0.2"

git add AI_USAGE.md
git commit -m "docs(ai-usage): registra a auditoria da v2.0.1 e o F5-Q1"

git add docs/evidence/F5/ADERENCIA-V2.0.1.md docs/evidence/F5-Q1
git commit -m "docs(F5): versiona a auditoria da v2.0.1 e promove o F5-Q1"

git add docs/evidence/F5-Q2
git commit -m "docs(F5-Q2): registra documentação e evidências da 2.0.2"

git status --short
# deve estar vazio

git checkout hotfix/2.0.2
git merge --no-ff bugfix/2.0.2-q2-docs -m "Merge branch 'bugfix/2.0.2-q2-docs' into hotfix/2.0.2"
git push origin hotfix/2.0.2 bugfix/2.0.2-q2-docs

if git remote get-url gitlab >/dev/null 2>&1; then
  git push gitlab hotfix/2.0.2 bugfix/2.0.2-q2-docs || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'
else
  echo 'AVISO: remote gitlab ausente (espelho, não bloqueia)'
fi

git fetch origin hotfix/2.0.2
test "$(git rev-parse hotfix/2.0.2)" = "$(git rev-parse origin/hotfix/2.0.2)" && \
test -z "$(git status --porcelain=v1)" && \
echo 'F5-Q2 FECHAMENTO GREEN'
```

Antes do F5-Q3, gere e envie um novo `tar.gz` da `hotfix/2.0.2` já fechada. O F5-Q3 não inicia sem essa nova baseline.
