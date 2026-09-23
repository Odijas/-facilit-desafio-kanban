# F3-L2 — EXECUÇÃO

Data: 2026-09-23

## Base

```text
[EXECUTADO PELO USUÁRIO · 2026-09-23] F3-L1 rev4 GREEN (exit code 0).
[VERIFICADO] base do F3-L2 = conteúdo do F3-L1 rev4 + docs/governance/REPLANEJAMENTO-F3.md.
```

## Executado neste ambiente

```text
[EXECUTADO · 2026-09-23] javac -Xlint:all -Werror (JDK 21) em domain + application + dublês em memória: exit 0.
[EXECUTADO · 2026-09-23] sonda descartável (fora do projeto) exercitando os casos de uso com dublês em memória: 20 verificações PASS, PROBE_GREEN.
  ataque: responsável cria/edita responsável, cria secretaria, define credencial → ForbiddenOperationException;
          transição/edição/exclusão de projeto alheio e remoção de si mesmo → ForbiddenOperationException;
          senha curta e senha acima de 72 bytes → IllegalArgumentException;
          e-mail de login já usado pelo ADMIN → ConflictException.
  legítimo: criação, transição e exclusão do próprio projeto; definição e revogação de credencial pelo ADMIN.
[EXECUTADO · 2026-09-23] javac sem dependências externas sobre todo backend/src: nenhum erro de símbolo do próprio projeto; erros restantes são exclusivamente de bibliotecas ausentes (Spring, JPA, JUnit, AssertJ).
[EXECUTADO · 2026-09-23] graphql-js 16.14.2 contra health.graphqls + kanban.graphqls: createSecretariat (gate/IT), setResponsibleCredentials, revokeResponsibleCredentials → VALID.
[EXECUTADO · 2026-09-23] frontend RED: novos testes contra os componentes anteriores → Test Files 3 failed | 3 passed; Tests 3 failed | 27 passed.
  × limits a responsible to their own projects
  × hides secretariat management actions from non-administrators
  × shows the responsible dashboard for a responsible user
[EXECUTADO · 2026-09-23] frontend GREEN (Node 22.22.2, pnpm 12.5.1):
  pnpm format: Checked 38 files. Fixed 3 files (formatação dos arquivos editados)
  pnpm lint: No fixes applied · exit 0
  pnpm typecheck: exit 0
  pnpm test: Test Files 6 passed (6); Tests 30 passed (30)
  pnpm build: built · exit 0
[EXECUTADO · 2026-09-23] varredura AST: STRICT_TYPES_GREEN 31; sem localStorage/sessionStorage.
[EXECUTADO · 2026-09-23] gate extraído por awk e `bash -n`: GATE_F3L2_BASH_SYNTAX_GREEN.
[EXECUTADO · 2026-09-23] `git diff --no-index --check` contra o rev4: acusou linha em branco no fim do README.md (erro próprio, corrigido antes do empacotamento); repetido sem diagnóstico.
```

## Não executado neste ambiente

```text
[DESCONHECIDO] compilação Spring completa, testes JUnit (unitários e ITs com Testcontainers) e runtime Docker: Maven Central bloqueado (403) e JDK 25 indisponível. Resolve: gate local.
[DESCONHECIDO] frontend em Node 24. Resolve: gate local.
```
