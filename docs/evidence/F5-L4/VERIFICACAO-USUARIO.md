# F5-L4 — VERIFICAÇÃO DO USUÁRIO — FINAL

Data: 2026-09-24
Base esperada: `f786e7c`
Branch: `feature/f5-l4-indicadores-bdd-cobertura`
Cobertura mínima: `95%` de linhas

## Gate final

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
rm -f /tmp/f5l4-final-*.log

fail() { echo "FALHA: $*" >&2; exit 1; }
run_logged() {
  local log="$1"; shift
  if ! "$@" >"$log" 2>&1; then
    tail -120 "$log" >&2
    return 1
  fi
}

echo '=== PRÉ-CONDIÇÕES ==='
[ "$(git branch --show-current)" = 'feature/f5-l4-indicadores-bdd-cobertura' ] || fail 'branch incorreta'
[ "$(git rev-parse --short=7 HEAD)" = 'f786e7c' ] || fail 'HEAD deve permanecer na base f786e7c antes dos commits'

git status --porcelain=v1 -uall > /tmp/f5l4-final-status.txt
python3 - <<'PY'
import sys
expected = {
    'README.md',
    'backend/pom.xml',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectDeadlineIndicator.java',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectDeadlineIndicators.java',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectGroupIndicator.java',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectRepository.java',
    'backend/src/main/java/br/com/facilit/kanban/application/project/ProjectService.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlController.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectDeadlinesResponse.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectGroupIndicatorResponse.java',
    'backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsRestController.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectJpaRepository.java',
    'backend/src/main/java/br/com/facilit/kanban/infrastructure/persistence/project/ProjectPersistenceAdapter.java',
    'backend/src/main/resources/graphql/kanban.graphqls',
    'backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceMockitoTest.java',
    'backend/src/test/java/br/com/facilit/kanban/application/support/InMemoryProjectRepository.java',
    'backend/src/test/java/br/com/facilit/kanban/bdd/StatusTransitionSteps.java',
    'backend/src/test/java/br/com/facilit/kanban/bdd/StatusTransitionsBddTest.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/graphql/ProjectGraphQlControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/graphql/ResponsibleGraphQlControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/graphql/SecretariatGraphQlControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/delivery/rest/ProjectIndicatorsRestControllerTest.java',
    'backend/src/test/java/br/com/facilit/kanban/integration/ProjectIndicatorsIT.java',
    'backend/src/test/java/br/com/facilit/kanban/integration/ResponsiblePersistenceAdapterIT.java',
    'backend/src/test/java/br/com/facilit/kanban/integration/SecretariatPersistenceAdapterIT.java',
    'backend/src/test/resources/features/transicoes.feature',
    'docs/evidence/F5-L4/CONSUMIDORES.md',
    'docs/evidence/F5-L4/DECISOES.md',
    'docs/evidence/F5-L4/EXECUCAO.md',
    'docs/evidence/F5-L4/FONTES-RAG.md',
    'docs/evidence/F5-L4/GATE.md',
    'docs/evidence/F5-L4/LEITURA.md',
    'docs/evidence/F5-L4/MATRIZ.md',
    'docs/evidence/F5-L4/RISCOS.md',
    'docs/evidence/F5-L4/VERIFICACAO-USUARIO.md',
}
actual = {line.rstrip('\n')[3:] for line in open('/tmp/f5l4-final-status.txt', encoding='utf-8') if line.strip()}
missing = sorted(expected - actual)
extra = sorted(actual - expected)
if missing or extra:
    for path in missing:
        print('ausente:', path, file=sys.stderr)
    for path in extra:
        print('fora do lote:', path, file=sys.stderr)
    raise SystemExit(1)
if any(path.startswith('frontend/') for path in actual):
    raise SystemExit('frontend alterado no F5-L4')
print(f'   {len(actual)} arquivos do lote; frontend intocado')
PY
git diff --check
echo 'F5_L4_PRECONDITIONS_GREEN'

echo '=== BACKEND: CLEAN VERIFY + BDD + JACOCO CHECK ==='
(cd backend && run_logged /tmp/f5l4-final-backend.log mvn -B -ntp clean verify)
grep -q 'BUILD SUCCESS' /tmp/f5l4-final-backend.log || fail 'Maven sem BUILD SUCCESS'
if grep -q '\[deprecation\]' /tmp/f5l4-final-backend.log; then
  grep '\[deprecation\]' /tmp/f5l4-final-backend.log >&2
  fail 'API depreciada no código do backend'
fi
if grep -Eqi 'self-attaching|loaded dynamically' /tmp/f5l4-final-backend.log; then
  grep -Ei 'self-attaching|loaded dynamically' /tmp/f5l4-final-backend.log | sort -u >&2
  fail 'Mockito foi anexado dinamicamente'
fi
python3 - <<'PY'
import glob
import xml.etree.ElementTree as ET
for kind, pattern in (
    ('unitários/BDD', 'backend/target/surefire-reports/TEST-*.xml'),
    ('integração', 'backend/target/failsafe-reports/TEST-*.xml'),
):
    files = glob.glob(pattern)
    if not files:
        raise SystemExit(f'sem relatórios {kind}')
    totals = {'tests': 0, 'failures': 0, 'errors': 0, 'skipped': 0}
    for name in files:
        root = ET.parse(name).getroot()
        for key in totals:
            totals[key] += int(root.get(key, 0))
    if any(totals[key] for key in ('failures', 'errors', 'skipped')):
        raise SystemExit((kind, totals))
    print(f"   {kind}: {len(files)} relatórios, {totals['tests']} testes, zero falhas/erros/ignorados")
report = 'backend/target/failsafe-reports/TEST-br.com.facilit.kanban.integration.ProjectIndicatorsIT.xml'
root = ET.parse(report).getroot()
if int(root.get('tests', 0)) < 1 or any(int(root.get(key, 0)) for key in ('failures', 'errors', 'skipped')):
    raise SystemExit('ProjectIndicatorsIT não passou')
print('   ProjectIndicatorsIT: GREEN')
PY
echo 'F5_L4_BACKEND_GREEN'

echo '=== BDD: 12 LINHAS ==='
python3 - <<'PY'
import json
from pathlib import Path
path = Path('backend/target/cucumber.json')
if not path.is_file():
    raise SystemExit('target/cucumber.json ausente')
data = json.loads(path.read_text(encoding='utf-8'))
scenarios = [e for feature in data for e in feature.get('elements', []) if e.get('type') == 'scenario']
statuses = [s.get('result', {}).get('status') for scenario in scenarios for s in scenario.get('steps', [])]
if len(scenarios) != 12 or not statuses or any(status != 'passed' for status in statuses):
    raise SystemExit((len(scenarios), statuses))
print(f'   {len(scenarios)} cenários / {len(statuses)} passos passed')
PY
echo 'F5_L4_BDD_GREEN'

echo '=== JACOCO: LIMITE 95% ==='
python3 - <<'PY'
import xml.etree.ElementTree as ET
from pathlib import Path
path = Path('backend/target/site/jacoco/jacoco.xml')
if not path.is_file():
    raise SystemExit('jacoco.xml ausente')
root = ET.parse(path).getroot()
counter = next(x for x in root.findall('counter') if x.get('type') == 'LINE')
missed = int(counter.get('missed'))
covered = int(counter.get('covered'))
total = missed + covered
ratio = covered / total
print(f'   covered={covered} missed={missed} total={total}')
print(f'JACOCO_LINE_COVERAGE={ratio * 100:.2f}%')
print(f'JACOCO_LINE_COVERAGE_RATIO={ratio:.4f}')
if ratio < 0.95:
    raise SystemExit(f'cobertura abaixo de 95%: {ratio:.4f}')
PY
grep -Fq -- '--- jacoco:0.8.15:check (jacoco-check) @ kanban ---' /tmp/f5l4-final-backend.log || fail 'jacoco:check não executou'
echo 'F5_L4_JACOCO_GREEN'

echo '=== ESTÁTICA ==='
python3 - <<'PY'
from pathlib import Path
feature = Path('backend/src/test/resources/features/transicoes.feature').read_text(encoding='utf-8').splitlines()
rows = [line for line in feature if line.lstrip().startswith('|') and line.split('|')[1].strip().isdigit()]
if len(rows) != 12:
    raise SystemExit(f'feature não contém 12 linhas: {len(rows)}')
pom = Path('backend/pom.xml').read_text(encoding='utf-8')
for token in ('cucumber-junit-platform-engine', 'jacoco-maven-plugin', '<goal>report</goal>', '<goal>check</goal>', '<jacoco.minimum.line.coverage>0.95</jacoco.minimum.line.coverage>'):
    if token not in pom:
        raise SystemExit(f'ausente no pom: {token}')
if list(Path('backend/src/main/resources/db/migration').glob('V8__*')):
    raise SystemExit('migration V8 inesperada no F5-L4')
print('   BDD=12; JaCoCo check=95%; sem migration nova')
PY
git diff --check
echo 'F5_L4_STATIC_GREEN'

echo '=== F5-L4 GREEN ==='
echo 'Resultado: exit code 0'
VERIFY
```
