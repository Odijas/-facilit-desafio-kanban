# F2-L1 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-22

| Requisito | Implementação | Verificação prevista | Estado |
|---|---|---|---|
| Spring Security | `spring-boot-starter-security` + `SecurityConfiguration` | `mvn clean verify` + Compose | CANDIDATE |
| Autenticação | `DatabaseUserDetailsService` + `DaoAuthenticationProvider` | login válido/inválido | CANDIDATE |
| Login | `POST /api/v1/auth/login` | `SecurityApiIT` + curl real | CANDIDATE |
| Logout | Spring Security logout `/api/v1/auth/logout` | sessão antiga retorna 401 | CANDIDATE |
| Autorização | `ROLE_ADMIN` em `/api/v1/**` e `/graphql` | anônimo 401 + autenticado 2xx | CANDIDATE |
| Senha segura | `DelegatingPasswordEncoder`/bcrypt | consulta DB: hash != plaintext e prefixo `{bcrypt}` | CANDIDATE |
| Sessão/cookie | `HttpSessionSecurityContextRepository`; `HttpOnly`, `SameSite=Lax` | headers `Set-Cookie` + `/me` | CANDIDATE |
| CSRF SPA | `CookieCsrfTokenRepository` + `SpaCsrfTokenRequestHandler` | POST sem header 403; com token permitido | CANDIDATE |
| Erro seguro | 401/403 genéricos com `ApiErrorCode` | respostas reais sem detalhe de credencial | CANDIDATE |
| Sem credencial/PII em logs | nenhum logger em auth/security | scan estático + logs runtime sem credenciais testadas | CANDIDATE |
| Persistência usuário | migration V4 `app_users` | Flyway `4:true`, tabela/índice e bootstrap | CANDIDATE |
| Regressão F1 | `KanbanApiIT` autenticado | 4 ITs existentes + negócio REST/GraphQL | CANDIDATE |
| Frontend F1 | sem alteração funcional no F2-L1 | lint/typecheck/test/build | CANDIDATE |
