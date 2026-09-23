# F0-L1 — DECISÕES

## D01 — Java 25 + Spring Boot 3.5.16

[VERIFICADO: RAG oficial · 2026-09-21] Java 25 é LTS e Spring Boot 3.5.16 declara compatibilidade até Java 25.

[DECISÃO] Manter Spring Boot 3.5.16 porque o Prompt Executivo Kanban aprovado fixou a linha 3.5.x. A release 3.5.16 é a última OSS dessa linha; migração para Boot 4 não será introduzida no prazo sem gate específico.

## D02 — REST e GraphQL como adaptadores do mesmo caso de uso

[VERIFICADO: arquivos F0-L1 · 2026-09-21] `HealthRestController` e `HealthGraphQlController` consomem o mesmo `HealthQuery`.

[DECISÃO] Essa forma será mantida para evitar duplicação de regras entre REST e GraphQL.

## D03 — Clean Architecture pragmática

[DECISÃO] A fundação contém `application`, `delivery` e `infrastructure` somente onde há responsabilidade real. Não foi criada interface para `HealthQuery`, pois haveria um único implementador sem necessidade de dublê.

## D04 — PostgreSQL 18.6 + Flyway

[VERIFICADO: RAG oficial · 2026-09-21] PostgreSQL 18.6 é release estável suportada publicada em 2026-08-13.

[DECISÃO] JPA usa `ddl-auto=validate`; evolução de schema pertence ao Flyway. A primeira migration de negócio será criada em F0-L2 junto ao modelo persistente, evitando migration vazia sem requisito.

## D05 — Frontend tipado com combinação estável

[EXECUTADO PELO USUÁRIO · 2026-09-21] TypeScript 7.0.2 + MUI 9.4.0 falharam no typecheck com `exactOptionalPropertyTypes: true`; `@testing-library/jest-dom` 7.0.1 + Vitest 5.0.0 também apresentaram conflito de declarações.

[VERIFICADO: RAG oficial · 2026-09-21] Material UI exige TypeScript >= 4.9 e recomenda `strict: true`; não exige `exactOptionalPropertyTypes`. O jest-dom documenta integração específica via `@testing-library/jest-dom/vitest`.

[DECISÃO] Corrigir para TypeScript 5.9.3 + Vitest 4.1.9 + jest-dom 6.9.1, manter `strict: true` e `noUncheckedIndexedAccess: true`, remover somente `exactOptionalPropertyTypes`, e declarar `@testing-library/dom` explicitamente como peer exigido por React Testing Library 16.

## D06 — Política pnpm idêntica no host e no Docker

[EXECUTADO PELO USUÁRIO · 2026-09-21] O build Docker falhou porque o lockfile continha `@tanstack/react-query@5.103.2`/`query-core@5.103.2`, publicados dentro da janela mínima do pnpm; a instalação local havia criado exceções que o Dockerfile não copiava.

[VERIFICADO: RAG oficial pnpm 12.x · 2026-09-21] `minimumReleaseAge` é 1440 minutos por padrão desde pnpm 11 e `pnpm-workspace.yaml` é o arquivo de configuração por projeto.

[DECISÃO] Fixar React Query em 5.102.8, versão anterior já maturada; versionar `frontend/pnpm-workspace.yaml` com política explícita; e copiá-lo no Docker antes de `pnpm install --frozen-lockfile`. Não haverá exceção para pacote recém-publicado.

## D07 — Contextos Docker mínimos

[EXECUTADO PELO USUÁRIO · 2026-09-21] O contexto Docker do frontend transferiu aproximadamente 196,75 MB porque `node_modules` local entrou no contexto.

[DECISÃO] Adicionar `.dockerignore` em frontend e backend para excluir artefatos locais de build/dependências.

## D08 — Segurança da configuração

[VERIFICADO: arquivos F0-L1 · 2026-09-21] Senha do banco não é versionada; `.env` é ignorado; a aplicação exige `DB_PASSWORD`; respostas de erro não incluem mensagem ou stack trace por configuração.

## D09 — Banco não publica porta no host

[EXECUTADO PELO USUÁRIO · 2026-09-21] O segundo gate construiu as imagens, mas o Compose falhou ao iniciar `db` porque a porta `0.0.0.0:5432` do host já estava ocupada.

[VERIFICADO: RAG oficial Docker · 2026-09-21] Serviços no mesmo projeto Compose alcançam-se pelo nome do serviço e pela porta do container; porta publicada no host é necessária apenas para acesso externo à rede Compose.

[DECISÃO] Remover a publicação `5432:5432` do serviço `db`. O backend continua usando `jdbc:postgresql://db:5432/...`; o banco deixa de disputar a porta do host e fica menos exposto. Não será criada porta alternativa sem requisito real de acesso externo.

## D10 — Layout de volume do PostgreSQL 18+

[EXECUTADO PELO USUÁRIO · 2026-09-21] Após remover a publicação da porta do banco, o PostgreSQL 18.6 recusou iniciar porque o volume estava montado no caminho legado `/var/lib/postgresql/data`.

[VERIFICADO: Docker Official Image PostgreSQL · 2026-09-21] A partir do PostgreSQL 18, `PGDATA` passou a ser versionado e o `VOLUME` oficial passou para `/var/lib/postgresql`.

[DECISÃO] Montar `postgres-data:/var/lib/postgresql`. Como o projeto ainda não continha dados de negócio, não houve migração de dados legados.

## D11 — Readiness sem espera temporal arbitrária

[EXECUTADO PELO USUÁRIO · 2026-09-21] O Compose iniciou os containers, mas a primeira chamada REST ocorreu antes de o Spring Boot aceitar conexões e retornou `curl: (56)`.

[VERIFICADO: Docker Docs · 2026-09-21] `service_started` indica inicialização do container, enquanto readiness depende de `healthcheck`/`service_healthy` ou de uma sondagem explícita.

[DECISÃO] O gate do F0-L1 usa sondagem REST limitada a 30 tentativas de 2 segundos, sem `sleep` fixo como critério de sucesso. O comportamento observado não exige alteração funcional no frontend neste lote.
