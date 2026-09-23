# F4 — REVISÃO DE SEGURANÇA (OWASP Top 10:2025)

Data: 2026-09-23 · Release `1.0.0` · Referência: <https://top10.owasp.org/2025/>

Cada categoria traz os controles do projeto, onde estão e como foram verificados. "Gate F4" aponta para as checagens executáveis de `VERIFICACAO-USUARIO.md` (`F4_SECURITY_GREEN`, `F4_RESPONSIBLE_AUTH_GREEN`, `F4_OBSERVABILITY_GREEN`). Os riscos residuais estão declarados; nenhum foi corrigido durante o freeze, que só admite correção mínima de falha.

## A01:2025 — Broken Access Control

- **Negar por padrão:** caminhos fora das regras explícitas caem em `denyAll`. A API e o `/graphql` exigem `ADMIN` ou `RESPONSIBLE` (`SecurityConfiguration`).
- **Regra de posse na camada de aplicação:** `Actor` é informado aos casos de uso. O responsável altera só os próprios projetos; responsáveis, secretarias e credenciais ficam só com o ADMIN (REST e GraphQL pelo mesmo caso de uso).
- **CSRF** para a SPA: cookie `XSRF-TOKEN` + cabeçalho `X-XSRF-TOKEN`.
- **Verificação:**
  - `SecurityApiIT.responsibleAuthenticatesAndManagesOnlyOwnProjects`, `ProjectServiceTest.responsible*`;
  - gate F4: projeto alheio, secretaria, credencial e mutation GraphQL → 403 `FORBIDDEN`;
  - gate F4: POST sem CSRF → 403.

## A02:2025 — Security Misconfiguration

- **Actuator:** expõe só `health` (sem detalhes) e `prometheus` (HTTP Basic técnico).
- **Erros sem mensagem interna nem stack trace:** `server.error.include-message/include-stacktrace: never`.
- **Cabeçalhos padrão do Spring Security:** `X-Content-Type-Options: nosniff` e `X-Frame-Options: DENY`.
- **Portas locais:** Prometheus e Grafana só em `127.0.0.1`; PostgreSQL não é publicado no host.
- **Sem credencial padrão:** administrador, responsável de demonstração e senhas de observabilidade vêm do ambiente, obrigatórias quando usadas.
- **Verificação:** gate F4 (endpoints do Actuator não expostos, cabeçalhos, portas, 404 sem detalhe interno); `ObservabilityIT`.
- **Residual:**
  - GraphiQL fica habilitado no `compose.yaml` (`GRAPHQL_GRAPHIQL_ENABLED: "true"`) para demonstração; o padrão da aplicação é desligado.
  - Swagger é público.
  - Em produção, desligar o GraphiQL e restringir a documentação.

## A03:2025 — Software Supply Chain Failures

- **Versões fixadas:** `pnpm install --frozen-lockfile` com versões exatas e `minimumReleaseAge: 1440` estrito (`frontend/pnpm-workspace.yaml`). Backend com versões gerenciadas pelo Spring Boot 3.5.16. Imagens Docker com tag fixa.
- **CI:** actions fixadas por SHA completo, token `contents: read`, sem `pull_request_target`, sem segredos.
- **Sem ferramenta baixada na hora no gate:** o newman foi removido no F3-L4 rev3.
- **Verificação:**
  - lockfile sem pacote marcado como depreciado;
  - `pnpm audit --prod` (informativo no gate F4; "No known vulnerabilities found" em 2026-09-23 no ambiente da IA);
  - política do workflow no gate.
- **Residual:** sem varredura de vulnerabilidades das dependências Maven (por exemplo, OWASP Dependency-Check); próximo passo.

## A04:2025 — Cryptographic Failures

- **Senhas:** `DelegatingPasswordEncoder`/bcrypt; limite de 72 bytes validado para responsável e credencial de métricas.
- **Segredos:** nenhum no repositório nem no histórico (varredura de todas as branches antes da publicação e no gate F4).
- **Verificação:** gate F4 confere que todo `password_hash` começa com `{bcrypt}`; `ResponsibleCredentialServiceTest`, `MetricsCredentialsTest`.
- **Residual:** o ambiente local roda em HTTP (`SESSION_COOKIE_SECURE=false` por padrão). Atrás de HTTPS, usar `SESSION_COOKIE_SECURE=true`.

## A05:2025 — Injection

- **Consultas:** por JPA com parâmetros (`Specification`/Criteria nos filtros); nenhuma SQL montada por concatenação de entrada.
- **GraphQL:** tipado e validado pelo schema.
- **Validação de entrada:** Bean Validation.
- **Frontend:** o React escapa a saída por padrão.
- **Verificação:** `KanbanApiIT.validatesRestInputAndReturnsStableProblemCodes`; filtros com texto livre testados em `KanbanApiIT` e no gate F4 (cenário 14).

## A06:2025 — Insecure Design

- **Regras de negócio determinísticas no domínio:** status, transições e métricas. Transições inválidas são bloqueadas com mensagem.
- **Autorização decidida na aplicação**, não no cliente. A UI só oculta ações.
- **Verificação:** `ProjectStatusTransitionTest` (17 testes), `ProjectServiceTest`.
- **Residual:** sem limite de tentativas nem bloqueio no login; próximo passo (por exemplo, rate limiting por IP e por conta).

## A07:2025 — Authentication Failures

- **Sessão:** em cookie `HttpOnly` e `SameSite=Lax`, com ID novo no login (`changeSessionId`). O logout invalida a sessão e apaga os cookies.
- **Credenciais:** as do responsável só pelo ADMIN, com mínimo de 12 caracteres. A de métricas exige no mínimo 16 caracteres; sem ela, o endpoint fica fechado.
- **Verificação:** gate F4 (flags do cookie, login do responsável, sessão encerrada → 401); `SecurityApiIT`.
- **Residual:**
  - a senha de bootstrap do ADMIN não tem tamanho mínimo validado (`SecurityAdminBootstrap` só exige que e-mail e senha venham juntos);
  - sem MFA.

## A08:2025 — Software or Data Integrity Failures

- **Pipeline:** com permissões mínimas e actions imutáveis (SHA).
- **Dependências:** lockfile congelado no CI e na imagem do frontend.
- **Entrada:** JSON só em DTOs tipados (Jackson), sem desserialização polimórfica de entrada.
- **Banco:** migrations Flyway versionadas com `validate` na subida.
- **Verificação:** gate F4 (política do workflow; migrations V1–V5 aplicadas do zero num banco novo).

## A09:2025 — Security Logging and Alerting Failures

- **Logs:** JSON ECS no Compose, sem segredos (checado no gate F4 com as cinco senhas da execução).
- **Erros:** o erro 500 registra `incidentId`, tipo e origem, sem a mensagem da exceção.
- **Métricas:** respostas 4xx (401, 403, 404 e 409) no painel do Grafana.
- **Verificação:** gate F3-L3 e gate F4 (`F4_OBSERVABILITY_GREEN`).
- **Residual:** não há regras de alerta no Prometheus nem centralização de logs.

## A10:2025 — Mishandling of Exceptional Conditions

- **Tratamento genérico:** REST devolve 500 `INTERNAL_ERROR` opaco, com `incidentId`. Exceções do framework mantêm o próprio status. GraphQL devolve `extensions.code = INTERNAL_ERROR`.
- **Erros não mascarados:** `dispatcherTypeMatchers(FORWARD, ERROR).permitAll()` impede que um erro vire 403.
- **Falha na subida (fail-fast):** configuração inválida impede a aplicação de subir (senha de métricas curta, bootstrap incompleto).
- **Verificação:** `RestExceptionHandlerTest`, `SecurityApiIT.unknownAuthenticatedRouteIsNotMaskedAsForbidden`; gate F4 (404 sem detalhe interno).
