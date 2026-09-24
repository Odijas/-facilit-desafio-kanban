# F5-L3 — EXECUÇÃO

Data: 2026-09-24

## Base

```text
[EXECUTADO PELO USUÁRIO · 2026-09-24 09:38] F5-L2 GREEN (exit code 0), docs/evidence/F5-L2/GATE.md.
[VERIFICADO: docs/evidence/F5-L2/GATE.md:1-55 · 2026-09-24] base do F5-L3 = `develop` com o F5-L2 encerrado e promovido; após a correção auditável do histórico, a base operacional deste pacote é o commit documental final `bf56b61`.
```

## RED (o que faltava para o desafio)

```text
[VERIFICADO: árvore do F5-L2] nenhum teste de controller com o service simulado (os controllers só eram exercidos pelos ITs
  com servidor real); nenhuma transação de aplicação: cada método do adaptador JPA abria a própria transação, então um caso
  de uso com mais de uma escrita (por exemplo, responsável + credencial) não era atômico; sem @Version: duas edições
  simultâneas do mesmo projeto se sobrescreviam; a tabela de transição só era testada no domínio e em trechos do gate.
```

## Verificações preparatórias deste ambiente

```text
[EXECUTADO · 2026-09-24] javac 21 -Xlint:all -Werror --release 21 sobre domain + application → exit 0.
[EXECUTADO · 2026-09-24] javac 21 -Xlint:all -Werror sobre ProjectJpaEntity, ProjectJpaRepository, ProjectPersistenceAdapter,
  ResponsibleJpa*, SecretariatJpaEntity e SpringTransactionRunner, com stubs das anotações JPA/Spring (inclui @Version e
  TransactionTemplate) → exit 0.
[EXECUTADO · 2026-09-24] javac sobre os 13 arquivos de teste novos e os 5 testes de serviço alterados (construtores), contra
  domain + application compilados: só erros de símbolos de bibliotecas ausentes aqui (JUnit, AssertJ, Mockito, Spring Test,
  Testcontainers) e de classes de entrega não compiladas; nenhum erro de sintaxe, de construtor ou de símbolo do projeto.
[EXECUTADO · 2026-09-24] harness sem JUnit com as classes reais (ProjectService + repositórios em memória +
  DirectTransactionRunner): confirmação, log de negócio exato e na mesma ordem, status desatualizado, data realizada futura
  → 7 verificações OK; 8 execuções da porta de transação para 8 casos de uso de escrita chamados.
[EXECUTADO · 2026-09-24] backend/pom.xml é XML válido.
```

## Ensaio do gate

```text
[EXECUTADO · 2026-09-24] repositório Git montado com a árvore do F5-L2 commitada → branch feature/f5-l3-camadas-de-teste
  + os 40 arquivos do pacote reconciliado; Maven simulado gerando relatórios com as contagens reais de @Test de cada classe; Docker simulado:
  bash -n → sintaxe OK
  F5_L3_PRECONDITIONS_GREEN (40 arquivos, todos do pacote reconciliado; frontend intocado)
  F5_L3_BACKEND_GREEN (23 classes/140 testes unitários; 9 classes/45 testes de integração; 13 classes com contagem exata)
  F5_L3_STATIC_GREEN
  parou no Docker simulado, como esperado.
[EXECUTADO · 2026-09-24] casos negativos: aviso "self-attaching" no log do Maven → FALHA do agente do Mockito;
  arquivo novo em frontend/ → "alteração fora do pacote"; README com "Flyway V1–V6" → FALHA na estática.
```

## Limitação do ambiente de preparação — resolvida pelo gate local

```text
[VERIFICADO: saída real do gate local fornecida pelo usuário · 2026-09-24] a limitação acima foi resolvida: `mvn clean verify`, testes de controller/Mockito/@DataJpaTest, `TransactionIT`, `StatusTransitionApiIT`, Docker com V7 e smoke de API passaram no gate F5-L3 com exit code 0.
```

## Gate local real

```text
[EXECUTADO PELO USUÁRIO · 2026-09-24] F5_L3_PRECONDITIONS_GREEN.
[EXECUTADO PELO USUÁRIO · 2026-09-24] F5_L3_BACKEND_GREEN: 23 classes/140 unitários; 9 classes/45 integração; 0 falhas/erros/ignorados; controller tests 17/9/6/2 REST e 5/3/2 GraphQL; ProjectServiceMockitoTest 5; ProjectPersistenceAdapterIT 5; TransactionIT 4; StatusTransitionApiIT 16.
[EXECUTADO PELO USUÁRIO · 2026-09-24] F5_L3_STATIC_GREEN.
[EXECUTADO PELO USUÁRIO · 2026-09-24] F5_L3_DOCKER_GREEN: migrations V1–V7 e projects.version bigint NOT NULL DEFAULT 0.
[EXECUTADO PELO USUÁRIO · 2026-09-24] F5_L3_API_GREEN: 18 cenários.
[EXECUTADO PELO USUÁRIO · 2026-09-24] === F5-L3 GREEN ===; Resultado: exit code 0.
```
