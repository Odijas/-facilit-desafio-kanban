# F3-L4 — MIGRAÇÃO PARA O GITHUB (etapa 3 de 4)

Execute somente depois de `=== F3-L4 HISTORICO GREEN ===` (`RECONSTRUCAO-HISTORICO.md`). A partir daqui a sua pasta está na `develop`.

Os passos publicam o repositório com todo o histórico, seguindo a documentação oficial do GitHub ("Duplicating a repository": `git clone --bare` + `git push --mirror`). Depois do passo 4 o código fica público, e isso não se desfaz.

Substitua nos comandos:

- `<GITLAB_URL>`: URL de clone no gitlab.com, por exemplo `git@gitlab.com:usuario/facilit-desafio-kanban.git`;
- `<OWNER>/<REPO>`: usuário e nome do repositório no GitHub.

## 1. Publicar as branches Gitflow no GitLab

```sh
cd ~/proj/facilit-desafio-kanban
git branch --show-current          # develop
git status --short                 # vazio
git push origin --all              # main (inalterada), develop e as feature/*
```

## 2. Criar o repositório vazio no GitHub

Em <https://github.com/new>:

- nome `<REPO>`;
- visibilidade **Public**;
- **sem** README, `.gitignore` ou licença, porque o destino do `--mirror` precisa estar vazio.

## 3. Clonar do GitLab sem área de trabalho

```sh
cd /tmp
rm -rf facilit-mirror.git
git clone --bare <GITLAB_URL> facilit-mirror.git
```

## 4. Publicar no GitHub com o histórico (irreversível)

```sh
cd /tmp/facilit-mirror.git
git push --mirror https://github.com/<OWNER>/<REPO>.git
cd /tmp && rm -rf facilit-mirror.git
```

A proteção de push do GitHub bloqueia o envio se detectar segredo conhecido. Se isso acontecer, **não** force o envio: traga a mensagem para análise.

## 5. Apontar o repositório local para o GitHub

```sh
cd ~/proj/facilit-desafio-kanban
git remote rename origin gitlab
git remote add origin https://github.com/<OWNER>/<REPO>.git
git fetch origin
git branch -u origin/develop develop
git branch -u origin/main main
git remote -v
```

## 6. Conferir no GitHub

- **Settings → General → Default branch:** `main`. Até a F4 ela fica no F2-L1; na F4 (25/09) a `release/1.0.0` vai para a `main` com a tag `v1.0.0`. Não envie o link aos avaliadores antes da F4.
- **Actions:** o workflow **CI** disparou nos pushes da `develop` e da `feature/f3-l4-engenharia-entrega`. As outras branches não têm o workflow. Se aparecer um aviso para habilitar workflows, habilite.

Em seguida, com a `develop` ativa, execute o gate de `VERIFICACAO-CI.md`.

## Rota Gitflow daqui em diante

- **Correção no F3-L4, se o pipeline falhar:** `fix/<assunto>` a partir da `develop`, commit `fix(<escopo>): …`, `merge --no-ff` na `develop` e push nos dois remotos (`git push gitlab develop && git push origin develop`).
- **F4 (freeze):**
  1. `git switch -c release/1.0.0 develop`, só com correções mínimas.
  2. `git switch main && git merge --no-ff release/1.0.0`.
  3. `git tag -a v1.0.0 -m "Entrega do desafio Kanban"`.
  4. `git switch develop && git merge --no-ff release/1.0.0`.
  5. Push de `main`, `develop` e da tag nos dois remotos.
