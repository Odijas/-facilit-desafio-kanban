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

**F2-L1 — Segurança: CANDIDATE em 2026-09-22, aguardando gate local.**

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

```bash
cp .env.example .env
# edite .env e defina POSTGRES_PASSWORD, APP_ADMIN_EMAIL e APP_ADMIN_PASSWORD
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

O candidato F2-L1 adiciona Spring Security com autenticação por e-mail/senha, conta administrativa persistida no PostgreSQL, senha armazenada somente por `DelegatingPasswordEncoder`/bcrypt, sessão HTTP com cookie `HttpOnly`/`SameSite=Lax`, autorização `ROLE_ADMIN`, CSRF compatível com SPA por cookie `XSRF-TOKEN`, login/logout, respostas 401/403 sem detalhes sensíveis e testes de integração específicos. REST de health e documentação permanecem públicos; os endpoints de negócio REST e `/graphql` exigem autenticação.
