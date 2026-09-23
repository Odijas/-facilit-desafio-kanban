# F2-L3 — DECISÕES

Data: 2026-09-22

## D01 — Drag-and-drop nativo, sem dependência nova

`[VERIFICADO: docs/governance/PROMPT-EXECUTIVO-KANBAN-v1.0.md:264-295 · 2026-09-22]` F2-L3 exige quadro Kanban e drag-and-drop.

`[VERIFICADO: frontend/package.json:18-39 · 2026-09-22]` A baseline não contém biblioteca de drag-and-drop. Para manter o diff mínimo e evitar nova dependência, o quadro usa a API HTML Drag and Drop por `draggable`, `dragstart`, `dragover` e `drop`.

## D02 — Status da UI deriva do contrato real

`[VERIFICADO: backend/src/main/java/br/com/facilit/kanban/domain/project/ProjectStatus.java:3-8 · 2026-09-22]` Os valores reais do backend são `NOT_STARTED`, `IN_PROGRESS`, `OVERDUE` e `COMPLETED`. A UI traduz apenas os rótulos visuais para `A iniciar`, `Em andamento`, `Atrasado` e `Concluído`.

## D03 — Reutilização do CSRF já validado

`[VERIFICADO: frontend/src/api/auth.ts:76-151 · 2026-09-22]` O mecanismo que obtém descritor/cookie CSRF é reutilizado por `getCsrfHeaders()`. Todas as mutações do Kanban usam o mesmo contrato.

## D04 — Estado remoto continua em React Query

`[VERIFICADO: frontend/package.json:22 · 2026-09-22]` Projetos, responsáveis, sessão e health são estado remoto. Após mutações, a UI invalida somente as chaves relacionadas; Zustand não foi introduzido.

## D05 — CRUD essencial demonstrável

`[VERIFICADO: backend/src/main/java/br/com/facilit/kanban/delivery/rest/ProjectRestController.java:99-142; backend/src/main/java/br/com/facilit/kanban/delivery/rest/ResponsibleRestController.java:40-120 · 2026-09-22]` A UI expõe criação, leitura, edição e exclusão de projeto, cadastro/listagem de responsáveis e transição por status. Regras de bloqueio permanecem no backend.

## D06 — Biome write antes do gate de leitura

`[EXECUTADO PELO USUÁRIO · 2026-09-22]` `biome format --write .` não cobriu `assist/source/organizeImports`; o gate bloqueou no lint.

`[CORRIGIDO: frontend/package.json:14 · 2026-09-22]` `pnpm format` usa `biome check --write .`, seguido por `pnpm lint`, typecheck, testes e build.

## D07 — Adaptadores explícitos na fronteira React Query → API

`[EXECUTADO PELO USUÁRIO · 2026-09-22]` Três testes mostraram que funções passadas diretamente como `mutationFn` receberam também `MutationFunctionContext`.

`[CORRIGIDO: frontend/src/features/kanban/KanbanBoard.tsx:68-136 · 2026-09-22]` Queries e mutations chamam a camada `api` por funções adaptadoras. O contexto do React Query não atravessa a fronteira da API e as assinaturas de `api/kanban.ts` permanecem inalteradas.

## D08 — App como composition root, não como componente monolítico

`[CORRIGIDO: frontend/src/App.tsx:1-18 · 2026-09-22]` `App` decide somente qual página renderizar. Navegação, autenticação, dashboard, loading e Kanban foram separados por responsabilidade.

`[VERIFICADO: frontend/src/features/auth/LoginPage.tsx; frontend/src/features/auth/ProtectedHome.tsx; frontend/src/features/dashboard/DashboardPage.tsx; frontend/src/app/navigation.ts · 2026-09-22]` Estado permanece no menor componente que o possui, preservando fluxo unidirecional e SRP sem criar camada ou interface especulativa.

## D09 — Kanban separa orquestração de apresentação

`[CORRIGIDO: frontend/src/features/kanban/KanbanBoard.tsx; KanbanColumn.tsx; ProjectDialog.tsx; ResponsibleDialog.tsx; DeleteProjectDialog.tsx · 2026-09-22]` `KanbanBoard` orquestra queries, mutations e estado de interação. Coluna e diálogos possuem somente responsabilidades visuais/formulário correspondentes.

## D10 — Roteamento nativo permanece isolado

`[VERIFICADO: frontend/package.json:18-39 · 2026-09-22]` React Router não está instalado na baseline. Introduzi-lo nesta correção exigiria nova resolução de dependência sem execução local neste ambiente. A History API existente foi isolada em `frontend/src/app/navigation.ts`, deixando eventual migração localizada e sem espalhar navegação pelos componentes.
