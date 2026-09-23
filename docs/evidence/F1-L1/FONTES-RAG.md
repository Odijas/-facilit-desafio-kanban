# F1-L1 — FONTES RAG

Data: 2026-09-22

## Fontes oficiais consultadas

1. Spring Boot 3.5.16 — Managed Dependency Coordinates
   - https://docs.spring.io/spring-boot/3.5/appendix/dependency-versions/coordinates.html
   - Confirma Spring Data JPA 3.5.13, Spring GraphQL 1.4.6 e Spring Framework 6.2.19 gerenciados pelo Boot 3.5.16.

2. Spring Boot 3.5 — SQL Databases / Spring Data JPA
   - https://docs.spring.io/spring-boot/3.5/reference/data/sql.html
   - Confirma entity scanning e repositories Spring Data JPA, inclusive paginação com `Page`/`Pageable`.

3. Spring GraphQL 1.4 — Annotated Controllers
   - https://docs.spring.io/spring-graphql/reference/1.4/controllers.html
   - Confirma `@QueryMapping`, `@MutationMapping` e binding de `@Argument` em objetos de entrada.

4. Spring GraphQL 1.4.6 — GraphQlTester API
   - https://docs.spring.io/spring-graphql/docs/1.4.x/api/org/springframework/graphql/test/tester/GraphQlTester.html
   - Referência da ferramenta de teste GraphQL já presente no projeto e reservada para os testes de contrato da F1-L3.

5. Spring GraphQL — Request Execution / Exceptions
   - https://docs.spring.io/spring-graphql/reference/request-execution.html
   - Confirma classificação e resolução de erros de data fetchers.

6. Spring Framework 6.2 — Spring MVC validation
   - https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-controller/ann-validation.html
   - Confirma Bean Validation em `@RequestBody` com `@Valid`.

## Hierarquia aplicada

[VERIFICADO] O `pom.xml` da baseline usa Spring Boot 3.5.16 e os starters de Web, GraphQL, Data JPA e Validation. As versões transitivas acima foram confirmadas pela tabela oficial de dependências gerenciadas do Boot 3.5.16.

[DESCONHECIDO] O dependency tree efetivamente resolvido no ambiente do usuário será reconfirmado no gate com `mvn dependency:tree`, pois este ambiente não possui Maven nem Java 25.

7. Spring Framework 6.2 — Composing Java-based Configurations / `@Import`
   - https://docs.spring.io/spring-framework/reference/6.2/core/beans/java/composing-configuration-classes.html
   - Confirma que `@Import` suporta referência a classes componentes regulares, permitindo registrar apenas `HealthQuery` no contexto de teste sem importar toda a composition root.
