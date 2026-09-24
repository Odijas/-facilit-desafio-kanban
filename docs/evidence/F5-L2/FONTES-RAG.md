# F5-L2 — FONTES RAG

Data: 2026-09-24

1. Documento do desafio (PDF):
   - Etapa 2: "Aplicar a Tabela de Transição acima (com mensagens de erro claras)"; testes com "confirmações obrigatórias".
   - Nota da tabela: "bloquear e retornar mensagem clara com dicas do que ajustar".
   - Requisitos não funcionais: "Exceções: tratamento e padronização de erros"; "Segurança: validação de inputs e logs adequados".
   - Documentação: "Swagger/OpenAPI com exemplos de requests/responses, schemas, e mensagens de erro".
2. Governança: `docs/governance/PLANO-CONFORMIDADE-F5.md` (D3, D4, lote F5-L2) e `PROMPT-EXECUTIVO-BASE-v1.1.md`.
3. Javadoc do Spring Framework 6.2.19, `HttpStatus`: `UNPROCESSABLE_ENTITY` presente e não depreciado.
   https://docs.spring.io/spring-framework/docs/6.2.x/javadoc-api/org/springframework/http/HttpStatus.html
4. RFC 9457 (Problem Details for HTTP APIs), formato já usado pelo projeto (`ProblemDetail`).
5. APIs usadas sem execução neste ambiente (Maven Central bloqueado), confirmadas pelo gate local:
   - springdoc-openapi 2.8.17 (`org.springdoc.core.customizers.OpenApiCustomizer`, `springdoc.override-with-generic-response`);
   - modelos do swagger-core (`io.swagger.v3.oas.models.*`);
   - Spring Boot 3.5 (`spring.web.locale`, `spring.web.locale-resolver`, ponte JUL→SLF4J no `LogbackLoggingSystem`);
   - Spring GraphQL (`@GraphQlExceptionHandler`);
   - Jackson 2.19 (`JsonNode.properties()`).
6. Frontend: versões congeladas do projeto (React 19.3, MUI 9.4, TanStack Query 5.102, Vitest 4.1.9, Biome 2.5.14), executadas neste ambiente.
