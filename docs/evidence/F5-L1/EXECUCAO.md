# F5-L1 — EXECUÇÃO

Data: 2026-09-24

## Base

```text
[EXECUTADO PELO USUÁRIO · 2026-09-23] release v1.0.0: main a3497d8, develop 399f1c7, CI 35908112482 success (docs/evidence/F4/SAIDA-RELEASE.txt).
[VERIFICADO] base do F5-L1 = snapshot 655549c2-facilit-desafio-kanban-auditoria-20260923-170540.tar.gz
             (SHA-256 bb7c202f657124415af6db3a83ad82222de5606228542145bcb21830c98c5727), 304 arquivos.
```

## RED: defeitos da v1.0.0 reproduzidos (domínio da v1.0.0, sem alteração)

```text
[EXECUTADO · 2026-09-24] JDK 21, classes de domínio do snapshot v1.0.0 + programa Red.java:
  v1.0.0 status gravado (lido sem recálculo): IN_PROGRESS atraso=0 %=50
  status correto hoje: ProjectScheduleMetrics[status=OVERDUE, delayDays=8, remainingTimePercentage=0]
  v1.0.0 Em andamento -> Atrasado ACEITO (tabela manda bloquear): OVERDUE
  v1.0.0 hoje em 24/09 22:30 (Recife): 2026-09-25
```

Cenário: projeto gravado há 10 dias como Em andamento (início previsto −12, término previsto −8, início realizado −12), sem edição desde então.

## GREEN neste ambiente (limitado)

```text
[EXECUTADO · 2026-09-24] javac 21 -Xlint:all -Werror --release 21 sobre backend/src/main/java/.../domain e .../application → exit 0.
[EXECUTADO · 2026-09-24] javac 21 -Xlint:all -Werror sobre os arquivos de infraestrutura alterados ou novos
  (persistence/project/*, persistence/responsible/ResponsibleJpa*, config/ApplicationBeans, ProjectScheduleRefreshJob,
  SchedulingConfiguration) contra stubs mínimos das anotações e tipos Spring/JPA usados → exit 0.
  Os stubs verificam só tipos e assinaturas escritos neste lote; não substituem o build real.
[EXECUTADO · 2026-09-24] harness sem JUnit (L1Harness.java) com as classes reais de domínio/aplicação e os
  InMemory*Repository + MutableClock de teste, reproduzindo os cenários de ProjectScheduleRefresherTest (8),
  ProjectStatusTransitionTest (19, com os dados checados contra o calculador), ProjectDatesTest (4) e
  ProjectServiceTest.rejectsActualDatesAfterTodayOnCreateAndUpdate:
  TOTAL pass=66 fail=0
[EXECUTADO · 2026-09-24] application.yml (PyYAML) válido; app.time-zone e app.schedule-refresh.cron presentes.
[EXECUTADO · 2026-09-24] coleção Postman: JSON válido; "hoje" UTC−3 → 2026-09-25T01:30Z vira 2026-09-24 (Node 22).
[EXECUTADO · 2026-09-24] ensaio do gate (VERIFICACAO-USUARIO.md) num repositório Git montado a partir do snapshot v1.0.0
  (tag v1.0.0, branch feature/f5-l1-regras-sempre-corretas, pacote aplicado), com `mvn` e `docker` simulados:
  bash -n → sintaxe OK
  F5_L1_PRECONDITIONS_GREEN (42 arquivos alterados, todos do pacote)
  F5_L1_BACKEND_GREEN (só o parser de relatórios; o Maven era simulado)
  F5_L1_STATIC_GREEN (42 arquivos sem espaço sobrando; README, ADR 0002, links, coleção e application.yml OK)
  parou no Docker simulado, como esperado.
```

## Não executado neste ambiente

```text
[NÃO EXECUTADO] mvn clean verify (Maven Central 403 neste ambiente; sem JDK 25), JUnit/AssertJ reais, Testcontainers,
  ScheduleFreshnessIT, ScheduleCalculationDateMigrationIT, subida Docker. Resolve: gate local (VERIFICACAO-USUARIO.md).
```

## rev1 RED no gate local e correção (rev2)

```text
[EXECUTADO PELO USUÁRIO · 2026-09-24 08:57] pacote rev1 conferido (SHA-256 SUCESSO), branch feature/f5-l1-regras-sempre-corretas:
  === PRÉ-CONDIÇÕES ===
  FALHA: a branch não parte da v1.0.0
  Resultado: exit code 1
[EXECUTADO · 2026-09-24] reprodução: repositório com a topologia Gitflow da release (main ← merge --no-ff da release + tag
  anotada v1.0.0; develop ← merge --no-ff da mesma release): `git merge-base --is-ancestor v1.0.0 develop` é falso.
  gate rev1 → "FALHA: a branch não parte da v1.0.0" (mesma saída do usuário).
  gate rev2 → F5_L1_PRECONDITIONS_GREEN, F5_L1_BACKEND_GREEN (parser, Maven simulado), F5_L1_STATIC_GREEN;
              parou no Docker simulado, como esperado.
```
