# F5-Q2 — EXECUÇÃO

Data: 2026-09-25

## Executado no ambiente da IA

- `[EXECUTADO]` baseline Q1 fechada extraída e SHA-256 conferido.
- `[EXECUTADO]` `ADERENCIA-V2.0.1.md` copiado para `docs/evidence/F5/` e comparado pelo SHA-256 original.
- `[EXECUTADO]` referências de teste corrigidas cruzadas com os métodos reais do código.
- `[EXECUTADO]` promoção documental do Q1 construída somente a partir da saída GREEN e do fechamento fornecidos pelo usuário.
- `[EXECUTADO]` checagens estáticas do candidate: escopo, conteúdo obrigatório, referências de testes, links/caminhos, espaços finais, quebra final e ausência de mudanças em código.

## Executado pelo usuário

- `[EXECUTADO: gate F5-Q2 · 2026-09-25]` `F5_Q2_PRECONDITIONS_GREEN`, `F5_Q2_DOCS_GREEN`, `F5_Q2_HISTORY_GREEN`, `F5_Q1_CI_GREEN`, `=== F5-Q2 GREEN ===` e `Resultado: exit code 0`.
- `[EXECUTADO: gate F5-Q2 · 2026-09-25]` escopo exato de 20 arquivos documentais; conteúdo/referências/testes/auditoria/promoção Q1/links OK.
- `[EXECUTADO: gate F5-Q2 · 2026-09-25]` histórico Q1 com 5 commits Conventional Commits; CI da `hotfix/2.0.2` no commit `9ab3bbd` em sucesso, run `36143782068`, jobs `frontend`, `backend` e `repository` em sucesso.
- `[EXECUTADO: fechamento F5-Q2 · 2026-09-25]` seis commits documentais do Q2, merge `--no-ff` da `bugfix/2.0.2-q2-docs` na `hotfix/2.0.2`, push no GitHub e no GitLab.
- `[EXECUTADO: fechamento F5-Q2 · 2026-09-25]` `HEAD`, `origin/hotfix/2.0.2` e `gitlab/hotfix/2.0.2` iguais a `9dceb9ce2755df6e1784683d7b4c3baa213ba26d`; árvore limpa.

A saída normalizada está em `SAIDA-GATE.txt` e os três ponteiros do fechamento em `FECHAMENTO.txt`.

## Saída das checagens locais

```text
LOCAL_STATIC_GREEN
changed_files=20
GATE_BASH_SYNTAX_GREEN
```

`[EXECUTADO]` O diff do candidate contra a baseline contém exatamente 20 arquivos, todos documentais/evidências. O gate shell extraído de `VERIFICACAO-USUARIO.md` passou em `bash -n`.
