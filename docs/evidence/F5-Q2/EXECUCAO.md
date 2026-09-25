# F5-Q2 — EXECUÇÃO

Data: 2026-09-25

## Executado no ambiente da IA

- `[EXECUTADO]` baseline Q1 fechada extraída e SHA-256 conferido.
- `[EXECUTADO]` `ADERENCIA-V2.0.1.md` copiado para `docs/evidence/F5/` e comparado pelo SHA-256 original.
- `[EXECUTADO]` referências de teste corrigidas cruzadas com os métodos reais do código.
- `[EXECUTADO]` promoção documental do Q1 construída somente a partir da saída GREEN e do fechamento fornecidos pelo usuário.
- `[EXECUTADO]` checagens estáticas do candidate: escopo, conteúdo obrigatório, referências de testes, links/caminhos, espaços finais, quebra final e ausência de mudanças em código.

## Não executado

- `[DESCONHECIDO]` estado Git da branch Q2 e `git diff --check` real após aplicação do pacote.
- `[DESCONHECIDO]` CI público da `hotfix/2.0.2` no momento do gate.

Essas duas lacunas são resolvidas por `VERIFICACAO-USUARIO.md`.

## Saída das checagens locais

```text
LOCAL_STATIC_GREEN
changed_files=20
GATE_BASH_SYNTAX_GREEN
```

`[EXECUTADO]` O diff do candidate contra a baseline contém exatamente 20 arquivos, todos documentais/evidências. O gate shell extraído de `VERIFICACAO-USUARIO.md` passou em `bash -n`.
