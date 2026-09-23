# F4 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-23

A matriz completa do desafio está em [`AUDITORIA.md`](AUDITORIA.md). Itens próprios da F4:

| Requisito da F4 (KANBAN v1.0) | Onde | Verificação | Estado |
|---|---|---|---|
| Build limpo e testes completos | backend e frontend | `F4_BACKEND_GREEN`, `F4_FRONTEND_GREEN` | CANDIDATE |
| Análise estática e `git diff --check` | workflow, repositório, histórico, commits, docs | `F4_STATIC_GREEN` | CANDIDATE |
| Subida integral por Docker Compose com banco limpo e migrations do zero | projeto `facilit-kanban-f4` | `F4_DOCKER_CLEAN_DB_GREEN` | CANDIDATE |
| Smoke REST, GraphQL, autenticação, UI e Swagger | API e frontend | `F4_API_GREEN`, `F4_RESPONSIBLE_AUTH_GREEN`, `F4_UI_SWAGGER_GREEN` | CANDIDATE |
| Prometheus e Grafana | sobreposição de observabilidade | `F4_OBSERVABILITY_GREEN` | CANDIDATE |
| Revisão de segurança | [`REVISAO-SEGURANCA.md`](REVISAO-SEGURANCA.md) | `F4_SECURITY_GREEN` + checagem das 10 categorias | CANDIDATE |
| Revisão de README, AI_USAGE e diagramas | documentação | `F4_STATIC_GREEN` | CANDIDATE |
| Revisão do histórico de commits | Git | Conventional Commits em `F4_STATIC_GREEN`; refs em `F4_RELEASE_REMOTES_GREEN` | CANDIDATE |
| Auditoria requisito → implementação → teste → evidência | [`AUDITORIA.md`](AUDITORIA.md) | checagem de seções e links | CANDIDATE |
| Pipeline verde na entrega | GitHub Actions | `F4_CI_GREEN`, `F4_RELEASE_CI_GREEN` | CANDIDATE |
| Release `v1.0.0` na `main` | Gitflow | `VERIFICACAO-RELEASE.md` | CANDIDATE |
| Promoção documental do F3-L4 | `docs/evidence/F3-L4`, README, `REPLANEJAMENTO-F3.md` | evidência das 4 etapas do usuário | GREEN |
