# F1-L2 — EXECUÇÃO

Data: 2026-09-22

## Executado neste ambiente

```text
[EXECUTADO] `javac -Xlint:all -Werror` sobre todo `domain` + `application`: GREEN.
[EXECUTADO] `F1L2Probe`: `F1_L2_APPLICATION_PROBE_GREEN`.
[EXECUTADO] o probe percorreu as 12 relações de origem→destino, incluindo caminhos legítimos e bloqueios, e validou listagem por status + persistência in-memory via `ProjectService`.
[EXECUTADO] `git diff --no-index --check` entre F1-L1 corrigido e F1-L2: sem erro de whitespace.
[EXECUTADO] busca de consumidores com `grep -RIn` registrada em `CONSUMIDORES.md`.
```

## Não executado neste ambiente

`[DESCONHECIDO]` Maven/Spring Context, binding REST/GraphQL, Spring Data JPA, PostgreSQL e Docker do F1-L2 ainda dependem do gate no ambiente do usuário. Este runtime possui Java 21 e não possui Maven/Docker configurados para o projeto.

## Gate executado pelo usuário

Em 2026-09-22, o usuário executou integralmente `VERIFICACAO-USUARIO.md` e reportou `=== F1-L2 GREEN ===` seguido de `Resultado: exit code 0`. Também reportou health REST/GraphQL, frontend HTTP 200 e os três containers ativos, com PostgreSQL `healthy`.

Os itens anteriormente `[DESCONHECIDO]` relativos ao binding real de status, filtro JPA, transições REST/GraphQL e execução Docker foram resolvidos por esse gate.
