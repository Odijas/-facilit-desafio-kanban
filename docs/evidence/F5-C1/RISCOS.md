# F5-C1 — RISCOS E LACUNAS

Data: 2026-09-24

- `[HIPÓTESE]` a imagem com o Dockerfile anterior falha no `jacoco:check`. O gate constrói as duas versões. Se a anterior passar, a lacuna 1 da auditoria era falso positivo; a correção continua inofensiva (o jar é o mesmo), e o registro é corrigido.
- `[HIPÓTESE]` o passo `docker build` cabe no `timeout-minutes: 30` do job `backend`: o `clean verify` mais o build da imagem, com download de dependências dentro do contêiner. Resolve: CI do push (`F5_C1_CI_GREEN`). Se estourar, o timeout sobe para 45 no mesmo lote.
- `[NÃO VERIFICÁVEL POR SCRIPT]` o clique no Swagger UI. O gate prova o que é observável sem navegador: `swagger-initializer.js` com o `requestInterceptor`, mais o fluxo de cookie e cabeçalho que o interceptor faz, contra a API real. A conferência no navegador está na seção 3 da `VERIFICACAO-USUARIO.md`.
- `[VERIFICADO]` o GraphiQL continua exigindo o token à mão (documentado no README).
- `[VERIFICADO]` fora do lote, conforme o plano: validação de tamanho e métricas por linha (F5-C2); coleção, AI_USAGE, README sem texto de trabalho, CHANGELOG e auditoria (F5-C3).
