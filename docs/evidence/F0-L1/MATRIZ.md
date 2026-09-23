# F0-L1 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → TESTE → EVIDÊNCIA

| Requisito do lote | Implementação | Teste/verificação | Evidência final | Estado |
|---|---|---|---|---|
| Backend Java/Spring | `backend/pom.xml`, `KanbanApplication` | `mvn -B -ntp clean verify` | 3 testes, 0 falhas, BUILD SUCCESS | GREEN |
| REST fundado | `HealthRestController` | controller test + smoke HTTP | teste Maven GREEN; `{"status":"UP"}` | GREEN |
| GraphQL fundado | schema + controller | GraphQlTester + smoke | teste Maven GREEN; `health.status=UP` | GREEN |
| Mesmo caso de uso REST/GraphQL | `HealthQuery` | unit + consumidores | unit GREEN; consumidores mapeados | GREEN |
| PostgreSQL | `compose.yaml` | `docker compose ps` | PostgreSQL 18.6 `healthy` | GREEN |
| Flyway | configuração + diretório migration | startup integrado | aplicação iniciou com datasource/Flyway configurados | GREEN |
| Front React/MUI | `frontend/src`, `package.json` | typecheck/build | typecheck GREEN; Vite build GREEN | GREEN |
| Teste unitário frontend | `App.test.tsx` | `pnpm test` | 2/2 GREEN | GREEN |
| Qualidade frontend | Biome + TS strict | lint/typecheck | ambos GREEN | GREEN |
| Docker Compose app+db | Compose + Dockerfiles | build/up/smoke | três serviços ativos; banco healthy | GREEN |
| Lockfile frontend | `pnpm-lock.yaml` | frozen install local/container | instalação e build Docker GREEN | GREEN |
| Governança | `docs/evidence/F0-L1` | reconciliação documental | pacote atualizado com evidência final | GREEN |
