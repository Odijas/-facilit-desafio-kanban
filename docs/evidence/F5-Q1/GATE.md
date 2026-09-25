# F5-Q1 — GATE

Data: 2026-09-25

Estado: **GREEN**.

## Execução decisiva

`[EXECUTADO: gate local fornecido pelo usuário em 2026-09-25]`

- escopo: 27 arquivos, versão 2.0.2 em `pom.xml`, `Dockerfile` e `package.json`;
- unitários/BDD: 25 classes, 177 testes, 0 falhas/erros/ignorados;
- integração: 12 classes, 52 testes, 0 falhas/erros/ignorados;
- JaCoCo: 1716/1803 linhas = 95,17%;
- BDD: 12 cenários GREEN;
- Docker com banco novo: migrations V1–V7;
- `/api-docs`: exemplos 400/403/404 coerentes por operação;
- 404 real de Secretaria igual ao exemplo OpenAPI;
- 422 da linha 2 sem `null`, orientando `plannedStart`/`plannedEnd`;
- marcadores `F5_Q1_SCOPE_GREEN`, `F5_Q1_BACKEND_GREEN`, `F5_Q1_DOCKER_API_GREEN`, `=== F5-Q1 GREEN ===` e `Resultado: exit code 0`.

Saída registrada em `SAIDA-GATE.txt`.

## RED 1 e correção

`[EXECUTADO: saída fornecida pelo usuário em 2026-09-25]` A primeira execução falhou somente em `OpenApiContractIT.errorExamplesMatchTheOperationResourceInputAndAuthorizationRule` para `POST /api/v1/projects`, porque o helper novo assumia exclusivamente `examples.VALIDATION_ERROR.value`.

`[VERIFICADO]` A correção manteve a validação semântica campo ↔ schema e passou a aceitar as formas OpenAPI realmente geradas (`example` ou `examples`). A reexecução integral acima ficou GREEN.

## Fechamento Gitflow

`[EXECUTADO: saída fornecida pelo usuário em 2026-09-25]` Foram criados commits semânticos do Q1, a `bugfix/2.0.2-q1-code` foi mesclada com `--no-ff` na `hotfix/2.0.2` e ambas foram publicadas no GitHub.

`[EXECUTADO: FECHAMENTO.txt · 2026-09-25]` `HEAD` e `origin/hotfix/2.0.2` são `9ab3bbd81dcf06f6251651cb4dc50aad1a8cf1f5`; o `git status --short` não produziu linha antes desses hashes, portanto a árvore estava limpa na captura.
