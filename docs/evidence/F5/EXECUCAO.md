# F5-L5 — EXECUÇÃO

Data: 2026-09-24

## Entrada

[EXECUTADO · 2026-09-24] snapshot de entrada: 392 arquivos; branch `develop`; `HEAD`, `origin/develop` e `gitlab/develop` em `17aebc8e5de9e58ec0d18276a7b74fcefe8ccb69`; status limpo.

[EXECUTADO · 2026-09-24] SHA-256 do snapshot: `10601a05ca3f1c46e44f139f24239c5f35b62ba7c1de2b1f2e2ba07ab356987c`.

## Baselines promovidas

- `[EXECUTADO PELO USUÁRIO · 2026-09-24]` F5-L1 GREEN: 79 unitários, 17 integração, migrations V1–V6 e 24 verificações API.
- `[EXECUTADO PELO USUÁRIO · 2026-09-24]` F5-L2 GREEN/revalidado: 91 unitários, 20 integração, contrato 422/confirmação, OpenAPI e logs de negócio.
- `[EXECUTADO PELO USUÁRIO · 2026-09-24]` F5-L3 GREEN: 140 unitários, 45 integração, migrations V1–V7, transações, concorrência e tabela pela API.
- `[EXECUTADO PELO USUÁRIO · 2026-09-24]` F5-L4 GREEN: 165 unitários/BDD, 48 integração, 12 cenários/36 passos Cucumber, JaCoCo 95,61% e `jacoco:check` 95%.

## Preparação do L5

[VERIFICADO · 2026-09-24] versão pré-release ainda `1.0.0` em POM, Dockerfile e package.json.

[VERIFICADO · 2026-09-24] o plano F5 determina `2.0.0`, `CHANGELOG.md`, auditoria final, freeze completo e Gitflow de release.

[DESCONHECIDO] O freeze da branch `release/2.0.0`, o CI dessa branch, os merges em `main`/`develop`, a tag `v2.0.0` e o CI final da `main` só serão conhecidos após execução dos gates deste diretório. Nenhum deles é declarado GREEN antecipadamente.
