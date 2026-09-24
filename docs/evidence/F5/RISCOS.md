# F5-L5 — RISCOS E LACUNAS

Data: 2026-09-24

- `[DESCONHECIDO]` freeze final da `release/2.0.0` ainda não executado; `VERIFICACAO-USUARIO.md` é o gate bloqueante.
- `[DESCONHECIDO]` CI da release e da `main` ainda não executados para o commit final; ambos são verificados pelos gates.
- `[VERIFICADO]` o bump muda o nome do jar; POM e Dockerfile são alterados juntos para evitar imagem quebrada.
- `[VERIFICADO]` as referências documentais à `v1.0.0` são históricas e não devem ser substituídas por busca global.
- `[VERIFICADO]` o gate de freeze usa projeto Compose isolado e remove somente os volumes dele; o projeto padrão é parado sem `-v`.
- `[VERIFICADO]` o gate reexecuta `mvn clean verify`, portanto o limite JaCoCo de 95% continua bloqueante na release.
- `[VERIFICADO]` riscos residuais de segurança já declarados no F4 não são silenciosamente tratados como resolvidos; o freeze repete os checks executáveis de autenticação, cookies, CSRF, exposição de Actuator e segredos.
