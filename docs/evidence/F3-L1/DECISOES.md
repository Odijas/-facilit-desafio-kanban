# F3-L1 — DECISÕES

Data: 2026-09-22

- `[VERIFICADO]` REST e GraphQL reutilizam `ProjectService`/`ProjectFilter` para filtros e `ProjectService.indicators()` para indicadores; não foi criado caso de uso duplicado por protocolo.
- `[VERIFICADO]` Secretaria ganhou serviço/porta próprios porque passou de referência de domínio para CRUD real; `ResponsibleService` continua apenas validando a referência.
- `[VERIFICADO]` exclusão de Secretaria é bloqueada enquanto houver Responsável associado, evitando depender de erro de FK como regra de aplicação.
- `[VERIFICADO]` período significa interseção com o período previsto do projeto: `plannedEnd >= plannedFrom` e `plannedStart <= plannedTo`.
- `[VERIFICADO]` indicadores usam as métricas de projeto já persistidas pela arquitetura aprovada: quantidade e média de `delayDays` por status, além de total e projetos com atraso.
- `[VERIFICADO]` índices existentes já cobrem status/ordenação, secretaria, responsável e datas. Busca textual é substring case-insensitive; não foi adicionada extensão PostgreSQL nova sem prova runtime no lote de baixo risco.
- `[VERIFICADO]` frontend manteve separação por feature e camada API independente de React Query; callbacks de mutation/query permanecem adaptadores explícitos.
- `[VERIFICADO]` nenhuma dependência foi adicionada.

## Rev2 — 2026-09-22

- `[EXECUTADO]` rev2 montado sobre o snapshot local do usuário (fonte primária), não sobre o F2-L3 não formatado usado pelo candidate.
- `[EXECUTADO]` removido `application/responsible/SecretariatRepository.java`: substituído por `application/secretariat/SecretariatRepository.java`, sem consumidores restantes. O gate passa a bloquear se o órfão existir.
- `[EXECUTADO]` defeito de produção corrigido em `KanbanBoard.tsx`: a cada tecla no filtro a query de projetos mudava de chave, voltava a `isPending` e o quadro inteiro (incluindo o campo de busca) era substituído pelo estado de carregamento. Correção mínima: `placeholderData: keepPreviousData` na query de projetos `[VERIFICADO: node_modules/@tanstack/query-core 5.102.8 — keepPreviousData exportado; QueryObserverPlaceholderResult.isPending: false]`.
- `[EXECUTADO]` `KanbanBoard.test.tsx`: o F3-L1 introduziu um segundo campo rotulado "Secretaria" (filtro); o teste passou a buscar o campo dentro do diálogo "Novo responsável". Manutenção de teste invalidado pela mudança do lote.
- `[EXECUTADO]` `SecretariatPanel.test.tsx`: após salvar, o diálogo MUI em transição de saída mantém o restante da página fora da árvore de acessibilidade; o teste passou a aguardar o fechamento do diálogo antes da ação seguinte, como o usuário real.
- `[DECISÃO DO USUÁRIO · 2026-09-22]` "Serviço de autenticação do responsável" (diferencial do desafio) entra na F3. Hoje só existe a conta ADMIN (`app_users.role IN ('ADMIN')`, V4). Classificação: pendência aceita, fora do rev2; exige desenho aprovado em gate antes de qualquer código.

## Rev3 — 2026-09-22

- `[VERIFICADO]` a busca de projetos deixou de usar JPQL com parâmetros anuláveis. `ProjectJpaRepository` passa a estender `JpaSpecificationExecutor<ProjectJpaEntity>` e `ProjectPersistenceAdapter` monta uma `Specification` que só adiciona o predicado do filtro informado. Nenhum parâmetro nulo é enviado ao PostgreSQL.
- `[VERIFICADO: docs.spring.io/spring-data/jpa/reference/3.5/jpa/specifications.html]` `JpaSpecificationExecutor.findAll(Specification, Pageable)` é API pública documentada da versão 3.5.x.
- `[VERIFICADO: spring-data-jpa 3.5.13 SimpleJpaRepository.getCountQuery]` a mesma `Specification` é aplicada à consulta de contagem; com `query.distinct(true)` a contagem usa `countDistinct(root)`.
- `[VERIFICADO]` o join com responsáveis só é criado quando há filtro por responsável ou secretaria; nesse caso a consulta é `distinct`. Sem esses filtros não há join nem duplicidade.
- `[VERIFICADO]` semântica preservada: status por igualdade; período como interseção (`plannedEnd >= plannedFrom`, `plannedStart <= plannedTo`); texto como substring com `lower` no banco dos dois lados; ordenação `name, id` inalterada.
- `[VERIFICADO]` contratos inalterados: porta `ProjectRepository`, `ProjectFilter`, REST, GraphQL e dublê `InMemoryProjectRepository`.
- `[VERIFICADO]` decisão de técnica, não de paradigma: continua repositório Spring Data dentro do adapter de infraestrutura, sem camada nova, sem classe nova. Registrada aqui para rejeição explícita no gate, se o usuário discordar.

## Rev4 — 2026-09-22

- `[EXECUTADO]` correção mínima no gate: `$secretariatId: ID` → `$secretariatId: ID!`. Uma variável não nula pode ser usada tanto em `projects(secretariatId: ID)` quanto em `secretariat(id: ID!)`; a validação por graphql-js contra o schema real confirmou. Nenhum arquivo de produção alterado no rev4.
