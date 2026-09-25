# F5-Q2 — RISCOS E LACUNAS

Data: 2026-09-25

1. `[DESCONHECIDO]` O `tar.gz` não contém `.git`; branch, diff aplicado e histórico só podem ser provados no gate do usuário.
2. `[DESCONHECIDO]` O status do GitHub Actions da `hotfix/2.0.2` não é inferido pelo pacote; o gate consulta a execução correspondente ao SHA publicado.
3. `[VERIFICADO]` A auditoria `ADERENCIA-V2.0.1.md` descreve o estado histórico da tag 2.0.1 e mantém suas ressalvas; ela não deve ser “atualizada” para aparentar auditoria da 2.0.2.
4. `[VERIFICADO]` A entrada `[2.0.2]` no CHANGELOG é preparação documental; a release/tag ainda não existe e pertence ao F5-Q3.
5. `[VERIFICADO]` Este lote não executa Maven/Docker porque não muda código; regressão de código do Q1 foi provada no gate Q1 e será novamente coberta pelo freeze Q3.
6. `[VERIFICADO]` A releitura integral do ADR 0002 revelou referências de teste antigas que não correspondiam a métodos atuais; foram corrigidas no mesmo arquivo já previsto pelo Q2 e registradas em `DECISOES.md`.
