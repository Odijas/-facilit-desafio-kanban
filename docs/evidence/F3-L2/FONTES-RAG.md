# F3-L2 — FONTES RAG

Data: 2026-09-23

1. Código do projeto e versões instaladas: Spring Boot 3.5.16, Spring Security 6.5.11 (confirmado pela saída do gate do usuário), Spring Data JPA 3.5.13, TanStack Query 5.102.8, MUI 9.4.0, TypeScript 5.9.3, Biome 2.5.14, Vitest 4.1.9.
2. Documentação oficial:
   - Spring Security 6.5.11 — Authorize HttpServletRequests, "All Dispatches Are Authorized" e `dispatcherTypeMatchers`: https://docs.spring.io/spring-security/reference/6.5/servlet/authorization/authorize-http-requests.html
   - Spring Framework — `ErrorResponse` (`getStatusCode`, `getBody`, implementações): https://docs.spring.io/spring-framework/docs/6.2.x/javadoc-api/org/springframework/web/ErrorResponse.html
   - Spring for GraphQL — argumento `java.security.Principal` em controllers anotados: https://docs.spring.io/spring-graphql/reference/controllers.html
   - Spring for GraphQL — propagação de contexto em WebMvc: https://docs.spring.io/spring-graphql/reference/request-execution.html
   - OWASP Top 10 2025 — A01 Broken Access Control e A07 Authentication Failures.
3. Execução local: javac (JDK 21) sobre domínio/aplicação, sonda de casos de uso, graphql-js 16.14.2, Biome/TypeScript/Vitest/Vite.
