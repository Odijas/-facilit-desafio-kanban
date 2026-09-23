# F0-L2 — GATE

Data: 2026-09-21.

Estado: **GREEN**.

[EXECUTADO: `mvn -B -ntp clean verify` no ambiente do usuário · 2026-09-21] Build concluído com sucesso em Java 25.0.3; 3 testes executados, 0 falhas, 0 erros, 0 ignorados.

[EXECUTADO: probe do domínio no ambiente do usuário · 2026-09-21] Resultado: `F0_L2_DOMAIN_PROBE_GREEN`.

[EXECUTADO: `docker compose up --build -d` no ambiente do usuário · 2026-09-21] PostgreSQL ficou healthy; backend e frontend iniciaram.

[EXECUTADO: sondagem REST/GraphQL no ambiente do usuário · 2026-09-21] REST retornou `{"status":"UP"}` e GraphQL retornou `{"data":{"health":{"status":"UP"}}}`.

[EXECUTADO: `git diff --check` dentro do gate do usuário · 2026-09-21] O gate terminou com `=== F0-L2 GREEN ===` e exit code 0.

F0-L2 promovido para GREEN. F0-L3 pode usar este estado como baseline funcional.
