# F5-C1 — DECISÕES

Data: 2026-09-24

Escopo: `docs/governance/PLANO-CORRECAO-RELEASE-2.0.0.md`, lote F5-C1 (entrega executável), aprovado pelo usuário em 2026-09-24 21h42 ("execute F5-C1").

## Build da imagem do backend

- `[VERIFICADO: backend/Dockerfile:6 e backend/pom.xml:204-217 no snapshot 43051359]` a imagem rodava `mvn -DskipITs verify`. Desde o F5-L4, o `verify` inclui o `jacoco:check`, com mínimo de 95% de linhas. Esse mínimo foi medido com unitários + integração, e na imagem só rodam os unitários.
- `[INFERÊNCIA, a provar no gate]` sem os testes de integração a cobertura fica abaixo de 95%, e o `docker compose up --build` falha no build. O gate reproduz isso: constrói a imagem com o Dockerfile anterior (RED esperado) e com o novo (GREEN).
- `[DECISÃO DO USUÁRIO, 2026-09-24]` os testes unitários continuam rodando na imagem, como na v1.0.0: `mvn -B -ntp -DskipITs -Djacoco.skip=true verify`. O limite de 95% continua valendo onde há unitários + integração: no CI (`mvn clean verify`) e nos gates.
- `[VERIFICADO: backend/pom.xml]` com `jacoco.skip`, o `prepare-agent` não define o agente, e `jacocoArgLine` fica com o valor vazio já declarado no `pom.xml`. O `argLine` do Surefire continua `-javaagent` do Mockito.
- O nome do jar (`kanban-2.0.0.jar`) não muda: o freeze do F5-L5 exige esse nome.

## Imagem no CI

- `[VERIFICADO: docs/evidence/F5/VERIFICACAO-USUARIO.md]` o freeze exige exatamente os jobs `frontend`, `backend` e `repository`, actions fixadas por SHA e nenhum segredo. Por isso a construção da imagem entra como **passo** `run:` no job `backend` (`docker build --pull -t facilit-kanban-backend:ci .`), sem job e sem action novos.
- Motivo: o defeito passou por três gates e pelo CI porque nada construía a imagem. Agora todo push constrói.

## CSRF no Swagger UI

- `[VERIFICADO: SecurityConfiguration.java:51-52, 61-64; AuthRestController.java]` a API exige CSRF em toda escrita, inclusive no login. O cookie `XSRF-TOKEN` é legível por JavaScript (`withHttpOnlyFalse`, path `/`), e o login apaga o token (`saveToken(null)`). A próxima requisição gera um novo, porque o `SpaCsrfTokenRequestHandler` carrega o token em toda requisição.
- `[VERIFICADO: springdoc-openapi v2.8.17, SwaggerUiConfigProperties.Csrf e Constants]` `springdoc.swagger-ui.csrf.enabled` existe. Os padrões são cookie `XSRF-TOKEN` e cabeçalho `X-XSRF-TOKEN`, os mesmos da API.
- `[VERIFICADO: AbstractSwaggerIndexTransformer.addCSRF]` com a opção ligada, o springdoc injeta no `swagger-initializer.js` um `requestInterceptor`. Ele lê `document.cookie` e envia `X-XSRF-TOKEN` nas chamadas da mesma origem.
- Roteiro no README: `GET /api/v1/auth/csrf` → login → `GET /api/v1/auth/csrf` de novo → operações. O mesmo vale para o GraphiQL: o token vai à mão no painel Headers, porque o GraphiQL do Spring GraphQL não tem interceptor de CSRF.
- Nada muda na segurança: o CSRF continua exigido, e o Swagger passa a cumprir o contrato que o frontend e a coleção já cumpriam.

## Desvios em relação ao plano

| Plano | Feito | Motivo |
|---|---|---|
| teste em `/api-docs/swagger-config` | teste em `/swagger-ui/swagger-initializer.js` | a configuração de CSRF não vai para o `swagger-config`; ela é injetada no `swagger-initializer.js` (código do springdoc) |
| roteiro só do Swagger | Swagger e GraphiQL | o GraphiQL tem a mesma exigência de CSRF e estava listado no README sem instrução |
