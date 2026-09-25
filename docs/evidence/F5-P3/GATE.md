# F5-P3 — GATE DE FREEZE E RELEASE 2.0.1

Data: 2026-09-25

Estado: **GREEN** (freeze em 2026-09-25; saída em `SAIDA-GATE.txt`). A release e a verificação pública vêm depois do freeze e ficam registradas em `SAIDA-RELEASE.txt`, na `develop`.

Escopo: lote F5-P3 de `docs/governance/PLANO-CORRECAO-RELEASE-2.0.1.md`. Cobre o freeze da `hotfix/2.0.1`, a release `v2.0.1` (Gitflow de hotfix), a verificação pública e o pacote da auditoria final.

Critérios do freeze (`VERIFICACAO-USUARIO.md`):

- `F5_P3_PRECONDITIONS_GREEN`:
  - branch `hotfix/2.0.1` limpa e igual à publicada;
  - versão 2.0.1 em pom, Dockerfile e package.json;
  - `v2.0.0` como ancestral e tag `v2.0.1` ainda inexistente;
  - F5-P1 e F5-P2 GREEN com saída registrada.
- `F5_P3_FRONTEND_GREEN`: format, lint, typecheck, strict, test e build, sem aviso de depreciação e sem arquivo alterado pelo format.
- `F5_P3_BACKEND_GREEN` e `F5_P3_JACOCO_BDD_GREEN`:
  - `mvn clean verify` com `kanban-2.0.1.jar`;
  - 25 classes e 174 testes unitários/BDD; 12 classes e 51 de integração; nenhuma falha, erro ou teste ignorado;
  - JaCoCo ≥ 95%; BDD com 12 cenários.
- `F5_P3_STATIC_GREEN`:
  - actionlint e política do workflow;
  - espaços, arquivos e segredos no histórico;
  - Conventional Commits;
  - seções do README, AI_USAGE, ADR, auditoria e revisão de segurança;
  - CHANGELOG com a `[2.0.1]` no topo;
  - links, sem frase desatualizada, coleção sem credencial.
- `F5_P3_DOCKER_CLEAN_DB_GREEN`: Compose próprio com banco novo, migrations V1–V7 e health.
- `F5_P3_UI_SWAGGER_GREEN`:
  - UI e proxy;
  - Swagger UI;
  - OpenAPI com exemplos em todas as 26 operações REST;
  - CSRF no Swagger UI.
- `F5_P3_API_GREEN`: 26 verificações de REST e GraphQL com ADMIN, inclusive a busca literal (`text=_`).
- `F5_P3_RESPONSIBLE_AUTH_GREEN`, `F5_P3_SECURITY_GREEN` e `F5_P3_OBSERVABILITY_GREEN`: iguais aos do freeze da 2.0.0.
- `F5_P3_CI_GREEN`: CI do commit da `hotfix/2.0.1` com os 3 jobs em sucesso.
- Marcador final `=== F5-P3 GREEN ===` e `Resultado: exit code 0`.

Critérios da release (`VERIFICACAO-RELEASE.md`):

- `F5_P3_RELEASE_REFS_GREEN`:
  - `main` = merge da `hotfix/2.0.1`, com a mesma árvore;
  - tag anotada `v2.0.1` na `main`;
  - `develop` contém a hotfix.
- `F5_P3_RELEASE_GITHUB_GREEN`: `main`, `develop`, `hotfix/2.0.1` e `v2.0.1` publicadas e iguais às locais.
- `F5_P3_RELEASE_CI_GREEN`: CI da `main` verde.
- `F5_P3_RELEASE_PAGE_GREEN`: repositório público; a tag traz o README, o CHANGELOG `[2.0.1]` e este gate no estado da release.
