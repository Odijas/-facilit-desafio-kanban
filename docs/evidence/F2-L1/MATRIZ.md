# F2-L1 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-22

| Requisito | Implementação | Verificação prevista | Estado |
|---|---|---|---|
| Spring Security | `spring-boot-starter-security` + `SecurityConfiguration` | `mvn clean verify` + Compose | GREEN |
| Autenticação | `DatabaseUserDetailsService` + `DaoAuthenticationProvider` | login válido/inválido | GREEN |
| Login | `POST /api/v1/auth/login` | `SecurityApiIT` + curl real | GREEN |
| Logout | Spring Security logout `/api/v1/auth/logout` | sessão antiga retorna 401 | GREEN |
| Autorização | `ROLE_ADMIN` em `/api/v1/**` e `/graphql` | anônimo 401 + autenticado 2xx | GREEN |
| Senha segura | `DelegatingPasswordEncoder`/bcrypt | consulta DB: hash != plaintext e prefixo `{bcrypt}` | GREEN |
| Sessão/cookie | `HttpSessionSecurityContextRepository`; `HttpOnly`, `SameSite=Lax` | headers `Set-Cookie` + `/me` | GREEN |
| CSRF SPA | `CookieCsrfTokenRepository` + `SpaCsrfTokenRequestHandler` | POST sem header 403; com token permitido | GREEN |
| Erro seguro | 401/403 genéricos com `ApiErrorCode` | respostas reais sem detalhe de credencial | GREEN |
| Sem credencial/PII em logs | nenhum logger em auth/security | scan estático + logs runtime sem credenciais testadas | GREEN |
| Persistência usuário | migration V4 `app_users` | Flyway `4:true`, tabela/índice e bootstrap | GREEN |
| Regressão F1 | `KanbanApiIT` autenticado | 4 ITs existentes + negócio REST/GraphQL | GREEN |
| Frontend F1 | sem alteração funcional no F2-L1 | lint/typecheck/test/build | GREEN |
