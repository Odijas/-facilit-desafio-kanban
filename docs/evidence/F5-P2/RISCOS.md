# F5-P2 — RISCOS E LACUNAS

Data: 2026-09-25

- `[HIPÓTESE]` O back-merge da `hotfix/2.0.1` na `develop` (F5-P3) não conflita.
  - A `develop` tem 2 commits de documentação depois da release: a correção do `VERIFICACAO-RELEASE.md` e a `SAIDA-RELEASE.txt`.
  - O hotfix altera outros arquivos de `docs/evidence/F5/`.
  - O gate imprime a sobreposição real antes do P3.
- `[VERIFICADO]` O `#9` (histórico) continua Parcial e declarado no README (linha "Histórico Git"), por decisão do usuário.
- `[VERIFICADO]` Fora do lote, e entram no F5-P3:
  - linha do F5-P2 no README;
  - resultado da conferência do Swagger;
  - freeze, tag `v2.0.1`, back-merge e auditoria final.
