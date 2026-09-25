# F5-Q3 — VERIFICAÇÃO DA RELEASE 2.0.2

Execute depois do passo 5 de `RELEASE.md`, com a `develop` ativa. O gate só lê: `git`, `git ls-remote` e a API pública do GitHub, sem token (até 45 requisições). Já traz a correção do falso negativo da 2.0.1: o conteúdo baixado vai para arquivo antes do `grep`, porque `curl | grep -q` com `pipefail` falha quando o `grep` fecha o pipe (curl 23).

O que ele confere:

1. A `main` é o merge da `hotfix/2.0.2`, com a mesma árvore.
2. A tag `v2.0.2` é anotada e aponta para a `main`.
3. A `develop` contém a `hotfix/2.0.2` (back-merge do Gitflow).
4. O GitHub publica `main`, `develop`, `hotfix/2.0.2` e `v2.0.2`, iguais aos locais.
5. O CI da `main` está verde nos três jobs.
6. O repositório é público, e a tag traz o README, o CHANGELOG e o gate do F5-Q3 no estado da release.

```sh
cd ~/proj/facilit-desafio-kanban
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5-Q3/VERIFICACAO-RELEASE.md > /tmp/f5q3-release-gate.sh
bash -n /tmp/f5q3-release-gate.sh
bash -o pipefail -c 'bash /tmp/f5q3-release-gate.sh 2>&1 | tee /tmp/saida-release-f5q3.txt'
```

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
fail() { echo "FALHA: $*" >&2; exit 1; }
GITHUB_REPO="$(git remote get-url origin | sed -E 's#^(git@github\.com:|https://github\.com/)##; s#\.git$##')"
API="https://api.github.com/repos/$GITHUB_REPO"
TMP="$(mktemp -d /tmp/f5q3r.XXXXXX)"
trap 'rm -rf "$TMP"' EXIT

echo '=== MAIN, TAG E DEVELOP ==='
git fetch -q origin --tags || fail 'git fetch falhou (tag local diferente da publicada?)'
MAIN_SHA="$(git rev-parse main)"
HOTFIX_SHA="$(git rev-parse hotfix/2.0.2)"
DEVELOP_SHA="$(git rev-parse develop)"
test "$(git rev-parse origin/main)" = "$MAIN_SHA" || fail 'main local difere de origin/main'
test "$(git rev-parse origin/develop)" = "$DEVELOP_SHA" || fail 'develop local difere de origin/develop'
test "$(git rev-parse origin/hotfix/2.0.2)" = "$HOTFIX_SHA" || fail 'hotfix/2.0.2 local difere de origin/hotfix/2.0.2'
test "$(git rev-list --parents -n 1 main | wc -w)" -eq 3 || fail 'main não é commit de merge'
git merge-base --is-ancestor "$HOTFIX_SHA" main || fail 'main não contém a hotfix/2.0.2'
git merge-base --is-ancestor 'v2.0.1^{commit}' main || fail 'main não contém a v2.0.1'
test "$(git rev-parse 'main^{tree}')" = "$(git rev-parse 'hotfix/2.0.2^{tree}')" || fail 'árvore da main difere da hotfix/2.0.2'
test "$(git cat-file -t v2.0.2)" = 'tag' || fail 'v2.0.2 não é tag anotada'
test "$(git rev-parse 'v2.0.2^{commit}')" = "$MAIN_SHA" || fail 'v2.0.2 não aponta para a main'
git merge-base --is-ancestor "$HOTFIX_SHA" develop || fail 'develop não contém a hotfix/2.0.2'
echo "   main $MAIN_SHA = v2.0.2; hotfix/2.0.2 $HOTFIX_SHA; develop $DEVELOP_SHA contém a hotfix"
echo 'F5_Q3_RELEASE_REFS_GREEN'

echo '=== GITHUB PÚBLICO ==='
git ls-remote --heads --tags origin >"$TMP/github.txt"
for ref in refs/heads/main refs/heads/develop refs/heads/hotfix/2.0.2 refs/tags/v2.0.2; do
  grep -q "[[:space:]]$ref\$" "$TMP/github.txt" || fail "$ref ausente no GitHub"
done
grep -q "^$MAIN_SHA[[:space:]]refs/tags/v2.0.2^{}\$" "$TMP/github.txt" || fail 'a tag v2.0.2 publicada não aponta para a main'
echo "   GitHub publicado: main, develop, hotfix/2.0.2 e v2.0.2 ($(wc -l <"$TMP/github.txt") refs listadas)"
echo 'F5_Q3_RELEASE_GITHUB_GREEN'

echo '=== CI DA MAIN ==='
STATE=""
for i in $(seq 1 35); do
  curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs?head_sha=$MAIN_SHA&event=push&per_page=50" >"$TMP/runs.json"
  STATE="$(python3 - "$TMP/runs.json" <<'PY'
import json, sys
runs = [r for r in json.load(open(sys.argv[1], encoding='utf-8'))['workflow_runs']
        if r.get('path', '').startswith('.github/workflows/ci.yml') and r.get('head_branch') == 'main']
if not runs:
    print('none -')
else:
    run = max(runs, key=lambda r: r['run_attempt'] * 10**12 + r['id'])
    print(f"{run['status']}:{run['conclusion']} {run['id']} {run['html_url']}")
PY
)"
  echo "   [$i] $STATE"
  case "$STATE" in
    completed:success*) break ;;
    completed:*) fail 'CI da main terminou sem sucesso' ;;
  esac
  if [ "$i" -eq 35 ]; then fail 'CI da main não concluiu em 35 minutos'; fi
  sleep 60
done
RUN="$(echo "$STATE" | cut -d' ' -f2)"
curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs/$RUN/jobs?per_page=50" >"$TMP/jobs.json"
python3 - "$TMP/jobs.json" <<'PY'
import json, sys
jobs = {j['name']: j['conclusion'] for j in json.load(open(sys.argv[1], encoding='utf-8'))['jobs']}
print('  ', jobs)
assert jobs == {'frontend': 'success', 'backend': 'success', 'repository': 'success'}, jobs
PY
echo 'F5_Q3_RELEASE_CI_GREEN'

echo '=== PÁGINA DO REPOSITÓRIO E CONTEÚDO DA TAG ==='
curl -fsS -H 'Accept: application/vnd.github+json' "$API" >"$TMP/repo.json"
python3 - "$TMP/repo.json" <<'PY'
import json, sys
r = json.load(open(sys.argv[1], encoding='utf-8'))
assert r['private'] is False and r['default_branch'] == 'main', (r['private'], r['default_branch'])
print('   público, branch padrão main')
PY
RAW="https://raw.githubusercontent.com/$GITHUB_REPO/v2.0.2"
curl -fsS "$RAW/README.md" -o "$TMP/README.md" || fail 'README da tag não pôde ser baixado'
curl -fsS "$RAW/CHANGELOG.md" -o "$TMP/CHANGELOG.md" || fail 'CHANGELOG da tag não pôde ser baixado'
curl -fsS "$RAW/docs/evidence/F5-Q3/GATE.md" -o "$TMP/GATE.md" || fail 'GATE.md do F5-Q3 na tag não pôde ser baixado'
grep -Fq 'F5-Q3 — Freeze da `hotfix/2.0.2` e release `v2.0.2`: freeze GREEN em 2026-09-25' "$TMP/README.md" || fail 'README da tag sem o estado da release'
grep -Fq 'F5-Q2 — Documentação e promoção da patch `2.0.2`' "$TMP/README.md" || fail 'README da tag sem o F5-Q2'
# Sem pipe entre dois grep: com pipefail, o grep -q que termina cedo derruba o primeiro (mesma classe do falso negativo da 2.0.1).
FIRST_ENTRY="$(grep -m1 '^## \[' "$TMP/CHANGELOG.md" || true)"
test "$FIRST_ENTRY" = '## [2.0.2] — 2026-09-25' || fail "CHANGELOG da tag com '$FIRST_ENTRY' no topo (esperado 2.0.2)"
grep -Fq 'Estado: **GREEN** (freeze' "$TMP/GATE.md" || fail 'GATE.md do F5-Q3 na tag sem o freeze GREEN'
echo '   README, CHANGELOG e GATE do F5-Q3 na tag com o estado da release'
echo 'F5_Q3_RELEASE_PAGE_GREEN'

printf '%s\n' '=== F5-Q3 RELEASE GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
exit "$STATUS"
```

GREEN exige `F5_Q3_RELEASE_REFS_GREEN`, `F5_Q3_RELEASE_GITHUB_GREEN`, `F5_Q3_RELEASE_CI_GREEN`, `F5_Q3_RELEASE_PAGE_GREEN`, `=== F5-Q3 RELEASE GREEN ===` e `Resultado: exit code 0`.
