# F5-P1 — RISCOS E LACUNAS

Data: 2026-09-25

- `[DESCONHECIDO]` O documento que o springdoc 2.8.17 gera de fato com as anotações novas.
  - Não há Maven Central aqui, então não houve execução do springdoc.
  - O código compila com as anotações reais do swagger-core 2.2.47.
  - Resolve: `OpenApiContractIT` (`F5_P1_BACKEND_GREEN`) e a mesma regra contra a API real (`F5_P1_API_GREEN`).
- `[VERIFICADO: gate local, 2026-09-25 8h25]` O `@Schema` nos componentes de record (`PageResponse<T>` genérico incluído) chega ao schema do springdoc: o `OpenApiContractIT` só acusou as listas.
  - Se algum campo ficar sem exemplo, o teste e o gate listam o campo, e a correção é um `@ExampleObject` na operação.
- `[VERIFICADO: gate local, 2026-09-25 8h25]` Hipótese refutada para listas: o exemplo de `@ArraySchema(arraySchema = …)` não chega ao `/api-docs`. Corrigido no rev2 com `ApiListExampleDocumentation` (ver `DECISOES.md`).
- `[HIPÓTESE]` O `OpenApiCustomizer` roda depois da geração dos schemas e o exemplo escalar no item é publicado.
  - É o mesmo ponto de extensão do `ApiErrorDocumentation`, já coberto pelo `OpenApiContractIT`.
  - Resolve: `OpenApiContractIT` e `F5_P1_API_GREEN`.
- `[VERIFICADO: gate local, 2026-09-25 8h25]` `ResponseEntity<Void>` (DELETE e credenciais) não gera `content` na resposta 2xx: nenhuma dessas operações foi acusada.
- `[VERIFICADO: gate local, 2026-09-25 8h25]` O `like … escape` do Hibernate 6 funciona no PostgreSQL 18.6: o `ProjectPersistenceAdapterIT`, com o teste novo, passou; das 51 integrações, só uma falhou. A busca na API real fica para o `F5_P1_API_GREEN`.
- `[VERIFICADO: harness]` Os padrões gerados por `containsPattern` (compilado aqui) e a semântica do `LIKE`, simulada com escape de barra invertida:
  - o novo código acerta os 6 casos do IT;
  - o código da v2.0.0 erra 5 deles (RED).
  - A simulação não substitui o PostgreSQL.
- `[VERIFICADO]` Fora do lote, e entram no F5-P2:
  - README, CHANGELOG `[2.0.1]`, AI_USAGE e as frases desatualizadas;
  - `ADERENCIA-V2.0.0.md` versionado;
  - promoção do F5-P1.
