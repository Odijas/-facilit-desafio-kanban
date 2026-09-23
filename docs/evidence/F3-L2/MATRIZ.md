# F3-L2 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-23

| Requisito | Implementação | Teste/verificação | Estado |
|---|---|---|---|
| Papel RESPONSIBLE vinculado ao responsável | V5 + `SecurityUserJpaEntity.responsibleId` | `SecurityApiIT` + gate | GREEN |
| Credenciais só pelo ADMIN | `ResponsibleCredentialService` + REST/GraphQL | `ResponsibleCredentialServiceTest`, sonda, IT, gate | GREEN |
| Política de senha (12 caracteres, 72 bytes) | `ResponsibleCredentialService` | unitário, sonda, IT 400, gate 400 | GREEN |
| Login do responsável | `DatabaseUserDetailsService` (papel), `/auth/login` | IT + gate `DEMO_BOOTSTRAP` | GREEN |
| `/auth/me` com `responsibleId` | `AuthRestController` | IT + gate | GREEN |
| Usuários de demonstração sem credencial versionada | `ResponsibleDemoBootstrap` + `.env.example` vazio | gate `DEMO_BOOTSTRAP` | GREEN |
| E-mail de login sincronizado e sem colisão | `ResponsiblePersistenceAdapter.save` + `ResponsibleService.update` | unitário, IT (sync + 409), sonda | GREEN |
| Responsável só altera os próprios projetos | `Actor` + `ProjectService` | `ProjectServiceTest`, sonda, IT, gate | GREEN |
| Responsável vê o quadro inteiro | leitura sem restrição de posse | IT + gate `LEGIT` | GREEN |
| Responsáveis e secretarias só pelo ADMIN | `Actor.requireAdmin` nos serviços | unitários, IT, gate | GREEN |
| GraphQL com a mesma regra | controllers → mesmos casos de uso | IT + gate (FORBIDDEN) | GREEN |
| Erro 500 opaco com código estável | `RestExceptionHandler`, `GraphQlErrorHandler` | `RestExceptionHandlerTest` | GREEN |
| Erro não mascarado como 403 | `dispatcherTypeMatchers(FORWARD, ERROR)` | IT 404 + gate `SAFE_ERROR` | GREEN |
| UI por papel | `permissions.ts`, `KanbanBoard`, `KanbanColumn`, `ProjectDialog`, `SecretariatPanel`, `DashboardPage` | 3 testes RED→GREEN | GREEN |
| Promoção documental do F3-L1 | `docs/evidence/F3-L1` | evidência do gate do usuário | GREEN |
