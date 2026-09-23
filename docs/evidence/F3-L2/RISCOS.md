# F3-L2 — RISCOS E LACUNAS

Data: 2026-09-23

- `[DESCONHECIDO]` compilação Spring, testes JUnit e ITs não executados neste ambiente (Maven Central 403, sem JDK 25). Resolve: gate local.
- `[HIPÓTESE]` o `Principal` chega aos controllers GraphQL pela propagação de contexto do Spring for GraphQL (argumento `Principal` documentado). `KanbanApiIT` exercita mutation GraphQL como ADMIN e revela a falha, se houver.
- `[HIPÓTESE]` o tratador genérico `@GraphQlExceptionHandler(Exception)` não intercepta exceções que já têm tratador específico (resolução pela exceção mais próxima). `KanbanApiIT` cobre data inválida → `INVALID_REQUEST`.
- `[HIPÓTESE]` o `TestRestTemplate` do projeto envia `PATCH` (cliente HTTP com suporte a PATCH no classpath de teste). `SecurityApiIT` usa PATCH na transição do responsável.
- `[VERIFICADO]` sessão de um responsável cujo e-mail foi alterado pelo ADMIN deixa de resolver o ator (403) até novo login. Comportamento seguro; documentado.
- `[VERIFICADO]` se já existir um responsável com o e-mail de demonstração sem credencial, a subida falha com conflito explícito (fail-fast), no mesmo espírito do bootstrap do ADMIN.
- `[VERIFICADO]` não há tela de gestão de credenciais no frontend; o ADMIN usa REST/Swagger ou GraphQL. Fora do escopo aprovado.
- `[VERIFICADO]` aviso não bloqueante mantido: bundle Vite acima de 500 kB.

## Fechamento — 2026-09-23

- `[EXECUTADO PELO USUÁRIO · 2026-09-23]` gate completo com exit code 0: compilação Spring, unitários, ITs com Testcontainers e runtime Docker confirmados; os `[DESCONHECIDO]` e `[HIPÓTESE]` acima (propagação do `Principal` no GraphQL, precedência do tratador genérico, PATCH no `TestRestTemplate`) foram exercitados pela suíte e pelo gate sem falha.
- `[VERIFICADO]` mantidos como estão, fora do escopo aprovado: ausência de tela de credenciais no frontend; aviso não bloqueante de bundle Vite acima de 500 kB.
