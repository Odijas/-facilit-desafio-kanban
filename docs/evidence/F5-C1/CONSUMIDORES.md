# F5-C1 — CONSUMIDORES

Data: 2026-09-24

- **`backend/Dockerfile`:**
  - `compose.yaml` (serviço `backend`, `build: ./backend`);
  - o freeze do F5-L5 (`up --build`, e a pré-condição que procura `kanban-2.0.0.jar`, que não mudou);
  - o novo passo do CI.
  - O artefato é o mesmo jar: só deixa de rodar o `jacoco:check` dentro da imagem.
- **`.github/workflows/ci.yml`:**
  - política do freeze: 3 jobs, `permissions: contents: read`, actions por SHA, `persist-credentials: false`, nenhum segredo. O passo novo é `run:` e não usa action nem segredo.
  - O `ubuntu-24.04` do GitHub já traz Docker `[HIPÓTESE: imagem padrão do runner; confirmada pelo CI do push]`.
- **`springdoc.swagger-ui.csrf.enabled`:**
  - muda só o `swagger-initializer.js` servido pelo springdoc;
  - não muda o `/api-docs` (contrato OpenAPI, `OpenApiContractIT`), a segurança da API, o frontend nem a coleção.
- **`OpenApiContractIT`:** passa de 3 para 4 testes; o total de integração passa de 48 para 49.
- **`README.md`:** seção "Como rodar" (roteiro do Swagger e do GraphiQL) e estado dos lotes. As seções exigidas pelo freeze continuam presentes.
