# F1-L1 — CONSUMIDORES MAPEADOS

Data: 2026-09-22

## Busca antes da mudança de comportamento de `Responsible`

Comando executado sobre a baseline F0 GREEN:

```bash
rg -n --glob '!docs/**' '\bProject\b|\bResponsible\b|\bProjectScheduleCalculator\b|\bProjectStatus\b' backend/src/main backend/src/test
```

Resultado relevante para `Responsible`: havia apenas a própria declaração `domain/responsible/Responsible.java`; não existia consumidor de produção nem teste desse tipo na baseline.

Consumidores existentes de `Project`/motor de agenda identificados na baseline:

- `infrastructure/config/ApplicationBeans.java` para `ProjectScheduleCalculator`;
- `domain/project/ProjectTest.java`;
- `domain/project/ProjectScheduleCalculatorTest.java`.

Todos foram lidos integralmente antes do lote.

## Consumidores após F1-L1

`Responsible` passa a ser consumido por:

- `ResponsibleService`;
- `ResponsiblePersistenceAdapter`;
- `ResponsibleResponse`;
- `ResponsibleGraphQlController`;
- testes de domínio/aplicação e fakes.

`Project` passa a ser consumido por:

- `ProjectService`;
- `ProjectPersistenceAdapter`;
- `ProjectResponse`;
- `ProjectGraphQlController`;
- testes de aplicação e fake.

Os contratos REST e GraphQL de CRUD são novos neste lote; não havia consumidores anteriores a preservar.

## Correção pós-gate RED — consumidores de `ApplicationBeans`

Busca executada no candidato F1-L1:

```bash
grep -RIn --exclude-dir=target --exclude-dir=node_modules 'ApplicationBeans' backend/src
```

Resultado: além da própria declaração, somente os dois testes de health importavam `ApplicationBeans`. Ambos foram corrigidos para importar apenas `HealthQuery`; nenhum consumidor de produção foi alterado.
