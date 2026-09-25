# F5-Q3 — EXECUÇÃO

Data: 2026-09-25

## Base verificada

```text
[EXECUTADO PELO USUÁRIO · 2026-09-25] F5-Q2 GREEN (exit code 0):
  F5_Q2_PRECONDITIONS_GREEN
  F5_Q2_DOCS_GREEN
  F5_Q2_HISTORY_GREEN
  F5_Q1_CI_GREEN
  === F5-Q2 GREEN ===
[EXECUTADO PELO USUÁRIO · 2026-09-25] fechamento do Q2:
  HEAD = origin/hotfix/2.0.2 = gitlab/hotfix/2.0.2 = 9dceb9ce2755df6e1784683d7b4c3baa213ba26d
[VERIFICADO · 2026-09-25] pacote base do Q3:
  SHA-256 00a72af28ff01f59b536f227cc15550a9efb9d50977a24bc4b2b1aff3f8c6157
  499 arquivos; sem .git, .env, target, node_modules, dist, coverage ou chaves.
```

## Executado no ambiente da IA

```text
[EXECUTADO] reabertura integral da governança, plano 2.0.2, evidências Q2 e padrão P3.
[EXECUTADO] promoção documental do Q2 construída somente com a saída e hashes fornecidos pelo usuário.
[EXECUTADO] criação dos gates Q3 por adaptação mínima do padrão F5-P3.
[EXECUTADO] verificação estática do candidate: 18 arquivos alterados, todos documentais/evidências; nenhum arquivo de backend/frontend/CI/observabilidade alterado.
[EXECUTADO] referências E1 cruzadas com os métodos de teste reais: `E1_STATIC_REFERENCES_GREEN`.
[EXECUTADO] `bash -n` do gate de freeze e do gate de release: `FREEZE_BASH_SYNTAX_GREEN` e `RELEASE_BASH_SYNTAX_GREEN`.
[EXECUTADO] os 9 blocos shell de `RELEASE.md` passaram em `bash -n`: `RELEASE_BLOCKS_BASH_SYNTAX_GREEN count=9`.
[EXECUTADO] whitespace/final de arquivo dos 18 arquivos: sem problemas; links Markdown relativos conferidos: `LINKS_GREEN checked=8`.
```

## Não executado neste ambiente

```text
[DESCONHECIDO] freeze real: pnpm, Maven, Docker, PostgreSQL, REST/GraphQL, segurança, Prometheus/Grafana e CI.
[DESCONHECIDO] merge/tag/back-merge/push reais da v2.0.2 e verificação pública.
[DESCONHECIDO] auditoria final da tag v2.0.2.
```

As lacunas acima são resolvidas, nessa ordem, por `VERIFICACAO-USUARIO.md`, `RELEASE.md`, `VERIFICACAO-RELEASE.md` e pelo `git archive v2.0.2` da auditoria final.
