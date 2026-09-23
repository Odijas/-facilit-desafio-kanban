# F0-L2 — RISCOS E LACUNAS

Data: 2026-09-21.

- [VERIFICADO: plano F0-L3 · 2026-09-21] O F0-L2 ainda não garante coerência entre `ProjectStatus` e datas; essa responsabilidade pertence ao motor de regras do F0-L3.
- [VERIFICADO: plano F0-L3 · 2026-09-21] Dias de atraso e percentual de tempo restante ainda não fazem parte do modelo final calculado; entram no F0-L3.
- [VERIFICADO: plano F0-L3 · 2026-09-21] Regras de consistência entre datas permanecem deliberadamente fora desta rodada.
- [VERIFICADO: desafio · 2026-09-21] Formato e unicidade de email precisam ser validados no CRUD posterior; no F0-L2 o domínio apenas rejeita email vazio.
- [VERIFICADO: desafio · 2026-09-21] Secretaria não possui atributos detalhados especificados além do vínculo; o modelo inicial usa somente `id`, `name` e auditoria para não inventar contrato.
- [EXECUTADO: ambiente do usuário · 2026-09-21] Build Maven em Java 25 e regressão Docker/REST/GraphQL foram comprovados no gate GREEN.
