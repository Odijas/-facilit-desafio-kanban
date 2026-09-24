# F5-L3 — LIVRO-RAZÃO DE LEITURA

Data: 2026-09-24

`[VERIFICADO: docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md]` leitura integral é arquivo lido do início ao fim. Arquivo editado por automação é relido.

## Lidos integralmente neste lote

- **Aplicação:** `ProjectService`, `ProjectScheduleRefresher`, `ProjectRepository`, `ProjectFilter`, `ProjectIndicators`, `ProjectStatusIndicator`, `PageResult`, `ResponsibleService`, `ResponsibleCredentialService`, `SecretariatService`.
- **Domínio:** `ProjectStatusTransition`.
- **Infraestrutura:** `ApplicationBeans`, `ProjectJpaEntity`, `ProjectJpaRepository`, `ProjectPersistenceAdapter`.
- **Entrega:** `ProjectRestController`, `ResponsibleRestController`, `SecretariatRestController`, `ProjectIndicatorsRestController`, `ProjectGraphQlController`, `ResponsibleGraphQlController`, `SecretariatGraphQlController`, `ProjectResponse`, `ProjectStatusRequest`.
- **Recursos:** `V1__create_kanban_core.sql` (tabelas `secretariats`, `responsibles`, `projects`), `V6`, `kanban.graphqls` (tipo `Project` e mutation `transitionProject`), `application.yml`, `backend/pom.xml`.
- **Testes:** `ProjectStatusTransitionTest`, `ScheduleFreshnessIT`, `KanbanApiIT`, `OpenApiContractIT`, e os testes de serviço alterados (construtores).

Os arquivos novos foram escritos inteiros:

- `TransactionRunner`, `SpringTransactionRunner`, `V7__add_project_version.sql`
- `DirectTransactionRunner`, `ProjectServiceMockitoTest`
- 4 testes `@WebMvcTest` e 3 `@GraphQlTest`
- `ProjectPersistenceAdapterIT`, `TransactionIT`, `StatusTransitionApiIT`, `ApiSession`
- evidências do lote

## Lidos em parte (trechos usados)

- `RestExceptionHandler` e `GraphQlErrorHandler`: tratadores de 409, 422 e confirmação.
- `SecurityApiIT`: login, CSRF e envio de `PATCH`.
- `RestExceptionHandlerTest`: teste do 409 de concorrência.
- `ScheduleCalculationDateMigrationIT`: uso do Flyway com alvo (confirma que a V7 não interfere).
- `README.md`: seções alteradas.
- `docs/evidence/F5-L2/*`: base documental já encerrada; o pacote F5-L3 não a modifica.

## Conferidos depois da edição automatizada

- Substituições por script (`ApplicationBeans`, construtores nos testes, tratadores de 409 e README): cada troca conferida 1× no script e relida no trecho. As evidências do F5-L2 permanecem inalteradas neste pacote reconciliado.
- Compilação de `domain` + `application` e do adaptador JPA com `-Xlint:all -Werror` (ver EXECUCAO).
