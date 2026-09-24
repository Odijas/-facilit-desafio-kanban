# F5-L4 — GATE

Data: 2026-09-24

Estado: **CANDIDATA FINAL**. O lote é GREEN/FECHADO somente quando `SAIDA-GATE-FINAL.txt` for versionado com todos os marcadores abaixo e `Resultado: exit code 0`.

Critérios de fechamento:

- branch `feature/f5-l4-indicadores-bdd-cobertura` sobre `f786e7c`;
- somente os 35 arquivos do F5-L4 alterados antes da evidência final;
- `mvn -B -ntp clean verify` GREEN;
- testes unitários/BDD e integração sem falhas, erros ou ignorados;
- Cucumber com exatamente 12 cenários e todos os passos `passed`;
- `ProjectIndicatorsIT` GREEN;
- JaCoCo `LINE/COVEREDRATIO >= 0.95`, também imposto por `jacoco:check`;
- nenhum autoanexo dinâmico do Mockito;
- nenhuma migration nova; frontend intocado;
- `git diff --check` GREEN.

Marcadores obrigatórios: `F5_L4_PRECONDITIONS_GREEN`, `F5_L4_BACKEND_GREEN`, `F5_L4_BDD_GREEN`, `F5_L4_JACOCO_GREEN`, `F5_L4_STATIC_GREEN`, `=== F5-L4 GREEN ===` e `Resultado: exit code 0`.
