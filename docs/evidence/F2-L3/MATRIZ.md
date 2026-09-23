# F2-L3 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-22

| Requisito | Implementação | Teste/verificação | Estado |
|---|---|---|---|
| Login | `features/auth/LoginPage.tsx` + API F2-L2 | `App.test.tsx` + proxy real | CANDIDATE |
| Proteção de rota | `features/auth/ProtectedHome.tsx` | `App.test.tsx` | CANDIDATE |
| Quadro Kanban | `features/kanban/KanbanBoard.tsx` + `KanbanColumn.tsx` | `KanbanBoard.test.tsx` | CANDIDATE |
| Drag-and-drop | `KanbanColumn.tsx` + orquestração no board | dragStart/drop unitário + API runtime | CANDIDATE |
| Listagem por status | projetos agrupados pelo status real | render das 4 colunas | CANDIDATE |
| CRUD essencial | board + diálogos focados | criação/edição/exclusão | CANDIDATE |
| Responsáveis | `ResponsibleDialog.tsx` + query/mutation | criação e associação | CANDIDATE |
| Erro de transição | `ProblemDetail.detail` → Alert | teste de bloqueio + runtime 400 | CANDIDATE |
| Atualização consistente | `invalidateQueries` após mutações | testes + gate runtime | CANDIDATE |
| Loading/erro | `LoadingState`, auth e board | testes existentes | CANDIDATE |
| Segurança | mutações reutilizam CSRF | `api/kanban.test.ts` + proxy | CANDIDATE |
| SRP/composição | `App.tsx` somente roteamento; features separadas | regressão `App.test.tsx` + leitura estrutural | CANDIDATE |
| Fronteira API/framework | callbacks adaptadores para query/mutation | 3 testes RED observados + scan estático | CANDIDATE |
| Qualidade frontend | Biome write/lint/typecheck/test/build | gate local | CANDIDATE |
