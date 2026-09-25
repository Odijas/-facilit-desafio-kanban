# F5-Q1 — EXECUÇÃO

Data: 2026-09-25

## Ambiente da IA

```text
java=/usr/bin/java
openjdk version "21.0.11" 2026-04-21
javac=/usr/bin/javac
mvn=
pnpm=
docker=
mvnw=no
```

`[DESCONHECIDO]` O GREEN oficial do lote não pode ser produzido neste ambiente: o projeto usa Java 25 e o gate requer Maven + Docker.

## Verificações realmente executadas

### Compilação parcial da camada pura

Comando: `javac --release 21` em todos os `.java` de `application` e `domain`.

```text
EXIT=0
```

`[EXECUTADO]` Isto prova apenas consistência Java das camadas puras sob JDK 21. Não prova Spring, JPA, OpenAPI, testes Maven nem Java 25.

### E2 — PUT/update remove início realizado sem confirmação

Harness temporário fora do repositório, usando `ProjectService` + dublês em memória:

```text
E2-update-clears-actualStart=OK|status=NOT_STARTED|actualStart=null
PIPE_EXIT=0
```

### E4/E5 — mensagens de bloqueio

Harness temporário fora do repositório:

```text
E4-line2-missing-dates=OK|... informe o início previsto (plannedStart) ou o término previsto (plannedEnd) ...
E5-line11-not-started=OK|... Informe o início realizado (actualStart) ...
E5-line11-overdue=OK|... Ajuste o término previsto (plannedEnd) ...
EXIT=0
```

As três mensagens foram verificadas também para ausência da substring `null`.

### D1 — consumidores

`[EXECUTADO]` a busca antes/depois está registrada em `CONSUMIDORES.md`; no candidato não restam os métodos mortos de projeto.

### Higiene do diff

```text
trailing_whitespace_count=0
```

`[EXECUTADO]` `cmp` entre o plano fornecido e sua cópia no projeto retornou `cmp_exit=0`.

## Não executado

`[DESCONHECIDO]` `mvn clean verify`, Testcontainers, Docker com banco novo e chamadas reais a `/api-docs`/REST. Executar `VERIFICACAO-USUARIO.md` antes de qualquer commit/merge.

## Gate local — RED 1 e correção

`[EXECUTADO: saída fornecida pelo usuário em 2026-09-25]` `mvn clean verify` chegou aos testes de integração com 52 testes e falhou somente em `OpenApiContractIT.errorExamplesMatchTheOperationResourceInputAndAuthorizationRule`, na operação `POST /api/v1/projects`: o helper do teste procurava exclusivamente `examples.VALIDATION_ERROR.value`, enquanto essa operação já possui um exemplo 400 declarado diretamente no controller.

`[VERIFICADO]` `ProjectRestController` declara o 400 de `POST /api/v1/projects` com `@ExampleObject` próprio; `ApiErrorDocumentation.add` preserva respostas já existentes. O teste não pode exigir uma forma de serialização específica (`example` versus `examples`) quando ambas representam o mesmo exemplo no OpenAPI.

`[EXECUTADO]` correção mínima aplicada somente ao consumidor de teste e ao gate: a leitura do exemplo `VALIDATION_ERROR` aceita exemplo nomeado, exemplo direto ou entrada não nomeada em `examples`, inclusive valor JSON textual. A validação continua exigindo que o campo de `violations[0].field` exista no schema real do corpo.

`[DESCONHECIDO]` GREEN após a correção. O usuário deve regerar `/tmp/f5q1-gate.sh` a partir de `VERIFICACAO-USUARIO.md` e executar novamente o gate integral.
