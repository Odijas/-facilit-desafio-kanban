# F5-Q2 — DECISÕES

Data: 2026-09-25

1. `[VERIFICADO]` Escopo restrito a documentação e promoção do Q1, conforme o plano. Nenhum arquivo de produção, teste, migration, frontend ou CI é alterado.
2. `[VERIFICADO]` E1 é corrigido no README usando os nomes reais dos métodos existentes no código; não se renomeiam testes para fazer a documentação caber.
3. `[VERIFICADO]` E2 documental passa a citar o teste específico criado no Q1: `ProjectServiceTest.updateCanClearActualStartWithoutTransitionConfirmationAndRecalculatesStatus`.
4. `[VERIFICADO]` O ADR 0002 deixa de citar `list`/`listByStatus`, removidos no Q1, e mantém somente os pontos reais `get`, `search` e `indicators`.
5. `[EXECUTADO]` A promoção do Q1 usa exclusivamente a saída GREEN e o fechamento fornecidos pelo usuário; nenhuma execução Maven/Docker é atribuída ao ambiente da IA.
6. `[VERIFICADO]` `ADERENCIA-V2.0.1.md` é versionado byte a byte a partir do arquivo original fornecido; seu conteúdo histórico não é reescrito para refletir a 2.0.2.
7. `[VERIFICADO]` O CHANGELOG prepara `[2.0.2]` porque o plano exige a entrada neste lote; a tag/release só ocorre no F5-Q3.
8. `[VERIFICADO]` A releitura integral obrigatória do ADR 0002 encontrou três referências de teste antigas/inexistentes no próprio arquivo. Como o ADR já está no escopo Q2 e as referências tratam da mesma regra documentada, foram corrigidas para métodos reais `line05...`/`line02...`; nenhum comportamento foi alterado.
