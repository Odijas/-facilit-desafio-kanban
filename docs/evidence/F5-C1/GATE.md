# F5-C1 — GATE

Data: 2026-09-24

Estado: **CANDIDATE**, aguardando o gate local.

Escopo: `docs/governance/PLANO-CORRECAO-RELEASE-2.0.0.md`, lote F5-C1 (entrega executável), na `release/2.0.0`.

Critérios:

- `F5_C1_PRECONDITIONS_GREEN`:
  - branch `bugfix/2.0.0-*` que parte da `release/2.0.0` (`59051d0`);
  - arquivos alterados iguais à lista do pacote;
  - frontend intocado.
- `F5_C1_BACKEND_GREEN`:
  - `mvn clean verify` sem falha, sem `[deprecation]` e sem autoanexo do Mockito;
  - unitários/BDD: 24 relatórios e 165 testes; integração: 12 relatórios e 49 testes; sem falha, erro ou teste ignorado;
  - `OpenApiContractIT` com 4 testes;
  - JaCoCo (unitários + integração) ≥ 95% de linhas.
- `F5_C1_STATIC_GREEN`:
  - espaços e linha final nos arquivos do lote e `git diff --check`;
  - actionlint;
  - política do workflow do freeze: 3 jobs, SHA, permissões e sem segredo;
  - Dockerfile com `-DskipITs -Djacoco.skip=true verify` e `kanban-2.0.0.jar`;
  - passo `docker build` no job `backend`;
  - `springdoc.swagger-ui.csrf.enabled: true`;
  - roteiro no README e seções exigidas pelo freeze.
- `F5_C1_DOCKER_IMAGE_GREEN`:
  - `docker compose build --no-cache backend` com o Dockerfile novo;
  - o Dockerfile anterior é construído para registrar o RED: falha em `Coverage checks have not been met`. Se passar, fica registrado como informativo (falso positivo da auditoria).
- `F5_C1_DOCKER_GREEN`: projeto Compose próprio (`facilit-kanban-f5c1`) com banco novo; migrations 1 a 7; backend UP com a imagem nova.
- `F5_C1_SWAGGER_GREEN`:
  - Swagger UI e `swagger-initializer.js` publicados, com o `requestInterceptor` de CSRF;
  - login sem cabeçalho → 403;
  - roteiro do README com o cabeçalho tirado do cookie: csrf → login → csrf → escrita (201) → exclusão (204).
- Marcador final `=== F5-C1 GREEN ===` e `Resultado: exit code 0`.

Depois do commit:

- conferência no navegador ("Swagger OK");
- `F5_C1_CI_GREEN`: CI da `release/2.0.0` com os 3 jobs e o passo `Imagem Docker do backend` em sucesso.
