# F5-P2 — VERIFICAÇÃO DO USUÁRIO

O gate prova o lote F5-P2 (documentação coerente com a tag e promoção do F5-P1) na branch `bugfix/2.0.1-p2-docs`, criada da `hotfix/2.0.1` já com o F5-P1, antes do commit. Ele cobre:

- pré-condições: F5-P1 mesclado na base, arquivos alterados iguais aos do pacote, nenhum código alterado;
- documentação:
  - frases desatualizadas removidas e frases novas presentes;
  - links relativos e caminhos citados existentes;
  - testes citados nas interpretações novas existentes;
  - F5-P1 e F5-L5 promovidos a GREEN;
- histórico: Conventional Commits desde a `v2.0.0`;
- aviso antecipado de sobreposição com a `develop` (para o back-merge do F5-P3);
- CI da `hotfix/2.0.1` com o F5-P1 (`F5_P1_CI_GREEN`).

Este lote não altera código: não há build, Docker nem API no gate. O código do F5-P1 foi provado no gate dele e é provado de novo no CI e no freeze do F5-P3.

## 0. Estágio Gitflow do F5-P1 (se ainda não foi feito)

Antes dos commits, faça a conferência do Swagger no navegador (seção 3 de `docs/evidence/F5-P1/VERIFICACAO-USUARIO.md`, 2 minutos). O resultado é registrado no F5-P3.

Depois, os commits semânticos do F5-P1, o merge `--no-ff` na `hotfix/2.0.1` e o push nos dois remotos. É a seção 4 de `docs/evidence/F5-P1/VERIFICACAO-USUARIO.md`.

```sh
cd ~/proj/facilit-desafio-kanban
git checkout bugfix/2.0.1-p1-swagger
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

O push da `hotfix/2.0.1` dispara o CI. O gate deste lote confere o resultado no fim (`F5_P1_CI_GREEN`).

## 1. Preparar a branch e aplicar o pacote

O bloco para no primeiro erro, mostra uma mensagem `PARE:` quando algo está fora do esperado e imprime `PREPARO_OK` só se tudo der certo. Serve para a primeira vez e para repetir (se já estiver na branch do lote, só reaplica o pacote).

```sh
cd ~/proj/facilit-desafio-kanban && F=bugfix/2.0.1-p2-docs && \
if [ "$(git branch --show-current)" != "$F" ]; then \
  { [ -z "$(git status --porcelain=v1)" ] || { echo 'PARE: árvore com alterações (o F5-P1 foi commitado? seção 0)'; false; }; } && \
  git checkout hotfix/2.0.1 && git pull --ff-only origin hotfix/2.0.1 && \
  { if git rev-parse -q --verify "refs/heads/$F" >/dev/null; then git checkout "$F"; else git checkout -b "$F"; fi; }; \
fi && \
{ git cat-file -p HEAD:backend/pom.xml | grep -q '<version>2.0.1</version>' && git cat-file -e HEAD:docs/evidence/F5-P1/GATE.md \
  || { echo 'PARE: a base da branch não tem o F5-P1 commitado (seção 0)'; false; }; } && \
(cd ~/Downloads && echo "<SHA256>  facilit-desafio-kanban-F5-P2-candidate.tar.gz" | sha256sum -c -) && \
tar -xzf ~/Downloads/facilit-desafio-kanban-F5-P2-candidate.tar.gz && \
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5-P2/VERIFICACAO-USUARIO.md > /tmp/f5p2-gate.sh && \
echo PREPARO_OK
```

Troque `<SHA256>` pelo hash informado com o pacote.

## 2. Rodar o gate

```sh
bash /tmp/f5p2-gate.sh 2>&1 | tee /tmp/saida-gate-f5p2.txt
```

No fim, o gate espera o CI da `hotfix/2.0.1` terminar (até 35 minutos).

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
git rev-parse --is-inside-work-tree >/dev/null
# Limpa a execução anterior, sem apagar o próprio gate (/tmp/f5p2-gate.sh), para ele poder ser repetido.
find /tmp -maxdepth 1 -name 'f5p2-*' ! -name 'f5p2-gate.sh' -exec rm -rf {} +

fail() { echo "FALHA: $*" >&2; exit 1; }

echo '=== PRÉ-CONDIÇÕES ==='
BRANCH="$(git branch --show-current)"
case "$BRANCH" in
  bugfix/2.0.1-*) ;;
  *) fail "branch atual: $BRANCH (esperado bugfix/2.0.1-p2-docs)" ;;
esac
git rev-parse -q --verify 'v2.0.0^{commit}' >/dev/null || fail 'tag v2.0.0 ausente (git fetch origin --tags)'
git merge-base --is-ancestor 'v2.0.0^{commit}' HEAD || fail 'a branch não parte da v2.0.0'
git cat-file -p HEAD:backend/pom.xml | grep -q '<version>2.0.1</version>' || fail 'a base da branch não tem o F5-P1 (seção 0)'
git cat-file -e HEAD:backend/src/main/java/br/com/facilit/kanban/delivery/rest/ApiListExampleDocumentation.java 2>/dev/null \
  || fail 'a base da branch não tem o F5-P1 rev2 (seção 0)'
git status --porcelain=v1 -uall >/tmp/f5p2-status.txt
python3 - <<'PY'
import sys
expected = {
    'README.md', 'AI_USAGE.md', 'CHANGELOG.md',
    'docs/evidence/F5/ADERENCIA-V2.0.0.md', 'docs/evidence/F5/AUDITORIA.md',
    'docs/evidence/F5/GATE.md', 'docs/evidence/F5/MATRIZ.md',
    'docs/evidence/F5-P1/GATE.md', 'docs/evidence/F5-P1/MATRIZ.md', 'docs/evidence/F5-P1/SAIDA-GATE.txt',
}
expected |= {f'docs/evidence/F5-P2/{name}.md' for name in (
    'CONSUMIDORES', 'DECISOES', 'EXECUCAO', 'FONTES-RAG', 'GATE', 'LEITURA', 'MATRIZ', 'RISCOS', 'VERIFICACAO-USUARIO')}
actual = set()
for line in open('/tmp/f5p2-status.txt', encoding='utf-8'):
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
code = [p for p in actual if not (p.endswith('.md') or p.endswith('.txt'))]
if code:
    sys.exit(f'o F5-P2 não altera código: {code}')
print(f'   {len(actual)} arquivos alterados, todos do pacote F5-P2; só documentação')
PY
echo "   branch $BRANCH sobre a hotfix/2.0.1 com o F5-P1 ($(git rev-parse --short HEAD))"
echo 'F5_P2_PRECONDITIONS_GREEN'

echo '=== DOCUMENTAÇÃO ==='
git diff --check
python3 - <<'PY'
import glob, os, re, sys
problems = []
names = [line[3:].rstrip('\n') for line in open('/tmp/f5p2-status.txt', encoding='utf-8') if line.strip()]
for name in names:
    data = open(name, 'rb').read()
    for index, text in enumerate(data.decode('utf-8').split('\n'), 1):
        if text != text.rstrip(' \t'):
            problems.append(f'espaço no fim da linha: {name}:{index}')
            break
    if not data.endswith(b'\n') or data.endswith(b'\n\n'):
        problems.append(f'fim de arquivo sem exatamente uma quebra de linha: {name}')

read = lambda path: open(path, encoding='utf-8').read()
readme, ai_usage, changelog = read('README.md'), read('AI_USAGE.md'), read('CHANGELOG.md')
stale = {
    'README.md': ['a tag só é criada após o freeze GREEN', 'em todas as listagens',
                  'A auditoria independente de aderência', 'OpenAPI com exemplos e schemas em'],
    'AI_USAGE.md': ['**Rejeitada:** o desafio pede GitHub Actions'],
    'CHANGELOG.md': ['observabilidade, CI/CD,'],
}
for path, phrases in stale.items():
    for phrase in phrases:
        if phrase in read(path):
            problems.append(f'{path} ainda com "{phrase}"')
for path in glob.glob('docs/evidence/F5/*.md'):
    for phrase in ('PENDENTE DE EXECUÇÃO', 'PENDENTE L5', 'continua pendente', 'ainda não foram executados'):
        if phrase in read(path):
            problems.append(f'{path} ainda com "{phrase}"')
required = {
    'README.md': ['F4 — Freeze e release `v1.0.0`: GREEN em 2026-09-23', 'F5-L5 — Release `v2.0.0`: GREEN em 2026-09-24',
                  'F5-P1 — Swagger com exemplos em todas as operações', 'Edição das datas (`PUT`) × tabela de transição',
                  'Transição que depende do término previsto', 'docs/evidence/F5/ADERENCIA-V2.0.0.md',
                  'PLANO-CORRECAO-RELEASE-2.0.1.md', 'nas listagens de projetos, responsáveis e secretarias',
                  'são procurados como caracteres comuns'],
    'AI_USAGE.md': ['PLANO-CORRECAO-RELEASE-2.0.1.md', 'F5-P1 rev1: exemplo de lista no Swagger',
                    'cita GitHub Actions no diferencial de CI/CD'],
    'CHANGELOG.md': ['observabilidade, CI, documentação'],
    'docs/evidence/F5/ADERENCIA-V2.0.0.md': ['97,5%', '941276281d0aa62e16b8b5099273f4798249789360d5b86e465a9f7386de2177'],
}
for path, phrases in required.items():
    for phrase in phrases:
        if phrase not in read(path):
            problems.append(f'{path} sem "{phrase}"')
first = re.search(r'^## \[([^\]]+)\] — (\S+)', changelog, re.M)
if not first or first.groups() != ('2.0.1', '2026-09-25'):
    problems.append(f'CHANGELOG: primeira entrada {first and first.groups()} (esperado 2.0.1 de 2026-09-25)')
for heading in ('Visão geral', 'Como rodar', 'Como testar', 'Swagger', 'Limitações e próximos passos'):
    if not re.search(r'^#{1,3} .*' + re.escape(heading), readme, re.M | re.I):
        problems.append(f'README sem a seção exigida pelo freeze: {heading}')

# Testes citados nas interpretações novas.
tests = {
    'backend/src/test/java/br/com/facilit/kanban/application/project/ProjectServiceTest.java':
        'void createsReadsListsUpdatesAndDeletesProjectWithRecalculatedMetrics(',
    'backend/src/test/java/br/com/facilit/kanban/domain/project/ProjectStatusTransitionTest.java':
        'void line01RequiresPlannedEndToClassifyTheStartedProject(',
}
for path, signature in tests.items():
    if signature not in read(path):
        problems.append(f'teste citado no README não existe: {signature} em {path}')

# Links relativos e caminhos citados entre crases nos documentos do lote.
docs = ['README.md', 'AI_USAGE.md', 'CHANGELOG.md'] + [n for n in names if n.endswith('.md') and n.startswith('docs/')]
checked = 0
for path in docs:
    text = read(path)
    base = os.path.dirname(path)
    for target in re.findall(r'\]\(([^)\s]+)\)', text):
        if re.match(r'^(https?:|mailto:|#)', target):
            continue
        checked += 1
        if not os.path.exists(os.path.normpath(os.path.join(base, target.split('#', 1)[0]))):
            problems.append(f'link quebrado em {path}: {target}')
    # Os relatórios de aderência citam arquivos propostos como correção; só os links deles são conferidos.
    if os.path.basename(path).startswith('ADERENCIA-'):
        continue
    for target in re.findall(r'`((?:docs|backend|frontend)/[^`*<>\s]+\.(?:md|java|txt|json))`', text):
        checked += 1
        if not os.path.exists(target):
            problems.append(f'caminho citado não existe em {path}: {target}')

gate = read('docs/evidence/F5-P1/GATE.md')
if 'Estado: **GREEN**' not in gate:
    problems.append('F5-P1 não promovido a GREEN')
if 'CANDIDATE' in read('docs/evidence/F5-P1/MATRIZ.md'):
    problems.append('matriz do F5-P1 ainda com CANDIDATE')
saida = read('docs/evidence/F5-P1/SAIDA-GATE.txt')
for marker in ('F5_P1_PRECONDITIONS_GREEN', 'F5_P1_BACKEND_GREEN', 'F5_P1_STATIC_GREEN', 'F5_P1_DOCKER_GREEN',
               'F5_P1_API_GREEN', '=== F5-P1 GREEN ===', 'Resultado: exit code 0'):
    if marker not in saida:
        problems.append(f'SAIDA-GATE do F5-P1 sem {marker}')
if 'Estado: **GREEN**' not in read('docs/evidence/F5/GATE.md'):
    problems.append('F5-L5 não registrado como GREEN')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print(f'   {len(names)} arquivos do lote OK; frases antigas removidas; {checked} links e caminhos resolvidos; '
      'testes citados existem; F5-P1 e F5-L5 GREEN')
PY
echo 'F5_P2_DOCS_GREEN'

echo '=== HISTÓRICO: CONVENTIONAL COMMITS DESDE A v2.0.0 ==='
git log --no-merges --format='%h %s' 'v2.0.0..HEAD' >/tmp/f5p2-commits.txt
python3 - <<'PY'
import re, sys
pattern = re.compile(r'^[0-9a-f]+ (feat|fix|docs|test|build|ci|chore|refactor|style|perf|revert)(\([^)]+\))?!?: \S.*$')
lines = [l.rstrip('\n') for l in open('/tmp/f5p2-commits.txt', encoding='utf-8') if l.strip()]
bad = [l for l in lines if not pattern.match(l)]
if bad or len(lines) < 4:
    print('\n'.join(bad) or f'só {len(lines)} commits desde a v2.0.0 (esperado ≥ 4, os do F5-P1)', file=sys.stderr)
    sys.exit(1)
for l in lines:
    print('   ' + l)
print(f'   {len(lines)} commits sem merge desde a v2.0.0, todos em Conventional Commits')
PY
echo 'F5_P2_HISTORY_GREEN'

echo '=== AVISO ANTECIPADO: SOBREPOSIÇÃO COM A develop (back-merge do F5-P3) ==='
git fetch -q origin develop
python3 - <<'PY'
import subprocess
run = lambda *a: set(subprocess.run(a, capture_output=True, text=True, check=True).stdout.split())
develop = run('git', 'diff', '--name-only', 'v2.0.0', 'origin/develop')
hotfix = run('git', 'diff', '--name-only', 'v2.0.0', 'HEAD')
hotfix |= {l[3:].strip() for l in open('/tmp/f5p2-status.txt', encoding='utf-8') if l.strip()}
common = sorted(develop & hotfix)
print(f"   develop desde a v2.0.0: {', '.join(sorted(develop)) or 'nada'}")
print(f"   {'AVISO: arquivos alterados nos dois lados: ' + ', '.join(common) if common else 'sem sobreposição com a hotfix'}")
PY

echo '=== CI DA hotfix/2.0.1 COM O F5-P1 ==='
git fetch -q origin hotfix/2.0.1
CI_SHA="$(git rev-parse origin/hotfix/2.0.1)"
git cat-file -p "$CI_SHA:backend/pom.xml" | grep -q '<version>2.0.1</version>' || fail 'origin/hotfix/2.0.1 ainda sem o F5-P1 (faça o push da seção 0)'
test "$CI_SHA" = "$(git rev-parse HEAD)" || fail "a base da branch ($(git rev-parse --short HEAD)) difere de origin/hotfix/2.0.1 (${CI_SHA:0:7})"
REPO="$(git remote get-url origin | sed -E 's#^(git@github\.com:|https://github\.com/)##; s#\.git$##')"
API="https://api.github.com/repos/$REPO"
STATE=''
for i in $(seq 1 35); do
  curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs?head_sha=$CI_SHA&per_page=50" >/tmp/f5p2-runs.json
  STATE="$(python3 -c "
import json
runs = [r for r in json.load(open('/tmp/f5p2-runs.json'))['workflow_runs'] if r.get('path', '').startswith('.github/workflows/ci.yml')]
run = max(runs, key=lambda r: r['run_attempt'] * 10**12 + r['id']) if runs else None
print(f\"{run['status']}:{run['conclusion']} {run['id']}\" if run else 'none -')
")"
  echo "   [$i] ${CI_SHA:0:7} $STATE"
  case "$STATE" in
    completed:success*) break ;;
    completed:*) fail 'CI da hotfix terminou sem sucesso' ;;
  esac
  [ "$i" -lt 35 ] || fail 'CI não concluiu em 35 minutos'
  sleep 60
done
RUN_ID_CI="$(echo "$STATE" | cut -d' ' -f2)"
curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs/$RUN_ID_CI/jobs?per_page=50" >/tmp/f5p2-jobs.json
python3 - <<'PY'
import json
jobs = {j['name']: j for j in json.load(open('/tmp/f5p2-jobs.json'))['jobs']}
assert {name: job['conclusion'] for name, job in jobs.items()} == {'frontend': 'success', 'backend': 'success', 'repository': 'success'}, sorted(jobs)
step = next(s for s in jobs['backend']['steps'] if s['name'].startswith('Imagem Docker do backend'))
assert step['conclusion'] == 'success', step
print(f"   jobs frontend/backend/repository: success · passo '{step['name']}': success · {jobs['backend']['html_url']}")
PY
echo 'F5_P1_CI_GREEN'

printf '%s\n' '=== F5-P2 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige `F5_P2_PRECONDITIONS_GREEN`, `F5_P2_DOCS_GREEN`, `F5_P2_HISTORY_GREEN`, `F5_P1_CI_GREEN`, `=== F5-P2 GREEN ===` e `Resultado: exit code 0`. Traga a saída (`/tmp/saida-gate-f5p2.txt`) e o resultado da conferência do Swagger (seção 0).

## 3. Depois do GREEN: commits semânticos, merge na hotfix e push (Gitflow)

```sh
cd ~/proj/facilit-desafio-kanban

git add README.md
git commit -m "docs(readme): corrige o estado das releases, a paginação e registra interpretações e auditorias"

git add AI_USAGE.md
git commit -m "docs(ai-usage): registra a patch release 2.0.1 e corrige a referência ao GitHub Actions"

git add CHANGELOG.md
git commit -m "docs(changelog): registra a versão 2.0.1 e corrige a entrada da 1.0.0"

git add docs/evidence/F5
git commit -m "docs(F5): registra o freeze e a release 2.0.0 como GREEN e versiona a auditoria da v2.0.0"

git add docs/evidence/F5-P1 docs/evidence/F5-P2
git commit -m "docs(F5-P2): promove o F5-P1 e registra as evidências do F5-P2"

git status --short        # deve ficar vazio
git checkout hotfix/2.0.1
git merge --no-ff bugfix/2.0.1-p2-docs -m "Merge branch 'bugfix/2.0.1-p2-docs' into hotfix/2.0.1"
git push origin hotfix/2.0.1 bugfix/2.0.1-p2-docs
git push gitlab hotfix/2.0.1 bugfix/2.0.1-p2-docs || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'
```

O CI desta `hotfix/2.0.1` é conferido no freeze do F5-P3.
