# Facilit — Desafio Técnico Kanban

Projeto para o desafio técnico de Backend Sênior: API Java para gestão de projetos em Kanban, com REST obrigatório e GraphQL como diferencial.

## Estado atual

**F0-L1 — Bootstrap: GREEN em 2026-09-21.**

**F0-L2 — Domínio: GREEN em 2026-09-21.**

**F0-L3 — Motor de regras: GREEN em 2026-09-21.**

**F0 — Fundação + domínio: GREEN em 2026-09-21.**

**F1-L1 — CRUD REST + GraphQL: GREEN em 2026-09-22.**

**F1-L2 — Kanban e transições: GREEN em 2026-09-22.**

**F1-L3 — API e qualidade: GREEN em 2026-09-22.**

**F1 — Backend funcional: GREEN em 2026-09-22.**

**F2-L1 — Segurança: GREEN em 2026-09-22.**

**F2-L2 — Fundação frontend autenticada: GREEN em 2026-09-22.**

**F2-L3 — Kanban UI: GREEN em 2026-09-22.**

**F2 — Segurança + UI: GREEN em 2026-09-22.**

**F3-L1 — Funcionalidades diferenciais: GREEN em 2026-09-23.**

**F3-L2 — Autenticação do responsável + erro seguro: CANDIDATE em 2026-09-23, aguardando gate local.**

A fundação contém:

- backend Java 25 + Spring Boot 3.5.16;
- REST e GraphQL sobre o mesmo caso de uso de health-check;
- PostgreSQL 18.6 + Flyway com migration V1 validada do zero;
- frontend React 19.3 + TypeScript + Vite + Material UI;
- teste unitário mínimo no frontend e testes de fundação no backend;
- Docker Compose para banco, backend e frontend;
- evidências e gates por lote versionados em `docs/evidence/`.

O F0-L2 adicionou o modelo de domínio puro para Projeto, Responsável, Secretaria, datas, status e auditoria mínima. O F0-L3 fechou o motor determinístico de status/métricas, consistência temporal, testes unitários de domínio e a migration inicial do schema relacional. O gate final da F0 passou com 18 testes backend, 2 testes frontend, migration aplicada do zero, Docker, REST, GraphQL e frontend validados.

## Preparar e subir o ambiente

O lockfile do frontend faz parte da baseline F0-L1. Instale as dependências respeitando-o:

```bash
cd frontend
corepack enable
corepack prepare pnpm@12.5.1 --activate
pnpm install --frozen-lockfile
cd ..
```

Depois, prepare o `.env` local. `APP_ADMIN_EMAIL` e `APP_ADMIN_PASSWORD` não possuem credenciais padrão e devem ser preenchidos localmente; `.env` permanece ignorado pelo Git. A senha de bootstrap é usada somente para criar o administrador quando ainda não existe e é persistida apenas como hash delegado/bcrypt.

Opcionalmente, para demonstrar o perfil de responsável, preencha juntos `APP_DEMO_RESPONSIBLE_NAME`, `APP_DEMO_RESPONSIBLE_EMAIL`, `APP_DEMO_RESPONSIBLE_POSITION` e `APP_DEMO_RESPONSIBLE_PASSWORD` (mínimo de 12 caracteres). Na primeira subida, o sistema cria o responsável e a credencial dele; nas seguintes, não altera nada. Nenhuma credencial padrão é versionada.

```bash
cp .env.example .env
# edite .env e defina POSTGRES_PASSWORD, APP_ADMIN_EMAIL e APP_ADMIN_PASSWORD
# opcional: APP_DEMO_RESPONSIBLE_NAME, _EMAIL, _POSITION e _PASSWORD
docker compose up --build -d
```

Serviços locais:

- frontend: `http://localhost:5173`
- REST health: `http://localhost:8080/api/v1/health`
- GraphQL: `http://localhost:8080/graphql` (autenticado)
- GraphiQL: `http://localhost:8080/graphiql`
- CSRF SPA: `GET http://localhost:8080/api/v1/auth/csrf`
- login: `POST http://localhost:8080/api/v1/auth/login`
- sessão atual: `GET http://localhost:8080/api/v1/auth/me`
- logout: `POST http://localhost:8080/api/v1/auth/logout`
- PostgreSQL: acessível apenas pela rede interna do Compose como `db:5432`

## Verificações

Backend:

```bash
cd backend
mvn -B -ntp clean verify
```

Frontend:

```bash
cd frontend
corepack enable
corepack prepare pnpm@12.5.1 --activate
pnpm install --frozen-lockfile
pnpm format
pnpm lint
pnpm typecheck
pnpm test
pnpm build
```

O Dockerfile do frontend usa `pnpm install --frozen-lockfile`, preservando a resolução validada no F0-L1.

## Governança

Os documentos governantes estão em `docs/governance`. Cada lote deve produzir o Pacote Anti-Alucinação previsto no Prompt Executivo Kanban.
## F1-L1 — CRUD

O F1-L1 adiciona CRUD paginado de Projeto e Responsável sobre os mesmos casos de uso para REST e GraphQL, persistência JPA sobre o schema Flyway existente, auditoria de criação/atualização, normalização e unicidade de e-mail e proteção contra remoção de responsável ainda associado a projeto. O contrato padronizado definitivo de erros permanece para F1-L3; neste lote existe apenas o mapeamento HTTP/GraphQL mínimo necessário para o CRUD.

## F1-L2 — Kanban e transições

O F1-L2 adiciona filtro paginado por status em REST/GraphQL e uma política de transição de domínio que aplica exclusivamente as ações automáticas da tabela, recalcula status/métricas e bloqueia qualquer resultado incompatível com o status solicitado. REST e GraphQL reutilizam `ProjectService.transition`.

## F1-L3 — API e qualidade

O F1-L3 padroniza erros REST/GraphQL com códigos estáveis, reforça Bean Validation nas duas fronteiras, publica OpenAPI/Swagger, adiciona índices alinhados às consultas paginadas reais e introduz integração/API tests com PostgreSQL 18.6 descartável via Testcontainers. O gate local foi concluído com exit code 0 e promoveu F1-L3/F1 para GREEN.

## F2-L1 — Segurança

O F2-L1 adiciona Spring Security com autenticação por e-mail/senha, conta administrativa persistida no PostgreSQL, senha armazenada somente por `DelegatingPasswordEncoder`/bcrypt, sessão HTTP com cookie `HttpOnly`/`SameSite=Lax`, autorização `ROLE_ADMIN`, CSRF compatível com SPA por cookie `XSRF-TOKEN`, login/logout, respostas 401/403 sem detalhes sensíveis e testes de integração específicos. REST de health e documentação permanecem públicos; os endpoints de negócio REST e `/graphql` exigem autenticação.


## F2-L2 — Fundação frontend autenticada

O F2-L2 conecta o frontend ao contrato de autenticação já validado no F2-L1. A aplicação passa a ter rota de login e área protegida, sessão validada por `GET /api/v1/auth/me`, login/logout com CSRF obtido do backend, estados explícitos de carregamento e erro e uma estrutura visual Material UI consistente para o painel. O estado remoto de autenticação permanece em React Query; nenhuma credencial é persistida em `localStorage` ou `sessionStorage`.

O roteamento deste lote cobre somente `/login` e `/` usando a History API nativa do navegador. Essa escolha evita dependência adicional para duas rotas e mantém o diff mínimo; a decisão deve ser reavaliada apenas se a navegação do F2-L3 exigir uma árvore de rotas maior.


## F2-L3 — Kanban UI

O F2-L3 adiciona o quadro Kanban autenticado com as quatro colunas do domínio (`NOT_STARTED`, `IN_PROGRESS`, `OVERDUE`, `COMPLETED`), drag-and-drop nativo, criação/edição/exclusão de projetos, cadastro de responsável, associação de responsáveis, feedback das mensagens de domínio e atualização do estado remoto via React Query. As mutações reutilizam o mesmo contrato CSRF validado no F2-L2.

A UI foi reorganizada por responsabilidade: `App.tsx` atua somente como composition root de navegação; autenticação fica em `features/auth`, painel em `features/dashboard`, Kanban em `features/kanban`, navegação em `app/navigation.ts` e estados visuais compartilhados em `components`. O quadro mantém a orquestração de queries/mutações enquanto coluna e diálogos permanecem componentes focados. A camada `api` continua independente do React Query; os callbacks do framework usam adaptadores explícitos.


## F3-L1 — Funcionalidades diferenciais

O F3-L1 adiciona indicadores de projetos, incluindo quantidade por status, média de dias de atraso por status, total e quantidade com atraso; CRUD de Secretaria em REST e GraphQL; e filtros avançados de projetos por status, secretaria, responsável, interseção do período previsto e texto. REST e GraphQL reutilizam o mesmo `ProjectService` e o mesmo `ProjectFilter`.

No frontend, indicadores, gerenciamento de secretarias e filtros ficam em features focadas. O quadro continua responsável pela orquestração do Kanban e invalida indicadores quando mutações de projeto alteram a carteira. Os filtros relacionais e temporais usam os índices já existentes da migration V1/V3; a busca textual por substring permanece sem extensão PostgreSQL adicional para manter o diferencial de baixo risco.


## F3-L2 — Autenticação do responsável + erro seguro

O F3-L2 adiciona o perfil `RESPONSIBLE` ao lado do `ADMIN`, conforme o `docs/governance/REPLANEJAMENTO-F3.md`:

- o ADMIN define ou revoga a senha de um responsável em `PUT`/`DELETE /api/v1/responsibles/{id}/credentials` ou pelas mutations GraphQL `setResponsibleCredentials`/`revokeResponsibleCredentials`; a senha exige no mínimo 12 caracteres e no máximo 72 bytes;
- o responsável entra pelo mesmo `POST /api/v1/auth/login`; `GET /api/v1/auth/me` passa a informar `responsibleId`;
- o responsável vê o quadro inteiro, os indicadores, os responsáveis e as secretarias, e cria, edita, transiciona e exclui apenas os projetos em que é responsável; responsáveis, secretarias e credenciais ficam só com o ADMIN;
- a regra de posse fica na camada de aplicação (`Actor` informado aos casos de uso), reutilizada por REST e GraphQL;
- o e-mail de login do responsável é copiado para `app_users` e sincronizado na mesma transação quando o ADMIN altera o e-mail do responsável; o índice único de login impede colisão com o e-mail do ADMIN (409);
- erros inesperados passam a responder 500 com `code = INTERNAL_ERROR` e `incidentId`, sem detalhes internos; rotas inexistentes respondem 404 em vez de 403.
