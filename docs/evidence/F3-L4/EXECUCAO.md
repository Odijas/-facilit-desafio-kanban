# F3-L4 — EXECUÇÃO

Data: 2026-09-23

## Base

```text
[EXECUTADO PELO USUÁRIO · 2026-09-23] F3-L3 rev2 GREEN (exit code 0).
[VERIFICADO] base do F3-L4 = conteúdo do F3-L3 GREEN + promoção documental do F3-L3.
```

## Executado neste ambiente

```text
[EXECUTADO · 2026-09-23] actionlint 1.7.12 (binário oficial) em .github/workflows/ci.yml → sem achados (ACTIONLINT_GREEN).
[EXECUTADO · 2026-09-23] check:strict: árvore real → STRICT_TYPES_GREEN 31; sonda com `any` e `as` em src/__probe.ts → "__probe.ts:1", "__probe.ts:2", exit 1 (sonda removida).
[EXECUTADO · 2026-09-23] frontend (Node 22.22.2, pnpm 12.5.1):
  pnpm format / lint: Checked 39 files. No fixes applied.
  pnpm typecheck: exit 0
  pnpm check:strict: STRICT_TYPES_GREEN 31
  pnpm test: Test Files 6 passed (6); Tests 30 passed (30)
  pnpm build: built · exit 0
[EXECUTADO · 2026-09-23] newman 6.2.2 (npx) com a coleção contra servidor simulado que reproduz sessão, CSRF por cookie, limpeza do XSRF-TOKEN no login e logout:
  requests 21 · test-scripts 21 · assertions 37 · failed 0 · exit 0
  (o simulador valida só a mecânica de cookies/CSRF/variáveis; o contrato real é validado no gate local contra o Compose)
[EXECUTADO · 2026-09-23] trechos WORKFLOW, REPOSITORY e DOCS do gate local num repositório Git temporário com o histórico F3-L1 rev4 → F3-L2 → F3-L3 → F3-L4:
  WORKFLOW_POLICY_OK actions=5 jobs=['backend', 'frontend', 'repository']
  WORKTREE_OK files=278 · HISTORY_SECRETS_OK · DOCS_OK
[EXECUTADO · 2026-09-23] controles negativos:
  commit com .env e senha → "arquivo sensível encontrado no histórico", exit 1
  commit com `APP_ADMIN_PASSWORD=supersecret` num .txt → "senha de ambiente com valor: commit … arquivo notes.txt", exit 1;
    no mesmo arquivo, `export POSTGRES_PASSWORD="$(gen)"` e `POSTGRES_PASSWORD=change-me-local-only` não foram acusados
  workflow com `actions/setup-node@v7` → "action sem SHA completo" e "sem comentário de versão", exit 1
[EXECUTADO · 2026-09-23] seleção do run no gate CI com JSON de exemplo (tentativa 2 bem-sucedida, outro workflow ignorado) → "completed:success 12".
[EXECUTADO · 2026-09-23] `bash -n` no gate local e no gate CI extraídos por awk → sem erro.
[EXECUTADO · 2026-09-23] `git diff --check` da árvore inteira (repositório temporário) → sem diagnóstico.
```

## Correções próprias antes do empacotamento

```text
[VERIFICADO] o comentário de cabeçalho do workflow cita "pull_request_target"; a primeira versão da política o acusaria. A checagem de termos proibidos passou a ignorar linhas de comentário.
[VERIFICADO] o rascunho do ADR afirmava que as ferramentas de leitura recebem o Actor. Os casos de uso de leitura (`ProjectService.search/get/indicators`) não recebem Actor (decisão P2). O texto foi corrigido.
[VERIFICADO] newman: a imagem Docker oficial está parada na 6.1.3 (2024-06). O gate passou a usar `npx --yes newman@6.2.2`, fora do workspace pnpm, que tem `minimumReleaseAgeStrict`.
```

## rev2 — reconstrução do histórico (2026-09-23)

```text
[EXECUTADO · 2026-09-23] script de reconstrução em repositório simulado (main com commit base + área de trabalho com o F3-L4; seis pacotes gerados a partir das árvores dos lotes, SHA-256 substituídos só na cópia de teste):
  execução 1, com arquivo solto `NOTES-local.txt` → "PARADO: arquivos novos fora dos grupos do F3-L4" + ROLLBACK; depois dela: só `main`, um worktree, área de trabalho com as mesmas 97 alterações
  execução 2, sem o arquivo → REBUILD_PRECONDITIONS/BACKUP/BASE/LOTS/FINAL_GREEN, `=== F3-L4 HISTORICO GREEN ===`, exit 0
    5 lotes com commits por área e merge --no-ff; F3-L4 com 7 commits por assunto; 40 commits novos na develop
[EXECUTADO · 2026-09-23] rascunho anterior com pacotes iguais entre lotes gerava feature sem commit e merge vazio → o script passou a parar com "ficou sem commits".
[EXECUTADO · 2026-09-23] script extraído por awk de RECONSTRUCAO-HISTORICO.md idêntico ao testado; `bash -n` sem erro.
[EXECUTADO · 2026-09-23] erro próprio: as edições do rev2 deixaram linha em branco no fim de DECISOES.md, GATE.md e RISCOS.md do F3-L4. A checagem de repositório do próprio gate e o `git diff --check` acusaram; corrigido e repetido com WORKTREE_OK e DOCS_OK.
```

## rev3 (2026-09-23)

```text
[EXECUTADO · 2026-09-23] smoke em curl extraído do gate contra servidor simulado (sessão, CSRF por cookie, limpeza do XSRF-TOKEN no login, logout): "ok 1" … "ok 21", F3_L4_API_SMOKE_GREEN, exit 0.
[EXECUTADO · 2026-09-23] controle negativo do smoke com senha errada → "FALHA: login ADMIN — esperado 200, obtido 401", exit 1.
[EXECUTADO · 2026-09-23] coleção regerada: só a descrição mudou (a menção ao newman saiu); itens, scripts e variáveis idênticos (comparação JSON sem `info.description`).
[EXECUTADO · 2026-09-23] reconstrução em repositório simulado:
  A — base sem docs/ → "arquivos novos em docs/: 83", commit "docs: adiciona governança e evidências de F0 a F2-L1", merge; REBUILD_FINAL_GREEN; branch develop; status limpo
  B — base com backend/pom.xml alterado → " M backend/pom.xml", PARADO + ROLLBACK, só main
[VERIFICADO: frontend/pnpm-lock.yaml] `grep -c 'deprecated:'` → 0.
[EXECUTADO · 2026-09-23] erro próprio repetido do rev2: as edições do rev3 voltaram a deixar linha em branco no fim de DECISOES.md, GATE.md e RISCOS.md. A checagem do gate acusou antes do empacotamento; corrigido normalizando o fim de todos os .md do F3-L4.
```

## Não executado neste ambiente

```text
[DESCONHECIDO] reconstrução sobre o repositório real, migração e pipeline no GitHub. Resolve: RECONSTRUCAO-HISTORICO.md, MIGRACAO-GITHUB.md e gate CI.
```
