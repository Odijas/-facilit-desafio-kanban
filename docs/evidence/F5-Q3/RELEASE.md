# F5-Q3 — ROTEIRO DA PATCH RELEASE 2.0.2 (Gitflow de hotfix)

Ordem: verificar fechamento do F5-Q2 → aplicar/promover Q2 + preparar Q3 → freeze → registrar freeze → `main` + tag → back-merge `develop` → verificação pública → registrar verificação → `git archive` para auditoria final.

Regras:

- Pare no primeiro erro.
- Nunca use `--force` e nunca mova uma tag publicada.
- A tag `v2.0.2` só é criada depois do freeze GREEN e do registro da saída.
- GitHub (`origin`) é bloqueante; GitLab (`gitlab`) é espelho e falha nele não invalida a release.
- Merges, tag e pushes são irreversíveis e permanecem sob execução do desenvolvedor.

## 0. Confirmar a base Q2 fechada

```sh
cd ~/proj/facilit-desafio-kanban

git checkout hotfix/2.0.2
git fetch origin --tags
git fetch gitlab || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'

test -z "$(git status --porcelain=v1)" || { echo 'PARE: árvore com alterações'; exit 1; }
HEAD_SHA="$(git rev-parse HEAD)"
ORIGIN_SHA="$(git rev-parse origin/hotfix/2.0.2)"
test "$HEAD_SHA" = '9dceb9ce2755df6e1784683d7b4c3baa213ba26d' || { echo "PARE: HEAD inesperado: $HEAD_SHA"; exit 1; }
test "$HEAD_SHA" = "$ORIGIN_SHA" || { echo 'PARE: hotfix local difere do GitHub'; exit 1; }
if git rev-parse -q --verify gitlab/hotfix/2.0.2 >/dev/null; then
  test "$HEAD_SHA" = "$(git rev-parse gitlab/hotfix/2.0.2)" || { echo 'PARE: GitLab difere da hotfix local'; exit 1; }
fi

git cat-file -e HEAD:docs/evidence/F5-Q2/GATE.md
git cat-file -e HEAD:docs/evidence/F5-Q2/VERIFICACAO-USUARIO.md
git cat-file -p HEAD:CHANGELOG.md | grep -q '^## \[2.0.2\] — 2026-09-25$' || { echo 'PARE: CHANGELOG 2.0.2 ausente'; exit 1; }

echo F5_Q3_BASE_OK
```

## 1. Aplicar o candidate do F5-Q3

```sh
cd ~/Downloads
echo '<SHA256>  facilit-desafio-kanban-F5-Q3-candidate.tar.gz' | sha256sum -c -

cd ~/proj
tar -xzf ~/Downloads/facilit-desafio-kanban-F5-Q3-candidate.tar.gz
cd ~/proj/facilit-desafio-kanban

git status --short
```

Troque `<SHA256>` pelo hash informado com o pacote. O diff deve conter somente:

- `README.md`;
- promoção em `docs/evidence/F5-Q2/` (`GATE.md`, `EXECUCAO.md`, `MATRIZ.md`, `RISCOS.md`, `SAIDA-GATE.txt`, `FECHAMENTO.txt`);
- novo diretório `docs/evidence/F5-Q3/`.

## 2. Commits do preparo e push da hotfix

```sh
cd ~/proj/facilit-desafio-kanban

git diff --check

git add README.md docs/evidence/F5-Q2
git commit -m "docs(F5-Q2): promove o lote a GREEN com gate e fechamento"

git add docs/evidence/F5-Q3
git commit -m "docs(F5-Q3): prepara freeze e release 2.0.2"

test -z "$(git status --porcelain=v1)" || { git status --short; echo 'PARE: árvore não ficou limpa'; exit 1; }

git push origin hotfix/2.0.2
git push gitlab hotfix/2.0.2 || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'
```

## 3. Gate de freeze

```sh
cd ~/proj/facilit-desafio-kanban
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' \
  docs/evidence/F5-Q3/VERIFICACAO-USUARIO.md > /tmp/f5q3-gate.sh
bash -n /tmp/f5q3-gate.sh
bash -o pipefail -c 'bash /tmp/f5q3-gate.sh 2>&1 | tee /tmp/saida-gate-f5q3.txt'
```

Só prossiga se a saída terminar com:

```text
=== F5-Q3 GREEN ===
Resultado: exit code 0
```

## 4. Registrar o freeze GREEN na hotfix

O script recusa uma saída não GREEN, normaliza ANSI/CR, grava `SAIDA-GATE.txt`, adiciona a linha Q3 ao README e promove `GATE.md`.

```sh
cd ~/proj/facilit-desafio-kanban
python3 - <<'PY'
import re, sys
raw = open('/tmp/saida-gate-f5q3.txt', encoding='utf-8', errors='replace', newline='').read()
lines = [re.sub(r'\x1b\[[0-9;?]*[A-Za-z]', '', line).rstrip('\r').split('\r')[-1].rstrip() for line in raw.split('\n')]
saida = '\n'.join(lines).strip('\n') + '\n'
if '\n=== F5-Q3 GREEN ===\n' not in '\n' + saida or '\nResultado: exit code 0\n' not in '\n' + saida:
    sys.exit('PARE: a saída do freeze não é GREEN')
open('docs/evidence/F5-Q3/SAIDA-GATE.txt', 'w', encoding='utf-8', newline='\n').write(saida)

def replace_once(path, old, new):
    text = open(path, encoding='utf-8').read()
    if text.count(old) != 1:
        sys.exit(f'PARE: {path} fora do esperado ({old[:60]}…)')
    open(path, 'w', encoding='utf-8', newline='\n').write(text.replace(old, new))

q2 = '- F5-Q2 — Documentação e promoção da patch `2.0.2` (E1/E2, ADR D1, CHANGELOG, AI_USAGE e auditoria `v2.0.1`): GREEN em 2026-09-25 (`docs/evidence/F5-Q2/SAIDA-GATE.txt`); CI do Q1 GREEN e fechamento Gitflow da `hotfix/2.0.2` registrado em `docs/evidence/F5-Q2/FECHAMENTO.txt`.\n'
q3 = '- F5-Q3 — Freeze da `hotfix/2.0.2` e release `v2.0.2`: freeze GREEN em 2026-09-25 (`docs/evidence/F5-Q3/SAIDA-GATE.txt`); a tag `v2.0.2` marca o merge desta versão na `main`, e a verificação pública fica em `docs/evidence/F5-Q3/SAIDA-RELEASE.txt`, na `develop`.\n'
replace_once('README.md', q2, q2 + q3)
replace_once('docs/evidence/F5-Q3/GATE.md', 'Estado: **CANDIDATE — freeze não executado**.',
             'Estado: **GREEN** (freeze em 2026-09-25; saída em `SAIDA-GATE.txt`). A release e a verificação pública vêm depois do freeze e ficam registradas em `SAIDA-RELEASE.txt`, na `develop`.')
print('REGISTRO_FREEZE_OK')
PY

git diff --check
git status --short
git add README.md docs/evidence/F5-Q3/GATE.md docs/evidence/F5-Q3/SAIDA-GATE.txt
git commit -m "docs(F5-Q3): registra freeze GREEN da release 2.0.2"
git push origin hotfix/2.0.2
git push gitlab hotfix/2.0.2 || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'
```

## 5. Fechar o Gitflow: `main`, tag e back-merge na `develop`

Se qualquer merge conflitar, pare. Não resolva automaticamente e não mova tag.

```sh
cd ~/proj/facilit-desafio-kanban

git checkout main
git pull --ff-only origin main
git merge --no-ff hotfix/2.0.2 -m "Merge branch 'hotfix/2.0.2' into main"

git rev-parse -q --verify refs/tags/v2.0.2 >/dev/null && { echo 'PARE: tag v2.0.2 já existe'; exit 1; } || true
git tag -a v2.0.2 -m "Facilit Kanban 2.0.2 — correções de aderência documental, OpenAPI e mensagens de transição"

git checkout develop
git pull --ff-only origin develop
git merge --no-ff hotfix/2.0.2 -m "Merge branch 'hotfix/2.0.2' into develop"

git push origin main develop hotfix/2.0.2 v2.0.2
git push gitlab main develop hotfix/2.0.2 v2.0.2 || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'
```

## 6. Verificação pública da release

Com a `develop` ativa:

```sh
cd ~/proj/facilit-desafio-kanban
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' \
  docs/evidence/F5-Q3/VERIFICACAO-RELEASE.md > /tmp/f5q3-release-gate.sh
bash -n /tmp/f5q3-release-gate.sh
bash -o pipefail -c 'bash /tmp/f5q3-release-gate.sh 2>&1 | tee /tmp/saida-release-f5q3.txt'
```

Só encerre esta etapa com `=== F5-Q3 RELEASE GREEN ===` e `Resultado: exit code 0`.

## 7. Registrar a verificação pública na `develop`

A tag não é movida. A prova pós-release entra apenas na `develop`.

```sh
cd ~/proj/facilit-desafio-kanban
python3 - <<'PY'
import re, sys
raw = open('/tmp/saida-release-f5q3.txt', encoding='utf-8', errors='replace', newline='').read()
lines = [re.sub(r'\x1b\[[0-9;?]*[A-Za-z]', '', line).rstrip('\r').split('\r')[-1].rstrip() for line in raw.split('\n')]
saida = '\n'.join(lines).strip('\n') + '\n'
if '\n=== F5-Q3 RELEASE GREEN ===\n' not in '\n' + saida or '\nResultado: exit code 0\n' not in '\n' + saida:
    sys.exit('PARE: a verificação pública não é GREEN')
open('docs/evidence/F5-Q3/SAIDA-RELEASE.txt', 'w', encoding='utf-8', newline='\n').write(saida)
print('REGISTRO_RELEASE_OK')
PY

git add docs/evidence/F5-Q3/SAIDA-RELEASE.txt
git commit -m "docs(F5-Q3): registra verificação pública da release 2.0.2"
git push origin develop
git push gitlab develop || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'
```

## 8. Gerar o snapshot da auditoria final

```sh
cd ~/proj/facilit-desafio-kanban
git archive --format=tar.gz --prefix=facilit-desafio-kanban-v2.0.2/ \
  -o ~/Downloads/facilit-desafio-kanban-v2.0.2-auditoria.tar.gz v2.0.2
sha256sum ~/Downloads/facilit-desafio-kanban-v2.0.2-auditoria.tar.gz
```

A auditoria final confronta esse snapshot da tag com o PDF e com um agente independente. O relatório `docs/evidence/F5/ADERENCIA-V2.0.2.md` só é criado depois dessa auditoria.

## Falhas

- **Freeze RED:** não tocar na `main`; corrigir somente a causa em `bugfix/2.0.2-*`, mergear na hotfix e repetir o freeze.
- **Conflito na `main`:** `git merge --abort`; nenhuma tag deve ter sido criada ainda.
- **Conflito no back-merge da `develop`:** `git merge --abort` e trazer a saída; não mover `main` nem a tag.
- **Push do GitHub com erro:** pare, confira `git ls-remote origin` e repita push normal.
- **GitLab com erro:** registrar o aviso e sincronizar depois com push normal; não usar `--force`.
