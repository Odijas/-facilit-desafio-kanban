# F5-C3 — FONTES RAG

Data: 2026-09-24

1. PDF do desafio: entrega com README, `AI_USAGE.md` (Etapa 4) e coleções Postman/Insomnia; Etapa 3 com novos endpoints.
2. `ADERENCIA-FINAL.md`: lacunas 6 a 9 e as inconsistências entre documentação e código.
3. `docs/evidence/F5/VERIFICACAO-USUARIO.md` e `VERIFICACAO-RELEASE.md`:
   - títulos de seção exigidos em README, AI_USAGE, AUDITORIA e CHANGELOG;
   - links relativos;
   - credenciais vazias na coleção;
   - Conventional Commits;
   - texto `F5-L5 — Release \`v2.0.0\`` no README da tag.
4. Código: `ProjectIndicatorsRestController`, `ProjectGroupIndicatorResponse`, `ProjectDeadlinesResponse`, `ProjectService.deadlines`, `ProjectJpaRepository.findDeadlines`, `kanban.graphqls` (páginas).
5. registry.npmjs.org, pacote `newman`: `dist-tags.latest = 6.2.2` (2026-01-16), `engines.node >= 16`, sem aviso de depreciação.
