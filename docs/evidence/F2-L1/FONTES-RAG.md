# F2-L1 — FONTES RAG

Data: 2026-09-22

1. **Código prevalente do projeto**, lido integralmente conforme `LEITURA.md`.
2. **Prompt Executivo Kanban v1.0**: F2-L1 exige Spring Security, autenticação, login/logout, autorização, senha segura, sessão/cookie, CSRF para SPA, erros seguros e ausência de credenciais/PII em logs.
3. **Spring Boot 3.5.16 — Managed Dependency Coordinates**: a linha 3.5.16 gerencia Spring Security 6.5.11: https://docs.spring.io/spring-boot/3.5/appendix/dependency-versions/coordinates.html
4. **Spring Security 6.5.11 — Authentication Persistence and Session Management**: autenticação criada em controller customizado deve ser salva explicitamente em `SecurityContextRepository`: https://docs.spring.io/spring-security/reference/6.5/servlet/authentication/session-management.html
5. **Spring Security 6.5.11 — DaoAuthenticationProvider**: autenticação usuário/senha usa `UserDetailsService` e `PasswordEncoder`: https://docs.spring.io/spring-security/reference/6.5/servlet/authentication/passwords/dao-authentication-provider.html
6. **Spring Security 6.5 — Password Storage**: `PasswordEncoderFactories.createDelegatingPasswordEncoder()` usa formato delegado e bcrypt para novas codificações: https://docs.spring.io/spring-security/reference/6.5/features/authentication/password-storage.html
7. **Spring Security 6.5 — CSRF para SPA**: `CookieCsrfTokenRepository.withHttpOnlyFalse()` + handler SPA baseado em header cru e XOR/BREACH: https://docs.spring.io/spring-security/reference/6.5/servlet/exploits/csrf.html
8. **Spring Security 6.5.11 — `CookieCsrfTokenRepository` API**: confirma `withHttpOnlyFalse()`, `setCookiePath` e que `saveToken(null, ...)` remove o token: https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/web/csrf/CookieCsrfTokenRepository.html
9. **Spring Boot 3.5.16 — Common Application Properties / Servlet Web**: `server.servlet.session.cookie.http-only`, `secure` e `same-site` são propriedades oficiais de cookie de sessão: https://docs.spring.io/spring-boot/3.5/appendix/application-properties/
10. **Execução F1-L3 do usuário**: gate integral finalizou com exit code 0; F1-L3/F1 são baseline GREEN deste candidato.
