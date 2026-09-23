# F1-L2 — GATE

Data: 2026-09-22

Estado: **GREEN**.

Evidência executada pelo usuário em 2026-09-22:

```text
{"status":"UP"}
=== PREPARA RESPONSÁVEL ===
=== REST: TRANSIÇÃO LEGÍTIMA ===
=== REST: LISTAGEM POR STATUS ===
=== REST: BLOQUEIO COM MENSAGEM CLARA ===
=== REST: NÃO PODE MARCAR ATRASADO ANTES DO INÍCIO ===
=== GRAPHQL: TRANSIÇÃO + FILTRO ===
=== FINALIZA TRANSIÇÃO REST ===
=== CLEANUP ===
=== REGRESSÃO HEALTH + FRONTEND ===
{"status":"UP"}
{"data":{"health":{"status":"UP"}}}
HTTP 200
=== GIT + CONTAINERS ===
backend: Up
PostgreSQL: Up (healthy)
frontend: Up
=== F1-L2 GREEN ===
Resultado: exit code 0
```

O gate filho usa `set -euo pipefail`; alcançar o marcador final com exit code `0` comprova que todas as verificações anteriores do script terminaram com sucesso.
