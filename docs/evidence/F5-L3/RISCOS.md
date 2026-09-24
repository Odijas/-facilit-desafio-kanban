# F5-L3 — RISCOS E LACUNAS

Data: 2026-09-24

- `[VERIFICADO: saída real do gate local fornecida pelo usuário · 2026-09-24]` os testes novos (`@WebMvcTest`, `@GraphQlTest`, Mockito, `@DataJpaTest`, Testcontainers), `mvn clean verify`, Docker e V7 foram executados localmente: `F5_L3_BACKEND_GREEN`, `F5_L3_DOCKER_GREEN` e exit code 0.
- `[VERIFICADO: saída real do gate local fornecida pelo usuário · 2026-09-24]` `TransactionIT` executou 4 testes com sucesso, incluindo concorrência otimista; o gate reportou 0 falhas e 0 erros.
- `[VERIFICADO: saída real do gate local fornecida pelo usuário · 2026-09-24]` o cenário concorrente de `TransactionIT` completou no gate sem travamento e sem erro.
- `[VERIFICADO: saída real do gate local fornecida pelo usuário · 2026-09-24]` os quatro slices REST e os três GraphQL executaram com sucesso no gate; a segurança ponta a ponta permanece coberta pelos testes de integração existentes, fora da responsabilidade desses slices.
- `[VERIFICADO: README.md:257-267 · 2026-09-24]` a API não recebe a versão do cliente; o `@Version` não detecta edição entre leitura na tela e envio (README, Limitações).
- `[VERIFICADO: docs/evidence/F5-L3/DECISOES.md:16-17 · 2026-09-24]` o recálculo feito dentro de um caso de uso que falha é desfeito junto. Custo: a próxima leitura refaz o recálculo. Sem efeito em dados (idempotente).
- `[VERIFICADO: backend/src/test/java/br/com/facilit/kanban/integration/StatusTransitionApiIT.java:65-82 · 2026-09-24]` `StatusTransitionApiIT` usa "hoje" do `Clock` da aplicação. Um teste que atravesse a meia-noite de São Paulo pode falhar por data; o mesmo vale para `KanbanApiIT` e `SecurityApiIT`.
- `[VERIFICADO: docs/evidence/F5-L3/DECISOES.md:51-58 · 2026-09-24]` fora do lote permanecem os itens do F5-L4/F5-L5 definidos pelo plano, incluindo indicadores/BDD/cobertura e release/versionamento.
