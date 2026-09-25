# F5-Q2 — MATRIZ REQUISITO → DOCUMENTAÇÃO → VERIFICAÇÃO → EVIDÊNCIA

Data: 2026-09-25

| Item | Alteração documental | Verificação | Estado antes do gate do usuário |
|---|---|---|---|
| E1 | README com os quatro nomes reais `lineNN...` | gate resolve referências contra os testes | `[VERIFICADO]` localmente |
| E2 | linha do `PUT` cita `updateCanClearActualStartWithoutTransitionConfirmationAndRecalculatesStatus` | método existe e foi executado no gate Q1 | `[VERIFICADO]` + `[EXECUTADO]` Q1 GREEN |
| E3–E5 | CHANGELOG/AI_USAGE registram correções do Q1 | conteúdo cruzado com plano e aderência | `[VERIFICADO]` |
| D1 + coerência do ADR | ADR 0002 cita somente `get`, `search`, `indicators`; referências de teste antigas foram alinhadas aos métodos reais; CHANGELOG registra remoção | busca por pontos removidos e métodos citados no ADR | `[VERIFICADO]` |
| promoção Q1 | GATE/EXECUCAO/MATRIZ/RISCOS + `SAIDA-GATE.txt`/`FECHAMENTO.txt` | marcadores GREEN + hashes HEAD/ORIGIN | `[EXECUTADO]` pelo usuário |
| auditoria v2.0.1 | `docs/evidence/F5/ADERENCIA-V2.0.1.md` | SHA-256 igual ao original fornecido | `[VERIFICADO]` |
| pacote documental | nenhum código/teste/CI alterado | escopo exato no gate | `[EXECUTADO]` — gate Q2 GREEN |
| CI do Q1 | `hotfix/2.0.2` publicada | consulta GitHub Actions pelo SHA da hotfix | `[EXECUTADO]` — run 36143782068 GREEN |
