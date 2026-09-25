# F5-C2 — DECISÕES

Data: 2026-09-24

Escopo: `docs/governance/PLANO-CORRECAO-RELEASE-2.0.0.md`, lote F5-C2 (rigor de testes e validação), mais a promoção do F5-C1. O usuário pediu seguir depois do F5-C1 GREEN ("avance para F5-C2", 2026-09-24 21h59).

## Métricas linha a linha (item 29 do PDF)

- `[VERIFICADO: transicoes.feature e StatusTransitionSteps no snapshot 43051359]` o BDD conferia só o status resultante. O PDF pede "cálculo de status/métricas — linha a linha da tabela de transição".
- **Colunas novas:** `atraso` e `percentual` nos `Exemplos` das 12 linhas, e um passo que confere `delayDays` e `remainingTimePercentage` do resultado. Nas linhas bloqueadas, `-` confere que nenhuma métrica nova foi calculada: o projeto não muda.
- **Cenários ajustados para as métricas não serem só 0 e 100** (a classificação de origem de cada cenário continua conferida no `Dado`):
  - `overdue_not_started`: 19/09–23/09, Atrasado com 1 dia. A linha 9 mostra o atraso zerando ao concluir.
  - `completed_future`: 22/09–02/10, realizados 22/09 e 24/09. A linha 11 volta a Em andamento com 80% restante.
- `[EXECUTADO · harness com as classes reais do domínio]` valores esperados de cada linha:
  - 01: 0 e 100;
  - 03: 0 e 0;
  - 04: 0 e 100;
  - 06: 0 e 0;
  - 09: 0 e 0;
  - 11: 0 e 80;
  - 12: 1 e 0;
  - 02, 05, 07, 08 e 10: bloqueadas.

  A tabela do `.feature` foi escrita a partir dessa execução, não calculada à mão.

## Limites de tamanho nas entradas (item 32 do PDF)

- `[DECISÃO DO USUÁRIO, 2026-09-24]` os limites:
  - nome de projeto, responsável e secretaria: até 200 caracteres;
  - cargo: até 200;
  - e-mail: até 254;
  - texto de busca: até 100;
  - responsáveis por projeto: de 1 a 50.
- **Onde fica o limite:** na borda da API, com `@Size` nos DTOs REST e nos inputs GraphQL. Os valores estão em `delivery/common/InputLimits`, usados pelas duas bordas.
  - Resposta: 400 `VALIDATION_ERROR`, com o campo em `violations` (REST) ou `extensions.code` (GraphQL).
  - Mensagem padrão do Hibernate Validator em pt: "tamanho deve ser entre 0 e N" `[VERIFICADO: hibernate-validator 8.0.2.Final, ValidationMessages_pt.properties]`.
- **Texto de busca:** o limite fica no `ProjectFilter` (aplicação), onde já estava a validação do período. REST e GraphQL constroem o mesmo filtro, então o limite vale nos dois, com 400 `INVALID_REQUEST`, como os demais erros de filtro.
- **Sem migration.** O banco continua `TEXT`, e a V8 não entra na véspera da release. O freeze exige as migrations 1 a 7.
- **E-mail de 254 caracteres:** é o maior endereço utilizável em SMTP (caminho de 256 da RFC 5321 menos os sinais `<` e `>`).
- **OpenAPI:** o springdoc publica `maxLength`/`maxItems` a partir do `@Size`. O gate confere no `/api-docs`.

## Desvios em relação ao plano

| Plano | Feito | Motivo |
|---|---|---|
| texto do filtro com `@Size` no parâmetro | limite no `ProjectFilter` → 400 `INVALID_REQUEST` | uma regra só para REST e GraphQL, sem depender de validação de método no controller; mesmo código de erro do período inválido |
| teste GraphQL de tamanho | `ProjectGraphQlControllerTest.rejectsInputAboveTheLimitsBeforeCallingTheService` (input e texto) | cobre as duas vias de validação do GraphQL |
