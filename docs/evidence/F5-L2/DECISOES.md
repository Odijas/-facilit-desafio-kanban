# F5-L2 — DECISÕES

Data: 2026-09-24

Escopo: `docs/governance/PLANO-CONFORMIDADE-F5.md`, lote F5-L2 (decisões D3 e D4, erros no Swagger e logs de negócio). O usuário pediu seguir o plano depois do F5-L1 GREEN ("prossiga no planejamento", 2026-09-24).

## Decisões do lote

- `[VERIFICADO]` **modelo de erro (D3).**
  - Entrada malformada continua em 400 (`VALIDATION_ERROR`, `INVALID_REQUEST`).
  - Dado bem formado que viola regra do domínio passa a 422, com código próprio:
    - `BUSINESS_RULE_VIOLATION`: ordem das datas, data realizada futura, término previsto obrigatório;
    - `TRANSITION_BLOCKED`: bloqueio da tabela, com `currentStatus`/`requestedStatus`;
    - `CONFIRMATION_REQUIRED`: confirmação obrigatória, com `clearedField`.
  - Exceções de domínio: `BusinessRuleException` (base), `TransitionBlockedException` e `ConfirmationRequiredException`.
- `[VERIFICADO: Spring Framework 6.2.19 javadoc]` `HttpStatus.UNPROCESSABLE_ENTITY` existe e não está depreciado. `UNPROCESSABLE_CONTENT` não existe nessa versão.
- `[VERIFICADO]` `DataIntegrityViolationException` e `ObjectOptimisticLockingFailureException` passam a 409 `CONFLICT`, sem expor a mensagem do banco.
  - Antes, a corrida entre a checagem da aplicação e a restrição do banco virava 500.
  - O `@Version` fica para o F5-L3; o tratamento já está pronto.
- `[VERIFICADO]` **mensagens em pt-BR** em todas as exceções que chegam ao cliente, nas regras de domínio, nos serviços, na segurança e nos tratadores REST/GraphQL.
  - Cada linha bloqueável da tabela tem orientação específica, com o nome do campo da API entre parênteses.
  - Mensagens internas de invariante (por exemplo, `PageResult` e `ProjectScheduleMetrics`) ficam em inglês. Indicam erro de programação e não chegam ao cliente.
- `[VERIFICADO]` Bean Validation em pt-BR com `spring.web.locale=pt_BR` e `spring.web.locale-resolver=fixed`.
  - O `LocalValidatorFactoryBean` usa a localidade do `LocaleContextHolder`, que vem do `LocaleResolver`.
  - `[HIPÓTESE]` o Hibernate Validator traz `ValidationMessages_pt_BR`. Resolve: gate (`VALIDATION_ERROR` sem texto em inglês).
- `[VERIFICADO]` **confirmação obrigatória (D4).**
  - **Onde se aplica:** Em andamento → A iniciar (apaga `actualStart`) e Concluído → Em andamento ou Atrasado (apaga `actualEnd`).
  - **Ordem:** a confirmação só é pedida quando a transição passaria. Bloqueio da tabela e regra de datas vêm antes, para não pedir confirmação de algo que não vai acontecer.
  - **REST:** `confirm` opcional no corpo do `PATCH`.
  - **GraphQL:** `confirm: Boolean = false` na mutation.
  - **UI:** diálogo com a mensagem do servidor e reenvio com `confirm: true`. A regra fica só no servidor.
- `[VERIFICADO]` `ProjectStatus.label()` com os nomes do quadro ("A iniciar", "Em andamento", "Atrasado", "Concluído"), usados nas mensagens. A serialização continua pelo nome do enum (JPA, Jackson e GraphQL).
- `[VERIFICADO]` **Swagger.**
  - `ApiErrorDocumentation` (um `OpenApiCustomizer` do springdoc) documenta, em toda operação `/api/v1`, as respostas de erro em `application/problem+json`, com esquema `Problem` e exemplo:
    - 401, 403 e 500 nas operações autenticadas;
    - 400 nas que têm corpo ou query;
    - 404 nos caminhos com `{id}`;
    - 409 nas escritas;
    - 422 nas escritas de projeto;
    - na transição, três exemplos de 422.
  - `springdoc.override-with-generic-response=false` evita respostas genéricas deduzidas do `@RestControllerAdvice`.
  - As respostas declaradas com `@ApiResponses` no controller são mantidas.
- `[VERIFICADO]` **logs de negócio.**
  - `BusinessLog` (aplicação, `System.Logger`, logger `br.com.facilit.kanban.business`) registra em `chave=valor` só ids, status e perfil do ator (`ADMIN` ou `RESPONSIBLE:<id>`).
  - Eventos: criação, alteração, transição, transição recusada (com o motivo), exclusão, credencial e recálculo diário.
  - Nunca registra nome, e-mail ou senha.
  - No Spring Boot, o JUL é ligado ao Logback (`SLF4JBridgeHandler` instalado pelo `LogbackLoggingSystem` com `jul-to-slf4j`). Resolve: gate `F5_L2_BUSINESS_LOGS_GREEN`.
- `[VERIFICADO]` coleção Postman: bloqueio passa a 422 `TRANSITION_BLOCKED`, e há dois cenários novos de confirmação (sem e com `confirm`).

## Versão (D6)

- `[VERIFICADO]` este lote quebra o contrato da v1.0.0. Bloqueio de transição sai de 400 `INVALID_REQUEST` para 422 `TRANSITION_BLOCKED`, e as linhas 4, 11 e 12 passam a exigir `confirm`.
- O número da versão só é gravado no F5-L5 (release). A recomendação continua `2.0.0` (SemVer). A decisão é do usuário e continua pendente.

## Desvios em relação ao plano

| Plano | Feito | Motivo |
|---|---|---|
| `@ApiResponses` em todos os endpoints | `OpenApiCustomizer` central + `@ApiResponses` já existentes | uma regra para 23 operações, sem repetir anotação; `OpenApiContractIT` garante a cobertura |
| `BusinessLogIT` ou teste com `OutputCaptureExtension` | teste de serviço com handler anexado ao logger JUL (`CapturedBusinessLog`) + gate lendo o log real do contêiner | o `ConsoleHandler` do JUL guarda o `System.err` da criação, e a captura de saída pode perder a linha. O handler no logger não depende disso. O gate prova o caminho real até o log ECS |
| `StatusTransitionApiIT` (L3) | continua no F5-L3 | escopo do plano; a API é exercitada no gate deste lote |
