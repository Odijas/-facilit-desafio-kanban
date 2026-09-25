# F5-P1 — DECISÕES

Data: 2026-09-25

Escopo: lote F5-P1 de `docs/governance/PLANO-CORRECAO-RELEASE-2.0.1.md`. O usuário aprovou o plano e as decisões 1 e 2 (2026-09-25, 7h50) e pediu a execução ("sim", 7h52).

## Exemplos no Swagger (obrigatório #40)

- `[VERIFICADO: tag v2.0.0]` Das 26 operações REST, só os creates de Projeto e Responsável tinham exemplo explícito de request/resposta.
  - `@Schema(example)` só em `ProjectRequest`, `ProjectStatusRequest` e `ResponsibleRequest`.
  - Nenhum `@RequestParam` e nenhum `{id}` tinha exemplo.
- **Estratégia: exemplo no schema, não em cada operação.** O Swagger UI monta o exemplo de toda operação a partir do schema, então `@Schema(example)` em cada campo dos records cobre request, resposta, listas e páginas sem repetir JSON em 26 lugares.
  - O `@ExampleObject` fica só onde já existia (creates) e no health.
  - Os creates ganharam `createdAt`/`updatedAt`, que a resposta real tem.
- **Valores fictícios em um lugar só:** `delivery/common/ApiExamples`, com os mesmos ids nos três recursos (secretaria `1000…`, responsável `2000…`, projeto `3000…`). Assim uma resposta aponta para os ids das outras.
- **Exemplos coerentes com as regras:**
  - O `ProjectResponse` de exemplo é um projeto concluído antes do prazo, com as mesmas datas do `ProjectRequest`: Concluído, 0 dia de atraso e 0%, valores conferidos no F5-C2.
  - O prazo de exemplo (`plannedEnd` 30/09 numa janela que começa em 25/09) tem `daysUntilDeadline` 5.
- **Listas (rev2).**
  - **Na primeira versão:** `@ArraySchema(arraySchema = @Schema(example = "[…]"))` punha o exemplo na própria lista (`responsibleIds`, `authorities`).
  - **O que o gate local mostrou (2026-09-25, 8h25):** o springdoc 2.8.17 real não publicou esse exemplo. O `OpenApiContractIT` acusou 7 operações com `responsibleIds[]`/`authorities[]` sem exemplo; os campos escalares passaram.
  - **Correção:** `ApiListExampleDocumentation`, um `OpenApiCustomizer` no mesmo padrão do `ApiErrorDocumentation`, põe o exemplo no **item** das três listas (`ProjectRequest.responsibleIds`, `ProjectResponse.responsibleIds` e `AuthResponse.authorities`) depois da geração.
  - O exemplo escalar no item é o mesmo mecanismo dos outros campos, e o Swagger UI monta a lista a partir dele.
  - Se uma lista sumir do schema, a geração do `/api-docs` falha em vez de o exemplo sumir calado.
  - Os `@ArraySchema` saíram, e `ProjectRequest.java` voltou a ser igual ao da v2.0.0.
  - Por que o springdoc descarta o exemplo do `arraySchema` nesse caso segue `[DESCONHECIDO]`; a correção não depende disso.
- **`HealthStatus` fica sem anotação.** Ele é da camada de aplicação, que não depende do OpenAPI. O exemplo `{"status": "UP"}` vai no `@ApiResponse` do `HealthRestController`, e o gate confere que nenhum arquivo de `application/` importa `io.swagger`.
- **Parâmetro `CsrfToken` do `GET /auth/csrf`:**
  - o springdoc não o ignora (fonte 6 de `FONTES-RAG.md`) e o publicaria como parâmetro de consulta;
  - o token vem do filtro de CSRF, não do cliente, então recebe `@Parameter(hidden = true)`;
  - o gate confere que a operação não tem parâmetro.
- **Senha nos exemplos:** um valor ilustrativo (`senha-de-exemplo-2026`), `WRITE_ONLY` no `LoginRequest` e no `ResponsibleCredentialsRequest`. O administrador real vem do `.env` (README).
- **Teste de contrato** (`OpenApiContractIT.everyRestOperationHasExamplesForParametersRequestBodyAndSuccessResponses`):
  - Percorre o `/api-docs` real e exige que:
    - as operações em `/api/v1/` sejam exatamente 26;
    - todo parâmetro, todo `requestBody` e toda resposta 2xx com corpo tenham exemplo.
  - Conta como exemplificado:
    - schema com `example` próprio;
    - referência, lista ou objeto cujas partes estejam todas exemplificadas, que é como o Swagger UI monta o exemplo.
  - A falha lista cada campo sem exemplo.
  - O gate aplica a mesma regra em Python, contra a API no Docker.

## Filtro de texto literal (decisão 1 do usuário)

- `[VERIFICADO: ProjectPersistenceAdapter.java:252 na v2.0.0]` O texto entrava cru no padrão `"%" + text + "%"`.
  - `100%` casava com "Meta 1000", e `_` casava com qualquer nome.
  - O PostgreSQL trata a barra invertida como escape padrão, então `c:\d` virava `c:d`.
- **Correção:**
  - `containsPattern` escapa a barra invertida, `%` e `_`;
  - o predicado usa `like(…, …, LIKE_ESCAPE)` com escape explícito;
  - vale para REST e GraphQL, que usam o mesmo `ProjectFilter` e o mesmo adaptador.
- Toca o diferencial D4 (filtros) só porque o comportamento estava errado.

## Versão

- `2.0.1` em `backend/pom.xml`, `backend/Dockerfile` (`kanban-2.0.1.jar`) e `frontend/package.json`. É o primeiro commit do lote, como no Gitflow de hotfix.
- O `@OpenAPIDefinition` continua `version = "v1"`: é a versão do contrato da API, não a do artefato.

## Desvios em relação ao plano

| Plano | Feito | Motivo |
|---|---|---|
| `HealthStatus` anotado na camada `application` | exemplo no `@ApiResponse` do controller | a camada de aplicação não depende do OpenAPI |
| CI do P1 no próprio gate | CI conferido no gate do F5-P2 | o push acontece depois do GREEN, como nos lotes F5-C |
| Conventional Commits no gate do P1 | conferidos no gate do F5-P2 (`git log v2.0.0..`) | os commits do lote nascem depois do GREEN |
| — | `CsrfToken` escondido do OpenAPI | achado ao montar a regra dos parâmetros; sem ele, o `GET /auth/csrf` teria um parâmetro de consulta sem sentido para o cliente |
