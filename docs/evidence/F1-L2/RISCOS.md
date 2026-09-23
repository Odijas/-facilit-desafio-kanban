# F1-L2 — RISCOS E LACUNAS

Data: 2026-09-22

- `[EXECUTADO]` Binding real de `ProjectStatus` em REST/GraphQL validado pelo gate do usuário.
- `[EXECUTADO]` `findByStatus(ProjectStatus, Pageable)` validado pela listagem real por status no gate do usuário.
- `[VERIFICADO]` Não foi criada migration: `projects.status` já possui índice `ix_projects_status` desde V1.
- `[VERIFICADO]` Não foi introduzido scheduler para recalcular status pelo mero passar do tempo; isso não é exigido pelo lote e ampliaria arquitetura/escopo.
- `[VERIFICADO]` O aviso Mockito da baseline continua pendência não bloqueante.
- `[VERIFICADO]` Padronização definitiva de erros e testes de integração/API pertencem ao F1-L3; neste lote o handler existente apenas transporta mensagens de domínio/aplicação.
