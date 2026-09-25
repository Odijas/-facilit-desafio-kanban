# F5-Q3 — GATE DE FREEZE E RELEASE 2.0.2

Data: 2026-09-25

Estado: **CANDIDATE — freeze não executado**.

Escopo: F5-Q3 de `docs/governance/PLANO-CORRECAO-RELEASE-2.0.2.md`: promoção do Q2, freeze da `hotfix/2.0.2`, release `v2.0.2`, verificação pública e preparação da auditoria final.

GREEN do freeze (`VERIFICACAO-USUARIO.md`) exige:

- `F5_Q3_PRECONDITIONS_GREEN`: hotfix limpa/publicada, versão 2.0.2, `v2.0.1` ancestral, Q1/Q2 GREEN e `v2.0.2` inexistente antes da release;
- `F5_Q3_FRONTEND_GREEN`;
- `F5_Q3_BACKEND_GREEN` e `F5_Q3_JACOCO_BDD_GREEN`: `mvn clean verify`, 25 classes/177 testes unitários+BDD, 12 classes/52 integrações, JaCoCo ≥95%, BDD 12/12;
- `F5_Q3_STATIC_GREEN`: workflow, repositório, Conventional Commits, documentação, E1 e links;
- `F5_Q3_DOCKER_CLEAN_DB_GREEN`: Docker com banco novo e migrations V1–V7;
- `F5_Q3_UI_SWAGGER_GREEN`: UI/Swagger/CSRF e E3 no `/api-docs` real;
- `F5_Q3_API_GREEN`, `F5_Q3_RESPONSIBLE_AUTH_GREEN`, `F5_Q3_SECURITY_GREEN`, `F5_Q3_OBSERVABILITY_GREEN`;
- `F5_Q3_CI_GREEN`: CI do commit da hotfix em sucesso nos três jobs;
- `=== F5-Q3 GREEN ===` e `Resultado: exit code 0`.

GREEN da release (`VERIFICACAO-RELEASE.md`) exige:

- `F5_Q3_RELEASE_REFS_GREEN`;
- `F5_Q3_RELEASE_GITHUB_GREEN`;
- `F5_Q3_RELEASE_CI_GREEN`;
- `F5_Q3_RELEASE_PAGE_GREEN`;
- `=== F5-Q3 RELEASE GREEN ===` e `Resultado: exit code 0`.

A promoção deste arquivo para GREEN só ocorre pelo passo de registro do freeze em `RELEASE.md`, depois de saída real GREEN.
