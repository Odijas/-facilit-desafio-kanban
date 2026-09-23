# F4 — ROTEIRO DA RELEASE 1.0.0 (Gitflow)

Ordem obrigatória. Os passos 1 a 5 não alteram a `main`. O passo 6 fecha a release, e o passo 7 confere o resultado.

## 1. Criar a branch de release a partir da `develop`

```sh
cd ~/proj/facilit-desafio-kanban
git switch develop
git pull --ff-only origin develop
git status --short                     # vazio
git switch -c release/1.0.0
```

## 2. Aplicar o pacote da F4

```sh
sha256sum ~/Downloads/facilit-desafio-kanban-F4-candidate.tar.gz
tar -xzf ~/Downloads/facilit-desafio-kanban-F4-candidate.tar.gz
git status --short
```

Devem aparecer só os arquivos listados em `CONSUMIDORES.md`.

## 3. Commits da release e publicação da branch

```sh
git add backend/pom.xml backend/Dockerfile frontend/package.json
git commit -m "build: define a versão 1.0.0"
git add README.md docs/governance/REPLANEJAMENTO-F3.md docs/evidence/F3-L4
git commit -m "docs(F3-L4): promove o F3-L4 e a F3 para GREEN"
git add docs/evidence/F4
git commit -m "docs(F4): auditoria de freeze, revisão de segurança e roteiro da release"
git status --short                     # vazio
git push -u origin release/1.0.0
git push gitlab release/1.0.0
```

O push da `release/1.0.0` dispara o CI no GitHub.

## 4. Gate de freeze

Instruções em `VERIFICACAO-USUARIO.md`:

```sh
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F4/VERIFICACAO-USUARIO.md > /tmp/f4-gate.sh
bash /tmp/f4-gate.sh 2>&1 | tee /tmp/saida-gate-f4.txt
```

Só siga com `=== F4 GREEN ===` e `Resultado: exit code 0`.

## 5. Registrar a saída do gate na release

O script remove cores, retornos de carro e espaços no fim das linhas, para o arquivo passar no `git diff --check` do CI:

```sh
python3 - <<'PY'
import re
text = open('/tmp/saida-gate-f4.txt', encoding='utf-8', errors='replace', newline='').read()
lines = []
for line in text.split('\n'):
    line = re.sub(r'\x1b\[[0-9;?]*[A-Za-z]', '', line).rstrip('\r').split('\r')[-1].rstrip()
    lines.append(line)
open('docs/evidence/F4/SAIDA-GATE.txt', 'w', encoding='utf-8', newline='\n').write('\n'.join(lines).strip('\n') + '\n')
PY
grep -c 'F4_.*_GREEN' docs/evidence/F4/SAIDA-GATE.txt   # 11
git add docs/evidence/F4/SAIDA-GATE.txt
git commit -m "docs(F4): registra a saída do gate de freeze"
git push origin release/1.0.0
git push gitlab release/1.0.0
```

## 6. Fechar a release: `main` + tag + volta para a `develop`

```sh
git switch main
git pull --ff-only origin main
git merge --no-ff --no-edit release/1.0.0
git tag -a v1.0.0 -m "Facilit Kanban 1.0.0 — entrega do desafio técnico"
git switch develop
git merge --no-ff --no-edit release/1.0.0
git push origin main develop v1.0.0
git push gitlab main develop v1.0.0
```

Os pushes disparam o CI da `main` e da `develop`.

## 7. Verificação da release

Siga `VERIFICACAO-RELEASE.md`. Depois do `=== F4 RELEASE GREEN ===`, o link para os avaliadores é <https://github.com/Odijas/facilit-desafio-kanban>, com a `main` na tag `v1.0.0`.

## Se algo falhar

- **Gate de freeze RED:** a correção mínima entra na própria `release/1.0.0`, como `fix(<escopo>): …`. Publique nos dois remotos e rode o gate de novo.
- **Rollback antes do passo 6:** nada chegou à `main`. Descarte com `git switch develop && git branch -D release/1.0.0`, e remova a branch remota se já foi publicada.
