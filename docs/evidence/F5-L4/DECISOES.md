# F5-L4 — DECISÕES

Data: 2026-09-24

## Indicadores adicionais

- `by-secretariat`: conta cada projeto uma única vez por secretaria, mesmo quando o projeto possui dois responsáveis
  da mesma secretaria. A média de atraso usa os projetos distintos dessa secretaria.
- `by-responsible`: um projeto conta uma vez para cada responsável associado.
- `deadlines`: considera projetos não concluídos com `plannedEnd` entre hoje e `hoje + withinDays`, inclusive.
  `withinDays` aceita 1–90; fora da faixa é 400 REST / `INVALID_REQUEST` GraphQL.
- Grupos sem projeto não aparecem; o endpoint é um resumo dos grupos efetivamente associados a projetos.

## Persistência

- Nenhuma migration nova: os índices necessários já existem em `planned_end`, `responsible_id` e `secretariat_id`.
- Secretaria usa SQL nativo com subconsulta `distinct (secretariat_id, project_id)` para impedir dupla contagem.
- Prazo usa projeção JPA leve; não carrega o grafo completo de responsáveis.

## BDD

- O primeiro gate com Cucumber-JVM `7.34.9` falhou no discovery com
  `NoClassDefFoundError: org/junit/platform/engine/support/discovery/DiscoveryIssueReporter`.
- Spring Boot `3.5.16` gerencia JUnit Platform `1.12.2`; o changelog oficial do Cucumber registra que a linha
  `7.22.1` usa exatamente JUnit Platform `1.12.2` / JUnit Jupiter `5.12.2`.
- A correção mínima é alinhar Cucumber-JVM para `7.22.1`, preservando o BOM/stack de testes gerenciado pelo
  Spring Boot em vez de sobrescrever JUnit Platform isoladamente.
- Integração por JUnit Platform Engine, com feature em pt-BR e 12 exemplos, um para cada linha da tabela do desafio.
- O JSON do Cucumber é gerado em `target/cucumber.json`; o gate exige 12 cenários e todos os passos `passed`.
- A compatibilidade efetiva com Java 25 continua sendo comprovada somente pelo `mvn clean verify` real.

## Cobertura

- JaCoCo `0.8.15`, release oficial que suporta Java 26 e portanto cobre Java 25.
- A primeira medição real foi `1597/1754` linhas = `91,05%`; ela serviu apenas como baseline, conforme o plano.
- O requisito de fechamento foi definido em `95%` de cobertura global de linhas. Não há exclusão de classes nem redução de código para inflar o indicador.
- A análise do `jacoco.xml` direcionou testes exclusivamente para lacunas reais de GraphQL e persistência. A nova medição real foi `1677/1754` = `95,61%`.
- O `jacoco:check` fica vinculado ao `verify` com `LINE/COVEREDRATIO >= 0.95`; como o CI já executa `mvn clean verify`, o mesmo limite passa a ser bloqueante no pipeline.
