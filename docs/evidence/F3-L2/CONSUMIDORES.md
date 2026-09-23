# F3-L2 — CONSUMIDORES

Data: 2026-09-23

Busca executada antes e depois das mudanças de assinatura:

```text
$ grep -rn ".create(|.update(|.transition(|.delete(|new ResponsibleService(|new SecretariatService(|new ProjectService(|new ResponsiblePersistenceAdapter(|AuthResponse.from(" backend/src/main backend/src/test
ApplicationBeans.java: new ResponsibleService / new SecretariatService / new ProjectService
ResponsibleDemoBootstrap.java: responsibleService.create
ProjectRestController.java: create / update / transition / delete
ResponsibleRestController.java: create / update / delete (+ credenciais)
SecretariatRestController.java: create / update / delete
ProjectGraphQlController.java, ResponsibleGraphQlController.java, SecretariatGraphQlController.java: mutations
AuthRestController.java: AuthResponse.from (login e /me)
ProjectServiceTest.java, ResponsibleServiceTest.java, SecretariatServiceTest.java: todas as chamadas atualizadas com Actor
ProjectStatusTransitionTest.java: usa ProjectStatusTransition (domínio), assinatura não alterada
```

- `SecurityUserJpaEntity`: construtor de 7 argumentos preservado; único consumidor (`SecurityAdminBootstrap`) sem alteração.
- `ResponsiblePersistenceAdapter`: construído apenas pelo Spring; nenhum teste o instancia.
- Contrato `AuthResponse` (+ `responsibleId`): consumidores `frontend/src/api/auth.ts` (parser), `App.test.tsx`, `api/auth.test.ts` — atualizados.
- `KanbanBoard` (+ prop `user`) e `SecretariatPanel` (+ prop `canManage`): consumidores `DashboardPage.tsx` e respectivos testes — atualizados; `App.test.tsx` usa dublês desses componentes.
- `KanbanColumn` (+ `canManageProject`) e `ProjectDialog` (+ `requiredResponsibleId`): consumidor único `KanbanBoard.tsx`.
