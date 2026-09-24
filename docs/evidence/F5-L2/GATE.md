# F5-L2 — GATE

Data: 2026-09-24

Estado: **CANDIDATE**, aguardando o gate local.

Escopo: `docs/governance/PLANO-CONFORMIDADE-F5.md`, lote F5-L2 (contrato de erro, confirmações, Swagger e logs), mais a promoção documental do F5-L1.

Critérios:

- `F5_L2_PRECONDITIONS_GREEN`:
  - branch `feature/f5-l2-*` que já contém o F5-L1 commitado;
  - arquivos alterados iguais à lista do pacote.
- `F5_L2_FRONTEND_GREEN`:
  - format sem mudança, lint, typecheck e `check:strict`;
  - testes com o fluxo de confirmação (33) e build;
  - sem aviso de depreciação.
- `F5_L2_BACKEND_GREEN`:
  - `mvn clean verify` sem falha e sem `[deprecation]`;
  - relatórios com `ProjectStatusTransitionTest` (24), `RestExceptionHandlerTest` (7), `ProjectServiceTest` (9) e `OpenApiContractIT` (3), todos sem falha nem teste ignorado.
- `F5_L2_STATIC_GREEN`:
  - espaços e linha final nos arquivos do lote;
  - `git diff --check`;
  - seções do README;
  - coleção Postman com 422 e confirmação;
  - F5-L1 promovido a GREEN.
- `F5_L2_DOCKER_GREEN`: projeto Compose próprio (`facilit-kanban-f5l2`) com banco novo; migrations 1 a 6.
- `F5_L2_API_GREEN`:
  - bloqueio → 422 `TRANSITION_BLOCKED` com orientação em pt-BR;
  - linha 4 sem `confirm` → 422 `CONFIRMATION_REQUIRED` (nada gravado); com `confirm` → 200;
  - o mesmo fluxo pelo GraphQL;
  - data realizada futura e ordem das datas → 422 `BUSINESS_RULE_VIOLATION`;
  - Bean Validation → 400 em pt-BR;
  - 404 e 409 em pt-BR.
- `F5_L2_OPENAPI_GREEN`: toda operação `/api/v1` com erros em `application/problem+json` e exemplo; a transição com os três exemplos de 422.
- `F5_L2_BUSINESS_LOGS_GREEN`:
  - log ECS do contêiner com `evento=projeto.transicao` e `evento=projeto.transicao.recusada` do projeto do gate;
  - nenhum e-mail do gate no log.
- Marcador final `=== F5-L2 GREEN ===` e `Resultado: exit code 0`.
