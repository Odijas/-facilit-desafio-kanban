# F5-Q1 — RISCOS E LACUNAS

Data: 2026-09-25

1. `[EXECUTADO]` Maven/Java 25 deixou de ser lacuna: gate real executou 177 unitários/BDD e 52 testes de integração sem falhas.
2. `[EXECUTADO]` Docker/Testcontainers deixou de ser lacuna: banco novo, migrations V1–V7 e API real ficaram GREEN.
3. `[EXECUTADO]` O RED 1 do helper OpenAPI foi corrigido dentro do próprio Q1; a reexecução integral ficou GREEN sem relaxar campo ↔ schema.
4. `[VERIFICADO]` A documentação usa o texto real de autorização (`Apenas o administrador pode realizar esta operação.`), não o texto inicialmente previsto no plano.
5. `[VERIFICADO]` README, ADR 0002, CHANGELOG e AI_USAGE permaneciam no estado da v2.0.1 ao fechar Q1 por faseamento; a atualização pertence ao F5-Q2.
6. `[EXECUTADO]` Fechamento Gitflow do Q1 confirmado: `HEAD=origin/hotfix/2.0.2=9ab3bbd81dcf06f6251651cb4dc50aad1a8cf1f5`, árvore limpa na captura, merge `--no-ff` e push no GitHub.
7. `[VERIFICADO]` A migração dos testes existentes acompanhou o refactor D1, preservando a compilabilidade dos commits conforme a decisão registrada em `DECISOES.md`.
