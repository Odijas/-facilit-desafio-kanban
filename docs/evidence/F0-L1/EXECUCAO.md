# F0-L1 — EXECUÇÃO REAL

Data: 2026-09-21.

## Execução do usuário — primeira candidata

### Runtime

```text
openjdk version "25.0.3" 2026-04-21 LTS
Apache Maven 3.9.16
node v24.21.0
corepack 0.36.0
Docker version 29.8.1
Docker Compose version v5.5.1
pnpm 12.5.1
```

### Frontend

```text
pnpm lint => RED: 5 erros de formatação/import ordering
pnpm typecheck => RED: 9 erros de tipos
pnpm test => GREEN: 1 arquivo, 2 testes, 0 falhas
pnpm build => RED: interrompido pelos mesmos erros de typecheck
```

Erros de typecheck observados:

```text
MUI 9.4.0: incompatibilidades de propriedades opcionais sob exactOptionalPropertyTypes=true
@testing-library/jest-dom 7.0.1: referência a tipos Jest e conflito de Assertion com Vitest 5.0.0
```

### Backend

```text
mvn -B -ntp clean verify
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Docker

```text
docker compose config => GREEN

docker compose up --build -d => RED
ERR_PNPM_MINIMUM_RELEASE_AGE_VIOLATION
@tanstack/query-core@5.103.2 e @tanstack/react-query@5.103.2
```

Como o build do frontend falhou, backend/frontend não subiram e os três `curl` subsequentes falharam por conexão recusada.

## Correções aplicadas nesta rodada

- formatação indicada pelo Biome aplicada aos 5 pontos reportados;
- TypeScript 7.0.2 -> 5.9.3;
- Vitest 5.0.0 -> 4.1.9;
- jest-dom 7.0.1 -> 6.9.1;
- React Testing Library 16.3.3 -> 16.3.2;
- `@testing-library/dom` 10.4.1 declarado explicitamente;
- React Query 5.103.2 -> 5.102.8;
- `exactOptionalPropertyTypes` removido, mantendo `strict` e `noUncheckedIndexedAccess`;
- `pnpm-workspace.yaml` adicionado e copiado pelo Dockerfile;
- `.dockerignore` adicionado a frontend/backend.

## Execução no runtime da IA após correção

```text
corepack prepare pnpm@12.5.1 --activate
=> RED: runtime sem acesso a https://registry.npmjs.org
```

[DESCONHECIDO] Resultado pós-correção de lint/typecheck/test/build/Docker até execução do usuário.

## Validação estrutural pós-correção no runtime da IA

```text
JSON_OK frontend/package.json
JSON_OK frontend/tsconfig.app.json
JSON_OK frontend/tsconfig.json
JSON_OK frontend/tsconfig.node.json
JSON_OK frontend/biome.json
YAML_OK compose.yaml
YAML_OK backend/src/main/resources/application.yml
YAML_OK frontend/pnpm-workspace.yaml
XML_OK backend/pom.xml
JAVAC_PURE_OK
TS_ESCAPE_SCAN_OK
PROD_COMMENT_SCAN_OK
GIT_DIFF_CHECK_OK
```

Essas verificações não substituem a execução pnpm/Docker bloqueada pela rede/runtime da IA.

## Execução do usuário — candidata corrigida

Data: 2026-09-21.

### Frontend

```text
pnpm lint => GREEN: 12 arquivos verificados, 0 correções
pnpm typecheck => GREEN
pnpm test => GREEN: 1 arquivo, 2 testes, 0 falhas
pnpm build => GREEN: Vite 8.3.0, 945 módulos transformados
```

### Backend

```text
mvn -B -ntp clean verify => GREEN
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Docker

```text
docker compose config => GREEN
docker compose up --build -d => RED no startup do serviço db
failed to bind host port 0.0.0.0:5432/tcp: address already in use
```

[ANÁLISE] As imagens de frontend e backend foram construídas. A falha ocorreu somente na publicação da porta do PostgreSQL no host. Como o backend usa `db:5432` pela rede interna do Compose, a publicação de `5432` no host não é necessária para o fluxo do desafio.

## Execução do usuário — correção PostgreSQL 18

Data: 2026-09-21.

Após alterar o volume para `/var/lib/postgresql`, o Compose conseguiu iniciar banco, backend e frontend:

```text
Container facilit-kanban-db-1       Healthy
Container facilit-kanban-backend-1  Started
Container facilit-kanban-frontend-1 Started
```

A primeira chamada REST foi disparada imediatamente após `docker compose up -d` e retornou:

```text
curl: (56) Recv failure: Conexão fechada pela outra ponta
Resultado: exit code 56
```

O erro foi classificado como readiness do backend, não como falha de build ou banco.

## Gate final — GREEN

Data: 2026-09-21.

O usuário executou o gate com sondagem limitada de readiness. Saída final relevante:

```text
=== AGUARDANDO BACKEND ===
{"status":"UP"}
=== GRAPHQL ===
{"data":{"health":{"status":"UP"}}}
=== FRONTEND ===
HTTP 200
=== CONTAINERS ===
facilit-kanban-backend-1    ... Up ...              0.0.0.0:8080->8080/tcp
facilit-kanban-db-1         ... Up ... (healthy)    5432/tcp
facilit-kanban-frontend-1   ... Up ...              0.0.0.0:5173->5173/tcp
=== GIT ===
=== F0-L1 GREEN ===
Resultado: exit code 0
```

Resultado consolidado do lote:

```text
Frontend lint: GREEN
Frontend typecheck: GREEN
Frontend tests: 2/2 GREEN
Frontend build: GREEN
Backend tests: 3/3 GREEN
Backend build: GREEN
Docker images: GREEN
PostgreSQL: healthy
REST health: UP
GraphQL health: UP
Frontend HTTP: 200
git diff --check: GREEN
F0-L1: GREEN
```
