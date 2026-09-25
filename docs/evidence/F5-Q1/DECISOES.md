# F5-Q1 — DECISÕES

Data: 2026-09-25

## Escopo

`[VERIFICADO]` O F5-Q1 altera somente versão 2.0.2, E2–E5, D1, testes e evidências do lote. README, ADR 0002, CHANGELOG e AI_USAGE pertencem ao F5-Q2 e permanecem intocados.

## E3 — exemplos de erro por operação

`[VERIFICADO]` Os textos foram derivados do código real:

- projeto inexistente: `ProjectService.get`;
- responsável inexistente: `ResponsibleService.get`;
- secretaria inexistente: `SecretariatService.get`;
- credencial inexistente no DELETE: `ResponsibleCredentialService.revoke`;
- escrita de responsável/secretaria: `Actor.requireAdmin` — `Apenas o administrador pode realizar esta operação.`;
- escrita de projeto por responsável sem vínculo: `ProjectService.requireMembership`;
- 403 do filtro Spring Security: `Acesso negado.`.

`[VERIFICADO]` O plano menciona `Acesso negado.` nas rotas somente ADMIN, mas o código real autoriza ADMIN/RESPONSIBLE no filtro e bloqueia essas escritas no serviço. Pela precedência do BASE, o exemplo documenta a resposta real do serviço, não o texto genérico do filtro.

## E4/E5

`[VERIFICADO]` `mismatchMessage` passou a orientar a data ausente sem concatenar valor nulo. Na linha 11, a orientação depende do status recalculado: `NOT_STARTED` pede `actualStart`; `OVERDUE` pede ajuste de `plannedEnd`.

## D1

`[VERIFICADO]` REST e GraphQL de projetos já usam `ProjectService.search`. Foram removidos somente `ProjectService.list/listByStatus` e a cadeia exclusiva correspondente. Índices e migrations não foram tocados.

## Reconciliação de commits

`[VERIFICADO]` O plano exige simultaneamente commits que compilem sozinhos e lista `refactor(projetos)` antes de `test(projetos)` com a migração dos testes. Remover as assinaturas antes de adaptar os testes quebraria compilação. Pelo BASE §6, manutenção de teste quebrado pela alteração é parte da própria mudança. Portanto, no fechamento, a migração dos testes existentes deve entrar no mesmo commit `refactor(projetos)`; o commit `test(projetos)` fica para o novo teste E2. Isso preserva a regra mais forte: cada commit compilável.

## E3 — correção do RED 1

`[EXECUTADO: saída fornecida pelo usuário]` o primeiro gate falhou no novo teste de contrato porque o helper exigia um exemplo 400 nomeado `VALIDATION_ERROR`. `[VERIFICADO]` `POST /api/v1/projects` já declara um exemplo 400 próprio no controller e `ApiErrorDocumentation.add` preserva respostas existentes. A menor correção correta é tornar a leitura do teste/gate agnóstica à representação OpenAPI (`example`, `examples` nomeado ou `examples` não nomeado), mantendo a mesma checagem semântica do campo contra o schema. Nenhuma produção foi alterada nesta correção.
