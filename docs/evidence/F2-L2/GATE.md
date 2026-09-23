# F2-L2 — GATE

Data: 2026-09-22

Estado: **GREEN**.

`[EXECUTADO PELO USUÁRIO · 2026-09-22]` O gate corrigido concluiu com `F2_L2_PROXY_AUTH_GREEN`, `F2_L2_PROXY_LOGOUT_GREEN`, containers `db/backend/frontend` ativos e `Resultado: exit code 0`.

`[VERIFICADO: docs/evidence/F2-L2/VERIFICACAO-USUARIO.md · 2026-09-22]` O shell usa `set -euo pipefail`; alcançar o final com exit code `0` confirma que formatação, lint, typecheck, testes, build, regressão backend, proxy real, autenticação, logout, scans estáticos e `git diff --check` não falharam.

F2-L2 é baseline promovida para F2-L3.
