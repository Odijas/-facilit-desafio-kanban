# F3-L4 — VERIFICAÇÃO DO PIPELINE NO GITHUB (etapa 4 de 4)

Execute depois de `MIGRACAO-GITHUB.md`. O gate usa apenas leitura pública:

- `git ls-remote`;
- a API REST do GitHub sem token. O limite é de 60 requisições por hora por IP; o gate usa no máximo 45.

O que ele confere:

1. O commit local (`HEAD`) é o mesmo publicado no GitHub.
2. As branches e tags do GitHub são idênticas às do GitLab (histórico preservado).
3. O repositório é público e não contém `.env`.
4. O workflow **CI** desse commit terminou com `success` nos três jobs: `frontend`, `backend` e `repository`. A espera é de até 35 minutos.

Defina `GITHUB_REPO` (formato `usuario/repositorio`) antes de rodar. Rode com a `develop` ativa. O remoto do GitLab deve se chamar `gitlab`, como no passo 5 da migração.

```sh
export GITHUB_REPO=<OWNER>/<REPO>
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F3-L4/VERIFICACAO-CI.md > /tmp/f3l4-ci-gate.sh
bash /tmp/f3l4-ci-gate.sh
```

## Gate

```bash
bash <<'VERIFY'
set -euo pipefail
cd ~/proj/facilit-desafio-kanban
: "${GITHUB_REPO:?defina GITHUB_REPO=usuario/repositorio}"
API="https://api.github.com/repos/$GITHUB_REPO"
rm -f /tmp/f3l4ci-*.json /tmp/f3l4ci-*.txt
trap 'rm -f /tmp/f3l4ci-*.json /tmp/f3l4ci-*.txt' EXIT

gh_get() {
  curl -fsS -H 'Accept: application/vnd.github+json' -H 'X-GitHub-Api-Version: 2022-11-28' "$1"
}

echo '=== COMMIT PUBLICADO ==='
HEAD_SHA="$(git rev-parse HEAD)"
BRANCH="$(git branch --show-current)"
REMOTE_SHA="$(git ls-remote "https://github.com/$GITHUB_REPO.git" "refs/heads/$BRANCH" | cut -f1)"
echo "local  $BRANCH $HEAD_SHA"
echo "github $BRANCH $REMOTE_SHA"
test -n "$REMOTE_SHA"
test "$REMOTE_SHA" = "$HEAD_SHA"
test -z "$(git status --porcelain --untracked-files=no)"
echo 'F3_L4_CI_COMMIT_GREEN'

echo '=== HISTÓRICO PRESERVADO (GITLAB = GITHUB) ==='
git ls-remote --heads --tags gitlab | sort >/tmp/f3l4ci-gitlab.txt
git ls-remote --heads --tags "https://github.com/$GITHUB_REPO.git" | sort >/tmp/f3l4ci-github.txt
diff -u /tmp/f3l4ci-gitlab.txt /tmp/f3l4ci-github.txt
echo "refs: $(wc -l </tmp/f3l4ci-github.txt)"
echo 'F3_L4_CI_HISTORY_GREEN'

echo '=== REPOSITÓRIO PÚBLICO SEM .env ==='
gh_get "$API" >/tmp/f3l4ci-repo.json
python3 - <<'PY'
import json
with open('/tmp/f3l4ci-repo.json', encoding='utf-8') as handle:
    repo = json.load(handle)
assert repo['private'] is False and repo.get('visibility', 'public') == 'public', repo.get('visibility')
print('default_branch', repo['default_branch'])
PY
test "$(curl -sS -o /dev/null -w '%{http_code}' "$API/contents/.env?ref=$HEAD_SHA")" = '404'
echo 'F3_L4_CI_REPOSITORY_GREEN'

echo '=== PIPELINE DO COMMIT ==='
RUN_ID=""
for i in $(seq 1 35); do
  gh_get "$API/actions/runs?head_sha=$HEAD_SHA&per_page=50" >/tmp/f3l4ci-runs.json
  STATE="$(python3 - <<'PY'
import json
with open('/tmp/f3l4ci-runs.json', encoding='utf-8') as handle:
    runs = [r for r in json.load(handle)['workflow_runs'] if r.get('path', '').startswith('.github/workflows/ci.yml')]
if not runs:
    print('none -')
else:
    run = max(runs, key=lambda r: r['run_attempt'] * 10**12 + r['id'])
    print(f"{run['status']}:{run['conclusion']} {run['id']} {run['html_url']}")
PY
)"
  echo "[$i] $STATE"
  case "$STATE" in
    completed:success*) RUN_ID="$(echo "$STATE" | cut -d' ' -f2)"; break ;;
    completed:*) echo 'pipeline terminou sem sucesso' >&2; exit 1 ;;
  esac
  if [ "$i" -eq 35 ]; then
    echo 'pipeline não concluiu em 35 minutos' >&2
    exit 1
  fi
  sleep 60
done
gh_get "$API/actions/runs/$RUN_ID/jobs?per_page=50" >/tmp/f3l4ci-jobs.json
python3 - <<'PY'
import json
with open('/tmp/f3l4ci-jobs.json', encoding='utf-8') as handle:
    jobs = {job['name']: job['conclusion'] for job in json.load(handle)['jobs']}
print(jobs)
assert jobs == {'frontend': 'success', 'backend': 'success', 'repository': 'success'}, jobs
PY
echo 'F3_L4_CI_PIPELINE_GREEN'

printf '%s\n' '=== F3-L4 GREEN ==='
VERIFY

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige os marcadores `F3_L4_CI_*_GREEN`, `=== F3-L4 GREEN ===` e `Resultado: exit code 0`.

Se o pipeline falhar, não faça nova tentativa às cegas. Traga a saída do gate e o link do run, e a correção fica dentro do próprio F3-L4.

Se houver correção depois da migração, siga a rota de `fix/<assunto>` em `MIGRACAO-GITHUB.md` e publique nos dois remotos (`git push gitlab develop && git push origin develop`). Assim a conferência de histórico continua válida.
