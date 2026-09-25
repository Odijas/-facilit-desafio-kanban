# F5-Q1 — RISCOS E LACUNAS

Data: 2026-09-25

1. `[DESCONHECIDO]` Maven/Java 25: o ambiente da IA só possui JDK 21 e não possui Maven. Resolver com `cd backend && mvn -B -ntp clean verify` no gate.
2. `[DESCONHECIDO]` Docker/Testcontainers: Docker não existe no ambiente da IA. Resolver com o projeto Compose isolado do gate, banco novo e migrations V1–V7.
3. `[EXECUTADO: saída do usuário]` o OpenAPI gerado foi exercitado por `OpenApiContractIT`; o RED 1 revelou que o helper novo assumia exclusivamente `examples.VALIDATION_ERROR.value` em `POST /api/v1/projects`, operação que já declara seu próprio exemplo 400. `[VERIFICADO]` helper e gate foram corrigidos sem relaxar a checagem campo ↔ schema. `[DESCONHECIDO]` resultado da reexecução.
4. `[VERIFICADO]` O plano dizia `Acesso negado.` para rotas somente ADMIN. O código real não faz essa autorização no filtro: `Actor.requireAdmin()` produz `Apenas o administrador pode realizar esta operação.`. A documentação foi alinhada ao comportamento real.
5. `[VERIFICADO]` README, ADR 0002, CHANGELOG e AI_USAGE ainda contêm o estado documental da v2.0.1 por decisão de faseamento; pertencem ao F5-Q2 e não são corrigidos no Q1.
6. `[DESCONHECIDO]` Branch, HEAD, remotos e estado Git não existem no `tar.gz` por exclusão de `.git`. O gate do usuário valida a branch `bugfix/2.0.2-*`; merges, tags e pushes continuam com o usuário.
7. `[VERIFICADO]` Conflito de granulação de commits do plano: a migração dos testes existentes deve acompanhar o refactor D1 para cada commit continuar compilável; ver `DECISOES.md`.
