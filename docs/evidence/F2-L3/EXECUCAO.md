# F2-L3 — EXECUÇÃO

Data: 2026-09-22

## Baseline

```text
[EXECUTADO PELO USUÁRIO · 2026-09-22] F2_L2_PROXY_AUTH_GREEN
[EXECUTADO PELO USUÁRIO · 2026-09-22] F2_L2_PROXY_LOGOUT_GREEN
[EXECUTADO PELO USUÁRIO · 2026-09-22] Resultado: exit code 0
[CONCLUSÃO] F2-L2 promovido para GREEN.
```

## Executado neste ambiente

```text
[EXECUTADO] extração e leitura integral da baseline F2-L2 corrigida.
[EXECUTADO] mapeamento dos consumidores de autenticação/CSRF/Kanban.
[EXECUTADO] implementação de cliente REST tipado, quadro, drag-and-drop, CRUD essencial, responsáveis e testes.
[EXECUTADO] parser sintático TypeScript/TSX em todos os arquivos frontend: TS_SYNTAX_GREEN.
[EXECUTADO] busca de consumidores após a alteração.
[EXECUTADO] consulta às fontes oficiais React/MDN/TanStack/MUI.
```

## Limitação de execução

```text
[EXECUTADO] `corepack pnpm --version` tentou obter pnpm 12.5.1 e falhou com `getaddrinfo EAI_AGAIN registry.npmjs.org`.
[DESCONHECIDO] Biome 2.5.14, TypeScript 5.9.3 com node_modules reais, Vitest 4.1.9, Vite 8.3.0, Maven/Docker e fluxo runtime aguardam o gate do usuário.
```

## Correção após gate RED

```text
[EXECUTADO PELO USUÁRIO · 2026-09-22] `pnpm format` com `biome format --write .` formatou 7 arquivos, mas `pnpm lint` bloqueou `KanbanBoard.test.tsx` por `assist/source/organizeImports`; gate encerrou com exit code 1.
[VERIFICADO] a causa é o script `format` limitado ao formatter, que não executava o safe fix de organização de imports.
[CORRIGIDO] `frontend/package.json` agora define `format` como `biome check --write .`; o import conhecido foi organizado conforme o diff seguro reportado pelo Biome.
[CORRIGIDO] o gate mantém `pnpm format` como primeiro passo e só depois executa `pnpm lint`, `pnpm typecheck`, `pnpm test` e `pnpm build`.
```

## Correção após segundo gate RED

```text
[EXECUTADO PELO USUÁRIO · 2026-09-22] `biome check --write .` concluiu; `biome check .` ficou GREEN; `tsc --noEmit` ficou GREEN.
[EXECUTADO PELO USUÁRIO · 2026-09-22] Vitest: 18 testes passaram e 3 falharam em `KanbanBoard.test.tsx`; o gate encerrou com exit code 1 antes do build.
[VERIFICADO] as três falhas tinham o mesmo mecanismo: `createProject`, `deleteProject` e `createResponsible` eram passados diretamente como `mutationFn` e receberam um segundo `MutationFunctionContext` do React Query.
[CORRIGIDO] `KanbanBoard.tsx` agora adapta explicitamente create/delete/responsible; update/transition já possuíam adaptadores. As `queryFn` não foram alteradas porque não participaram da falha observada.
[CORRIGIDO] nenhum contrato público de `api/kanban.ts` ou teste foi relaxado para aceitar argumentos do framework.
[DESCONHECIDO] suíte Vitest/build após esta correção dependem do novo gate local.
```


## Correção após terceiro apontamento — testes + estrutura

```text
[EXECUTADO PELO USUÁRIO · 2026-09-22] `biome check --write .` corrigiu 7 arquivos; `biome check .` e `tsc --noEmit` ficaram GREEN; Vitest ficou 18 pass / 3 fail.
[VERIFICADO] os 3 fails eram createProject/deleteProject/createResponsible observando um segundo `MutationFunctionContext` fornecido pelo React Query.
[VERIFICADO] o usuário também apontou concentração indevida de navegação, autenticação, dashboard e loading dentro de `App.tsx`, incompatível com o objetivo de SRP/Clean Code do projeto.
[CORRIGIDO] callbacks de query/mutation passam por adaptadores explícitos antes de chamar `api/auth.ts`, `api/health.ts` e `api/kanban.ts`.
[CORRIGIDO] `App.tsx` foi reduzido a composition root de navegação; autenticação, dashboard, navegação, loading e Kanban foram separados por responsabilidade.
[CORRIGIDO] `KanbanBoard` deixou de conter os componentes de coluna e diálogos; permaneceu responsável pela orquestração do feature.
[CORRIGIDO] `KanbanBoard.tsx` e `KanbanBoard.test.tsx` foram movidos da raiz de `src` para `features/kanban`; aplicação sobre uma workspace já extraída exige remover os dois caminhos antigos antes de extrair o snapshot, pois `tar` não representa exclusões.
[EXECUTADO] parser TypeScript/TSX: TS_SYNTAX_GREEN em 22 arquivos.
[EXECUTADO] validação de imports relativos: RELATIVE_IMPORTS_GREEN.
[EXECUTADO] AST scan de `any`, type assertions `as` e non-null assertions: STRICT_ESCAPE_SCAN_GREEN em 22 arquivos.
[EXECUTADO] scan de callbacks diretas de React Query para API: FRAMEWORK_CALLBACK_ADAPTERS_GREEN.
[DESCONHECIDO] Biome real, typecheck semântico, Vitest, Vite build e runtime integrado desta revisão aguardam o gate do usuário; node_modules não estão presentes neste ambiente e o registry npm está inacessível.
```


## Verificação estática final da correção estrutural

```text
[EXECUTADO · 2026-09-22] TS_SYNTAX_GREEN 22 files.
[EXECUTADO · 2026-09-22] STRICT_ESCAPE_SCAN_GREEN 22 files.
[EXECUTADO · 2026-09-22] RELATIVE_IMPORTS_GREEN.
[EXECUTADO · 2026-09-22] FRAMEWORK_CALLBACK_ADAPTERS_GREEN.
[EXECUTADO · 2026-09-22] STRUCTURE_GREEN.
[EXECUTADO · 2026-09-22] GATE_BASH_SYNTAX_GREEN.
[EXECUTADO · 2026-09-22] tentativa de obter o binário oficial Biome 2.5.14 diretamente do GitHub falhou por `Could not resolve host: github.com`; portanto não há alegação de Biome real executado neste ambiente.
[DESCONHECIDO] lint/typecheck/test/build reais desta revisão estrutural permanecem dependentes do gate local.
```
