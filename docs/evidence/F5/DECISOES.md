# F5-L5 — DECISÕES

Data: 2026-09-24

- `[VERIFICADO: docs/governance/PLANO-CONFORMIDADE-F5.md · 2026-09-24]` D3 e D4 quebram o contrato público; a versão final é `2.0.0`.
- `[VERIFICADO: docs/governance/PLANO-CONFORMIDADE-F5.md · 2026-09-24]` o L5 não adiciona funcionalidade: versiona, documenta, congela e publica o estado GREEN dos L1–L4.
- `[VERIFICADO: docs/evidence/F4/RELEASE.md · 2026-09-24]` mantém-se o Gitflow já usado: `release/2.0.0` nasce da `develop`, vai à `main` por merge `--no-ff`, recebe tag anotada `v2.0.0` e é mesclada de volta à `develop`.
- `[VERIFICADO: frontend/pnpm-lock.yaml · 2026-09-24]` o lockfile não contém a versão do pacote raiz; por isso não muda no bump.
- `[VERIFICADO: backend/pom.xml + backend/Dockerfile · 2026-09-24]` o Dockerfile depende do nome versionado do jar Maven e precisa mudar junto com o POM.
- `[VERIFICADO: docs/evidence/F5-L4/SAIDA-GATE-FINAL.txt · 2026-09-24]` a baseline pré-release já tem 165 testes unitários/BDD, 48 de integração, 12 cenários BDD e 95,61% de linhas; o freeze final precisa reexecutar, não reutilizar esse resultado como se fosse da release.
