# F5-Q1 — CONSUMIDORES MAPEADOS

Data: 2026-09-25

## D1 — antes

`[EXECUTADO]` busca no pacote base por `listByStatus`, `findByStatus`, `projectRepository.findAll`, `service.list` e `projectService.list` encontrou consumidores de projeto em:

- `ProjectService` → `ProjectRepository.findAll/findByStatus`;
- `ProjectPersistenceAdapter` → `ProjectJpaRepository.findByStatus`;
- `ProjectServiceTest`;
- `ProjectServiceMockitoTest`;
- `ProjectScheduleRefresherTest`;
- `ScheduleFreshnessIT`;
- `ProjectPersistenceAdapterIT`;
- `InMemoryProjectRepository`.

A mesma busca também encontrou métodos `list` de **Responsável** e **Secretaria**; eles são contratos distintos e não foram alterados.

## D1 — depois

`[EXECUTADO]` a busca equivalente em `backend/src/main` e `backend/src/test` não encontra mais `ProjectService.list/listByStatus`, `ProjectRepository.findAll/findByStatus`, `ProjectPersistenceAdapter.findAll/findByStatus` nem `ProjectJpaRepository.findByStatus` para projetos.

`[EXECUTADO]` os consumidores de produção de listagem de projetos permanecem no caminho canônico:

```text
ProjectRestController.java:148: service.search(filter, pageQuery)
ProjectGraphQlController.java:65: service.search(filter, pageQuery)
```

`[VERIFICADO]` Todos os testes consumidores removidos foram migrados para `search`; nenhum arquivo de teste foi apagado.
