# F2-L2 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-22

| Requisito | Implementação | Evidência | Estado |
|---|---|---|---|
| React/TS/Vite/MUI | baseline + `App.tsx` | gate frontend exit 0 | GREEN |
| Roteamento | `/login`, `/` e fallback | testes frontend | GREEN |
| Integração API | `api/auth.ts` + proxy Vite | `F2_L2_PROXY_AUTH_GREEN` | GREEN |
| Login | formulário MUI + mutation | login real pelo proxy | GREEN |
| Sessão | `GET /auth/me` | sessão real validada | GREEN |
| Logout | mutation + limpeza de cache | `F2_L2_PROXY_LOGOUT_GREEN` | GREEN |
| CSRF SPA | descritor backend + cookie XSRF | login/logout reais | GREEN |
| Loading/erros | estados explícitos | testes frontend | GREEN |
| Sem credencial persistida | nenhum storage do navegador | scan do gate | GREEN |
| Docker | frontend/backend/db | containers ativos no gate | GREEN |
