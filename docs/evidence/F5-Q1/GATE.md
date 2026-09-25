# F5-Q1 — GATE

Data: 2026-09-25

Estado: **CANDIDATE — CORRIGIDO APÓS RED 1, AGUARDANDO REEXECUÇÃO DO GATE LOCAL**.

## Já verificado

- `[EXECUTADO]` compilação parcial `application` + `domain` com `javac --release 21`: exit 0.
- `[EXECUTADO]` harness E2: update remove `actualStart` e recalcula `NOT_STARTED`.
- `[EXECUTADO]` harness E4/E5: três caminhos corrigidos, sem `null`.
- `[EXECUTADO]` mapeamento D1: métodos mortos ausentes do candidato; REST/GraphQL de projetos usam `search`.
- `[EXECUTADO]` diff sem trailing whitespace.
- `[VERIFICADO]` plano copiado byte a byte sem alteração.

## Bloqueios para GREEN

- `[DESCONHECIDO]` `mvn clean verify` com Java 25/Maven.
- `[DESCONHECIDO]` contagens reais de testes após o lote.
- `[DESCONHECIDO]` Docker com banco novo e migrations V1–V7.
- `[DESCONHECIDO]` `/api-docs` real e as chamadas REST exigidas pelo plano.

Execute `VERIFICACAO-USUARIO.md`. O lote só pode virar **GREEN** depois de saída `=== F5-Q1 GREEN ===` e `Resultado: exit code 0`.

Nenhum commit, merge ou push deve ser feito antes do GREEN.

## RED 1 observado

`[EXECUTADO: saída fornecida pelo usuário em 2026-09-25]` `mvn clean verify` executou 52 testes de integração e falhou em 1 teste: `OpenApiContractIT.errorExamplesMatchTheOperationResourceInputAndAuthorizationRule`, para `POST /api/v1/projects`. O Docker/API do gate não chegou a executar porque `set -e` interrompeu no Maven RED.

`[VERIFICADO]` a correção mantém a força do contrato: o teste continua comparando o campo documentado no erro 400 com as propriedades do schema do request body, mas deixa de depender da forma `examples.VALIDATION_ERROR.value` quando a operação já possui exemplo 400 próprio.
