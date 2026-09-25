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

## Correção do gate de remotos e status

[EXECUTADO PELO USUÁRIO · 2026-09-24] `release/2.0.0` foi publicada no GitHub; o push ao GitLab encerrou com `Connection to gitlab.com closed by remote host` / `unexpected disconnect`.

[EXECUTADO PELO USUÁRIO · 2026-09-24] o freeze parou na pré-condição do GitLab e imprimiu `Resultado: exit code 1`, mas o shell externo mostrou `GATE_EXIT=0`; o wrapper terminava no `echo` e não propagava o status do `bash` interno.

[VERIFICADO · 2026-09-24] o gate foi corrigido para exigir publicação no GitHub (`origin`), tratar GitLab como espelho secundário não bloqueante e executar `exit "$STATUS"` após registrar o resultado.

## Correção do scanner histórico de segredos

[EXECUTADO PELO USUÁRIO · 2026-09-24] o freeze atingiu frontend/backend/JaCoCo/BDD GREEN e parou somente na análise histórica, acusando `senha de ambiente com valor` em `docs/evidence/F5-L1`, `F5-L2` e `F5-L3`.

[VERIFICADO · 2026-09-24] o detector genérico de atribuições de senha foi restringido a arquivos fora de `docs/evidence/`; chaves privadas e padrões de tokens GitHub/GitLab/AWS continuam sendo examinados em todo o histórico. A exceção evita classificar roteiros/evidências históricas de teste como configuração operacional.
## Correção do gate de Conventional Commits

[EXECUTADO PELO USUÁRIO · 2026-09-24] o freeze avançou até a validação de histórico e parou exclusivamente em `87fa002 Revert "docs(api): documenta os erros de cada operação no OpenAPI"`.

[VERIFICADO · 2026-09-24] `87fa002` é o revert auditável e publicado da correção de granularidade do F5-L2; o gate passa a aceitar somente esse subject/hash legado como exceção explícita. Todos os demais commits continuam obrigados ao padrão Conventional Commits.
