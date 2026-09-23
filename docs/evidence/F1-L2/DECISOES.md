# F1-L2 — DECISÕES

Data: 2026-09-22

## D01 — Regra de transição no domínio puro

A tabela de transições foi implementada em `ProjectStatusTransition`, sem dependência de Spring/JPA. O caso de uso `ProjectService.transition` somente coordena relógio, auditoria e persistência.

## D02 — Recalcular sempre depois dos efeitos

Cada transição aplica somente os efeitos automáticos previstos na tabela e chama `ProjectScheduleCalculator`. Se o status resultante não for o solicitado, a operação é bloqueada com mensagem de ajuste.

## D03 — Status persistido define a linha de origem

A linha da tabela é escolhida pelo status persistido do projeto; o resultado é recalculado com a data corrente. Isso permite que transições para atraso sejam reconhecidas quando o tempo passou desde o último recálculo, sem criar job ou scheduler fora de escopo.

## D04 — Listagem por status reaproveita paginação existente

REST usa `GET /api/v1/projects?status=...` e GraphQL adiciona o argumento opcional `status`. A porta de persistência recebeu `findByStatus`, implementada com `Pageable`, aproveitando o índice `ix_projects_status` já criado na V1.

## D05 — Endpoint de transição

REST: `PATCH /api/v1/projects/{id}/status` com `{ "status": "..." }`.
GraphQL: `transitionProject(id, status)`.
Ambos chamam o mesmo `ProjectService.transition`.
