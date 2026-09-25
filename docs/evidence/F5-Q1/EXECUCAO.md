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

## Não executado no ambiente da IA

`[VERIFICADO]` Maven/Java 25, Testcontainers, Docker com banco novo e chamadas reais a `/api-docs`/REST não foram executados pelo ambiente da IA. `[EXECUTADO: gate do usuário em 2026-09-25]` Essas provas foram executadas no ambiente do desenvolvedor e terminaram GREEN, conforme a seção final e `SAIDA-GATE.txt`.

## Gate local — RED 1 e correção

`[EXECUTADO: saída fornecida pelo usuário em 2026-09-25]` `mvn clean verify` chegou aos testes de integração com 52 testes e falhou somente em `OpenApiContractIT.errorExamplesMatchTheOperationResourceInputAndAuthorizationRule`, na operação `POST /api/v1/projects`: o helper do teste procurava exclusivamente `examples.VALIDATION_ERROR.value`, enquanto essa operação já possui um exemplo 400 declarado diretamente no controller.

`[VERIFICADO]` `ProjectRestController` declara o 400 de `POST /api/v1/projects` com `@ExampleObject` próprio; `ApiErrorDocumentation.add` preserva respostas já existentes. O teste não pode exigir uma forma de serialização específica (`example` versus `examples`) quando ambas representam o mesmo exemplo no OpenAPI.

`[EXECUTADO]` correção mínima aplicada somente ao consumidor de teste e ao gate: a leitura do exemplo `VALIDATION_ERROR` aceita exemplo nomeado, exemplo direto ou entrada não nomeada em `examples`, inclusive valor JSON textual. A validação continua exigindo que o campo de `violations[0].field` exista no schema real do corpo.

`[EXECUTADO: gate do usuário em 2026-09-25]` A reexecução integral após essa correção terminou GREEN; a saída está registrada abaixo e em `SAIDA-GATE.txt`.

## Gate local — GREEN final

`[EXECUTADO: saída fornecida pelo usuário em 2026-09-25]` A reexecução integral após o RED 1 terminou com `Resultado: exit code 0`.

```text
F5_Q1_SCOPE_GREEN
unitários/BDD: 25 classes, 177 testes, 0 falhas/erros/ignorados
integração: 12 classes, 52 testes, 0 falhas/erros/ignorados
JaCoCo linhas: 1716/1803 = 95.17%
BDD: 12 cenários GREEN
F5_Q1_BACKEND_GREEN
migrations V1 2 3 4 5 6 7 em banco novo
/api-docs: exemplos de 400/403/404 coerentes por operação
404 real de secretaria = exemplo do OpenAPI
422 linha 2 sem "null" e com orientação de plannedStart/plannedEnd
F5_Q1_DOCKER_API_GREEN
=== F5-Q1 GREEN ===
Resultado: exit code 0
```

A saída integral curta do gate está em `SAIDA-GATE.txt`.

## Fechamento do lote

`[EXECUTADO: saída fornecida pelo usuário em 2026-09-25]` Commits semânticos criados; merge `--no-ff` de `bugfix/2.0.2-q1-code` em `hotfix/2.0.2`; push das duas branches no GitHub.

`[EXECUTADO: FECHAMENTO.txt · 2026-09-25]` `HEAD=ORIGIN=9ab3bbd81dcf06f6251651cb4dc50aad1a8cf1f5` e árvore limpa na captura.
