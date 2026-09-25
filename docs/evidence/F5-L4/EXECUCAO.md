# F5-L4 — EXECUÇÃO

Data: 2026-09-24
Base: `f786e7ce058d16a1f7ad4b3968c09aa9e8bc67d7`

## Preparação

- `[EXECUTADO]` snapshot extraído: 371 arquivos.
- `[EXECUTADO]` leitura e mapeamento dos consumidores listados em `LEITURA.md` e `CONSUMIDORES.md`.
- `[EXECUTADO]` novos records de aplicação compilados com `javac -Xlint:all -Werror`: GREEN.
- `[EXECUTADO]` `backend/pom.xml` parseado como XML: GREEN.
- `[DESCONHECIDO]` `mvn clean verify`: não executado no ambiente de preparação porque Maven não está instalado.

## Estado desta candidata

Candidata de **medição controlada** do F5-L4. Ela implementa indicadores, BDD e geração de relatório JaCoCo.
O gate do usuário deve produzir `JACOCO_LINE_COVERAGE`. O mínimo de cobertura ainda não é fixado, por decisão
explícita do plano: medir primeiro, fixar depois.

Não declarar F5-L4 GREEN antes da segunda execução, já com o limite versionado.

## Primeira medição — falha de discovery e correção

- `[EXECUTADO]` `mvn clean verify` no ambiente do usuário: RED antes da execução dos testes.
- `[EXECUTADO]` causa raiz extraída de `target/surefire-reports/*.dump`:
  `NoClassDefFoundError: org/junit/platform/engine/support/discovery/DiscoveryIssueReporter`.
- `[VERIFICADO]` Spring Boot `3.5.16` gerencia JUnit Platform `1.12.2`.
- `[VERIFICADO]` changelog oficial do Cucumber: `7.22.1` usa JUnit Platform `1.12.2` / JUnit Jupiter `5.12.2`;
  `7.24.0` já migra para JUnit Platform `1.13.3`.
- `[DECISÃO]` alinhar Cucumber para `7.22.1` e não sobrescrever a versão de JUnit gerenciada pelo Spring Boot.
- `[DESCONHECIDO]` resultado após a correção: depende da reexecução do gate pelo usuário.

## Segunda medição — falha de bootstrap e correção JPQL

- `[EXECUTADO]` após alinhar Cucumber para `7.22.1`, o discovery avançou e a suíte passou a executar.
- `[EXECUTADO]` o `mvn clean verify` ainda ficou RED: a primeira criação de `ApplicationContext` falhou e os demais testes de integração foram interrompidos pelo `ApplicationContext failure threshold`.
- `[VERIFICADO]` o candidato F5-L4 havia alterado a consulta JPQL `summarizeByStatus()` de aliases simples (`projectCount`, `averageDelayDays`) para aliases com aspas duplas (`"projectCount"`, `"averageDelayDays"`).
- `[VERIFICADO]` essa mudança não era necessária para o F5-L4 e divergia da consulta que já estava GREEN no baseline `f786e7c`.
- `[CORREÇÃO]` restaurados exatamente os aliases JPQL do baseline em `ProjectJpaRepository.summarizeByStatus()`.
- `[DESCONHECIDO]` resultado após esta correção: depende da reexecução do gate pelo usuário.

## Terceira medição — falha no teste de cobertura e correção Mockito

- `[EXECUTADO]` `mvn clean verify` após a correção JPQL: RED em um único teste unitário novo, `ResponsibleGraphQlControllerTest.mapsPreviouslyUncoveredGraphQlErrors`.
- `[EXECUTADO]` falha observada: `ResourceNotFoundException` na linha em que o próprio teste tentava substituir o stub anterior com outro `when(service.get(...)).thenThrow(...)`.
- `[VERIFICADO]` o stub anterior já fazia `service.get(...)` lançar `ResourceNotFoundException`; a nova chamada `when(service.get(...))` invocava esse comportamento durante a própria reconfiguração do mock.
- `[CORREÇÃO]` os quatro restubbings de exceção usam `doThrow(...).when(service).get(...)`, que não invoca o método durante a configuração e permite substituir o comportamento entre os cenários.
- `[DESCONHECIDO]` resultado após esta correção: depende da reexecução real de `mvn clean verify` e da nova medição JaCoCo.


## Quarta medição — cobertura acima do requisito

- `[EXECUTADO]` baseline JaCoCo após a implementação funcional/BDD: `1597` linhas cobertas, `157` perdidas, `91,05%`.
- `[DECISÃO]` mínimo de fechamento definido em `95%` de linhas.
- `[VERIFICADO]` `jacoco.xml` mostrou lacunas concentradas em testes GraphQL e de persistência; nenhuma exclusão de produção foi adotada.
- `[CORREÇÃO]` adicionados somente testes direcionados às lacunas; uma falha de restubbing Mockito foi corrigida com `doThrow(...).when(...)`.
- `[EXECUTADO]` nova medição informada pelo usuário: `1677` linhas cobertas, `77` perdidas, `95,61%` (`0.9561`).
- `[DECISÃO]` `jacoco:check` passa a exigir `LINE/COVEREDRATIO >= 0.95` no `verify`.
- `[DESCONHECIDO]` fechamento final permanece condicionado à reexecução do gate já com o `jacoco:check` versionável e à persistência de `SAIDA-GATE-FINAL.txt`.

## Quinta execução — falso RED no verificador do JaCoCo

- `[EXECUTADO]` gate final: `mvn -B -ntp clean verify` concluiu os testes e gerou cobertura `95,61%`, mas o script encerrou com `FALHA: jacoco:check não executou`.
- `[VERIFICADO]` o verificador procurava a string `jacoco-maven-plugin:0.8.15:check`, incompatível com o formato real emitido pelo Maven no mesmo projeto: `--- jacoco:0.8.15:check (jacoco-check) @ kanban ---`.
- `[CORREÇÃO]` o gate passa a procurar literalmente o marcador real `--- jacoco:0.8.15:check (jacoco-check) @ kanban ---`; produção, testes e configuração JaCoCo permanecem inalterados.
- `[DESCONHECIDO]` fechamento final permanece condicionado à reexecução do gate corrigido e a `GATE_EXIT=0`.
