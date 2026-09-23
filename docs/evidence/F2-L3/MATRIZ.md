# F2-L3 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-22

| Requisito | Implementação | Teste/verificação | Estado |
|---|---|---|---|
| Login | `features/auth/LoginPage.tsx` + API F2-L2 | `App.test.tsx` + proxy real | GREEN |
| Proteção de rota | `features/auth/ProtectedHome.tsx` | `App.test.tsx` | GREEN |
| Quadro Kanban | `features/kanban/KanbanBoard.tsx` + `KanbanColumn.tsx` | `KanbanBoard.test.tsx` | GREEN |
| Drag-and-drop | `KanbanColumn.tsx` + orquestração no board | dragStart/drop unitário + API runtime | GREEN |
| Listagem por status | projetos agrupados pelo status real | render das 4 colunas | GREEN |
| CRUD essencial | board + diálogos focados | criação/edição/exclusão | GREEN |
| Responsáveis | `ResponsibleDialog.tsx` + query/mutation | criação e associação | GREEN |
| Erro de transição | `ProblemDetail.detail` → Alert | teste de bloqueio + runtime 400 | GREEN |
| Atualização consistente | `invalidateQueries` após mutações | testes + gate runtime | GREEN |
| Loading/erro | `LoadingState`, auth e board | testes existentes | GREEN |
| Segurança | mutações reutilizam CSRF | `api/kanban.test.ts` + proxy | GREEN |
| SRP/composição | `App.tsx` somente roteamento; features separadas | regressão `App.test.tsx` + leitura estrutural | GREEN |
| Fronteira API/framework | callbacks adaptadores para query/mutation | 3 testes RED observados + scan estático | GREEN |
| Qualidade frontend | Biome write/lint/typecheck/test/build | gate local | GREEN |
