# F4 — GATE

Data: 2026-09-23

Estado: **GREEN**. `[EXECUTADO PELO USUÁRIO · 2026-09-23]` O rev1 ficou RED por falso positivo do próprio gate (checagem de porta do banco); a rev2 foi executada integralmente e terminou com `=== F4 GREEN ===` e exit code 0. A verificação final da release também terminou com `=== F4 RELEASE GREEN ===` e exit code 0.

Escopo: freeze (`PROMPT-EXECUTIVO-KANBAN-v1.0.md`, F4). Não há feature nova. O único ajuste de código é a versão `1.0.0` (pom, Dockerfile, package.json), que faz parte da release.

Critérios do gate de freeze (`VERIFICACAO-USUARIO.md`, na `release/1.0.0` publicada):

- `F4_PRECONDITIONS_GREEN`: branch `release/1.0.0` validada; versão `1.0.0` consistente;
- `F4_FRONTEND_GREEN`:
  - format sem alterar arquivos, lint, typecheck, `check:strict`, testes e build;
  - nenhum aviso de depreciação;
  - `pnpm audit --prod` informativo;
- `F4_BACKEND_GREEN`:
  - `mvn clean verify`, com totais de Surefire e Failsafe sem falhas nem erros;
  - jar `kanban-1.0.0.jar`;
  - nenhum `[deprecation]` do compilador;
- `F4_STATIC_GREEN`:
  - actionlint e política do workflow;
  - espaços, arquivos sensíveis e segredos em todo o histórico;
  - Conventional Commits desde `283ce5d`;
  - documentação (README, AI_USAGE, ADR, auditoria, revisão de segurança, diagramas, links, coleção);
  - `git diff --check`;
- `F4_DOCKER_CLEAN_DB_GREEN`: projeto Compose próprio com volumes novos; migrations V1–V5 aplicadas do zero, sem falha; health só com `status`/`groups`;
- `F4_UI_SWAGGER_GREEN`: index do frontend, proxy `/api`, Swagger UI e OpenAPI com as rotas principais;
- `F4_API_GREEN`: os 21 cenários REST e GraphQL da coleção;
- `F4_RESPONSIBLE_AUTH_GREEN`: login do responsável de demonstração, CRUD do próprio projeto, ataques a projeto alheio, secretaria, credencial e GraphQL → 403;
- `F4_SECURITY_GREEN`:
  - cookie `HttpOnly` e `SameSite=Lax`;
  - `nosniff` e `X-Frame-Options: DENY`;
  - CSRF obrigatório;
  - 404 sem detalhe interno;
  - senhas bcrypt;
  - métricas autenticadas;
  - Actuator mínimo;
  - portas locais e banco não publicado;
- `F4_OBSERVABILITY_GREEN`: métricas com histograma, alvo do Prometheus `up`, datasource e painel do Grafana, logs ECS sem as cinco senhas da execução;
- `F4_CI_GREEN`: CI do commit da release com sucesso em `frontend`, `backend` e `repository`;
- marcador `=== F4 GREEN ===` e exit code 0.

Critérios da verificação da release (`VERIFICACAO-RELEASE.md`, após o merge):

- `F4_RELEASE_REFS_GREEN`: `main`, `release/1.0.0`, `develop` e tag anotada `v1.0.0` validados no GitHub;
- `F4_RELEASE_CI_GREEN`: CI da `main` com `frontend`, `backend` e `repository` em `success`;
- `F4_RELEASE_PAGE_GREEN`: repositório GitHub público, `main` como branch padrão e README da tag acessível;
- marcador `=== F4 RELEASE GREEN ===` e exit code 0.


Evidência executada: [`SAIDA-GATE.txt`](SAIDA-GATE.txt) e [`SAIDA-RELEASE.txt`](SAIDA-RELEASE.txt).
