# F5-C1 — EXECUÇÃO

Data: 2026-09-24

## Base

```text
[EXECUTADO PELO USUÁRIO · 2026-09-24 21:24] snapshot da release/2.0.0 @ 59051d0 (SHA-256 43051359…abac17fd718f, 404 arquivos,
  árvore limpa), usado na auditoria final (ADERENCIA-FINAL.md) e como base deste lote.
[EXECUTADO PELO USUÁRIO] F5-L4 GREEN (docs/evidence/F5-L4/SAIDA-GATE-FINAL.txt): 24 relatórios/165 testes unitários e BDD,
  12 relatórios/48 de integração, JaCoCo 95,61% — sem etapa Docker.
```

## RED (o que o lote corrige)

```text
[VERIFICADO: backend/Dockerfile:6] RUN mvn -B -ntp -DskipITs verify, com jacoco:check (LINE ≥ 0,95) ligado a verify
  (backend/pom.xml:204-217). O limite foi medido com unitários + integração; na imagem rodam só os unitários.
[INFERÊNCIA, a reproduzir no gate] docker compose up --build falha no build do backend (Coverage checks have not been met).
[VERIFICADO: application.yml:37-43; SecurityConfiguration.java:61-64] Swagger UI sem springdoc.swagger-ui.csrf; a API exige
  X-XSRF-TOKEN em toda escrita, inclusive no login → o "Try it out" autenticado respondia 403.
```

## GREEN neste ambiente (limitado)

```text
[EXECUTADO · 2026-09-24] código-fonte do springdoc-openapi v2.8.17 conferido: propriedade csrf.enabled, cookie XSRF-TOKEN,
  cabeçalho X-XSRF-TOKEN e requestInterceptor injetado no swagger-initializer.js.
[EXECUTADO · 2026-09-24] ensaio do gate num repositório Git montado com o snapshot da release commitado + os 15 arquivos
  do pacote; Maven e Docker simulados; API simulada por um servidor local com o comportamento de CSRF da aplicação
  (cookie em toda requisição, 403 sem cabeçalho igual ao cookie, login apaga o cookie):
  bash -n → sintaxe OK
  F5_C1_PRECONDITIONS_GREEN (15 arquivos; frontend intocado)
  F5_C1_BACKEND_GREEN (parser de relatórios e do jacoco.xml)
  F5_C1_STATIC_GREEN (Dockerfile, CI com a política do freeze, CSRF no application.yml, README)
  F5_C1_DOCKER_IMAGE_GREEN (e o registro do RED do Dockerfile anterior)
  F5_C1_DOCKER_GREEN
  F5_C1_SWAGGER_GREEN (8 verificações do fluxo cookie → cabeçalho)
  === F5-C1 GREEN === · Resultado: exit code 0
[EXECUTADO · 2026-09-24] caso negativo: Dockerfile anterior no lugar do novo → pré-condição acusa arquivo fora do pacote.
```

## Não executado neste ambiente

```text
[NÃO EXECUTADO] mvn clean verify, construção real das imagens, API real e CI (Maven Central 403, sem JDK 25 e sem Docker aqui).
  Resolve: gate local (VERIFICACAO-USUARIO.md, seção 2), conferência no navegador (seção 3) e CI (seção 4).
```
