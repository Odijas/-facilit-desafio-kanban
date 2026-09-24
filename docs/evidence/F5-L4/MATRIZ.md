# F5-L4 — MATRIZ DE ACEITE

Data: 2026-09-24

| Item | Evidência objetiva |
|---|---|
| Média/quantidade por status | já existente e preservada; regressão pelo `mvn clean verify` |
| Por secretaria | REST + GraphQL + persistência + `ProjectIndicatorsIT` |
| Por responsável | REST + GraphQL + persistência + `ProjectIndicatorsIT` |
| Prazos 1–90 dias | REST + GraphQL; 0 e 91 rejeitados; `ProjectIndicatorsIT` |
| Média diferente de zero | integração cria atraso de 5 dias e espera média 2,5 |
| BDD formal | `transicoes.feature`, 12 linhas; gate lê `target/cucumber.json` |
| JaCoCo | baseline 91,05%; testes dirigidos por lacunas elevaram para 95,61%; `jacoco:check` bloqueia `LINE/COVEREDRATIO < 0.95` |
| Frontend | nenhuma alteração |
| Migration | nenhuma nova; índices necessários já presentes |
