# F5-P1 — CONSUMIDORES

Data: 2026-09-25

- **Anotações OpenAPI** (`@Schema`, `@Parameter`, `@ApiResponse`) e **`ApiListExampleDocumentation`** (`OpenApiCustomizer`):
  - só mudam o documento `/api-docs` e o Swagger UI;
  - nenhum efeito em serialização JSON, validação ou rotas: o Jackson e o Bean Validation não leem essas anotações;
  - o frontend e a coleção Postman não mudam.
- **`@Parameter(hidden = true)` no `CsrfToken`:** muda só o documento. O Spring continua injetando o token.
- **`ApiExamples`:** constantes de compilação usadas só em anotações. Nenhum código de produção lê esses valores.
- **Filtro de texto** (`ProjectPersistenceAdapter.matching`):
  - Chamado por `ProjectService.search`, que é usado por `ProjectRestController.list` e `ProjectGraphQlController.projects`.
  - Buscas sem `%`, `_` ou barra invertida têm o mesmo resultado de antes: o `ESCAPE` só muda o sentido desses três caracteres.
  - O frontend envia o texto digitado. Quem digitar `%` passa a achar o caractere literal, e não mais "qualquer coisa".
- **Versão 2.0.1:**
  - o `Dockerfile` copia `kanban-2.0.1.jar`, o nome que o Maven gera com a versão nova;
  - o `package.json` só muda o campo `version`, e o lockfile do pnpm não guarda a versão do próprio projeto.
- **Contagens:**
  - unitários/BDD continuam com 25 relatórios e 174 testes;
  - integração passa de 12 relatórios e 49 testes para 12 e 51 (`OpenApiContractIT` de 4 para 5; `ProjectPersistenceAdapterIT` de 5 para 6).
