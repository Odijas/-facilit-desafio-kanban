# F0-L3 — GATE

Data: 2026-09-21.

Estado: **GREEN**.

[EXECUTADO: runtime da IA · 2026-09-21] Domínio puro compilado com `javac -Xlint:all -Werror` e probe legítimo/ataque retornou `F0_L3_DOMAIN_PROBE_GREEN`.

[EXECUTADO: ambiente do usuário · 2026-09-21] Frontend: `pnpm install --frozen-lockfile`, Biome, typecheck, 2 testes e build concluíram sem erro.

[EXECUTADO: `mvn -B -ntp clean verify` no ambiente do usuário · 2026-09-21] `BUILD SUCCESS`; 18 testes executados, 0 falhas, 0 erros, 0 ignorados.

[EXECUTADO: Docker Compose no ambiente do usuário · 2026-09-21] Imagens backend/frontend construídas; PostgreSQL 18.6 iniciou `healthy`; backend e frontend permaneceram ativos.

[EXECUTADO: Flyway/PostgreSQL no ambiente do usuário · 2026-09-21] Migration V1 retornou `1:true`; quatro tabelas core e cinco índices explícitos foram confirmados.

[EXECUTADO: caminhos de persistência no ambiente do usuário · 2026-09-21] Caminho legítimo retornou `DB_LEGITIMATE_PATH_GREEN`; métricas inválidas foram rejeitadas pela constraint e retornaram `DB_CONSTRAINT_ATTACK_BLOCKED`.

[EXECUTADO: regressão runtime no ambiente do usuário · 2026-09-21] REST retornou `{"status":"UP"}`, GraphQL retornou `{"data":{"health":{"status":"UP"}}}` e frontend retornou HTTP 200.

[EXECUTADO: `git diff --check` no ambiente do usuário · 2026-09-21] Nenhum erro foi emitido.

**Conclusão:** F0-L3 GREEN e **Fase 0 GREEN**. A promoção para F1 depende apenas do fechamento de baseline/commit descrito ao final desta entrega.
