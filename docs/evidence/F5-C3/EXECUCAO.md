# F5-C3 — EXECUÇÃO

Data: 2026-09-24

## Base

```text
[EXECUTADO PELO USUÁRIO · 2026-09-24 22:14] F5-C2 GREEN (exit code 0), docs/evidence/F5-C2/SAIDA-GATE.txt; no mesmo gate,
  F5_C1_CI_GREEN: CI da release/2.0.0 @ e986aba (run 36080772265) com o passo da imagem Docker em sucesso.
[VERIFICADO] base do F5-C3 = árvore do F5-C2 (pacote SHA-256 664a9ca8…d236c03e5) + promoção documental do F5-C2.
```

## RED (o que o lote corrige)

```text
[VERIFICADO: coleção no snapshot 43051359] 23 requisições; os endpoints by-secretariat, by-responsible e deadlines do
  F5-L4 não eram exercitados, embora o README dissesse que a coleção "percorre indicadores".
[VERIFICADO: AI_USAGE.md] sem menção à F5 nem ao defeito central da v1.0.0 corrigido por ela.
[VERIFICADO: README.md] texto de trabalho ("o F5-L5 prepara a release"), "GraphQL espelha o REST" (páginas GraphQL sem
  totalElements), estrutura de pastas sem o ADR 0002, histórico reconstruído não declarado.
[VERIFICADO: docs/evidence/F5/GATE.md] "a release contém somente versionamento/documentação do L5" — contradito pelos lotes C.
```

## GREEN neste ambiente (limitado)

```text
[EXECUTADO · 2026-09-24] coleção: JSON regravado com a mesma indentação; diff só com inserções (143 linhas); 27 requisições.
[EXECUTADO · 2026-09-24] os 28 scripts da coleção compilam em Node 22 (new Function).
[EXECUTADO · 2026-09-24] scripts das 4 requisições novas executados com um objeto pm simulado e respostas no formato dos DTOs
  (ProjectGroupIndicatorResponse, ProjectDeadlinesResponse, ProblemDetail): 8 asserções OK nos casos certos; nos casos
  errados (contagem 2, prazo ausente, código VALIDATION_ERROR) as asserções falham como devem.
[EXECUTADO · 2026-09-24] npm i newman@6.2.2 → instalado (aviso informativo: @faker-js/faker@5.5.3 depreciado, dependência
  transitiva do newman).
```

## Ensaio do gate

```text
[EXECUTADO · 2026-09-24] repositório Git montado com a release/2.0.0 já com o F5-C2, remoto local, branch
  bugfix/2.0.0-c3-documentacao + os arquivos do pacote; Docker, npx/newman e API do GitHub simulados
  (relatório JSON do newman no formato real: run.stats e run.failures):
  bash -n → sintaxe OK
  F5_C3_PRECONDITIONS_GREEN (22 arquivos; nada em backend/, frontend/ ou .github/)
  F5_C3_STATIC_GREEN (checagens documentais do freeze, copiadas do docs/evidence/F5/VERIFICACAO-USUARIO.md, + as do lote)
  F5_C3_DOCKER_GREEN · F5_C3_NEWMAN_GREEN · F5_C2_CI_GREEN · === F5-C3 GREEN === · Resultado: exit code 0
[EXECUTADO · 2026-09-24] caso negativo: relatório do newman com 1 asserção falha → a falha é listada pelo nome e o gate sai
  com exit code 1.
[EXECUTADO · 2026-09-24] newman 6.2.2 real contra um servidor simulado: o relatório JSON tem run.stats.requests.total = 27 e
  run.failures[].source.name / error.test / error.message — a mesma estrutura que o gate lê.
```

## Não executado neste ambiente

```text
[NÃO EXECUTADO] newman contra a aplicação real, subida com banco novo e CI (sem Docker aqui).
  Resolve: gate local (VERIFICACAO-USUARIO.md, seção 2).
```
