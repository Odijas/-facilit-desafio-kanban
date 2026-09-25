# F5-L4 — RISCOS

Data: 2026-09-24

| Risco | Mitigação / gate |
|---|---|
| Incompatibilidade entre Cucumber e JUnit Platform | Materializou no primeiro gate: Cucumber 7.34.9 exigiu API ausente no JUnit Platform 1.12.2 gerenciado pelo Spring Boot 3.5.16. Correção: Cucumber 7.22.1, alinhado oficialmente à mesma plataforma; revalidar com `mvn clean verify` e 12 cenários passed |
| Regressão em consulta JPQL preexistente | O candidato havia colocado aspas duplas nos aliases de `summarizeByStatus()` sem necessidade. Correção mínima: restaurar os aliases simples do baseline GREEN e reexecutar o gate |
| Restubbing de exceções em mock Mockito invocar o stub anterior | Materializou no lote de cobertura: substituir `when(mock.method()).thenThrow(...)` após um stub que já lança executou a exceção durante a configuração. Correção mínima: `doThrow(...).when(mock).method()` nos quatro cenários de erro |
| Falso RED do gate ao reconhecer `jacoco:check` | Materializou após cobertura 95,61%: o `grep` usava formato de coordenadas que não corresponde à linha real do Maven. Correção: validar literalmente `--- jacoco:0.8.15:check (jacoco-check) @ kanban ---` e reexecutar o gate |
| JaCoCo sobrescrever o `javaagent` do Mockito | `jacocoArgLine` separado + gate rejeita aviso de autoanexo dinâmico do Mockito |
| Cobertura abaixo do mínimo | baseline real 91,05%; lacunas reais foram cobertas por testes até 95,61%; `jacoco:check` fixa mínimo de 95% sem exclusões artificiais |
| Projeto duplicado em secretaria por múltiplos responsáveis | SQL agrega sobre pares distintos projeto/secretaria + `ProjectIndicatorsIT` |
| Janela de prazo ambígua | contrato documentado: hoje até hoje + N, inclusivo; N=1..90 |
| Reabrir L1–L3 | lote não altera evidências F5-L1/F5-L2/F5-L3 nem migrations existentes |
