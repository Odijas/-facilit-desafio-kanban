# F5-L5 — GATE DE FREEZE E RELEASE

Data: 2026-09-24

Estado: **GREEN**.

- `[EXECUTADO PELO USUÁRIO · 2026-09-24]` Freeze: `=== F5-L5 GREEN ===` e exit code 0 na `release/2.0.0` @ `9eca274` (`SAIDA-GATE.txt`); CI da release na run 36082256488.
- `[EXECUTADO PELO USUÁRIO · 2026-09-24]` Release: `main` @ `3dac1ea` = tag anotada `v2.0.0`, back-merge na `develop`, GitHub e GitLab atualizados; CI da `main` verde (run 36082635036).
- `[EXECUTADO PELO USUÁRIO · 2026-09-24]` A verificação pública (`VERIFICACAO-RELEASE.md`) terminou GREEN depois de corrigido um falso negativo do próprio script (`curl | grep -q` com `pipefail`). A correção e a saída (`SAIDA-RELEASE.txt`) foram versionadas na `develop`, depois da tag.

Critérios, como definidos ao preparar o lote:

O F5-L5 só fica GREEN quando:

- `release/2.0.0` nasce da `develop` sincronizada em `17aebc8` e contém somente o versionamento e a documentação do L5 e os lotes de correção da release F5-C1 a F5-C3 (`docs/governance/PLANO-CORRECAO-RELEASE-2.0.0.md`), cada um GREEN no próprio gate e mesclado por `--no-ff` a partir de `bugfix/2.0.0-*`;
- `VERIFICACAO-USUARIO.md` termina com todos os marcadores `F5_*_GREEN`, `=== F5-L5 GREEN ===` e `Resultado: exit code 0`;
- a saída real é normalizada e versionada em `SAIDA-GATE.txt`;
- a release é mesclada por `--no-ff` na `main`, recebe tag anotada `v2.0.0` e é mesclada de volta na `develop`;
- o GitHub, repositório público exigido para a entrega, contém `release/2.0.0`, `main`, `develop` e a tag; o GitLab permanece espelho secundário não bloqueante;
- `VERIFICACAO-RELEASE.md` confirma CI GREEN da `main` e página pública do repositório;
- a saída real da verificação final é versionada em `SAIDA-RELEASE.txt`.

O gate de freeze reexecuta frontend, backend, JaCoCo 95%, BDD, Docker com banco limpo e migrations V1–V7, smoke REST/GraphQL, indicadores F5, autenticação, segurança, Swagger, Prometheus/Grafana, logs, análise estática, histórico e CI da branch de release.
