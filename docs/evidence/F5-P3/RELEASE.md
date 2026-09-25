# F5-P3 — ROTEIRO DA PATCH RELEASE 2.0.1 (Gitflow de hotfix)

Ordem: commits do F5-P2 → pacote do F5-P3 → freeze → registro do freeze → `main` + tag + `develop` → verificação pública → registro na `develop` → auditoria final.

Regras:

- Nunca usar `--force`.
- A tag só é criada depois do freeze GREEN e do registro da saída.
- O GitLab é espelho: falha nele não bloqueia; sincronize depois com push normal.
- Cada bloco é uma sequência de comandos independentes; rode um de cada vez e pare no primeiro erro.

## 0. Commits do F5-P2, merge na hotfix e push

É a seção 3 de `docs/evidence/F5-P2/VERIFICACAO-USUARIO.md`.

```sh
cd ~/proj/facilit-desafio-kanban
git checkout bugfix/2.0.1-p2-docs

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

## 1. Aplicar o pacote do F5-P3 na `hotfix/2.0.1`

O bloco para no primeiro erro e imprime `PREPARO_OK` só se tudo der certo.

```sh
cd ~/proj/facilit-desafio-kanban && \
git checkout hotfix/2.0.1 && git pull --ff-only origin hotfix/2.0.1 && \
{ [ -z "$(git status --porcelain=v1)" ] || { echo 'PARE: árvore com alterações'; false; }; } && \
{ git cat-file -e HEAD:docs/evidence/F5-P2/GATE.md && git cat-file -p HEAD:CHANGELOG.md | grep -q '^## \[2.0.1\]' \
  || { echo 'PARE: a hotfix/2.0.1 não tem o F5-P2 (passo 0)'; false; }; } && \
(cd ~/Downloads && echo "<SHA256>  facilit-desafio-kanban-F5-P3-candidate.tar.gz" | sha256sum -c -) && \
tar -xzf ~/Downloads/facilit-desafio-kanban-F5-P3-candidate.tar.gz && \
git status --short && \
echo PREPARO_OK
```

Troque `<SHA256>` pelo hash informado com o pacote. O `git status` deve listar só `README.md`, `docs/evidence/F5-P2/` (3 arquivos) e `docs/evidence/F5-P3/` (novo).

## 2. Commits do pacote e push

```sh
cd ~/proj/facilit-desafio-kanban
git add README.md docs/evidence/F5-P2
git commit -m "docs(F5-P2): promove o lote a GREEN com a saída do gate"
git add docs/evidence/F5-P3
git commit -m "docs(F5-P3): prepara o freeze e o roteiro da patch release 2.0.1"
git status --short        # deve ficar vazio
git push origin hotfix/2.0.1
git push gitlab hotfix/2.0.1 || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'
```

## 3. Gate de freeze

```sh
cd ~/proj/facilit-desafio-kanban
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5-P3/VERIFICACAO-USUARIO.md > /tmp/f5p3-gate.sh
bash -n /tmp/f5p3-gate.sh
bash -o pipefail -c 'bash /tmp/f5p3-gate.sh 2>&1 | tee /tmp/saida-gate-f5p3.txt'
```

Só prossiga com `=== F5-P3 GREEN ===` e `Resultado: exit code 0`. Traga a saída antes do passo 4.

## 4. Registrar o freeze (saída, README e gate)

O script normaliza a saída, grava `SAIDA-GATE.txt`, põe a linha do F5-P3 no README e marca o gate como GREEN. Ele recusa se a saída não for GREEN. Rode o bloco `python3` sozinho e confira o `git status` antes do commit.

```sh
cd ~/proj/facilit-desafio-kanban
python3 - <<'PY'
import re, sys
raw = open('/tmp/saida-gate-f5p3.txt', encoding='utf-8', errors='replace', newline='').read()
lines = [re.sub(r'\x1b\[[0-9;?]*[A-Za-z]', '', line).rstrip('\r').split('\r')[-1].rstrip() for line in raw.split('\n')]
saida = '\n'.join(lines).strip('\n') + '\n'
if '\n=== F5-P3 GREEN ===\n' not in '\n' + saida or '\nResultado: exit code 0\n' not in '\n' + saida:
    sys.exit('PARE: a saída do freeze não é GREEN')
open('docs/evidence/F5-P3/SAIDA-GATE.txt', 'w', encoding='utf-8', newline='\n').write(saida)

def replace_once(path, old, new):
    text = open(path, encoding='utf-8').read()
    if text.count(old) != 1:
        sys.exit(f'PARE: {path} fora do esperado ({old[:50]}…)')
    open(path, 'w', encoding='utf-8', newline='\n').write(text.replace(old, new))

p2 = '- F5-P2 — Documentação coerente com a tag (estado das releases, paginação, interpretações, AI_USAGE, CHANGELOG e evidências do F5-L5): GREEN em 2026-09-25.\n'
replace_once('README.md', p2, p2 + '- F5-P3 — Freeze da `hotfix/2.0.1` e release `v2.0.1`: freeze GREEN em 2026-09-25 '
             '(`docs/evidence/F5-P3/SAIDA-GATE.txt`); a tag `v2.0.1` marca o merge desta versão na `main`, e a '
             'verificação pública fica em `docs/evidence/F5-P3/SAIDA-RELEASE.txt`, na `develop`.\n')
replace_once('docs/evidence/F5-P3/GATE.md', 'Estado: **CANDIDATE** (freeze a executar).',
             'Estado: **GREEN** (freeze em 2026-09-25; saída em `SAIDA-GATE.txt`). A release e a verificação pública '
             'vêm depois do freeze e ficam registradas em `SAIDA-RELEASE.txt`, na `develop`.')
print('REGISTRO_OK')
PY
git status --short        # README.md, docs/evidence/F5-P3/GATE.md e docs/evidence/F5-P3/SAIDA-GATE.txt
git add README.md docs/evidence/F5-P3/GATE.md docs/evidence/F5-P3/SAIDA-GATE.txt
git commit -m "docs(F5-P3): registra a saída do freeze e o estado da release 2.0.1"
git push origin hotfix/2.0.1
git push gitlab hotfix/2.0.1 || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'
```

## 5. Fechar o Gitflow: `main`, tag e back-merge na `develop`

A `develop` só tem, desde a `v2.0.0`, `docs/evidence/F5/SAIDA-RELEASE.txt` e `VERIFICACAO-RELEASE.md`, sem arquivo em comum com a hotfix (aviso do gate do F5-P2). O merge deve sair sem conflito; se houver conflito, pare e traga a saída.

```sh
cd ~/proj/facilit-desafio-kanban
git checkout main
git pull --ff-only origin main
git merge --no-ff hotfix/2.0.1 -m "Merge branch 'hotfix/2.0.1' into main"
git tag -a v2.0.1 -m "Facilit Kanban 2.0.1 — Swagger com exemplos em todas as operações, busca literal e documentação coerente"

git checkout develop
git pull --ff-only origin develop
git merge --no-ff hotfix/2.0.1 -m "Merge branch 'hotfix/2.0.1' into develop"

git push origin main develop hotfix/2.0.1 v2.0.1
git push gitlab main develop hotfix/2.0.1 v2.0.1 || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'
```

## 6. Verificação pública da release

Com a `develop` ativa:

```sh
cd ~/proj/facilit-desafio-kanban
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5-P3/VERIFICACAO-RELEASE.md > /tmp/f5p3-release-gate.sh
bash -n /tmp/f5p3-release-gate.sh
bash -o pipefail -c 'bash /tmp/f5p3-release-gate.sh 2>&1 | tee /tmp/saida-release-f5p3.txt'
```

Só encerre com `=== F5-P3 RELEASE GREEN ===` e `Resultado: exit code 0`.

## 7. Registrar a verificação na `develop`

A tag não é movida. A prova pós-release entra só na `develop`.

```sh
cd ~/proj/facilit-desafio-kanban
python3 - <<'PY'
import re, sys
raw = open('/tmp/saida-release-f5p3.txt', encoding='utf-8', errors='replace', newline='').read()
lines = [re.sub(r'\x1b\[[0-9;?]*[A-Za-z]', '', line).rstrip('\r').split('\r')[-1].rstrip() for line in raw.split('\n')]
saida = '\n'.join(lines).strip('\n') + '\n'
if '\n=== F5-P3 RELEASE GREEN ===\n' not in '\n' + saida or '\nResultado: exit code 0\n' not in '\n' + saida:
    sys.exit('PARE: a verificação pública não é GREEN')
open('docs/evidence/F5-P3/SAIDA-RELEASE.txt', 'w', encoding='utf-8', newline='\n').write(saida)
print('REGISTRO_OK')
PY
git add docs/evidence/F5-P3/SAIDA-RELEASE.txt
git commit -m "docs(F5-P3): registra a verificação pública da release 2.0.1"
git push origin develop
git push gitlab develop || echo 'AVISO: GitLab indisponível (espelho, não bloqueia)'
```

## 8. Pacote para a auditoria final

```sh
cd ~/proj/facilit-desafio-kanban
git archive --format=tar.gz --prefix=facilit-desafio-kanban-v2.0.1/ \
  -o ~/Downloads/facilit-desafio-kanban-v2.0.1-auditoria.tar.gz v2.0.1
sha256sum ~/Downloads/facilit-desafio-kanban-v2.0.1-auditoria.tar.gz
```

A auditoria (análise própria confrontada com um agente independente) usa esse pacote. O relatório `docs/evidence/F5/ADERENCIA-V2.0.1.md` entra na `develop` com `docs(F5): versiona a auditoria de aderência da v2.0.1`.

## Falha

- **Freeze RED:** corrigir só a causa, num `bugfix/2.0.1-*` mesclado na `hotfix/2.0.1`, e repetir o freeze; não tocar na `main`.
- **Conflito no back-merge:** `git merge --abort` e trazer a saída; a `main` e a tag já publicadas não mudam.
- **Push do GitHub com erro:** não prosseguir; conferir com `git ls-remote origin` e repetir o push normal.
- **Nunca** `--force`, e nunca mover a tag.
