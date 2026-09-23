# F3-L4 — RECONSTRUÇÃO DO HISTÓRICO GIT POR LOTE (etapa 2 de 4)

A `main` tem só 4 commits, até o F2-L1 candidate (`283ce5d`). Do F2-L1 GREEN ao F3-L4, tudo está na área de trabalho sem commit. Este roteiro cria um commit por área de cada lote, em Gitflow (`develop` + `feature/<lote>` + `merge --no-ff`), usando como fotografia os pacotes que passaram no gate.

| Branch | Fonte | Lote |
|---|---|---|
| `feature/f2-l1-documentacao` | `F2-L1-candidate`, só os arquivos novos em `docs/` | governança e evidências de F0 a F2-L1, nunca commitadas no `283ce5d` |
| `feature/f2-l2-fundacao-frontend-autenticada` | `F2-L2-corrigido` | F2-L2 (+ promoção do F2-L1) |
| `feature/f2-l3-kanban-ui` | `F3-L1-local-20260922-143305` (estado real após o gate do F2-L3) | F2-L3 |
| `feature/f3-l1-diferenciais` | `F3-L1-candidate-rev4` | F3-L1 |
| `feature/f3-l2-autenticacao-responsavel` | `F3-L2-candidate` | F3-L2 |
| `feature/f3-l3-observabilidade` | `F3-L3-candidate-rev2` | F3-L3 |
| `feature/f3-l4-engenharia-entrega` | **sua área de trabalho atual** (a versão verificada pelo gate local) | F3-L4 |

Os pacotes são completos (inspeção de 2026-09-23: todos com `./`, chaves presentes e 0 entradas indevidas). Por isso cada lote é uma fotografia exata, e as remoções entram no lote certo.

Garantias do script:

- **Pré-condições:** branch `main` em `283ce5d`; nenhuma das branches novas pode existir; SHA-256 de cada pacote confere.
- **Backup antes de qualquer mudança:** `git bundle --all` e `tar` da área de trabalho em `~/Downloads/backup-kanban-<data>/`. O `tar` inclui o `.env`, então mantenha a pasta só na sua máquina.
- **Isolamento:** a reconstrução acontece num `git worktree` em `/tmp`. Sua pasta e a `main` não são tocadas.
- **Paradas com rollback automático** (worktree e branches novas removidos):
  - o pacote F2-L1 difere do `283ce5d` em algo além de arquivos novos em `docs/`;
  - um lote fica sem commit;
  - sobra arquivo sem commit;
  - aparece arquivo novo fora dos grupos do F3-L4.
- **Conferência final:** a árvore da `develop` precisa ser idêntica à da sua área de trabalho. Só então a sua pasta passa a apontar para a `develop`, por `git symbolic-ref` + `git reset`, sem alterar nenhum arquivo. `git status` precisa sair limpo, e a `main` continua em `283ce5d`.
- **Limites:** nada é publicado. As datas dos commits são as de hoje. O autor é o seu `git config user.name`/`user.email`.

Pré-requisitos: pacote F3-L4 rev2 aplicado e gate local (`VERIFICACAO-USUARIO.md`) de novo em `=== F3-L4 LOCAL GREEN ===`.

```sh
cd ~/proj/facilit-desafio-kanban
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F3-L4/RECONSTRUCAO-HISTORICO.md > /tmp/f3l4-historico.sh
bash /tmp/f3l4-historico.sh
```

## Script

```bash
bash <<'REBUILD'
set -euo pipefail
REPO="${REBUILD_REPO:-$HOME/proj/facilit-desafio-kanban}"
DL="${REBUILD_DL:-$HOME/Downloads}"
BASE_COMMIT=283ce5d
STAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP="$DL/backup-kanban-$STAMP"
WT="/tmp/kanban-rebuild-$STAMP"
PREFIX=facilit-desafio-kanban

# pacote | prefixo do SHA-256 (fixado a partir das saídas de 2026-09-23)
PACKAGES=(
  "F2-L1-candidate|29c96bfd9a7ddb25"
  "F2-L2-corrigido|e8144a19fc7c3fb0"
  "F3-L1-local-20260922-143305|f3534889592a44ca304973f147dceebd4a085b2cc3ef7c31447cfbb404e7f5f6"
  "F3-L1-candidate-rev4|b35b1e4a824535230e4b477f89bd3edf42bd0556c6c76499e9c7b6a662c0e6df"
  "F3-L2-candidate|0a3597da1e8473465fe29a2b1c7bc64eff92d418ede0a9a9cd18ba97d8c6be8d"
  "F3-L3-candidate-rev2|3f2fbdf8b267c3301e3aef8536e7f95787a6609bf87fb14a658eeaec4f8bb82d"
)

fail() { echo "PARADO: $*" >&2; exit 1; }

BRANCHES=(develop feature/f2-l1-documentacao feature/f2-l2-fundacao-frontend-autenticada feature/f2-l3-kanban-ui feature/f3-l1-diferenciais
          feature/f3-l2-autenticacao-responsavel feature/f3-l3-observabilidade feature/f3-l4-engenharia-entrega)
CREATED=0
DONE=0
# Em qualquer falha depois de criar o worktree: desfaz worktree e branches desta execução.
# A main e a sua área de trabalho não são tocadas antes da conferência final.
rollback() {
  if [ "$DONE" -eq 0 ] && [ "$CREATED" -eq 1 ]; then
    cd "$REPO"
    git worktree remove --force "$WT" >/dev/null 2>&1 || true
    for ref in "${BRANCHES[@]}"; do git branch -D "$ref" >/dev/null 2>&1 || true; done
    echo "ROLLBACK: worktree e branches desta execução removidos; main e área de trabalho intactas." >&2
  fi
}
trap rollback EXIT

# Adiciona ao índice só os caminhos alterados que casam com o pathspec (sem erro quando nada casa).
stage() {
  local files
  files="$(git status --porcelain=v1 -z -uall -- "$@" | tr '\0' '\n' | sed -n 's/^.. //p')"
  if [ -n "$files" ]; then
    printf '%s\n' "$files" | while IFS= read -r path; do git add -A -- "$path"; done
  fi
}

commit_if_staged() {
  if ! git diff --cached --quiet; then
    git commit -q -m "$1"
    echo "   $(git log -1 --format='%h %s')"
  fi
}

# Commits por área de um lote já materializado no worktree.
commit_lot() {
  local lot="$1" type="$2" title="$3" docs_msg="$4"
  stage backend ':(exclude)backend/src/test'
  commit_if_staged "$type(backend): $title"
  stage backend/src/test
  commit_if_staged "test(backend): testes do lote $lot"
  stage frontend ':(exclude,glob)frontend/**/*.test.ts' ':(exclude,glob)frontend/**/*.test.tsx' ':(exclude)frontend/src/test'
  commit_if_staged "$type(frontend): $title"
  stage ':(glob)frontend/**/*.test.ts' ':(glob)frontend/**/*.test.tsx' frontend/src/test
  commit_if_staged "test(frontend): testes do lote $lot"
  stage compose.yaml compose.observability.yaml .env.example .editorconfig .gitignore observability .github
  commit_if_staged "build: infraestrutura do lote $lot"
  stage README.md AI_USAGE.md docs
  commit_if_staged "docs($lot): $docs_msg"
  stage .
  commit_if_staged "chore($lot): demais arquivos do lote"
  test -z "$(git status --porcelain=v1)" || fail "sobrou alteração sem commit no $lot"
}

materialize_package() {
  local pkg="$1"
  git ls-files -z | xargs -0 -r rm -f --
  tar -xzf "$DL/$PREFIX-$pkg.tar.gz" -C "$WT"
}

start_feature() { git switch -q develop; git switch -q -c "$1"; echo "== $1"; }
finish_feature() {
  test "$(git rev-list --count "develop..$1")" -gt 0 || fail "$1 ficou sem commits (pacote igual ao lote anterior?)"
  git switch -q develop
  git merge -q --no-ff --no-edit "$1"
  echo "   merge: $(git log -1 --format='%h %s')"
}

echo '=== PRÉ-CONDIÇÕES ==='
cd "$REPO"
test "$(git branch --show-current)" = main || fail 'a branch atual deve ser main'
case "$(git rev-parse HEAD)" in "$BASE_COMMIT"*) ;; *) fail "HEAD deve ser $BASE_COMMIT" ;; esac
for ref in "${BRANCHES[@]}"; do
  if git show-ref --verify --quiet "refs/heads/$ref"; then fail "branch já existe: $ref"; fi
done
for item in "${PACKAGES[@]}"; do
  pkg="${item%%|*}"; expected="${item##*|}"
  file="$DL/$PREFIX-$pkg.tar.gz"
  test -f "$file" || fail "pacote ausente: $file"
  actual="$(sha256sum "$file" | cut -d' ' -f1)"
  case "$actual" in "$expected"*) echo "   ok $pkg ${actual:0:16}" ;; *) fail "SHA-256 divergente: $pkg" ;; esac
done
# Árvore da área de trabalho atual (versionáveis, respeitando .gitignore), sem tocar no índice real.
TMP_INDEX="$(mktemp)"; rm -f "$TMP_INDEX"
FINAL_TREE="$(GIT_INDEX_FILE="$TMP_INDEX" git read-tree --empty && GIT_INDEX_FILE="$TMP_INDEX" git add -A && GIT_INDEX_FILE="$TMP_INDEX" git write-tree)"
rm -f "$TMP_INDEX"
echo "   árvore atual: $FINAL_TREE ($(git status --porcelain=v1 -uall | wc -l) caminhos alterados ou novos)"
echo 'REBUILD_PRECONDITIONS_GREEN'

echo '=== BACKUP ==='
mkdir -p "$BACKUP"
git bundle create -q "$BACKUP/repo.bundle" --all
tar -czf "$BACKUP/worktree.tar.gz" --exclude=./frontend/node_modules --exclude=./backend/target --exclude=./frontend/dist -C "$REPO" .
(cd "$BACKUP" && sha256sum repo.bundle worktree.tar.gz | tee SHA256SUMS)
echo "   backup em $BACKUP"
echo 'REBUILD_BACKUP_GREEN'

echo '=== F2-L1 CANDIDATE x COMMIT BASE ==='
git worktree add -q -b develop "$WT" HEAD
CREATED=1
cd "$WT"
materialize_package F2-L1-candidate
# Aceita só arquivos NOVOS em docs/ (documentação nunca commitada); qualquer outra diferença para.
OTHER="$(git status --porcelain=v1 -uall | grep -v '^?? docs/' || true)"
if [ -n "$OTHER" ]; then
  printf '%s\n' "$OTHER" | head -20 >&2
  fail "o pacote F2-L1-candidate difere do commit $BASE_COMMIT fora de docs/ (novos)"
fi
DOCS_NEW="$(git status --porcelain=v1 -uall | grep -c '^?? docs/' || true)"
echo "   base idêntica no código; arquivos novos em docs/: $DOCS_NEW"
if [ "$DOCS_NEW" -gt 0 ]; then
  git switch -q -c feature/f2-l1-documentacao
  stage docs
  commit_if_staged "docs: adiciona governança e evidências de F0 a F2-L1"
  test -z "$(git status --porcelain=v1)" || fail 'sobrou alteração na documentação do F2-L1'
  finish_feature feature/f2-l1-documentacao
fi
echo 'REBUILD_BASE_GREEN'

echo '=== LOTES ==='
start_feature feature/f2-l2-fundacao-frontend-autenticada
materialize_package F2-L2-corrigido
commit_lot F2-L2 feat "adiciona fundação frontend autenticada (login, sessão, CSRF)" "registra evidências do F2-L2 e promove o F2-L1"
finish_feature feature/f2-l2-fundacao-frontend-autenticada

start_feature feature/f2-l3-kanban-ui
materialize_package F3-L1-local-20260922-143305
commit_lot F2-L3 feat "adiciona quadro Kanban com drag-and-drop" "registra evidências do F2-L3 e promove o F2-L2"
finish_feature feature/f2-l3-kanban-ui

start_feature feature/f3-l1-diferenciais
materialize_package F3-L1-candidate-rev4
commit_lot F3-L1 feat "adiciona indicadores, CRUD de secretaria e filtros avançados" "registra evidências do F3-L1 e promove o F2-L3"
finish_feature feature/f3-l1-diferenciais

start_feature feature/f3-l2-autenticacao-responsavel
materialize_package F3-L2-candidate
commit_lot F3-L2 feat "adiciona autenticação do responsável e erro seguro" "registra evidências do F3-L2 e promove o F3-L1"
finish_feature feature/f3-l2-autenticacao-responsavel

start_feature feature/f3-l3-observabilidade
materialize_package F3-L3-candidate-rev2
commit_lot F3-L3 feat "adiciona Actuator, Prometheus, Grafana e logs ECS" "registra evidências do F3-L3 e promove o F3-L2"
finish_feature feature/f3-l3-observabilidade

start_feature feature/f3-l4-engenharia-entrega
git read-tree -u --reset "$FINAL_TREE"
git reset -q
stage .github/workflows
commit_if_staged "ci: adiciona workflow do GitHub Actions com build, testes e análise"
stage frontend/scripts frontend/package.json
commit_if_staged "build(frontend): adiciona verificação de tipagem estrita (check:strict)"
stage docs/api
commit_if_staged "test(api): adiciona coleção Postman/Insomnia ponta a ponta"
stage docs/adr
commit_if_staged "docs(adr): propõe camada de IA com agente e RAG"
stage AI_USAGE.md
commit_if_staged "docs: adiciona AI_USAGE.md"
stage README.md
commit_if_staged "docs: reescreve README com arquitetura, execução, testes e limitações"
stage docs/evidence docs/governance
commit_if_staged "docs(F3-L4): registra evidências do F3-L4 e promove o F3-L3"
if git status --porcelain=v1 -uall | grep -q '^??'; then
  git status --porcelain=v1 -uall | grep '^??' >&2
  fail 'arquivos novos fora dos grupos do F3-L4 (remova ou ignore e rode de novo)'
fi
stage .
commit_if_staged "chore(F3-L4): demais ajustes verificados no gate local"
test -z "$(git status --porcelain=v1)" || fail 'sobrou alteração sem commit no F3-L4'
finish_feature feature/f3-l4-engenharia-entrega
echo 'REBUILD_LOTS_GREEN'

echo '=== CONFERÊNCIA FINAL ==='
test "$(git rev-parse 'develop^{tree}')" = "$FINAL_TREE" || fail 'develop difere da área de trabalho atual'
git switch -q --detach
DONE=1
cd "$REPO"
git worktree remove --force "$WT"
git symbolic-ref HEAD refs/heads/develop
git reset -q
test "$(git branch --show-current)" = develop
test -z "$(git status --porcelain=v1)" || { git status --short | head -20; fail 'git status não ficou limpo'; }
test "$(git rev-parse main)" = "$(git rev-parse "$BASE_COMMIT")" || fail 'main foi alterada'
git log --oneline --graph --decorate -40
echo "   commits novos em develop: $(git rev-list --count main..develop)"
echo 'REBUILD_FINAL_GREEN'

printf '%s\n' '=== F3-L4 HISTORICO GREEN ==='
REBUILD

STATUS=$?
echo "Resultado: exit code $STATUS"
```

GREEN exige `REBUILD_PRECONDITIONS_GREEN`, `REBUILD_BACKUP_GREEN`, `REBUILD_BASE_GREEN`, `REBUILD_LOTS_GREEN`, `REBUILD_FINAL_GREEN`, `=== F3-L4 HISTORICO GREEN ===` e `Resultado: exit code 0`.

Para desfazer depois de um GREEN, sem publicar nada:

```sh
cd ~/proj/facilit-desafio-kanban
git symbolic-ref HEAD refs/heads/main && git reset -q
git branch -D develop feature/f2-l1-documentacao feature/f2-l2-fundacao-frontend-autenticada feature/f2-l3-kanban-ui feature/f3-l1-diferenciais \
  feature/f3-l2-autenticacao-responsavel feature/f3-l3-observabilidade feature/f3-l4-engenharia-entrega
```

A área de trabalho continua igual. Volta a ser a `main` com as alterações sem commit.
