# F5-P1 — EXECUÇÃO

Data: 2026-09-25

## Base

```text
[EXECUTADO PELO USUÁRIO · 2026-09-24] release v2.0.0 publicada: tag v2.0.0 = main @ 3dac1ea; CI da main verde
  (run 36082635036); VERIFICACAO-RELEASE GREEN.
[VERIFICADO] base deste lote = git archive v2.0.0 (SHA-256 941276…2177, 439 arquivos), o mesmo snapshot da auditoria
  ADERENCIA-V2.0.0.md.
```

## RED (o que o lote corrige)

```text
[VERIFICADO: código da v2.0.0] #40 Parcial:
  - de 26 operações REST, só POST /projects e POST /responsibles têm @ExampleObject de request/resposta;
  - @Schema(example) só em ProjectRequest, ProjectStatusRequest e ResponsibleRequest;
  - 0 de 13 @RequestParam e 0 de 12 {id} com exemplo;
  - sem exemplo: SecretariatRequest, ResponsibleCredentialsRequest, as 7 respostas, LoginRequest, CsrfResponse,
    AuthResponse e HealthStatus.
[VERIFICADO: ProjectPersistenceAdapter.java:252 da v2.0.0] LIKE com o texto cru ("%" + text + "%").
[EXECUTADO · harness] padrão antigo × nomes do IT, com a semântica do LIKE do PostgreSQL (barra = escape padrão):
  "100%" → Meta 100%, Meta 1000 · "%" → todos · "lote_a" → Lote_A, LoteXA · "_" → todos · "c:\d" → Pasta C:docs.
  5 dos 6 casos do IT falham com o código antigo.
```

## Gate local do candidato (rev1) — RED

```text
[EXECUTADO PELO USUÁRIO · 2026-09-25 08:25] seção 0 → HOTFIX_OK (hotfix/2.0.1 = v2.0.0 = 3dac1ea, GitHub e GitLab);
  seção 1 → sha256 SUCESSO, PREPARO_OK; gate:
  F5_P1_PRECONDITIONS_GREEN (33 arquivos)
  BACKEND: integração 51 testes, 1 falha → OpenApiContractIT.everyRestOperationHasExamplesForParametersRequestBodyAndSuccessResponses:
    GET/PUT /projects/{id}, PATCH /projects/{id}/status, GET /projects (página) → ProjectResponse.responsibleIds[] sem exemplo
    PUT /projects/{id} corpo → ProjectRequest.responsibleIds[] sem exemplo
    POST /auth/login e GET /auth/me → AuthResponse.authorities[] sem exemplo
  Resultado: exit code 1. Nada commitado.
  Leitura: todos os campos escalares e parâmetros receberam exemplo; só o exemplo de @ArraySchema(arraySchema = …) não
  chegou ao /api-docs. As outras 50 integrações passaram, inclusive o IT do filtro literal no PostgreSQL 18.6.
[CORREÇÃO · rev2] ApiListExampleDocumentation (OpenApiCustomizer) põe o exemplo no item das 3 listas; @ArraySchema
  removido; ProjectRequest.java volta a ser igual ao da v2.0.0 (fica no pacote só para desfazer o rev1 na sua árvore);
  o gate aceita o exemplo de responsibleIds na lista ou no item.
```

## GREEN neste ambiente (limitado)

```text
[EXECUTADO · 2026-09-25] swagger-annotations 2.2.47 (versão do springdoc 2.8.17): 42 fontes baixadas do GitHub e
  compiladas com javac 21 → as anotações usadas existem com esses atributos.
[EXECUTADO · 2026-09-25, rev2] javac 21 -Xlint:all -Werror sobre 21 arquivos de produção: delivery/rest (menos o handler
  e a documentação de erros), delivery/auth, ApiExamples e o adaptador. Domínio, aplicação e o que eles referenciam
  entram via -sourcepath. Anotações reais do swagger; stubs mínimos de Spring/Jakarta e de swagger-models/springdoc
  (assinaturas conferidas nas fontes 2.2.47/2.8.17) → exit 0, 83 classes.
[EXECUTADO · 2026-09-25] javac 21 -Xlint:all -Werror sobre OpenApiContractIT e ProjectPersistenceAdapterIT, com stubs de
  JUnit, AssertJ, Jackson (assinaturas conferidas no JsonNode 2.19.2), Spring Test e Testcontainers → exit 0.
[EXECUTADO · harness] containsPattern real (classe compilada, chamada por reflexão):
  100% → %100\%% · % → %\%% · lote_a → %lote\_a% · _ → %\_% · c:\d → %c:\\d% · meta → %meta%;
  com ESCAPE '\', os 6 casos do IT dão o resultado esperado.
```

## Ensaio do gate

```text
[EXECUTADO · 2026-09-25] repositório Git montado com a árvore da v2.0.0 (commit + tag anotada v2.0.0 + main e develop),
  remotos locais origin e gitlab, pacote em ~/Downloads; Maven e Docker simulados; API simulada por um servidor local
  com CSRF, sessão, /api-docs sintético com as 26 operações, swagger-initializer.js e busca literal (REST e GraphQL):
  seção 0 → HOTFIX_OK · seção 1 → sha256 OK, PREPARO_OK · bash -n → sintaxe OK
  F5_P1_PRECONDITIONS_GREEN (33 arquivos) · F5_P1_BACKEND_GREEN · F5_P1_STATIC_GREEN · F5_P1_DOCKER_GREEN
  F5_P1_API_GREEN (17 verificações) · === F5-P1 GREEN === · Resultado: exit code 0
  seção 4 → 4 commits semânticos + merge --no-ff na hotfix/2.0.1 + push nos dois remotos; árvore limpa.
[EXECUTADO · casos negativos, cada um com exit code 1]
  exemplo removido de SecretariatRequest.name → "POST /api/v1/secretariats corpo … SecretariatRequest.name sem exemplo";
  CsrfToken publicado como parâmetro → "GET /api/v1/auth/csrf documenta parâmetros";
  swagger-initializer.js sem interceptor → "Swagger UI sem o envio do CSRF";
  busca sem escape → "nomes: esperado ['Meta 100%'], obtido [...4 nomes]";
  README.md alterado → "alteração fora do pacote: README.md".
[EXECUTADO · 2026-09-25, rev2] mesmo ensaio, reproduzindo o seu estado: seção 0, preparo com o pacote rev1 (branch com
  o rev1 aplicado, sem commit) e preparo com o rev2 por cima → PREPARO_OK; gate → todos os marcadores GREEN, 33
  arquivos, OpenAPI com exemplo no item de responsibleIds, Resultado: exit code 0; seção 4 → 4 commits + merge, árvore
  limpa. Caso negativo novo: item de ProjectRequest.responsibleIds sem exemplo → "ProjectRequest.responsibleIds[] sem
  exemplo", exit code 1. O ensaio não reproduz o springdoc real: o /api-docs da API simulada é escrito à mão.
[CORRIGIDO NO ENSAIO] a limpeza inicial (rm -f /tmp/f5p1-*) apagava o próprio /tmp/f5p1-gate.sh e impedia repetir o
  gate sem refazer o preparo; agora preserva o arquivo do gate.
```

## Não executado neste ambiente

```text
[NÃO EXECUTADO] mvn clean verify, springdoc real (/api-docs), PostgreSQL, imagem Docker, Swagger UI e CI (Maven Central
  403, sem JDK 25 e sem Docker aqui). Resolve: gate local (VERIFICACAO-USUARIO.md, seção 2), conferência no navegador
  (seção 3) e CI no gate do F5-P2.
```
