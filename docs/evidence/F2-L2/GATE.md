# F2-L2 — GATE

Data: 2026-09-22

Estado: **CANDIDATE CORRIGIDO / aguardando reexecução local**.

Critérios para GREEN:

- frontend `pnpm format`, `pnpm lint`, `pnpm typecheck`, `pnpm test` e `pnpm build` sem falhas;
- suíte backend permanece GREEN;
- login via frontend usa o contrato CSRF real do backend e cria sessão válida;
- rota `/` exige sessão válida e redireciona anônimo para `/login`;
- logout invalida a sessão e retorna para `/login`;
- estados de loading e erro possuem cobertura unitária;
- nenhum segredo é persistido no navegador;
- frontend continua acessível via Docker Compose;
- `git diff --check` limpo.

Promoção somente após `VERIFICACAO-USUARIO.md` terminar com `=== F2-L2 GREEN ===` e `Resultado: exit code 0`.
