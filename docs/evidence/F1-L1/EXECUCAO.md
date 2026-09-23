# F1-L1 — EXECUÇÃO

Data: 2026-09-22

## Executado neste ambiente

```text
[EXECUTADO] javac -Xlint:all -Werror sobre todo `domain` + `application`: passou após correção do serialVersionUID nas exceções.
[EXECUTADO] javac -Xlint:all -Werror sobre os fakes de aplicação: passou.
[EXECUTADO] F1L1ApplicationProbe: F1_L1_APPLICATION_PROBE_GREEN.
[EXECUTADO] Busca de consumidores da baseline com ripgrep antes das mudanças.
[EXECUTADO] RAG nas documentações oficiais Spring Boot 3.5.16, Spring Data JPA, Spring GraphQL 1.4 e Spring Framework 6.2.
[EXECUTADO] scan das camadas internas confirmou ausência de imports Spring/JPA em `domain` e `application`.
[EXECUTADO] scan de código de produção confirmou ausência de comentários inline.
[EXECUTADO] verificação de whitespace nos arquivos do escopo passou.
[EXECUTADO] script do gate local passou em `bash -n`.
```

O probe exercitou caminho legítimo e caminho de ataque: criação de responsável, normalização de e-mail, bloqueio de duplicidade, criação/listagem de projeto, bloqueio da remoção de responsável associado e exclusões após remover a associação.

## Não executado neste ambiente

`[DESCONHECIDO]` Maven, Spring Context, PostgreSQL, Flyway, REST, GraphQL e Docker não foram executados aqui porque este runtime não possui Maven, Docker nem Java 25. O gate local obrigatório está em `VERIFICACAO-USUARIO.md`.

## Correção pós-gate RED

```text
[EXECUTADO PELO USUÁRIO] `mvn clean verify` falhou nos dois slice tests de health porque `ApplicationBeans` exigia `ResponsibleRepository`, ausente no contexto reduzido.
[VERIFICADO] consumidores de `ApplicationBeans` em testes: somente `HealthRestControllerTest` e `HealthGraphQlControllerTest`.
[CORRIGIDO] ambos os testes passaram a importar diretamente `HealthQuery`, mantendo a composition root de produção intacta.
[EXECUTADO] busca estática confirmou ausência de outros consumidores de teste de `ApplicationBeans`; `git diff --check` passou no pacote corrigido.
```

`[RESOLVIDO]` A lacuna acima foi fechada pela reexecução do usuário descrita a seguir.

## Fechamento pós-correção

```text
[EXECUTADO PELO USUÁRIO · 2026-09-22] health REST: {"status":"UP"}
[EXECUTADO PELO USUÁRIO · 2026-09-22] mvn + gate pós-correção: exit code 0.
[EXECUTADO PELO USUÁRIO · 2026-09-22] comando externo de validação: exit code 0.
```

A correção dos slice tests foi, portanto, validada no ambiente real do usuário e o F1-L1 foi promovido para GREEN.
