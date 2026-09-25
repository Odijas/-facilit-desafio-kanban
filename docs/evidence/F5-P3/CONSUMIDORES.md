# F5-P3 — CONSUMIDORES

Data: 2026-09-25

- **`README.md`:** ganha a linha do F5-P2 no pacote e a do F5-P3 no registro do freeze. A verificação pública lê o README da tag.
- **`docs/evidence/F5-P2/*`:** promoção. O freeze lê o estado GREEN e a saída.
- **`docs/evidence/F5-P3/GATE.md`:** o passo 4 do `RELEASE.md` troca o estado, e a verificação pública lê o estado na tag.
- **Código, testes, CI, coleção e frontend:** não mudam neste lote. O freeze os executa de novo.
- **`develop`:** recebe a hotfix pelo back-merge. Ela só alterou `docs/evidence/F5/SAIDA-RELEASE.txt` e `VERIFICACAO-RELEASE.md` desde a `v2.0.0`, sem arquivo em comum com a hotfix (gate do F5-P2).
