# F3-L1 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-22

| Requisito | Implementação | Teste/verificação | Estado |
|---|---|---|---|
| Quantidade por status | `ProjectIndicators` + agregação JPA | service test + `KanbanApiIT` + gate runtime | GREEN |
| Média de atraso por status | `avg(delayDays)` por status | service test + REST/GraphQL IT | GREEN |
| Resumos low-risk | total + projetos com atraso | painel + API/IT | GREEN |
| CRUD Secretaria | service/JPA/REST/GraphQL | `SecretariatServiceTest`, IT e UI test | GREEN |
| Bloqueio de Secretaria referenciada | `existsBySecretariatId` + `ConflictException` | unitário + IT 409 + gate | GREEN |
| Filtro secretaria | `ProjectFilter` + query JPA | IT REST/GraphQL + UI | GREEN |
| Filtro responsável | mesmo pipeline | IT REST/GraphQL + UI | GREEN |
| Filtro período | interseção do período previsto | unitário + IT REST/GraphQL + UI | GREEN |
| Filtro texto | substring case-insensitive | IT REST/GraphQL + API/UI | GREEN |
| UI indicadores | `features/indicators` | component test | GREEN |
| UI Secretaria | `features/secretariats` | create/update/delete component test | GREEN |
| UI filtros | `ProjectFiltersBar` | board + API tests | GREEN |
| Baseline F2 | sem relaxar auth/CSRF/Kanban | suíte completa + smoke | GREEN |
| Reconciliação rev2 | órfão removido + gate bloqueia se presente | grep + `bash -n` + gate local | GREEN |
| Filtro de texto sem desmontar o quadro | `placeholderData: keepPreviousData` | `KanbanBoard.test.tsx` RED→GREEN | GREEN |
| Busca sem parâmetros anuláveis | `Specification` no adapter | `KanbanApiIT` (2 testes RED no rev2) + gate `F3_L1_LIST_UNFILTERED_GREEN` | GREEN |
| Gate GraphQL válido contra o schema | variável `$secretariatId: ID!` | graphql-js RED→GREEN + gate local | GREEN |
