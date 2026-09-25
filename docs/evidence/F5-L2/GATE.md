# F5-L2 — GATE

Data: 2026-09-24

Estado: **GREEN** (gate local e revalidação pós-correção de histórico em 2026-09-24).

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


## Evidência final

```text
[EXECUTADO PELO USUÁRIO · 2026-09-24] gate F5-L2 original:
  F5_L2_PRECONDITIONS_GREEN (63 arquivos)
  F5_L2_FRONTEND_GREEN (33 testes)
  F5_L2_BACKEND_GREEN (91 unitários + 20 integração; 0 falhas, 0 erros, 0 ignorados)
  F5_L2_STATIC_GREEN
  F5_L2_DOCKER_GREEN (migrations V1-V6)
  F5_L2_API_GREEN (18 verificações)
  F5_L2_OPENAPI_GREEN (23 operações /api/v1)
  F5_L2_BUSINESS_LOGS_GREEN (11 eventos; sem e-mail nem senha)
  === F5-L2 GREEN ===
  Resultado: exit code 0

[VERIFICADO: commits 8177c8e, 87fa002, a91cc32, 009b432 · 2026-09-24] o erro de shell na criação do primeiro commit granular fez 43 arquivos de backend serem absorvidos pelo commit docs(api), totalizando 45 arquivos em 8177c8e; a branch corretiva reverteu esse agrupamento e recriou 43 + 2 arquivos nos commits corretos, sem reescrever o histórico publicado.

[EXECUTADO PELO USUÁRIO · 2026-09-24] equivalência pós-correção:
  BASE_TREE=122f2449c0bd7f588ea43ee57e795389a058b8c3
  FIX_TREE=122f2449c0bd7f588ea43ee57e795389a058b8c3
  ARVORE_EQUIVALENTE_GREEN

[EXECUTADO PELO USUÁRIO · 2026-09-24] revalidação do gate em árvore reconstruída sobre o F5-L1:
  arquivos_reconstruidos=63
  todos os marcadores F5_L2_*_GREEN
  === F5-L2 GREEN ===
  Resultado: exit code 0
  GATE_EXIT=0
  F5_L2_REVALIDADO_GREEN
```

[EXECUTADO PELO USUÁRIO · 2026-09-24] fechamento da correção:
  `f7e17b5` registra `SAIDA-GATE-HISTORICO.txt`;
  `7a28996` faz merge `--no-ff` de `fix/f5-l2-historico-granular` em `develop`;
  `develop`, `origin/develop` e `gitlab/develop` = `7a28996ef2c28189ae84d4c8498c12c70fcb364f`.

Conclusão: **F5-L2 GREEN — FECHADO**, funcionalmente equivalente antes/depois da correção auditável da granularidade do histórico.
