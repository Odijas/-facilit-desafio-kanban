# F5-L2 — RISCOS E LACUNAS

Data: 2026-09-24

- `[DESCONHECIDO]` compilação dos tratadores REST/GraphQL, `ApiErrorDocumentation` (springdoc/swagger-models), `RestExceptionHandlerTest` e `OpenApiContractIT`, porque o Maven Central está bloqueado neste ambiente. Domínio e aplicação compilam aqui com `-Xlint:all -Werror`. Resolve: `F5_L2_BACKEND_GREEN`.
- `[HIPÓTESE]` assinaturas do swagger-models usadas: `Schema.addProperty`, `StringSchema._enum(List)`, `ArraySchema.items`, `Schema.$ref`, `MediaType.addExamples`, `Example.value/summary`, `ApiResponses.addApiResponse`, `PathItem.readOperationsMap`. Todas são da API pública estável do swagger-core 2.2. Um erro aparece na compilação do gate.
- `[HIPÓTESE]` `springdoc.override-with-generic-response=false` impede o springdoc de colar respostas deduzidas do `@RestControllerAdvice` em todas as operações. Se a propriedade não se comportar assim, `OpenApiContractIT` continua válido: ele só exige a presença das respostas.
- `[HIPÓTESE]` a ponte JUL→SLF4J leva o `BusinessLog` ao log ECS do contêiner. O gate lê o log real (`F5_L2_BUSINESS_LOGS_GREEN`).
- `[VERIFICADO]` quebra de contrato em relação à v1.0.0:
  - bloqueio de transição sai de 400 `INVALID_REQUEST` para 422 `TRANSITION_BLOCKED`;
  - `confirm` passa a ser exigido nas linhas 4, 11 e 12.
  - O frontend, a coleção e o gate foram atualizados juntos. O número da versão (D6) fica para o F5-L5, com decisão do usuário.
- `[VERIFICADO]` o gate do F4 (histórico) espera 400 no bloqueio; ele não se aplica à árvore do F5.
- `[VERIFICADO]` fora do lote, conforme o plano (F5-L3): testes de controller com mocks, transação por caso de uso, `@Version` e a tabela de transição testada pela API em IT.
