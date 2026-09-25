# F5-P3 — FONTES RAG

Data: 2026-09-25

1. `docs/governance/PLANO-CORRECAO-RELEASE-2.0.1.md`, lote F5-P3.
2. `docs/evidence/F5/VERIFICACAO-USUARIO.md`, `VERIFICACAO-RELEASE.md` e `RELEASE.md` (freeze e release da 2.0.0, executados GREEN pelo usuário em 2026-09-24).
3. Falso negativo da verificação da 2.0.0 (2026-09-24): `curl | grep -q` com `pipefail` terminava com curl 23. A correção, baixar para arquivo antes, foi executada pelo usuário e versionada na `develop` (`4fd706f`).
4. Saída do gate do F5-P2 (2026-09-25, 9h00): CI da `hotfix/2.0.1` na run 36131837374 e aviso de sobreposição com a `develop`.
