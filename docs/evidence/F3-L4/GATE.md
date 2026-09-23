# F3-L4 — GATE

Data: 2026-09-23

Estado: **CANDIDATE rev3**.

- Etapa 1 (gate local) GREEN no rev1 e no rev2.
- Etapa 2 do rev2 parou com rollback: o `283ce5d` não tem `docs/`.
- O rev3 troca o newman por smoke em `curl` (sem ferramenta depreciada) e aceita a documentação nunca commitada como commit próprio. Por isso as etapas 1 e 2 devem ser executadas de novo.

Escopo: Engenharia de entrega (`PROMPT-EXECUTIVO-KANBAN-v1.0.md`, antigo F3-L3; `docs/governance/REPLANEJAMENTO-F3.md` §5):

- GitHub Actions: build, testes e análise automatizada aplicável;
- README;
- `AI_USAGE.md`;
- ADR de agente/RAG, com diagrama, contrato proposto, montagem de contexto, erros e timeouts de provedor, trade-offs, limitações e próximos passos;
- migração para o GitHub com histórico.

O fechamento tem quatro etapas, e o lote só vira GREEN com a quarta.

## Etapa 1 — gate local (`VERIFICACAO-USUARIO.md`)

- `F3_L4_FRONTEND_GREEN`: Biome write, lint, typecheck, `check:strict`, 30 testes e build;
- `F3_L4_BACKEND_GREEN`: `mvn clean verify`;
- `F3_L4_WORKFLOW_GREEN`: `actionlint` 1.7.12 sem achados, mais a política do workflow:
  - actions por SHA completo com comentário de versão;
  - `permissions: contents: read`;
  - sem `pull_request_target`, `self-hosted`, `secrets.` ou permissão de escrita;
  - `persist-credentials: false` em todo checkout;
  - jobs `frontend`, `backend` e `repository`;
- `F3_L4_REPOSITORY_GREEN`:
  - sem espaço no fim de linha nem linha em branco no fim de arquivo em arquivos versionáveis;
  - sem `.env` ou chaves na árvore nem no histórico;
  - sem senha com valor, chave privada ou token GitLab, GitHub ou AWS em nenhum commit;
  - sem `localStorage`/`sessionStorage`;
- `F3_L4_DOCS_GREEN`:
  - seções obrigatórias do README, do `AI_USAGE.md` e do ADR;
  - diagramas mermaid;
  - pelo menos uma sugestão da IA rejeitada;
  - links relativos válidos;
  - coleção no schema v2.1 sem credencial;
- `F3_L4_API_SMOKE_GREEN`: os 21 cenários da coleção executados por `curl` contra o Compose, sem falha;
- `F3_L4_STATIC_GREEN`: OpenAPI com as rotas principais e `git diff --check`;
- marcador `=== F3-L4 LOCAL GREEN ===` e exit code 0.

## Etapa 2 — histórico por lote (`RECONSTRUCAO-HISTORICO.md`)

- `REBUILD_PRECONDITIONS_GREEN`: `main` em `283ce5d`, branches novas inexistentes, SHA-256 dos pacotes conferem;
- `REBUILD_BACKUP_GREEN`: bundle e tar com SHA-256;
- `REBUILD_BASE_GREEN`: F2-L1 candidate idêntico ao `283ce5d` no código; arquivos novos em `docs/` viram o commit de `feature/f2-l1-documentacao`;
- `REBUILD_LOTS_GREEN`: cada lote com pelo menos um commit, sem sobra;
- `REBUILD_FINAL_GREEN`: árvore da `develop` idêntica à área de trabalho, `git status` limpo, `main` inalterada;
- marcador `=== F3-L4 HISTORICO GREEN ===` e exit code 0.

## Etapa 3 — migração (`MIGRACAO-GITHUB.md`)

Executada pelo usuário: push das branches Gitflow no GitLab, `git clone --bare` e `git push --mirror` para o GitHub público.

## Etapa 4 — pipeline (`VERIFICACAO-CI.md`)

- `F3_L4_CI_COMMIT_GREEN`: `HEAD` local igual ao publicado;
- `F3_L4_CI_HISTORY_GREEN`: branches e tags do GitHub idênticas às do GitLab;
- `F3_L4_CI_REPOSITORY_GREEN`: repositório público, sem `.env`;
- `F3_L4_CI_PIPELINE_GREEN`: workflow CI do commit com `success` em `frontend`, `backend` e `repository`;
- marcador `=== F3-L4 GREEN ===` e exit code 0.

## Evidência recebida — etapa 1, rev1

```text
[EXECUTADO PELO USUÁRIO · 2026-09-23] gate local rev1 (trecho a partir do Compose):
  {"status":"UP","groups":["liveness","readiness"]}
  newman: requests 21 · test-scripts 21 · assertions 37 · failed 0
  F3_L4_API_COLLECTION_GREEN
  F3_L4_STATIC_GREEN
  === F3-L4 LOCAL GREEN ===
  Resultado: exit code 0
[VERIFICADO: script com `set -euo pipefail`] FRONTEND, BACKEND, WORKFLOW, REPOSITORY e DOCS precedem o Compose; o exit code 0 final só ocorre se todas as etapas terminarem com sucesso.
```

## Evidência recebida — rev2

```text
[EXECUTADO PELO USUÁRIO · 2026-09-23] etapa 1 rev2: newman 21 requisições / 37 asserções / 0 falhas; F3_L4_API_COLLECTION_GREEN; F3_L4_STATIC_GREEN; === F3-L4 LOCAL GREEN ===; exit code 0.
  aviso do Node: "[DEP0176] DeprecationWarning: fs.F_OK is deprecated" (vindo do newman 6.2.2).
[EXECUTADO PELO USUÁRIO · 2026-09-23] etapa 2 rev2:
  REBUILD_PRECONDITIONS_GREEN (SHA-256 dos 6 pacotes conferem; árvore atual 65d3690d…, 228 caminhos alterados ou novos)
  REBUILD_BACKUP_GREEN (~/Downloads/backup-kanban-20260923-134925)
  F2-L1 x 283ce5d: 65 files changed, 3683 insertions(+), todos em docs/ no trecho recebido
  PARADO + ROLLBACK; exit code 1
[EXECUTADO PELO USUÁRIO · 2026-09-23] `pnpm install --frozen-lockfile 2>&1 | grep -i deprecat` → sem saída.
```
