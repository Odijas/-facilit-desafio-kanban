# F5-P1 — FONTES RAG

Data: 2026-09-25

1. PDF do desafio, "Documentação": "Swagger/OpenAPI com exemplos de requests/responses, schemas, e mensagens de erro" (obrigatório #40).
2. `ADERENCIA-V2.0.0.md` (auditoria da tag `v2.0.0`): #40 Parcial. Exemplos explícitos só nos creates de Projeto e Responsável e em 3 requests.
3. `docs/governance/PLANO-CORRECAO-RELEASE-2.0.1.md`: lote F5-P1 e decisões 1 e 2 do usuário (2026-09-25, 7h50).
4. springdoc-openapi v2.8.17 (GitHub, `pom.xml`): usa swagger-core **2.2.47**.
5. swagger-core v2.2.47 (GitHub):
   - `swagger-annotations`: atributos de `@Parameter` (`example`, `hidden`), `@ArraySchema` (`arraySchema`, `schema`, `uniqueItems`) e `@Schema` (`example`, `accessMode`). As 42 classes do módulo foram baixadas e compiladas aqui.
   - `ModelResolver.resolveExample`: o texto do `example` é lido como JSON. `"0"` vira número, `"false"` vira booleano, `"[…]"` vira lista, e texto que não é JSON fica string.
   - `ModelResolver` (aplicação do `@ArraySchema`): pela leitura, `arraySchema` aplicaria o exemplo ao próprio schema da lista. **Refutado na prática** pelo gate local (ver `DECISOES.md`, listas).
   - swagger-models 2.2.47: `Schema.getItems()`, `Schema.getProperties()`, `Schema.setExample(Object)` e `Components.getSchemas()`, as assinaturas usadas pelo `ApiListExampleDocumentation`.
   - `ParameterProcessor`: `@Parameter(example)` vira `parameter.example`.
6. springdoc-openapi v2.8.17 (GitHub):
   - `OpenApiCustomizer.customise(OpenAPI)`: ponto de extensão aplicado depois da geração, o mesmo do `ApiErrorDocumentation`;
   - `AbstractRequestService.PARAM_TYPES_TO_IGNORE` inclui `Principal`, e `SpringDocSecurityConfiguration` ignora `Authentication`;
   - `CsrfToken` não está entre os ignorados, então o parâmetro do `GET /auth/csrf` recebe `@Parameter(hidden = true)`.
7. jackson-databind 2.19.2 (GitHub, `JsonNode.java`): `properties()` devolve conjunto vazio fora de objeto, e `has(campo)` é `get(campo) != null`. É a base do teste de contrato.
8. PostgreSQL, semântica do `LIKE`: `%` e `_` são curingas. O escape padrão é a barra invertida, e `ESCAPE` define o caractere explicitamente.
   - `[CONHECIMENTO, não consultado nesta sessão]`.
   - A prova é o IT novo e a busca na API real do gate, contra o PostgreSQL 18.6.
