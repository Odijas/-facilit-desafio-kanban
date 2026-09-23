# F0-L1 — FONTES-RAG

Data da consulta: 2026-09-21.

## Hierarquia aplicada

1. Estado real do repositório e execução enviada pelo usuário.
2. Documentação oficial da versão/linha escolhida.
3. Registros oficiais dos pacotes.
4. Desafio e prompts governantes versionados no projeto.

## Backend e runtime

- Oracle Java SE Support Roadmap — Java 25 LTS:
  https://www.oracle.com/br/java/technologies/java-se-support-roadmap.html
- Spring Boot 3.5 — requisitos de sistema e compatibilidade Java:
  https://docs.spring.io/spring-boot/3.5/system-requirements.html
- Spring Boot — Spring for GraphQL:
  https://docs.spring.io/spring-boot/reference/web/spring-graphql.html
- PostgreSQL 18.6 — release oficial:
  https://www.postgresql.org/docs/release/18.6/

## Frontend — correção F0-L1

- Material UI — TypeScript >= 4.9 e recomendação de `strict: true`:
  https://mui.com/material-ui/guides/typescript/
- Material UI — React 17/18/19 suportados:
  https://mui.com/material-ui/getting-started/installation/
- TypeScript — semântica de `exactOptionalPropertyTypes`:
  https://www.typescriptlang.org/tsconfig/exactOptionalPropertyTypes.html
- TypeScript 5.9.3 — registro oficial npm:
  https://www.npmjs.com/package/typescript
- React Testing Library 16 — `@testing-library/dom` é peer obrigatório:
  https://www.npmjs.com/package/@testing-library/react
- jest-dom — integração documentada com Vitest via `@testing-library/jest-dom/vitest`:
  https://github.com/testing-library/jest-dom
- jest-dom 6.9.1 — versão estável anterior à major 7:
  https://www.npmjs.com/package/@testing-library/jest-dom
- Vitest 4.1.9 — registro oficial npm/ecossistema Vitest:
  https://www.npmjs.com/package/vitest
- TanStack React Query — 5.102.8 publicada antes da janela de 24h:
  https://www.npmjs.com/package/@tanstack/react-query?activeTab=versions
- pnpm 12.x — `minimumReleaseAge`, `minimumReleaseAgeExclude` e configuração em `pnpm-workspace.yaml`:
  https://pnpm.io/settings/dependency-resolution
  https://pnpm.io/settings

## Limite atual

[DESCONHECIDO] O runtime da IA não alcança o registry npm e não possui Docker. A combinação corrigida somente será promovida após `pnpm install`, lint, typecheck, testes, build e Docker executados pelo usuário.

## Docker Compose — correção do conflito de porta

- Docker Docs — Networking in Compose: serviços no mesmo projeto são descobertos por nome e a comunicação serviço-a-serviço usa a porta do container; a porta do host é usada para acesso externo:
  https://docs.docker.com/compose/how-tos/networking/
- Docker Docs — Publishing and exposing ports: publicar porta cria acesso a partir do host e, por padrão, pode expor o serviço em todas as interfaces do host:
  https://docs.docker.com/get-started/docker-concepts/running-containers/publishing-ports/

## PostgreSQL 18 — layout de volume

- Docker Official Image PostgreSQL — mudança de `PGDATA`/`VOLUME` no PostgreSQL 18+:
  https://github.com/docker-library/docs/blob/master/postgres/content.md
- Docker Official Image PostgreSQL — template Alpine que define `PGDATA=/var/lib/postgresql/<major>/docker` e `VOLUME /var/lib/postgresql` para 18+:
  https://github.com/docker-library/postgres/blob/master/Dockerfile-alpine.template

## Readiness no Compose

- Docker Docs — startup order e `service_healthy`:
  https://docs.docker.com/compose/how-tos/startup-order/
