# F5-C1 — MATRIZ LACUNA → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-24

| Lacuna (`ADERENCIA-FINAL.md`) | Requisito (PDF) | Implementação | Teste | Evidência | Estado |
|---|---|---|---|---|---|
| 1 — build Docker do backend | "containerizado com Docker… docker-compose orquestrando aplicação e banco" (obrigatório #2) | `backend/Dockerfile` com `-Djacoco.skip=true`; passo `docker build` no job `backend` do CI | build da imagem anterior (RED) e da nova (GREEN) no gate; CI do push | `F5_C1_DOCKER_IMAGE_GREEN`, `F5_C1_DOCKER_GREEN`, `F5_C1_CI_GREEN` | CANDIDATE |
| 3 — Swagger UI sem CSRF | "como acessar Swagger" (README) e Swagger utilizável (obrigatório #40) | `springdoc.swagger-ui.csrf.enabled: true`; roteiro no README (Swagger e GraphiQL) | `OpenApiContractIT.swaggerUiSendsTheCsrfTokenFromTheCookie`; fluxo cookie → cabeçalho contra a API real no gate; conferência no navegador | `F5_C1_SWAGGER_GREEN` e seção 3 da verificação | CANDIDATE |
