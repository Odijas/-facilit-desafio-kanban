# F4 — RISCOS E LACUNAS

Data: 2026-09-23

- `[EXECUTADO PELO USUÁRIO · 2026-09-23]` gate de freeze concluído com `=== F4 GREEN ===` e exit code 0 (`SAIDA-GATE.txt`); verificação final da release concluída com `F4_RELEASE_REFS_GREEN`, `F4_RELEASE_CI_GREEN`, `F4_RELEASE_PAGE_GREEN`, `=== F4 RELEASE GREEN ===` e exit code 0 (`SAIDA-RELEASE.txt`).
- `[EXECUTADO PELO USUÁRIO · 2026-09-23]` o `mvn clean verify` exibiu 1 aviso de método terminalmente depreciado em `sun.misc.Unsafe`; 64 testes unitários e 13 de integração concluíram com 0 falhas/erros. O aviso permaneceu informativo no gate.
- `[EXECUTADO PELO USUÁRIO · 2026-09-23]` `pnpm audit --prod` retornou `No known vulnerabilities found` na execução do gate; a consulta continua dependente do registro npm em execuções futuras.
- `[VERIFICADO]` o gate para o projeto Compose padrão para liberar as portas; volte a subi-lo depois, se precisar (`docker compose up -d`).
- `[VERIFICADO]` riscos de segurança residuais listados em `REVISAO-SEGURANCA.md` (A02, A03, A04, A06, A07 e A09).
- `[VERIFICADO]` aviso não bloqueante mantido: bundle Vite acima de 500 kB.
