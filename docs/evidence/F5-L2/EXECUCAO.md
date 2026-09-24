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
