# F5-Q2 — CONSUMIDORES MAPEADOS

Data: 2026-09-25

`[VERIFICADO]` O F5-Q2 não altera assinatura pública, contrato de API nem comportamento de produção; portanto não há consumidores de código a migrar.

Referências documentais mapeadas:

- nomes de testes do README → `ProjectStatusTransitionTest.java` e `ProjectServiceTest.java`;
- pontos de recálculo do ADR 0002 → `ProjectService.get`, `ProjectService.search` e indicadores no código já validado no Q1;
- estado do Q1 → `docs/evidence/F5-Q1/{GATE,EXECUCAO,MATRIZ,RISCOS,SAIDA-GATE,FECHAMENTO}`;
- origem E1–E5/D1 → `docs/evidence/F5/ADERENCIA-V2.0.1.md`;
- navegação do README/AI_USAGE → arquivos relativos conferidos pelo gate documental.

`[EXECUTADO]` O gate F5-Q2 recusa referência de teste explícita em README/AI_USAGE cujo método não exista no arquivo de teste correspondente.
