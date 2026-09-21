# Facilit — Desafio Técnico Kanban

Projeto para o desafio técnico de Backend Sênior: API Java para gestão de projetos em Kanban, com REST obrigatório e GraphQL como diferencial.

## Estado atual

**F0-L1 — Bootstrap: GREEN em 2026-09-21.**

**F0-L2 — Domínio: GREEN em 2026-09-21.**

**F0-L3 — Motor de regras: GREEN em 2026-09-21.**

**F0 — Fundação + domínio: GREEN em 2026-09-21.**

A fundação contém:

- backend Java 25 + Spring Boot 3.5.16;
- REST e GraphQL sobre o mesmo caso de uso de health-check;
- PostgreSQL 18.6 + Flyway com migration V1 validada do zero;
- frontend React 19.3 + TypeScript + Vite + Material UI;
- teste unitário mínimo no frontend e testes de fundação no backend;
- Docker Compose para banco, backend e frontend;
- evidências e gates por lote em `docs/evidence/F0-L1`, `F0-L2` e `F0-L3`.

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

Depois, suba o ambiente:

```bash
cp .env.example .env
docker compose up --build -d
```

Serviços locais:

- frontend: `http://localhost:5173`
- REST health: `http://localhost:8080/api/v1/health`
- GraphQL: `http://localhost:8080/graphql`
- GraphiQL: `http://localhost:8080/graphiql`
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
