# F2-L2 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-22

| Requisito | Implementação | Verificação prevista | Estado |
|---|---|---|---|
| React/TS/Vite/MUI | baseline preservada + `App.tsx` | lint/typecheck/build | CANDIDATE |
| Roteamento | `/login`, `/` e fallback por History API | testes de navegação | CANDIDATE |
| Integração API | `api/auth.ts` + `api/health.ts` | testes + proxy real | CANDIDATE |
| Login | formulário MUI + mutation | teste de interação + login real pelo proxy | CANDIDATE |
| Sessão | `GET /auth/me` em React Query | usuário autenticado/anônimo | CANDIDATE |
| Logout | mutation + limpeza do cache | teste unitário + logout real | CANDIDATE |
| CSRF SPA | descritor backend + cookie `XSRF-TOKEN` | `auth.test.ts` + proxy real | CANDIDATE |
| Loading | `LoadingState`, login/logout pendentes | testes unitários | CANDIDATE |
| Erros | login inválido, sessão indisponível, backend indisponível | testes unitários | CANDIDATE |
| Estrutura visual | login + app bar + painel Material UI | build + inspeção manual | CANDIDATE |
| Sem credencial persistida | nenhum storage de navegador | scan estático | CANDIDATE |
