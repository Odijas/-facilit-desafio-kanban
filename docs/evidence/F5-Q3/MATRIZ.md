# F5-Q3 — MATRIZ ETAPA → VERIFICAÇÃO → EVIDÊNCIA

Data: 2026-09-25

| Etapa | Verificação | Evidência |
|---|---|---|
| Promoção F5-Q2 | marcadores GREEN, CI Q1 e fechamento Gitflow | `docs/evidence/F5-Q2/SAIDA-GATE.txt`, `FECHAMENTO.txt` |
| Freeze da `hotfix/2.0.2` | `VERIFICACAO-USUARIO.md`: frontend, backend, JaCoCo/BDD, estática, Docker/banco novo, UI/Swagger, REST/GraphQL, autenticação, segurança, observabilidade e CI | `SAIDA-GATE.txt` depois da execução |
| E1 — referências documentais | nomes citados em README/AI_USAGE resolvidos contra os testes reais | `F5_Q3_STATIC_GREEN` |
| E3 — OpenAPI contextual | `OpenApiContractIT` + validação do `/api-docs` do Docker por operação | `F5_Q3_BACKEND_GREEN` + `F5_Q3_UI_SWAGGER_GREEN` |
| Release `v2.0.2` | refs, tag anotada, árvore da main, back-merge, GitHub e CI | `VERIFICACAO-RELEASE.md` → `SAIDA-RELEASE.txt` |
| Auditoria final | `git archive v2.0.2`, análise principal + agente independente | `docs/evidence/F5/ADERENCIA-V2.0.2.md` somente depois da auditoria |
