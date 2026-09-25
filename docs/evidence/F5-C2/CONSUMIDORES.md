# F5-C2 — CONSUMIDORES

Data: 2026-09-24

- **DTOs REST com `@Size`** (`ProjectRequest`, `ResponsibleRequest`, `SecretariatRequest`):
  - `ProjectRestController`, `ResponsibleRestController`, `SecretariatRestController` (`@Valid @RequestBody`);
  - OpenAPI (`maxLength`/`maxItems`).
  - Entradas dentro dos limites não mudam de comportamento.
  - O frontend não envia textos acima de 200 caracteres em uso normal. Se enviar, recebe 400 com a mensagem, e os diálogos já mostram `detail`/`violations`.
- **Inputs GraphQL com `@Size`** (`ProjectGraphQlInput`, `ResponsibleGraphQlInput`, `SecretariatGraphQlInput`): mutations de criação e edição; `GraphQlErrorHandler` → `VALIDATION_ERROR`.
- **`ProjectFilter` com limite de texto:**
  - chamadores `ProjectRestController.list`, `ProjectGraphQlController.projects`, testes de serviço e de controller;
  - o frontend envia o texto do filtro digitado pelo usuário e recebe 400 com a mensagem acima de 100 caracteres.
- **Sem limite novo:** `ResponsibleCredentialsRequest` (a senha já tem limite de 72 bytes), `ProjectStatusRequest` (enum), filtros por id e data.
- **BDD:** só o `.feature` e o `StatusTransitionSteps`; os 12 cenários continuam, cada um com 4 passos.
- **Contagens:** unitários/BDD passam de 24 relatórios e 165 testes para 25 e 174 (`ProjectFilterTest` novo com 3; +3, +1, +1 e +1 nos testes de controller). A integração continua com 12 relatórios e 49 testes.
