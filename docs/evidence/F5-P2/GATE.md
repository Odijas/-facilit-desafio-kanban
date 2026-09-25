# F5-P2 — GATE

Data: 2026-09-25

Estado: **GREEN** (gate local em 2026-09-25, 9h00; saída em `SAIDA-GATE.txt`).

- CI da `hotfix/2.0.1` com o F5-P1: run 36131837374, 3 jobs e o passo `Imagem Docker do backend` em sucesso.
- Aviso de sobreposição: a `develop` alterou só `docs/evidence/F5/SAIDA-RELEASE.txt` e `VERIFICACAO-RELEASE.md` desde a `v2.0.0`; nenhum arquivo em comum com a `hotfix`.

Escopo: lote F5-P2 de `docs/governance/PLANO-CORRECAO-RELEASE-2.0.1.md` (documentação coerente com a tag), mais a promoção do F5-P1, na `hotfix/2.0.1`.

Critérios:

- `F5_P2_PRECONDITIONS_GREEN`:
  - branch `bugfix/2.0.1-*` a partir da `v2.0.0`, com o F5-P1 rev2 mesclado na base;
  - arquivos alterados iguais à lista do pacote (19), só documentação.
- `F5_P2_DOCS_GREEN`:
  - espaços e linha final, `git diff --check`;
  - frases desatualizadas ausentes em README, AI_USAGE, CHANGELOG e evidências do F5, e as novas presentes;
  - primeira entrada do CHANGELOG `[2.0.1] — 2026-09-25`;
  - seções do README exigidas pelo freeze;
  - testes citados nas interpretações novas existem;
  - links relativos e caminhos citados existem;
  - F5-P1 e F5-L5 GREEN.
- `F5_P2_HISTORY_GREEN`: commits desde a `v2.0.0` em Conventional Commits (≥ 4, os do F5-P1).
- Aviso de sobreposição com a `develop` (informativo, para o F5-P3).
- `F5_P1_CI_GREEN`: CI da `hotfix/2.0.1` com o F5-P1 (3 jobs em sucesso e o passo `Imagem Docker do backend`).
- Marcador final `=== F5-P2 GREEN ===` e `Resultado: exit code 0`.
