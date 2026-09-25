# F5-L4 — CONSUMIDORES

Data: 2026-09-24

- `ProjectRepository` tem dois implementadores: `ProjectPersistenceAdapter` em produção e
  `InMemoryProjectRepository` nos testes. Ambos recebem os três novos métodos.
- `ProjectService` continua sendo a única entrada da aplicação usada pelos controllers REST e GraphQL.
- O recálculo de status/métricas permanece anterior a todas as leituras de indicadores, inclusive agrupamentos e prazos.
- `ProjectIndicatorsRestController` ganha três rotas sob o mesmo prefixo; nenhum consumidor existente muda.
- `ProjectGraphQlController` adiciona três queries sem remover ou renomear contratos existentes.
- `kanban.graphqls` só recebe campos/tipos aditivos.
- O CI já executa `mvn clean verify`; o JaCoCo ligado ao lifecycle será executado pelo mesmo comando.
- Surefire/Failsafe já usam o agente do Mockito. O JaCoCo usa uma propriedade separada (`jacocoArgLine`) e os
  `argLine` compõem ambos os agentes.
- Frontend não é consumidor dos novos endpoints neste lote e permanece intocado.
