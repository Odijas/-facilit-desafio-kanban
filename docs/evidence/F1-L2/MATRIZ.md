# F1-L2 — MATRIZ requisito → implementação → teste → evidência

| Requisito | Implementação | Teste/verificação | Estado |
|---|---|---|---|
| Listagem por status | `ProjectRepository.findByStatus`, REST `?status=`, GraphQL `status` | `ProjectServiceTest` + gate real | GREEN |
| Transição entre estados | `ProjectStatusTransition` + `ProjectService.transition` | `ProjectStatusTransitionTest` | GREEN |
| Ações automáticas | início/fim real aplicados conforme tabela | testes linha a linha | GREEN |
| Bloqueios | validação específica + comparação final recalculada | testes de erro + HTTP 400 | GREEN |
| Mensagens claras | mensagens específicas por transição problemática | AssertJ + gate REST | GREEN |
| Recálculo pós-efeitos | `ProjectScheduleCalculator.calculate` | testes + probe | GREEN |
| Status solicitado = resultante | comparação obrigatória antes de persistir | testes + probe | GREEN |
| REST | `PATCH /projects/{id}/status`, filtro `status` | curls do gate | GREEN |
| GraphQL | `transitionProject`, filtro `projects(status:)`, enum | chamadas GraphQL do gate | GREEN |
| Cobertura linha a linha | 17 testes em `ProjectStatusTransitionTest` cobrindo 12 relações + variantes | `mvn clean verify` | GREEN |
