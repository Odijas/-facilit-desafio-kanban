# F5-L3 — DECISÕES

Data: 2026-09-24

Escopo: `docs/governance/PLANO-CONFORMIDADE-F5.md`, lote F5-L3 (camadas de teste completas e decisão D5, transação e concorrência), sobre a base F5-L2 já encerrada e promovida. O usuário pediu seguir o plano depois do F5-L2 GREEN (2026-09-24).

## Transação por caso de uso (D5)

- `[VERIFICADO]` porta `TransactionRunner` na aplicação (`execute(Supplier)` e `run(Runnable)`). A aplicação continua sem dependência de Spring.
- `[VERIFICADO: Spring Framework 6.2.19, TransactionTemplate.execute(TransactionCallback)]` implementação `SpringTransactionRunner` na infraestrutura, com `TransactionTemplate` sobre o `PlatformTransactionManager` do Spring Boot (propagação `REQUIRED`: uma chamada dentro de outra transação participa dela).
- `[VERIFICADO]` casos de uso de escrita que rodam inteiros numa transação:
  - `ProjectService`: `create`, `update`, `transition`, `delete`;
  - `ResponsibleService`: `create`, `update`, `delete`;
  - `SecretariatService`: `create`, `update`, `delete`;
  - `ResponsibleCredentialService`: `setPassword`, `revoke`.
- `[VERIFICADO]` os logs de negócio de sucesso saem depois do commit, para não registrar algo que foi desfeito. O log de transição recusada (`projeto.transicao.recusada`) sai dentro da transação, no momento da recusa: a recusa aconteceu mesmo que a transação seja desfeita.
- `[VERIFICADO]` leituras continuam fora de transação de aplicação: cada método do adaptador JPA já tem `@Transactional(readOnly = true)` e o recálculo diário grava pelo próprio adaptador. Dentro de um caso de uso de escrita, o recálculo participa da transação do caso de uso e é desfeito junto se o caso de uso falhar; a próxima leitura refaz o recálculo (é idempotente). `TransactionIT.blockedTransitionRollsBackTheRecalculationDoneInsideTheUseCase` fixa esse comportamento.

## Concorrência otimista

- `[VERIFICADO]` migration **V7** `version BIGINT NOT NULL DEFAULT 0` em `projects` e `@Version Long version` em `ProjectJpaEntity`.
  - O plano dizia "V6 também"; a V6 já tinha saído no F5-L1 e migration aplicada não muda, então a coluna entra numa migration nova.
  - `Long` (e não `long`): com id atribuído pela aplicação, o Spring Data decide entre `persist` e `merge` pela versão nula. Com `long`, a versão 0 faria um projeto novo parecer existente.
- `[VERIFICADO]` duas edições simultâneas do mesmo projeto: a segunda a gravar recebe `OptimisticLockingFailureException`, e a API responde 409 `CONFLICT` (REST e GraphQL).
- `[VERIFICADO: Spring Framework 6.2.19, HibernateJpaDialect]` o Hibernate sinaliza a versão desatualizada com `StaleObjectStateException`/`StaleStateException`, que o Spring traduz para `ObjectOptimisticLockingFailureException`. Uma `OptimisticLockException` da JPA sem causa do Hibernate viraria `JpaOptimisticLockingFailureException`. Os tratadores REST e GraphQL passam a capturar a classe base `OptimisticLockingFailureException`, que cobre os dois casos (no F5-L2 capturavam só a primeira).
- `[VERIFICADO]` limite: a API não recebe a versão do cliente. O `@Version` impede duas gravações simultâneas de se sobrescreverem, mas não detecta edição entre a leitura na tela e o envio. Registrado no README (Limitações). Expor a versão (ETag/If-Match ou campo no corpo) mudaria o contrato e fica fora do plano.

## Camadas de teste

- `[VERIFICADO]` **controllers com o service simulado** (item 24):
  - REST: `@WebMvcTest(<Controller>.class)` + `@AutoConfigureMockMvc(addFilters = false)` + `@MockitoBean` do service e do `AuthenticatedActorResolver`. Os filtros de segurança ficam de fora: a segurança já é provada por `SecurityApiIT` com o servidor real. O slice carrega o `RestExceptionHandler`, então os testes provam o contrato de erro de cada controller.
  - GraphQL: `@GraphQlTest(<Controller>.class)` + `@WithMockUser` + `@MockitoBean`. O slice carrega o `GraphQlErrorHandler` (`@ControllerAdvice` está nos includes do `@GraphQlTest`).
  - `[VERIFICADO: Spring Framework 6.2]` `@MockitoBean` (`org.springframework.test.context.bean.override.mockito`) substitui o `@MockBean` do Spring Boot, depreciado desde o Boot 3.4.
- `[VERIFICADO]` **service com Mockito**: `ProjectServiceMockitoTest` verifica interações que o repositório em memória não mostra (bloqueio não chama `save`; recálculo antes da consulta, com `InOrder`; validação dos responsáveis dentro da transação). Os testes com repositório em memória continuam, com `DirectTransactionRunner`.
- `[VERIFICADO]` **repositório** (item 37): `ProjectPersistenceAdapterIT` com `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + Testcontainers `@ServiceConnection`, sobre as migrations reais. Cobre gravação e leitura, data do cálculo, versão, consulta de desatualizados (ordem e limite), atualização condicional do recálculo, filtros combinados, paginação por nome e indicadores.
  - Achado na revisão: a ordem de `uuid` no PostgreSQL (bytes sem sinal) não é a de `UUID.compareTo` (compara `long` com sinal). O teste ordena o esperado pelo texto canônico, que coincide com a ordem do banco.
- `[VERIFICADO]` **transações** (item 37): `TransactionIT` com o contexto completo e PostgreSQL real:
  - falha depois da gravação (com `flush`, o `INSERT` chega ao banco) → nada fica;
  - transição bloqueada desfaz o recálculo feito dentro do caso de uso;
  - edição concorrente: a edição 1 lê (versão 0), a edição 2 grava e confirma numa transação independente (`REQUIRES_NEW`), a edição 1 grava → `OptimisticLockingFailureException`; o banco fica com a edição 2 e versão 1;
  - edições em sequência incrementam a versão.
- `[VERIFICADO]` **tabela pela API** (item 29): `StatusTransitionApiIT` com servidor real, sessão e CSRF. As 12 linhas pela REST (mais o bloqueio da linha 12 quando as datas não classificam Atrasado) e 3 pela GraphQL (sucesso, bloqueio e confirmação). Cada projeto nasce pela API com as datas que colocam o status de origem no dia de hoje, e o teste confere esse status antes da transição. Nas recusas, confere que nada foi gravado.
- `[VERIFICADO]` `ApiSession` (suporte de teste) concentra login com CSRF e envio de requisições para o IT novo. `KanbanApiIT` e `SecurityApiIT` ficam como estão, para não mexer em testes GREEN fora do escopo.

## Mockito como agente Java

- `[VERIFICADO: Javadoc do Mockito 5.17.0, seção 0.3 "Explicitly setting up instrumentation for inline mocking (Java 21+)"]` a partir do JDK 21 o Mockito recomenda anexar o próprio jar como `-javaagent`, porque o autoanexo dinâmico gera aviso e deixará de funcionar. No Maven, o exemplo usa `maven-dependency-plugin:properties` e `-javaagent:${org.mockito:mockito-core:jar}` no `argLine`.
- Aplicado no Surefire e no Failsafe. O exemplo da documentação usa `@{argLine} ` antes do agente, para somar a outro agente (por exemplo, JaCoCo). Aqui não há outro `argLine`, e `@{argLine}` sem propriedade definida ficaria literal na linha de comando. Se o JaCoCo entrar no F5-L4, o `argLine` passa a `@{argLine} -javaagent:...` com a propriedade `argLine` definida.
- O gate falha se o log do Maven tiver "self-attaching" (aviso do Mockito) ou "loaded dynamically" (aviso do JDK).

## Desvios em relação ao plano

| Plano | Feito | Motivo |
|---|---|---|
| coluna `version` "na V6" | V7 | V6 já aplicada no F5-L1; migration publicada não muda |
| 409 só com `ObjectOptimisticLockingFailureException` (F5-L2) | classe base `OptimisticLockingFailureException` | cobre também a tradução JPA pura, sem depender de qual exceção o Hibernate lança |
| `ProjectPersistenceAdapterIT` com `summarizeByStatus` | coberto por `indicators()` do adaptador, que usa `summarizeByStatus` e `countByDelayDaysGreaterThan` | o teste passa pela porta da aplicação, não pelo repositório Spring Data direto |
| 3 cenários GraphQL | linhas 1, 2 e 11 | cobrem sucesso, bloqueio e confirmação |
