# F5-L5 — VERIFICAÇÃO DA RELEASE 2.0.0

Execute depois do passo 6 de `RELEASE.md`, com a `develop` ativa. O gate só lê: `git`, `git ls-remote` e a API pública do GitHub, sem token (até 45 requisições).

O que ele confere:

1. A `main` é o merge da `release/2.0.0` e contém a release inteira.
2. A tag `v2.0.0` é anotada e aponta para a `main`.
3. A `develop` contém a `release/2.0.0` (merge de volta do Gitflow).
4. O GitHub publica `main`, `develop`, `release/2.0.0` e `v2.0.0`.
5. O CI da `main` está verde nos três jobs.
6. A página inicial do repositório mostra o README da release.

```sh
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5/VERIFICACAO-RELEASE.md > /tmp/f5-release-gate.sh
bash /tmp/f5-release-gate.sh
```

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
fail() { echo "FALHA: $*" >&2; exit 1; }
GITHUB_REPO="$(git remote get-url origin | sed -E 's#^(git@github\.com:|https://github\.com/)##; s#\.git$##')"
API="https://api.github.com/repos/$GITHUB_REPO"

echo '=== MAIN, TAG E DEVELOP ==='
git fetch -q origin --tags || fail 'git fetch falhou (tag local diferente da publicada?)'
MAIN_SHA="$(git rev-parse main)"
RELEASE_SHA="$(git rev-parse release/2.0.0)"
test "$(git rev-parse origin/main)" = "$MAIN_SHA" || fail 'main local difere de origin/main'
test "$(git rev-list --parents -n 1 main | wc -w)" -eq 3 || fail 'main não é commit de merge'
git merge-base --is-ancestor "$RELEASE_SHA" main || fail 'main não contém a release/2.0.0'
test "$(git rev-parse 'main^{tree}')" = "$(git rev-parse 'release/2.0.0^{tree}')" || fail 'árvore da main difere da release'
test "$(git cat-file -t v2.0.0)" = 'tag' || fail 'v2.0.0 não é tag anotada'
test "$(git rev-parse 'v2.0.0^{commit}')" = "$MAIN_SHA" || fail 'v2.0.0 não aponta para a main'
git merge-base --is-ancestor "$RELEASE_SHA" develop || fail 'develop não contém a release/2.0.0'
echo "   main $MAIN_SHA = v2.0.0; release/2.0.0 $RELEASE_SHA"
echo 'F5_RELEASE_REFS_GREEN'

echo '=== GITHUB PÚBLICO ==='
git ls-remote --heads --tags origin | sort >/tmp/f5r-github.txt
grep -q "refs/heads/main$" /tmp/f5r-github.txt || fail 'main ausente no GitHub'
grep -q "refs/heads/develop$" /tmp/f5r-github.txt || fail 'develop ausente no GitHub'
grep -q "refs/heads/release/2.0.0$" /tmp/f5r-github.txt || fail 'release/2.0.0 ausente no GitHub'
grep -q "refs/tags/v2.0.0$" /tmp/f5r-github.txt || fail 'tag v2.0.0 ausente no GitHub'
echo "   GitHub publicado: $(wc -l </tmp/f5r-github.txt) refs listadas"
rm -f /tmp/f5r-github.txt
echo 'F5_RELEASE_GITHUB_GREEN'

echo '=== CI DA MAIN ==='
STATE=""
for i in $(seq 1 35); do
  curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs?head_sha=$MAIN_SHA&event=push&per_page=50" >/tmp/f5r-runs.json
  STATE="$(python3 - <<'PY'
import json
runs = [r for r in json.load(open('/tmp/f5r-runs.json', encoding='utf-8'))['workflow_runs']
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
curl -fsS -H 'Accept: application/vnd.github+json' "$API/actions/runs/$RUN/jobs?per_page=50" >/tmp/f5r-jobs.json
python3 -c "import json; jobs = {j['name']: j['conclusion'] for j in json.load(open('/tmp/f5r-jobs.json'))['jobs']}; print('  ', jobs); assert jobs == {'frontend': 'success', 'backend': 'success', 'repository': 'success'}, jobs"
rm -f /tmp/f5r-*.json
echo 'F5_RELEASE_CI_GREEN'

echo '=== PÁGINA DO REPOSITÓRIO ==='
curl -fsS -H 'Accept: application/vnd.github+json' "$API" >/tmp/f5r-repo.json
python3 -c "import json; r = json.load(open('/tmp/f5r-repo.json')); assert r['private'] is False and r['default_branch'] == 'main', (r['private'], r['default_branch']); print('   público, branch padrão main')"
curl -fsS "https://raw.githubusercontent.com/$GITHUB_REPO/v2.0.0/README.md" | grep -q 'F5-L5 — Release `v2.0.0`' || fail 'README da tag sem o estado da release'
rm -f /tmp/f5r-*.json
echo 'F5_RELEASE_PAGE_GREEN'

printf '%s\n' '=== F5 RELEASE GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
exit "$STATUS"
```

GREEN exige `F5_RELEASE_REFS_GREEN`, `F5_RELEASE_GITHUB_GREEN`, `F5_RELEASE_CI_GREEN`, `F5_RELEASE_PAGE_GREEN`, `=== F5 RELEASE GREEN ===` e `Resultado: exit code 0`.
