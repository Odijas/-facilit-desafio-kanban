# F3-L4 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

Data: 2026-09-23

| Requisito | Implementação | Teste/verificação | Estado |
|---|---|---|---|
| CI no GitHub Actions: build e testes | `.github/workflows/ci.yml`, jobs `frontend` e `backend` | actionlint (aqui e no gate local); pipeline real (gate CI) | GREEN |
| Análise automatizada aplicável | Biome, TypeScript strict, `check:strict`, `javac -Xlint:all -Werror`, job `repository` (`git diff --check`, armazenamento de navegador, arquivos sensíveis) | gate local e pipeline | GREEN |
| Workflow seguro | `permissions: contents: read`, SHA completo, `persist-credentials: false`, sem `pull_request_target`, runner próprio ou segredos | política no gate local | GREEN |
| Tipagem estrita como script do projeto | `frontend/scripts/check-strict-types.mjs` + `pnpm check:strict` | sonda RED (`any`/`as`) → exit 1; árvore real → `STRICT_TYPES_GREEN 31` | GREEN |
| README completo do desafio | `README.md` (visão geral, decisões, regras, Docker, testes, Swagger, segurança, observabilidade, CI, estrutura, IA, limitações) | `F3_L4_DOCS_GREEN` | GREEN |
| `AI_USAGE.md` (Etapa 4) | ferramentas, estruturação (SDD/prompts executivos), divisão de papéis, sugestões rejeitadas, erros pegos, trecho de prompt | `F3_L4_DOCS_GREEN` | GREEN |
| ADR de agente/RAG (Etapa 5) | `docs/adr/0001-camada-ia-agente-rag.md` | `F3_L4_DOCS_GREEN` | GREEN |
| Diagramas | mermaid de arquitetura (README) e de componentes da IA (ADR) | `F3_L4_DOCS_GREEN` | GREEN |
| Coleção Postman/Insomnia | `docs/api/facilit-kanban.postman_collection.json` (21 requisições) | newman contra o Compose real no rev1 e no rev2 (usuário, 0 falhas); mesmos 21 cenários por `curl` no gate rev3 | GREEN |
| Histórico Git granular em Gitflow | `RECONSTRUCAO-HISTORICO.md` (feature por lote, commits por área, Conventional Commits) | teste em repositório simulado (aqui); `F3-L4 HISTORICO GREEN` (usuário) | GREEN |
| Repositório público no GitHub com histórico | `MIGRACAO-GITHUB.md` (`clone --bare` + `push --mirror`) | gate CI (`HISTORY`, `REPOSITORY`) | GREEN |
| Sem segredo publicado | varredura de árvore e histórico antes da publicação | gate local (`REPOSITORY`) e gate CI (`.env` 404) | GREEN |
| Promoção documental do F3-L3 | `docs/evidence/F3-L3`, README, `REPLANEJAMENTO-F3.md` §2 | evidência do gate do usuário | GREEN |
