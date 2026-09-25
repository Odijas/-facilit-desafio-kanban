# F5-L5 — RISCOS E LACUNAS

Data: 2026-09-24

- `[DESCONHECIDO]` freeze final da `release/2.0.0` ainda não executado; `VERIFICACAO-USUARIO.md` é o gate bloqueante.
- `[DESCONHECIDO]` CI da release e da `main` ainda não executados para o commit final; ambos são verificados pelos gates.
- `[VERIFICADO]` o bump muda o nome do jar; POM e Dockerfile são alterados juntos para evitar imagem quebrada.
- `[VERIFICADO]` as referências documentais à `v1.0.0` são históricas e não devem ser substituídas por busca global.
- `[VERIFICADO]` o gate de freeze usa projeto Compose isolado e remove somente os volumes dele; o projeto padrão é parado sem `-v`.
- `[VERIFICADO]` o gate reexecuta `mvn clean verify`, portanto o limite JaCoCo de 95% continua bloqueante na release.
- `[VERIFICADO]` riscos residuais de segurança já declarados no F4 não são silenciosamente tratados como resolvidos; o freeze repete os checks executáveis de autenticação, cookies, CSRF, exposição de Actuator e segredos.

- `[EXECUTADO PELO USUÁRIO · 2026-09-24]` o GitLab apresentou desconexão remota durante o push da release; como o desafio exige repositório público no GitHub, o espelho GitLab não bloqueia o freeze nem a publicação.
- `[VERIFICADO · 2026-09-24]` o wrapper anterior do gate mascarava exit code RED porque terminava após `echo`; os gates F5 agora propagam explicitamente o status com `exit "$STATUS"`.
- `[EXECUTADO PELO USUÁRIO · 2026-09-24]` a primeira execução do freeze após a correção de remotos parou em falsos positivos de senha dentro de `docs/evidence/F5-L1..L3`; o detector de atribuição de senha foi limitado a arquivos operacionais, enquanto padrões fortes de chave/token permanecem globais.
