# F2-L2 — FONTES RAG

Data: 2026-09-22

1. `[VERIFICADO]` `frontend/package.json`: React 19.3.0, Material UI 9.4.0, React Query 5.102.8, Vite 8.3.0, Vitest 4.1.9, Testing Library 16.3.2/User Event 14.6.1.
2. `[VERIFICADO]` `AuthRestController.java`: contratos reais `/csrf`, `/login`, `/me`.
3. `[VERIFICADO]` `SecurityConfiguration.java`: logout, cookies, CSRF e autorização reais.
4. `[VERIFICADO]` `SecurityApiIT.java`: sessão, CSRF, login inválido e logout já exercitados no backend.
5. React 19 — `useSyncExternalStore`: https://react.dev/reference/react/useSyncExternalStore
6. TanStack Query v5 — queries/mutations: https://tanstack.com/query/v5/docs/framework/react/overview
7. MDN — History API: https://developer.mozilla.org/docs/Web/API/History_API
8. `[EXECUTADO PELO USUÁRIO · 2026-09-22]` gate F2-L1 terminou com exit code 0; F2-L1 é baseline GREEN.
9. `[EXECUTADO PELO USUÁRIO · 2026-09-22]` TypeScript/MUI 9.4.0: `alignItems`/`fontWeight` como props diretas falharam no typecheck.
10. Material UI v9 — remoção de system props diretas; usar `sx`: https://mui.com/material-ui/migration/upgrade-to-v9/
11. Testing Library — Vitest sem globals requer `cleanup()` explícito em `afterEach`: https://testing-library.com/docs/react-testing-library/setup/
12. Vitest — `globals` default `false`: https://vitest.dev/config/globals
13. `[EXECUTADO PELO USUÁRIO · 2026-09-22]` `@types/react` 19.3.0 reportou `FormEvent` como deprecated.
