# F2-L2 — DECISÕES

Data: 2026-09-22

## D01 — Sessão remota em React Query

`[VERIFICADO: frontend/package.json · 2026-09-22]` React Query 5.102.8 já faz parte da baseline. O estado da sessão é tratado como estado remoto com a chave `auth/me`, evitando introduzir Zustand ou outro armazenamento global sem necessidade comprovada.

## D02 — Duas rotas com History API nativa

`[VERIFICADO: docs/governance/PROMPT-EXECUTIVO-KANBAN-v1.0.md:249-262 · 2026-09-22]` F2-L2 exige roteamento, mas não prescreve biblioteca. Como este lote possui apenas `/login` e `/`, foi usada a History API nativa com `useSyncExternalStore`, sem nova dependência ou alteração do lockfile. Se F2-L3 exigir uma árvore de rotas maior, a decisão deve passar por novo gate.

## D03 — CSRF dirigido pelo backend

`[VERIFICADO: backend/src/main/java/br/com/facilit/kanban/delivery/auth/AuthRestController.java:42-45 · 2026-09-22]` O frontend consulta `/api/v1/auth/csrf`, usa `headerName` e `cookieName` retornados pelo backend e lê somente o cookie CSRF não sensível. O token de sessão continua no cookie `HttpOnly` e não é acessado por JavaScript.

## D04 — Sem persistência de credenciais

`[VERIFICADO: frontend/src/api/auth.ts · 2026-09-22]` E-mail/senha existem apenas no estado do formulário e são enviados ao endpoint de login. Não há `localStorage`, `sessionStorage` ou armazenamento manual de `JSESSIONID`.
