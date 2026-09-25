# F5-Q3 — CONSUMIDORES

Data: 2026-09-25

- `README.md`: recebe a promoção do F5-Q2 neste pacote e, somente após freeze GREEN, a linha do F5-Q3.
- `docs/evidence/F5-Q2/*`: promoção do estado real; o freeze lê `GATE.md` e `SAIDA-GATE.txt`.
- `docs/evidence/F5-Q3/GATE.md`: começa CANDIDATE; o roteiro de release o promove a GREEN apenas com saída real do freeze.
- `docs/evidence/F5-Q3/VERIFICACAO-USUARIO.md`: consumido pelo desenvolvedor para o freeze da hotfix.
- `docs/evidence/F5-Q3/VERIFICACAO-RELEASE.md`: consumido depois da tag para validar refs, CI e conteúdo publicado.
- `docs/evidence/F5-Q3/RELEASE.md`: roteiro operacional do fechamento da release.
- Código, testes, migrations, CI, frontend e observabilidade: não são alterados; são consumidores indiretos do freeze, que os reexecuta.
