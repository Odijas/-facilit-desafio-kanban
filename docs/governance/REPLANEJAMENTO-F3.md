# Replanejamento da F3 — decisões registradas

Data: 2026-09-23 · Estado: **APROVADO** — todas as decisões registradas (P1, P2 e P3 resolvidas em 2026-09-23) · Subordinado ao PROMPT-EXECUTIVO-BASE v1.1 e ao PROMPT-EXECUTIVO-KANBAN v1.0.

---

## 1. Decisões do usuário

| # | Questão | Decisão (2026-09-23) |
|---|---|---|
| 1 | Emenda: novo F3-L2 e renumeração dos lotes seguintes | **Sim** |
| 2 | Escopo de acesso do responsável | **CRUD somente dos projetos em que é responsável; responsáveis, secretarias e credenciais exclusivamente com o ADMIN** |
| 3 | Abordagem de CI/CD | **Migrar para o GitHub e usar só GitHub Actions, no F3-L4** (P3) |
| 4 | GitLab em uso | **gitlab.com** (origem até a migração no F3-L4) |
| P1 | Usuários padrão e e-mail de login | **Bootstrap por variáveis de ambiente, sem credencial versionada; e-mail copiado para `app_users` e sincronizado** |
| P2 | Visibilidade do responsável | **Vê o quadro inteiro; altera só os próprios projetos** |

---

## 2. Estado da F3

| Lote | Conteúdo | Estado |
|---|---|---|
| F3-L1 | Funcionalidades diferenciais: indicadores, CRUD de Secretaria, filtros avançados | **GREEN** em 2026-09-23 (gate rev4, exit code 0) |
| **F3-L2** | **Autenticação do responsável + erro seguro** | **GREEN** em 2026-09-23 (gate local, exit code 0) |
| F3-L3 | Observabilidade (antigo F3-L2) | **GREEN** em 2026-09-23 (gate rev2, exit code 0) |
| F3-L4 | Engenharia de entrega (antigo F3-L3), com migração para o GitHub | **GREEN** em 2026-09-23 (gate local, histórico, migração e pipeline 35894739171 verde) |

A promoção documental do F3-L1 (GATE, MATRIZ, EXECUCAO, RISCOS, README) entra no pacote do F3-L2, como ocorreu com o F2-L3 dentro do pacote do F3-L1.

---

## 3. Justificativa do encaixe

- `[VERIFICADO: KANBAN v1.0, F2-L1]` a autenticação entregue no F2-L1 cobre só o ADMIN (`V4__create_security_users.sql:9`: `role IN ('ADMIN')`); F2-L1 está GREEN e não é reaberto.
- `[VERIFICADO: desafio, Diferenciais]` "Serviço de autenticação do responsável" é diferencial do desafio.
- `[VERIFICADO: KANBAN v1.0, Critério de prioridade]` autenticação (5) vem antes de indicadores (7), observabilidade (10), CI/CD (11) e IA/RAG (12).
- `[VERIFICADO: SecurityConfiguration.java:80–83]` o erro seguro entra no mesmo lote: mesmo arquivo, mesma fronteira HTTP e mesma suíte (`SecurityApiIT`).

---

## 4. F3-L2 — escopo aprovado

### 4.1 Autenticação do responsável

1. **Modelo (P1)** — migration V5: `app_users.responsible_id UUID NULL`, FK para `responsibles(id)` com `ON DELETE CASCADE` e `UNIQUE`; `role IN ('ADMIN','RESPONSIBLE')`; `CHECK ((role = 'RESPONSIBLE') = (responsible_id IS NOT NULL))`.
2. **Credenciais (só ADMIN)** — `PUT /api/v1/responsibles/{id}/credentials` (define ou redefine a senha, 204) e `DELETE /api/v1/responsibles/{id}/credentials` (revoga). Mutation GraphQL sobre o mesmo caso de uso.
3. **Login (P1)** — o mesmo `POST /api/v1/auth/login`; `DatabaseUserDetailsService` passa a carregar `ROLE_RESPONSIBLE`; senha com o `DelegatingPasswordEncoder`/bcrypt existente e tamanho mínimo validado. O e-mail de login fica em `app_users` (cópia do e-mail do responsável); o índice único `uq_app_users_email_lower` `[VERIFICADO: V4__create_security_users.sql:12]` garante no banco a unicidade entre ADMIN e RESPONSIBLE. Quando o ADMIN altera o e-mail de um responsável com credencial, a credencial é atualizada na mesma transação; conflito de e-mail retorna 409.
3a. **Usuários de demonstração (P1)** — sem credencial padrão versionada `[VERIFICADO: OWASP A07:2025 — "Do not ship or deploy with any default credentials, particularly for admin users."]`. O ADMIN continua pelo bootstrap existente (`APP_ADMIN_EMAIL`/`APP_ADMIN_PASSWORD`, `SecurityAdminBootstrap.java:35–61`). O responsável de demonstração ganha bootstrap opcional no mesmo padrão (`APP_DEMO_RESPONSIBLE_*`: nome, e-mail, cargo, senha), idempotente, criando o responsável e a credencial quando ausentes. `.env.example` mantém os campos vazios; o README explica como preenchê-los.
4. **Autorização (decisão 2)**
   - ADMIN: tudo, inclusive responsáveis, secretarias e credenciais.
   - RESPONSIBLE: lê o quadro inteiro, indicadores, responsáveis e secretarias (P2); cria projeto incluindo-se obrigatoriamente como responsável; edita, transiciona e exclui **somente** projetos em que é responsável.
   - RESPONSIBLE não cria, edita nem exclui responsáveis, secretarias ou credenciais.
   - Fundamento: OWASP A01:2025 — negar por padrão e exigir posse do registro `[VERIFICADO]`; como o login usa o e-mail do responsável, editar outro responsável permitiria tomar a conta dele.
   - Fundamento de P2 `[VERIFICADO: desafio]`: quadro único por status, indicadores globais e filtro "por responsável" só fazem sentido com visão do quadro inteiro.
5. **Onde fica a regra de posse** — camada de aplicação (regra de negócio), com o ator informado ao caso de uso; exige mapear os consumidores REST e GraphQL antes de alterar assinaturas.
6. **Frontend** — ações conforme `authorities` de `/auth/me`; ações administrativas ocultas para RESPONSIBLE.
7. **Testes** — ataque: responsável altera projeto alheio → 403; responsável cria/edita responsável ou secretaria → 403; senha abaixo do mínimo → 400. Legítimo: login, CRUD do próprio projeto e transição do próprio projeto → sucesso.

### 4.2 Erro seguro

1. `SecurityConfiguration`: `.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()` antes das demais regras `[VERIFICADO: Spring Security 6.5.11, "All Dispatches Are Authorized"]`.
2. `RestExceptionHandler`: `@ExceptionHandler(Exception.class)` → 500 `ProblemDetail`, código `INTERNAL_ERROR`, mensagem genérica, log ERROR no servidor, sem stack trace nem PII na resposta (BASE §4).
3. GraphQL: exceções não resolvidas viram `INTERNAL_ERROR` opaco com log ERROR `[VERIFICADO: Spring GraphQL 2.0.5]`; `[HIPÓTESE]` mesmo comportamento na 1.4.x usada pelo Boot 3.5.16. Adicionar `extensions.code = INTERNAL_ERROR` para manter o contrato de códigos estáveis.
4. Teste: exceção inesperada devolve 500 `INTERNAL_ERROR`, não 403, sem detalhes internos.

### 4.3 Pendências

Nenhuma. P1 e P2 resolvidas em 2026-09-23 (itens 3, 3a e 4 acima).

---

## 5. F3-L4 — CI/CD (P3, decidido)

- **Decisão**: migrar o repositório para o GitHub e usar somente GitHub Actions; sem GitLab CI e sem pipeline duplicado.
- **Momento**: no F3-L4 (24/09), última etapa antes do freeze; a F4 (25/09) apenas confirma o pipeline verde. Motivo: a F4 só admite correção mínima, e uma falha no primeiro pipeline dentro do freeze não teria correção adequada.
- **Migração com histórico** `[VERIFICADO: docs.github.com, Duplicating a repository]`: criar o repositório no GitHub; `git clone --bare` do GitLab; `git push --mirror` para o GitHub. Preserva branches, tags e histórico de commits exigido pelo desafio.
- **Antes de publicar** `[DESCONHECIDO]` se o histórico já conteve segredo; verificação: `git log --all -p -- .env`. A proteção de push do GitHub para usuários vem ativada por padrão e bloqueia envio de segredos a repositórios públicos `[VERIFICADO: docs.github.com, push protection]`.
- **GitHub Actions** `[VERIFICADO: docs.github.com, secure use]`: `permissions: contents: read`; actions fixadas por SHA completo; sem `pull_request_target`; sem runner próprio; sem segredos.
- **Jobs**: `frontend` — Node 24, pnpm 12.5.1 via corepack, `pnpm install --frozen-lockfile`, `pnpm lint`, `pnpm typecheck`, `pnpm test`, `pnpm build`; `backend` — JDK 25 + Maven, `mvn -B -ntp clean verify`. `[HIPÓTESE]` runners Ubuntu do GitHub têm Docker para o Testcontainers; o primeiro pipeline confirma. `[DESCONHECIDO]` versões exatas das actions de setup, a verificar no marketplace oficial ao abrir o F3-L4.

---

## 6. Sequência

1. F3-L2 — Autenticação do responsável + erro seguro.
2. F3-L3 — Observabilidade.
3. F3-L4 — Engenharia de entrega, com migração para o GitHub e GitHub Actions.
4. F4 — Freeze em 25/09/2026.
