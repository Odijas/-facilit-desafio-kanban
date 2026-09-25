# F5-L2 — EXECUÇÃO

Data: 2026-09-24

## Base

```text
[EXECUTADO PELO USUÁRIO · 2026-09-24 09:07] F5-L1 rev2 GREEN (exit code 0), docs/evidence/F5-L1/GATE.md.
[VERIFICADO] base do F5-L2 = árvore do F5-L1 rev2 (a mesma do pacote SHA-256 ef481a2a…4d65) + promoção documental do F5-L1.
```

## RED (comportamento da v1.0.0 / F5-L1 que este lote muda)

```text
[VERIFICADO: código do F5-L1] ProjectStatusTransition lançava IllegalArgumentException → RestExceptionHandler 400 INVALID_REQUEST,
  com mensagens em inglês ("Cannot transition IN_PROGRESS to OVERDUE: ..."); linhas 4, 11 e 12 apagavam a data sem confirmação;
  DataIntegrityViolationException sem tratador → 500 INTERNAL_ERROR; @ApiResponses só em POST /projects e POST /responsibles.
[EXECUTADO PELO USUÁRIO · 2026-09-23] gate F4 (v1.0.0): "transição bloqueada Em andamento → Atrasado" → 400, code INVALID_REQUEST.
```

## GREEN neste ambiente (limitado)

```text
[EXECUTADO · 2026-09-24] javac 21 -Xlint:all -Werror --release 21 sobre domain + application → exit 0.
[EXECUTADO · 2026-09-24] harness sem JUnit com as classes reais: 24 cenários de ProjectStatusTransitionTest conferidos
  (mensagens idênticas às esperadas nos testes; tipos TransitionBlockedException, ConfirmationRequiredException,
  BusinessRuleException) e L2Harness (confirmação via serviço, log de negócio exato por handler JUL, mensagem de status
  desatualizado, data realizada futura): TOTAL pass=7 fail=0.
  Suportes de teste (InMemory*, MutableClock, CapturedBusinessLog) compilados com -Xlint:all -Werror.
[EXECUTADO · 2026-09-24] frontend (Node 22, pnpm 12.5.1, lockfile congelado):
  pnpm format: Checked 40 files · Fixed 1 file (formatação do código novo)
  pnpm lint: Checked 40 files · No fixes applied
  pnpm typecheck: exit 0
  pnpm check:strict: STRICT_TYPES_GREEN 32
  pnpm test: Test Files 6 passed (6) · Tests 33 passed (33)   (30 anteriores + 3 novos)
  pnpm build: built (aviso não bloqueante de chunk > 500 kB, já conhecido)
[EXECUTADO · 2026-09-24] application.yml válido (PyYAML); coleção Postman JSON válido.
```

## Ensaio do gate

```text
[EXECUTADO · 2026-09-24] repositório Git montado com v1.0.0 → commit do F5-L1 → branch feature/f5-l2-contrato-de-erro + pacote,
  frontend real (pnpm) e Maven/Docker simulados:
  bash -n → sintaxe OK
  F5_L2_PRECONDITIONS_GREEN (63 arquivos, todos do pacote)
  F5_L2_FRONTEND_GREEN (STRICT_TYPES_GREEN 32; Tests 33 passed; format sem alteração)
  F5_L2_BACKEND_GREEN (só o parser de relatórios; o Maven era simulado)
  F5_L2_STATIC_GREEN
  parou no Docker simulado, como esperado.
[EXECUTADO · 2026-09-24] trechos da etapa de API testados isolados com dados sintéticos:
  expect_json (igualdade, prefixo com "^", caminho em lista, null) → OK; caso negativo → falha como esperado.
  Achado e corrigido antes da entrega: as chamadas usavam '^detail=...' em vez de 'detail=^...'.
  checagem do OpenAPI → aponta operação sem 409 documentado (caso negativo sintético).
  checagem dos eventos no log ECS → 8 eventos reconhecidos, logger br.com.facilit.kanban.business.
```

## Não executado neste ambiente

```text
[NÃO EXECUTADO] mvn clean verify (Maven Central 403; sem JDK 25): compilação dos tratadores REST/GraphQL, ApiErrorDocumentation,
  RestExceptionHandlerTest, OpenApiContractIT, ITs e o log real no contêiner. Resolve: gate local (VERIFICACAO-USUARIO.md).
```


## Gate local e correção auditável da granularidade do histórico

```text
[EXECUTADO PELO USUÁRIO · 2026-09-24] gate F5-L2 completo: frontend 33/33; backend 91 unitários + 20 integração; Docker V1-V6; API 18 verificações; OpenAPI 23 operações; 11 eventos de negócio sem e-mail/senha; === F5-L2 GREEN ===; Resultado: exit code 0.

[VERIFICADO: saída do shell e commit 8177c8e · 2026-09-24] o comando do primeiro commit granular falhou com "unrecognized history modifier" por causa de `!` no assunto entre aspas duplas. Os 43 arquivos de backend permaneceram staged e foram absorvidos pelo commit seguinte, que passou a conter 45 arquivos (43 backend + ApiErrorDocumentation.java + OpenApiContractIT.java).

[EXECUTADO PELO USUÁRIO · 2026-09-24] correção sem force-push e sem reescrita de histórico publicado, na branch fix/f5-l2-historico-granular:
  87fa002 Revert "docs(api): documenta os erros de cada operação no OpenAPI"
  a91cc32 feat(backend)!: erros de negócio 422, confirmação obrigatória, mensagens em pt-BR e logs de negócio
  009b432 docs(api): documenta os erros de cada operação no OpenAPI

[EXECUTADO PELO USUÁRIO · 2026-09-24] prova de equivalência:
  BASE_TREE=122f2449c0bd7f588ea43ee57e795389a058b8c3
  FIX_TREE=122f2449c0bd7f588ea43ee57e795389a058b8c3
  ARVORE_EQUIVALENTE_GREEN

[EXECUTADO PELO USUÁRIO · 2026-09-24] gate F5-L2 reexecutado em worktree temporário reconstruído sobre 5e3781b:
  arquivos_reconstruidos=63
  F5_L2_PRECONDITIONS_GREEN
  F5_L2_FRONTEND_GREEN
  F5_L2_BACKEND_GREEN
  F5_L2_STATIC_GREEN
  F5_L2_DOCKER_GREEN
  F5_L2_API_GREEN
  F5_L2_OPENAPI_GREEN
  F5_L2_BUSINESS_LOGS_GREEN
  === F5-L2 GREEN ===
  Resultado: exit code 0
  GATE_EXIT=0
  F5_L2_REVALIDADO_GREEN
```

[EXECUTADO PELO USUÁRIO · 2026-09-24] a saída integral da revalidação foi preservada em `docs/evidence/F5-L2/SAIDA-GATE-HISTORICO.txt` no commit `f7e17b5` e promovida a `develop` pelo merge `7a28996`.

[VERIFICADO: saída do usuário · 2026-09-24] após o merge, `develop`, `origin/develop` e `gitlab/develop` apontam para `7a28996ef2c28189ae84d4c8498c12c70fcb364f`, com working tree limpa.
