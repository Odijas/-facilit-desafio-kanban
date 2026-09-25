# F5-Q3 — DECISÕES

Data: 2026-09-25

Escopo: F5-Q3 de `docs/governance/PLANO-CORRECAO-RELEASE-2.0.2.md`, após F5-Q2 GREEN e fechamento Gitflow comprovado.

- **Sem código de produção:** o Q3 altera somente documentação/evidências. Código, testes, migrations, CI e contratos permanecem os mesmos do Q1/Q2.
- **Promoção do Q2:** `GATE.md`, `EXECUCAO.md`, `MATRIZ.md` e `RISCOS.md` passam a refletir a execução real; entram também `SAIDA-GATE.txt` e `FECHAMENTO.txt`.
- **Freeze baseado no F5-P3:** reutiliza o padrão já validado na release 2.0.1 e muda somente o necessário: branch/tag/versão 2.0.2, lotes Q1/Q2, contagens 177/52 e verificações E1/E3.
- **E1 no freeze:** todas as referências de métodos de teste em `README.md` e `AI_USAGE.md` são resolvidas contra os arquivos de teste reais; as cinco referências corrigidas pelo Q2 são exigidas explicitamente.
- **E3 no freeze:** além de `OpenApiContractIT` rodar no `mvn clean verify`, o `/api-docs` do Docker é validado por operação para campo do 400, recurso do 404, `withinDays` em `/deadlines` e 403 sem texto de projeto em rotas de Responsável/Secretaria.
- **Nenhum GREEN antecipado:** F5-Q3 permanece CANDIDATE até o freeze real terminar com todos os marcadores e exit code 0. A linha do Q3 no README e o estado GREEN do gate só são gravados depois do freeze.
- **Irreversíveis com o usuário:** merge em `main`, tag `v2.0.2`, back-merge e pushes são executados pelo usuário, conforme a regra do plano.
- **Auditoria final:** somente depois da tag; o roteiro gera `git archive v2.0.2`. O relatório `ADERENCIA-V2.0.2.md` não é criado antes dessa auditoria.
