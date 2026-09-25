# F5-C3 — VERIFICAÇÃO DO USUÁRIO

O gate prova o lote F5-C3 (documentação de entrega) na branch `bugfix/2.0.0-c3-documentacao`, criada da `release/2.0.0` já com o F5-C2, antes do commit. Ele cobre:

- pré-condições: F5-C2 commitado na base, arquivos alterados iguais aos do pacote, nada em código, frontend ou CI;
- as checagens documentais do freeze do F5-L5 (as mesmas linhas do `docs/evidence/F5/VERIFICACAO-USUARIO.md`) e Conventional Commits do histórico inteiro;
- **a coleção Postman inteira, com o newman, contra a aplicação subida com banco novo**;
- CI da `release/2.0.0` com o F5-C2 (`F5_C2_CI_GREEN`).

**Isolamento:**

- O gate usa um projeto Compose próprio (`facilit-kanban-f5c3`) e o remove no fim (`down -v` só desse projeto).
- O projeto padrão é apenas parado (`docker compose down`, sem `-v`).
- A porta 8080 precisa estar livre.
- O newman roda por `npx` (Node da máquina; baixa o `newman@6.2.2` do npm na primeira vez).

## 0. Estágio Gitflow do F5-C2 (se ainda não foi feito)

Commits semânticos do F5-C2, merge `--no-ff` na `release/2.0.0` e push nos dois remotos. É a seção 3 de `docs/evidence/F5-C2/VERIFICACAO-USUARIO.md`.

```sh
cd ~/proj/facilit-desafio-kanban
git checkout bugfix/2.0.0-c2-testes-validacao
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

O push da `release/2.0.0` dispara o CI. O gate deste lote confere o resultado no fim (`F5_C2_CI_GREEN`).

## 1. Preparar a branch e aplicar o pacote

O bloco para no primeiro erro, mostra uma mensagem `PARE:` quando algo está fora do esperado e imprime `PREPARO_OK` só se tudo der certo. Serve para a primeira vez e para repetir (se já estiver na branch do lote, só reaplica o pacote).

```sh
cd ~/proj/facilit-desafio-kanban && F=bugfix/2.0.0-c3-documentacao && \
if [ "$(git branch --show-current)" != "$F" ]; then \
  { [ -z "$(git status --porcelain=v1)" ] || { echo 'PARE: árvore com alterações (o F5-C2 foi commitado? seção 0)'; false; }; } && \
  git checkout release/2.0.0 && git pull --ff-only origin release/2.0.0 && \
  { if git rev-parse -q --verify "refs/heads/$F" >/dev/null; then git checkout "$F"; else git checkout -b "$F"; fi; }; \
fi && \
{ git cat-file -e HEAD:backend/src/main/java/br/com/facilit/kanban/delivery/common/InputLimits.java 2>/dev/null \
  && git cat-file -e HEAD:docs/evidence/F5-C2/GATE.md 2>/dev/null \
  || { echo 'PARE: a base da branch não tem o F5-C2 commitado (seção 0)'; false; }; } && \
(cd ~/Downloads && echo "<SHA256>  facilit-desafio-kanban-F5-C3-candidate.tar.gz" | sha256sum -c -) && \
tar -xzf ~/Downloads/facilit-desafio-kanban-F5-C3-candidate.tar.gz && \
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5-C3/VERIFICACAO-USUARIO.md > /tmp/f5c3-gate.sh && \
echo PREPARO_OK
```

Troque `<SHA256>` pelo hash informado com o pacote.

## 2. Rodar o gate

```sh
bash /tmp/f5c3-gate.sh 2>&1 | tee /tmp/saida-gate-f5c3.txt
```

Os logs longos ficam em `/tmp/f5c3-*.log`. No fim, o gate espera o CI da `release/2.0.0` terminar (até 35 minutos).

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
git rev-parse --is-inside-work-tree >/dev/null
rm -f /tmp/f5c3-*

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
  *) fail "branch atual: $BRANCH (esperado bugfix/2.0.0-c3-documentacao)" ;;
esac
git cat-file -e HEAD:backend/src/main/java/br/com/facilit/kanban/delivery/common/InputLimits.java 2>/dev/null \
  || fail 'a base da branch não tem o F5-C2 commitado (seção 0)'
git cat-file -e HEAD:docs/evidence/F5-C2/GATE.md 2>/dev/null || fail 'evidências do F5-C2 ausentes no commit base'
command -v npx >/dev/null || fail 'npx (Node) não encontrado: o newman roda por npx'
git status --porcelain=v1 -uall >/tmp/f5c3-status.txt
python3 - <<'PY'
import sys
expected = {
    'AI_USAGE.md',
    'CHANGELOG.md',
    'README.md',
    'docs/api/facilit-kanban.postman_collection.json',
    'docs/evidence/F5-C1/GATE.md',
    'docs/evidence/F5-C2/GATE.md',
    'docs/evidence/F5-C2/MATRIZ.md',
    'docs/evidence/F5-C2/SAIDA-GATE.txt',
    'docs/evidence/F5-C3/CONSUMIDORES.md',
    'docs/evidence/F5-C3/DECISOES.md',
    'docs/evidence/F5-C3/EXECUCAO.md',
    'docs/evidence/F5-C3/FONTES-RAG.md',
    'docs/evidence/F5-C3/GATE.md',
    'docs/evidence/F5-C3/LEITURA.md',
    'docs/evidence/F5-C3/MATRIZ.md',
    'docs/evidence/F5-C3/RISCOS.md',
    'docs/evidence/F5-C3/VERIFICACAO-USUARIO.md',
    'docs/evidence/F5/ADERENCIA-FINAL.md',
    'docs/evidence/F5/AUDITORIA.md',
    'docs/evidence/F5/GATE.md',
    'docs/evidence/F5/RELEASE.md',
    'docs/governance/PLANO-CORRECAO-RELEASE-2.0.0.md',
}
actual = set()
for line in open('/tmp/f5c3-status.txt', encoding='utf-8'):
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
code = [path for path in actual if path.startswith(('backend/', 'frontend/', '.github/'))]
if code:
    sys.exit(f'o lote não deveria alterar código, frontend ou CI: {code}')
print(f'   {len(actual)} arquivos alterados, todos do pacote F5-C3; nada em backend/, frontend/ ou .github/')
PY
echo "   branch $BRANCH sobre a release/2.0.0 com o F5-C2 ($(git rev-parse --short HEAD))"
echo 'F5_C3_PRECONDITIONS_GREEN'

echo '=== ESTÁTICA: CHECAGENS DOCUMENTAIS DO FREEZE + LOTE ==='
git diff --check
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
need('CHANGELOG.md', ['2.0.0', 'Breaking changes'])
need('docs/evidence/F4/REVISAO-SEGURANCA.md', ['A01:2025', 'A02:2025', 'A03:2025', 'A04:2025', 'A05:2025',
                                              'A06:2025', 'A07:2025', 'A08:2025', 'A09:2025', 'A10:2025'])
for path, text in (('README.md', readme), ('docs/adr/0001-camada-ia-agente-rag.md', adr)):
    if '```mermaid' not in text:
        problems.append(f'{path}: diagrama mermaid ausente')
for path in ('README.md', 'AI_USAGE.md', 'docs/adr/0001-camada-ia-agente-rag.md', 'docs/evidence/F5/AUDITORIA.md',
             'docs/evidence/F4/REVISAO-SEGURANCA.md'):
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
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print('   README, AI_USAGE, ADR, auditoria, revisão de segurança, diagramas, links e coleção OK')
PY
git log --no-merges --format='%h %s' 283ce5d..HEAD >/tmp/f5c3-commits.txt
python3 - <<'PY'
import re, sys
pattern = re.compile(r'^[0-9a-f]+ (feat|fix|docs|test|build|ci|chore|refactor|style|perf|revert)(\([^)]+\))?!?: \S.*$')
allowed_non_conventional = {
    '87fa002 Revert "docs(api): documenta os erros de cada operação no OpenAPI"',
}
lines = [line.rstrip('\n') for line in open('/tmp/f5c3-commits.txt', encoding='utf-8') if line.strip()]
bad = [line for line in lines if not pattern.match(line) and line not in allowed_non_conventional]
if bad:
    print('\n'.join(bad), file=sys.stderr)
    sys.exit(1)
print(f'   Conventional Commits: {len(lines)} commits (sem merges); 1 revert auditado legado aceito')
PY
python3 - <<'PY'
import json, os, sys
problems = []
names = [line[3:].rstrip('\n') for line in open('/tmp/f5c3-status.txt', encoding='utf-8') if line.strip()]
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
readme = open('README.md', encoding='utf-8').read()
for text in ('F5-L5 — Release `v2.0.0`', 'o total de itens (`totalElements`) só existe no REST', '0002 status sempre atual',
             'Histórico Git: os lotes de F2-L1 a F3-L4', 'docs/evidence/F5/ADERENCIA-FINAL.md', 'F5-C2 — Rigor de testes e validação',
             'GraphiQL sem envio automático do token CSRF'):
    if text not in readme:
        problems.append(f'README sem "{text}"')
for text in ('o F5-L5 prepara a release', 'espelha o REST'):
    if text in readme:
        problems.append(f'README ainda com "{text}"')
ai_usage = open('AI_USAGE.md', encoding='utf-8').read()
for text in ('PLANO-CORRECAO-RELEASE-2.0.0.md', 'status congelado no tempo', 'imagem Docker do backend quebrada', 'Swagger UI sem CSRF'):
    if text not in ai_usage:
        problems.append(f'AI_USAGE sem "{text}"')
changelog = open('CHANGELOG.md', encoding='utf-8').read()
for text in ('Limites de tamanho nas entradas', 'O Swagger UI envia o token CSRF', 'Coleção Postman com os indicadores'):
    if text not in changelog:
        problems.append(f'CHANGELOG sem "{text}"')
if 'F5-C1 a F5-C3' not in open('docs/evidence/F5/GATE.md', encoding='utf-8').read():
    problems.append('docs/evidence/F5/GATE.md sem os lotes C')
if '## 3a. Correções antes do freeze' not in open('docs/evidence/F5/RELEASE.md', encoding='utf-8').read():
    problems.append('docs/evidence/F5/RELEASE.md sem a seção dos lotes C')
if not os.path.isfile('docs/evidence/F5/ADERENCIA-FINAL.md'):
    problems.append('ADERENCIA-FINAL.md ausente em docs/evidence/F5/')
if '**GREEN**' not in open('docs/evidence/F5-C2/GATE.md', encoding='utf-8').read():
    problems.append('F5-C2 não promovido a GREEN')
collection = json.load(open('docs/api/facilit-kanban.postman_collection.json', encoding='utf-8'))
requests = []
def walk(items):
    for item in items:
        if 'item' in item:
            walk(item['item'])
        else:
            requests.append(item['name'])
walk(collection['item'])
for name in ('Indicadores por secretaria', 'Indicadores por responsável', 'Prazos nos próximos 10 dias', 'Prazos com janela inválida → 400'):
    if name not in requests:
        problems.append(f'coleção sem "{name}"')
if len(requests) != 27:
    problems.append(f'coleção com {len(requests)} requisições (esperado 27)')
if problems:
    print('\n'.join(problems), file=sys.stderr)
    sys.exit(1)
print(f'   {len(names)} arquivos do lote OK; README, AI_USAGE, CHANGELOG, roteiro F5 e coleção ({len(requests)} requisições) OK')
PY
echo 'F5_C3_STATIC_GREEN'

echo '=== DOCKER: BANCO NOVO + MIGRATIONS 1–7 ==='
RUN_ID="$(date +%s)"
export POSTGRES_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export APP_ADMIN_EMAIL="f5c3-admin-${RUN_ID}@example.invalid"
export APP_ADMIN_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(32))')"
export APP_DEMO_RESPONSIBLE_NAME='' APP_DEMO_RESPONSIBLE_EMAIL='' APP_DEMO_RESPONSIBLE_POSITION='' APP_DEMO_RESPONSIBLE_PASSWORD=''
export METRICS_PASSWORD='' SESSION_COOKIE_SECURE=false APP_TIME_ZONE=America/Sao_Paulo
GATE_PROJECT=facilit-kanban-f5c3
DC=(docker compose -p "$GATE_PROJECT" -f compose.yaml)

cleanup() {
  set +e
  "${DC[@]}" down -v --remove-orphans >/dev/null 2>&1
  rm -f /tmp/f5c3-*.json
}
trap cleanup EXIT

docker compose down --remove-orphans >/dev/null 2>&1 || true
"${DC[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
run_logged /tmp/f5c3-compose.log "${DC[@]}" up --build -d db backend
for i in $(seq 1 60); do
  if curl -fsS http://localhost:8080/actuator/health >/tmp/f5c3-health.json 2>/dev/null; then
    break
  fi
  if [ "$i" -eq 60 ]; then "${DC[@]}" logs --no-color backend | tail -80; fail 'backend não subiu'; fi
  sleep 2
done
MIGRATIONS="$(sql 'SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank;' | tr '\n' ' ' | sed 's/ *$//')"
test "$MIGRATIONS" = '1 2 3 4 5 6 7' || fail "migrations aplicadas: '$MIGRATIONS'"
echo "   banco novo ($GATE_PROJECT); migrations V$MIGRATIONS"
echo 'F5_C3_DOCKER_GREEN'

echo '=== COLEÇÃO POSTMAN INTEIRA (NEWMAN) ==='
npx --yes newman@6.2.2 run docs/api/facilit-kanban.postman_collection.json \
  --env-var "baseUrl=http://localhost:8080" \
  --env-var "adminEmail=$APP_ADMIN_EMAIL" \
  --env-var "adminPassword=$APP_ADMIN_PASSWORD" \
  --reporters cli,json --reporter-json-export /tmp/f5c3-newman.json --color off >/tmp/f5c3-newman.log 2>&1 || true
test -s /tmp/f5c3-newman.json || { tail -40 /tmp/f5c3-newman.log >&2; fail 'o newman não gerou relatório'; }
python3 - <<'PY'
import json, sys
run = json.load(open('/tmp/f5c3-newman.json', encoding='utf-8'))['run']
stats = run['stats']
failures = run.get('failures', [])
for failure in failures:
    source = failure.get('source', {}).get('name', '?')
    error = failure.get('error', {})
    print(f"   FALHOU: {source} — {error.get('test') or error.get('name')}: {error.get('message')}", file=sys.stderr)
print(f"   requisições: {stats['requests']['total']} (falhas {stats['requests']['failed']}) · "
      f"asserções: {stats['assertions']['total']} (falhas {stats['assertions']['failed']}) · "
      f"scripts de teste com erro: {stats['testScripts']['failed']} · pré-requisição com erro: {stats['prerequestScripts']['failed']}")
assert not failures, f'{len(failures)} falha(s) no newman'
assert stats['requests']['total'] == 27 and stats['requests']['failed'] == 0, stats['requests']
assert stats['assertions']['failed'] == 0 and stats['assertions']['total'] > 0, stats['assertions']
assert stats['testScripts']['failed'] == 0 and stats['prerequestScripts']['failed'] == 0, stats
PY
echo 'F5_C3_NEWMAN_GREEN'

echo '=== CI DA release/2.0.0 COM O F5-C2 ==='
git fetch -q origin release/2.0.0
CI_SHA="$(git rev-parse origin/release/2.0.0)"
git cat-file -e "$CI_SHA:backend/src/main/java/br/com/facilit/kanban/delivery/common/InputLimits.java" 2>/dev/null \
  || fail 'origin/release/2.0.0 ainda sem o F5-C2 (faça o push da seção 0)'
REPO="$(git remote get-url origin | sed -E 's#^(git@github\.com:|https://github\.com/)##; s#\.git$##')"
API="https://api.github.com/repos/$REPO"
STATE=''
for i in $(seq 1 35); do
  curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs?head_sha=$CI_SHA&per_page=50" >/tmp/f5c3-runs.json
  STATE="$(python3 -c "
import json
runs = [r for r in json.load(open('/tmp/f5c3-runs.json'))['workflow_runs'] if r.get('path', '').startswith('.github/workflows/ci.yml')]
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
curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs/$RUN_ID_CI/jobs?per_page=50" >/tmp/f5c3-jobs.json
python3 - <<'PY'
import json
jobs = {j['name']: j for j in json.load(open('/tmp/f5c3-jobs.json'))['jobs']}
assert {name: job['conclusion'] for name, job in jobs.items()} == {'frontend': 'success', 'backend': 'success', 'repository': 'success'}, sorted(jobs)
step = next(s for s in jobs['backend']['steps'] if s['name'].startswith('Imagem Docker do backend'))
assert step['conclusion'] == 'success', step
print(f"   jobs frontend/backend/repository: success · passo '{step['name']}': success · {jobs['backend']['html_url']}")
PY
echo 'F5_C2_CI_GREEN'

printf '%s\n' '=== F5-C3 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige todos os marcadores `F5_C3_*_GREEN`, `F5_C2_CI_GREEN`, `=== F5-C3 GREEN ===` e `Resultado: exit code 0`. Traga a saída (`/tmp/saida-gate-f5c3.txt`).

## 3. Depois do GREEN: commits semânticos, merge na release e push (Gitflow)

```sh
cd ~/proj/facilit-desafio-kanban

git add docs/api/facilit-kanban.postman_collection.json
git commit -m "test(api): coleção cobre os indicadores por secretaria, por responsável e de prazos"

git add AI_USAGE.md
git commit -m "docs(ai): registra a F5, as auditorias independentes e os erros da IA pegos pelo processo"

git add CHANGELOG.md
git commit -m "docs(changelog): completa a 2.0.0 com as correções da release"

git add README.md docs
git commit -m "docs(F5-C3): promove o F5-C2, versiona a auditoria final e ajusta o roteiro da release"

git status --short        # deve ficar vazio
git checkout release/2.0.0
git merge --no-ff bugfix/2.0.0-c3-documentacao -m "Merge branch 'bugfix/2.0.0-c3-documentacao' into release/2.0.0"
git push origin release/2.0.0 bugfix/2.0.0-c3-documentacao
git push gitlab release/2.0.0 bugfix/2.0.0-c3-documentacao
```

Depois disso, com o CI da `release/2.0.0` verde, a release segue pelo passo 4 de `docs/evidence/F5/RELEASE.md` (gate de freeze).
