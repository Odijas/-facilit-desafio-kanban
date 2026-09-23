# F1-L3 — FONTES RAG

Data: 2026-09-22

1. **Código prevalente do projeto**, lido integralmente conforme `LEITURA.md`.
2. **Desafio Técnico Backend — Kanban**: Swagger/OpenAPI, validação de inputs, padronização de erros, paginação, índices, testes de unidade/integração/API e GraphQL como diferencial.
3. **Spring Boot 3.5.16 — Managed Dependency Coordinates**: Spring GraphQL 1.4.6, Spring Data JPA 3.5.13 e Testcontainers 1.21.4 são versões gerenciadas pela baseline: https://docs.spring.io/spring-boot/3.5/appendix/dependency-versions/coordinates.html
4. **Spring GraphQL 1.4 — Annotated Controllers / Validation**: `@Argument @Valid` usa Bean Validation e falhas geram `ConstraintViolationException`: https://docs.spring.io/spring-graphql/reference/1.4/controllers.html
5. **Spring Boot 3.5.16 — `@ServiceConnection`**: integração oficial entre containers de teste e connection details: https://docs.spring.io/spring-boot/3.5/api/java/org/springframework/boot/testcontainers/service/connection/ServiceConnection.html
6. **Testcontainers 1.21.4**: versão gerenciada pela baseline; construção explícita por `DockerImageName` evita APIs legadas ambíguas.
7. **springdoc-openapi**: matriz oficial Spring Boot 3.5.x → springdoc 2.8.x: https://springdoc.org/v2/
8. **Maven Central**: `springdoc-openapi-starter-webmvc-ui:2.8.17` publicado: https://central.sonatype.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui/2.8.17
9. **springdoc modules**: propriedades oficiais `springdoc.api-docs.path` e Swagger UI: https://springdoc.org/modules.html
10. **Maven Failsafe**: integração em `integration-test`/`verify`; `skipITs` pula apenas a execução dos testes de integração, mantendo compilação: https://maven.apache.org/surefire/maven-failsafe-plugin/integration-test-mojo.html
