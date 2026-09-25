# F5-C3 — RISCOS E LACUNAS

Data: 2026-09-24

- `[HIPÓTESE]` a coleção inteira passa no newman contra a aplicação real. Nunca tinha rodado num runner automatizado nos gates anteriores. Se uma requisição antiga falhar, é defeito da coleção entregue, e a correção entra neste lote (rev2). Resolve: `F5_C3_NEWMAN_GREEN`.
- `[VERIFICADO]` o `npm i newman@6.2.2` avisa `@faker-js/faker@5.5.3` depreciado, dependência transitiva do newman. É ferramenta do gate, não dependência do projeto, e fica como informativo.
- `[VERIFICADO]` a data `[2.0.0] — 2026-09-25` do CHANGELOG é a do prazo e da tag planejada. Se a tag sair em outra data, o CHANGELOG é ajustado no freeze.
- `[VERIFICADO]` fora do lote: freeze e release (F5-L5), que exigem o CI verde da `release/2.0.0` depois do merge deste lote.
