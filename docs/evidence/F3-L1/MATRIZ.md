# F3-L1 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-22

| Requisito | Implementação | Teste/verificação | Estado |
|---|---|---|---|
| Quantidade por status | `ProjectIndicators` + agregação JPA | service test + `KanbanApiIT` + gate runtime | CANDIDATE |
| Média de atraso por status | `avg(delayDays)` por status | service test + REST/GraphQL IT | CANDIDATE |
| Resumos low-risk | total + projetos com atraso | painel + API/IT | CANDIDATE |
| CRUD Secretaria | service/JPA/REST/GraphQL | `SecretariatServiceTest`, IT e UI test | CANDIDATE |
| Bloqueio de Secretaria referenciada | `existsBySecretariatId` + `ConflictException` | unitário + IT 409 + gate | CANDIDATE |
| Filtro secretaria | `ProjectFilter` + query JPA | IT REST/GraphQL + UI | CANDIDATE |
| Filtro responsável | mesmo pipeline | IT REST/GraphQL + UI | CANDIDATE |
| Filtro período | interseção do período previsto | unitário + IT REST/GraphQL + UI | CANDIDATE |
| Filtro texto | substring case-insensitive | IT REST/GraphQL + API/UI | CANDIDATE |
| UI indicadores | `features/indicators` | component test | CANDIDATE |
| UI Secretaria | `features/secretariats` | create/update/delete component test | CANDIDATE |
| UI filtros | `ProjectFiltersBar` | board + API tests | CANDIDATE |
| Baseline F2 | sem relaxar auth/CSRF/Kanban | suíte completa + smoke | CANDIDATE |
| Reconciliação rev2 | órfão removido + gate bloqueia se presente | grep + `bash -n` + gate local | CANDIDATE |
| Filtro de texto sem desmontar o quadro | `placeholderData: keepPreviousData` | `KanbanBoard.test.tsx` RED→GREEN | CANDIDATE |
| Busca sem parâmetros anuláveis | `Specification` no adapter | `KanbanApiIT` (2 testes RED no rev2) + gate `F3_L1_LIST_UNFILTERED_GREEN` | CANDIDATE |
| Gate GraphQL válido contra o schema | variável `$secretariatId: ID!` | graphql-js RED→GREEN + gate local | CANDIDATE |
