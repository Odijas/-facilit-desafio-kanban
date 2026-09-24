# F5-L1 — RISCOS E LACUNAS

Data: 2026-09-24

- `[DESCONHECIDO]` build Spring real, JUnit/AssertJ, Testcontainers e Docker não executados neste ambiente (Maven Central 403, sem JDK 25). A lógica foi executada por harness e a infraestrutura compilada contra stubs. Resolve: gate local (`F5_L1_BACKEND_GREEN`, `F5_L1_API_GREEN`).
- `[HIPÓTESE]` projeção por interface (`StaleScheduleView`) com `Pageable` num `@Query` que retorna `List`, e `@Modifying` com parâmetro `short`. É padrão do Spring Data JPA/Hibernate 6, mas não foi executado aqui. O Hibernate valida as consultas na subida, então um erro aparece em qualquer IT. Resolve: `F5_L1_BACKEND_GREEN`.
- `[HIPÓTESE]` `System.Logger` do `ProjectScheduleRefresher` chega ao Logback pela ponte JUL do Spring Boot. O aviso só ocorre com dado inconsistente, então não afeta o gate. Se não chegar, o aviso sai no JUL padrão (stderr) e não se perde.
- `[VERIFICADO]` janela de até 3 horas logo após a migration: linhas gravadas pela v1.0.0 entre 21h e meia-noite de Recife têm data de cálculo = dia seguinte (UTC) e só convergem à meia-noite local (ADR 0002 §4).
- `[VERIFICADO]` o gate usa "hoje" em `America/Sao_Paulo` e recusa rodar entre 23h50 e 0h10, porque a virada do dia no meio do gate invalida as asserções de data.
- `[VERIFICADO]` o gate do F4 (`docs/evidence/F4/VERIFICACAO-USUARIO.md`) é histórico. Ele espera migrations `1 2 3 4 5` e usa data UTC, então não se aplica à árvore do F5.
- `[VERIFICADO]` fora do lote, conforme o plano:
  - códigos de erro de negócio (422), mensagens em pt-BR das regras antigas e confirmações obrigatórias (F5-L2);
  - testes de controller com mocks, transação por caso de uso e `@Version` (F5-L3).
- `[VERIFICADO]` frontend sem alteração neste lote; o gate não roda o frontend. O CI da branch roda o job `frontend` no push.
- `[VERIFICADO]` achado próprio, repetido:
  - O gate rev1 exigia `v1.0.0` como ancestral de `HEAD`. Em Gitflow, a `develop` contém a branch release, e não o merge da `main` onde fica a tag.
  - É o mesmo erro já corrigido no gate de release do F4, reincidente aqui.
  - O rev2 aceita a tag ou o 2º pai do merge e compara o conteúdo com a tag.
- `[VERIFICADO]` o passo 1 do rev1 não podia ser repetido (`git checkout -b` falhava com a branch já existente) e não parava em erro. No rev2, o bloco é encadeado com `&&` e trata a branch existente.
