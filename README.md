# Facilit — Desafio Técnico Kanban

Projeto para o desafio técnico de Backend Sênior: API Java para gestão de projetos em Kanban, com REST obrigatório e GraphQL como diferencial.

## Estado atual

**F0-L1 — Bootstrap: GREEN em 2026-09-21.**

A fundação contém:

- backend Java 25 + Spring Boot 3.5.16;
- REST e GraphQL sobre o mesmo caso de uso de health-check;
- PostgreSQL 18.6 e Flyway preparados;
- frontend React 19.3 + TypeScript + Vite + Material UI;
- teste unitário mínimo no frontend e testes de fundação no backend;
- Docker Compose para banco, backend e frontend;
- evidências do lote em `docs/evidence/F0-L1`.

As regras de Projeto, Responsável, status e métricas entram nos lotes F0-L2 e F0-L3.

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
