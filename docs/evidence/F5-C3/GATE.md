# F5-C3 — GATE

Data: 2026-09-24

Estado: **GREEN** (gate local em 2026-09-24 22h27).

Escopo: `docs/governance/PLANO-CORRECAO-RELEASE-2.0.0.md`, lote F5-C3 (documentação de entrega), mais a promoção do F5-C2, na `release/2.0.0`.

Critérios:

- `F5_C3_PRECONDITIONS_GREEN`:
  - branch `bugfix/2.0.0-*` cuja base tem o F5-C2 commitado;
  - arquivos alterados iguais à lista do pacote;
  - nada em `backend/`, `frontend/` ou `.github/`.
- `F5_C3_STATIC_GREEN`:
  - checagens documentais do freeze: seções de README, AI_USAGE, ADR, AUDITORIA, CHANGELOG e revisão de segurança; mermaid; links; credenciais vazias na coleção;
  - Conventional Commits de todo o histórico;
  - `F5-L5 — Release \`v2.0.0\`` no README;
  - sem texto de trabalho;
  - F5-C2 GREEN;
  - coleção com 27 requisições;
  - espaços, linha final e `git diff --check`.
- `F5_C3_DOCKER_GREEN`: projeto Compose próprio (`facilit-kanban-f5c3`), `up --build` com banco novo, migrations 1 a 7.
- `F5_C3_NEWMAN_GREEN`: a coleção inteira, com newman 6.2.2, contra a aplicação: 27 requisições, nenhuma asserção nem script com falha.
- `F5_C2_CI_GREEN`: CI da `release/2.0.0` com o F5-C2, com os 3 jobs e o passo da imagem Docker em sucesso.
- Marcador final `=== F5-C3 GREEN ===` e `Resultado: exit code 0`.

Evidência recebida do usuário: `SAIDA-GATE.txt` (saída integral do gate, pacote SHA-256 `daa1ac39…afef9fe6` conferido).

- **Coleção Postman inteira no newman 6.2.2**, contra a aplicação com banco novo: 27 requisições e 52 asserções, nenhuma falha. É a primeira execução automatizada da coleção inteira.
- **Checagens documentais do freeze:** todas passaram, e o histórico tem 69 commits no padrão Conventional Commits.
- **`F5_C2_CI_GREEN`:** CI da `release/2.0.0` @ `edcbc36` (run 36081814889) com os 3 jobs e o passo da imagem Docker em sucesso.

Na promoção, a data da `[2.0.0]` no CHANGELOG passa a 2026-09-24, a data do freeze e da tag (ver `RISCOS.md`).

Conclusão: F5-C3 promovido para GREEN em 2026-09-24. Com ele, os três lotes do plano de correção ficam GREEN.
