# F5-C1 — FONTES RAG

Data: 2026-09-24

1. `ADERENCIA-FINAL.md` (2026-09-24): lacunas 1 (build Docker) e 3 (Swagger sem CSRF), com a análise do auditor independente.
2. `docs/governance/PLANO-CORRECAO-RELEASE-2.0.0.md`: lote F5-C1 e decisões do usuário.
3. Código-fonte do springdoc-openapi, tag `v2.8.17` no GitHub:
   - `springdoc-openapi-starter-common/.../properties/SwaggerUiConfigProperties.java` (classe `Csrf`, `isCsrfEnabled`);
   - `springdoc-openapi-starter-common/.../utils/Constants.java` (`CSRF_DEFAULT_COOKIE_NAME = "XSRF-TOKEN"`, `CSRF_DEFAULT_HEADER_NAME = "X-XSRF-TOKEN"`, `SWAGGER_INITIALIZER_JS`);
   - `springdoc-openapi-starter-common/.../ui/AbstractSwaggerIndexTransformer.java` (`addCSRF`: `requestInterceptor` com `document.cookie` e cabeçalho da mesma origem).
4. `docs/evidence/F5/VERIFICACAO-USUARIO.md`: política do workflow (jobs, SHA, permissões) e checagens estáticas que o freeze aplica.
5. `docs/evidence/F5-L4/SAIDA-GATE-FINAL.txt`: contagens de base (24 relatórios / 165 testes unitários e BDD; 12 relatórios / 48 de integração; JaCoCo 95,61%).
