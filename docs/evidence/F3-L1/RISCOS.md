# F3-L1 — RISCOS E LACUNAS

Data: 2026-09-22

- `[DESCONHECIDO]` gate completo ainda não executado no ambiente do usuário; não promover antes de exit code 0.
- `[VERIFICADO]` a pesquisa textual usa `%texto%`, portanto os B-tree existentes não são alegados como índice para substring. `pg_trgm` não foi introduzido sem prova de disponibilidade/runtime; é trade-off explícito do lote low-risk.
- `[VERIFICADO]` indicadores agregam `delayDays` persistido, coerente com a arquitetura baseline que recalcula métricas nas operações de projeto; este lote não altera a política temporal aprovada em F0/F1.
- `[VERIFICADO]` filtros de período exigem as datas correspondentes do projeto quando o limite é informado; projetos sem data não são incluídos nesse limite.
- `[VERIFICADO]` Secretaria não possui unicidade de nome porque o desafio não define essa regra e adicioná-la seria requisito inventado.

## Rev2 — 2026-09-22

- `[DESCONHECIDO]` backend (`mvn clean verify`, Testcontainers) e runtime Docker não executados neste ambiente (Maven Central 403, sem JDK 25). Resolve: gate local.
- `[DESCONHECIDO]` frontend executado em Node 22.22.2; o projeto exige Node 24. Resolve: gate local.
- `[VERIFICADO]` o gate exige repositório Git (`git diff --check`); o rev2 verifica isso no início.
- `[VERIFICADO]` pendência classificada: autenticação do responsável (F3), sem desenho aprovado.
- `[VERIFICADO]` aviso de build Vite: bundle de 539.52 kB (> 500 kB). Não bloqueante; code-splitting seria mudança fora do escopo do lote.

## Rev3 — 2026-09-22

- `[DESCONHECIDO]` compilação e ITs backend do rev3 não executados neste ambiente. Resolve: gate local.
- `[VERIFICADO: SecurityConfiguration.java:80–83]` pendência fora do escopo do F3-L1: `/error` cai em `anyRequest().denyAll()`; qualquer exceção não tratada (500) chega ao cliente como 403 "Access denied", mascarando a causa. Afeta o tratamento padronizado de erros exigido pelo desafio. Não corrigido neste lote; proposto para decisão em gate.
- `[VERIFICADO]` o gate passa a exercitar listagem sem filtros e filtro de texto isolado (`F3_L1_LIST_UNFILTERED_GREEN`), caminhos que nem o gate nem `KanbanApiIT` cobriam.

## Rev4 — 2026-09-22

- `[HIPÓTESE]` graphql-js e graphql-java implementam a mesma regra de validação da especificação GraphQL (uso de variáveis em posição permitida); a mensagem reproduzida por graphql-js é equivalente à recebida do backend. Só o gate local confirma no graphql-java.

## Fechamento — 2026-09-23

- `[EXECUTADO PELO USUÁRIO · 2026-09-23]` gate completo com exit code 0; as lacunas de execução backend/runtime registradas nos rev2–rev4 foram fechadas.
- `[VERIFICADO]` pendências classificadas e transferidas para o F3-L2 aprovado: autenticação do responsável; `/error` em `denyAll` (500 não tratado chegando como 403).
- `[VERIFICADO]` aviso não bloqueante mantido: bundle Vite acima de 500 kB.
- `[VERIFICADO]` pendência do F3-L4 (engenharia de entrega): opção de CI ainda não decidida pelo usuário.
