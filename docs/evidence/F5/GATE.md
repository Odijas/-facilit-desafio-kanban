# F5-L5 — GATE DE FREEZE E RELEASE

Data: 2026-09-24

Estado ao preparar o lote: **PENDENTE DE EXECUÇÃO DO USUÁRIO**.

O F5-L5 só fica GREEN quando:

- `release/2.0.0` nasce da `develop` sincronizada em `17aebc8` e contém somente versionamento/documentação do L5;
- `VERIFICACAO-USUARIO.md` termina com todos os marcadores `F5_*_GREEN`, `=== F5-L5 GREEN ===` e `Resultado: exit code 0`;
- a saída real é normalizada e versionada em `SAIDA-GATE.txt`;
- a release é mesclada por `--no-ff` na `main`, recebe tag anotada `v2.0.0` e é mesclada de volta na `develop`;
- o GitHub, repositório público exigido para a entrega, contém `release/2.0.0`, `main`, `develop` e a tag; o GitLab permanece espelho secundário não bloqueante;
- `VERIFICACAO-RELEASE.md` confirma CI GREEN da `main` e página pública do repositório;
- a saída real da verificação final é versionada em `SAIDA-RELEASE.txt`.

O gate de freeze reexecuta frontend, backend, JaCoCo 95%, BDD, Docker com banco limpo e migrations V1–V7, smoke REST/GraphQL, indicadores F5, autenticação, segurança, Swagger, Prometheus/Grafana, logs, análise estática, histórico e CI da branch de release.
