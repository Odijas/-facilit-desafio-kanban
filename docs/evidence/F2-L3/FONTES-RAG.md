# F2-L3 — FONTES RAG

Data: 2026-09-22

1. `[VERIFICADO]` `docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md` — RAG, leitura integral, TDD, Clean Architecture pragmática, SOLID, diff mínimo e anti-overengineering.
2. `[VERIFICADO]` `docs/governance/PROMPT-EXECUTIVO-KANBAN-v1.0.md:230-295` — escopo e gate F2, inclusive Kanban UI e testes frontend.
3. `[VERIFICADO]` `frontend/package.json:18-39` — React 19.3.0, Material UI 9.4.0, React Query 5.102.8, Vitest 4.1.9 e Testing Library.
4. `[EXECUTADO PELO USUÁRIO]` gate F2-L3 — React Query entregou `MutationFunctionContext` como segundo argumento às funções passadas diretamente como `mutationFn`.
5. `[VERIFICADO]` TanStack Query v5 `UseMutationOptions.mutationFn` — assinatura `(variables, context) => Promise<TData>`: https://tanstack.com/query/latest/docs/framework/react/reference/interfaces/UseMutationOptions
6. `[VERIFICADO]` React — Thinking in React: decomposição em hierarquia de componentes e ownership do estado no ancestral/componente responsável: https://react.dev/learn/thinking-in-react
7. `[VERIFICADO]` React — eventos comuns, incluindo `onDragStart`, `onDragOver` e `onDrop`: https://react.dev/reference/react-dom/components/common
8. `[VERIFICADO]` MDN — HTML Drag and Drop API e `DataTransfer`: https://developer.mozilla.org/en-US/docs/Web/API/HTML_Drag_and_Drop_API
9. `[VERIFICADO]` Material UI v9 — system props removidas e uso de `sx`: https://mui.com/material-ui/migration/upgrade-to-v9/
10. `[VERIFICADO]` Biome CLI — `biome check --write` aplica formatter e safe fixes/actions: https://biomejs.dev/reference/cli/
