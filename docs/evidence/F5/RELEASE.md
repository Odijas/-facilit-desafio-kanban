# F5-L5 — ROTEIRO DA RELEASE 2.0.0 (Gitflow)

A release parte do último `develop` GREEN (`17aebc8`). Até o merge na `main`, nenhuma etapa altera a release pública anterior.

## 1. Criar `release/2.0.0`

```sh
cd ~/proj/facilit-desafio-kanban
git switch develop
git pull --ff-only origin develop
test "$(git rev-parse HEAD)" = "$(git rev-parse gitlab/develop)"
test -z "$(git status --porcelain=v1)"
git switch -c release/2.0.0
```

## 2. Aplicar o pacote F5-L5

```sh
sha256sum ~/Downloads/facilit-desafio-kanban-F5-L5-candidate.tar.gz
tar -xzf ~/Downloads/facilit-desafio-kanban-F5-L5-candidate.tar.gz -C .
git diff --check
git status --short
```

Devem aparecer somente os arquivos listados em `CONSUMIDORES.md`.

## 3. Commits semânticos da release

```sh
git add backend/pom.xml backend/Dockerfile frontend/package.json
git commit -m "build(release): define a versão 2.0.0"

git add CHANGELOG.md README.md docs/evidence/F5
git commit -m "docs(F5): prepara auditoria e freeze da release 2.0.0"

git status --short
git push -u origin release/2.0.0
git push gitlab release/2.0.0
```

O push dispara o CI da branch de release.

## 4. Gate de freeze

```sh
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5/VERIFICACAO-USUARIO.md > /tmp/f5-gate.sh
bash -n /tmp/f5-gate.sh
bash /tmp/f5-gate.sh 2>&1 | tee /tmp/saida-gate-f5.txt
```

Só prossiga com `=== F5-L5 GREEN ===` e `Resultado: exit code 0`.

## 5. Versionar a saída real do freeze

```sh
python3 - <<'PY'
import re
text = open('/tmp/saida-gate-f5.txt', encoding='utf-8', errors='replace', newline='').read()
lines = []
for line in text.split('\n'):
    line = re.sub(r'\x1b\[[0-9;?]*[A-Za-z]', '', line).rstrip('\r').split('\r')[-1].rstrip()
    lines.append(line)
open('docs/evidence/F5/SAIDA-GATE.txt', 'w', encoding='utf-8', newline='\n').write('\n'.join(lines).strip('\n') + '\n')
PY
grep -Fqx '=== F5-L5 GREEN ===' docs/evidence/F5/SAIDA-GATE.txt
grep -Fqx 'Resultado: exit code 0' docs/evidence/F5/SAIDA-GATE.txt
git add docs/evidence/F5/SAIDA-GATE.txt
git commit -m "docs(F5): registra saída do gate de freeze"
git push origin release/2.0.0
git push gitlab release/2.0.0
```

## 6. Fechar Gitflow: `main`, tag e back-merge

```sh
git switch main
git pull --ff-only origin main
git merge --no-ff release/2.0.0 -m "Merge branch 'release/2.0.0' into main"
git tag -a v2.0.0 -m "Facilit Kanban 2.0.0 — conformidade final do desafio técnico"

git switch develop
git merge --no-ff release/2.0.0 -m "Merge branch 'release/2.0.0' into develop"

git push origin main develop release/2.0.0 v2.0.0
git push gitlab main develop release/2.0.0 v2.0.0
```

## 7. Verificação pública da release

```sh
awk 'BEGIN{f=0} /^```bash$/{f=1;next} /^```$/{if(f){exit}} f{print}' docs/evidence/F5/VERIFICACAO-RELEASE.md > /tmp/f5-release-gate.sh
bash -n /tmp/f5-release-gate.sh
bash /tmp/f5-release-gate.sh 2>&1 | tee /tmp/saida-release-f5.txt
```

Só encerre com `=== F5 RELEASE GREEN ===` e `Resultado: exit code 0`.

## 8. Registrar a prova final em `develop`

A tag não é movida. A prova pós-release entra somente na `develop`.

```sh
python3 - <<'PY'
import re
text = open('/tmp/saida-release-f5.txt', encoding='utf-8', errors='replace', newline='').read()
lines=[]
for line in text.split('\n'):
    line = re.sub(r'\x1b\[[0-9;?]*[A-Za-z]', '', line).rstrip('\r').split('\r')[-1].rstrip()
    lines.append(line)
open('docs/evidence/F5/SAIDA-RELEASE.txt', 'w', encoding='utf-8', newline='\n').write('\n'.join(lines).strip('\n') + '\n')
PY
grep -Fqx '=== F5 RELEASE GREEN ===' docs/evidence/F5/SAIDA-RELEASE.txt
grep -Fqx 'Resultado: exit code 0' docs/evidence/F5/SAIDA-RELEASE.txt
git add docs/evidence/F5/SAIDA-RELEASE.txt
git commit -m "docs(F5): registra verificação final da release 2.0.0"
git push origin develop
git push gitlab develop
```

## Falha

- Freeze RED: corrigir somente a causa no próprio `release/2.0.0`, reexecutar o gate e não tocar na `main`.
- Push remoto com erro transitório: não reescrever histórico; confirmar refs com `git ls-remote` e repetir o push normal.
- Nunca usar `--force` no fechamento da release.
