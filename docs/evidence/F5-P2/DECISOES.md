# F5-P2 — DECISÕES

Data: 2026-09-25

Escopo: lote F5-P2 de `docs/governance/PLANO-CORRECAO-RELEASE-2.0.1.md`, mais a promoção do F5-P1. O usuário pediu seguir depois do F5-P1 GREEN ("prossiga para P2", 8h44).

- **Estado das releases no README:**
  - F4 GREEN (conforme `docs/evidence/F4/GATE.md`);
  - F5-L5 GREEN com o freeze e a verificação pública;
  - linha do F5-P1 GREEN.
  - A linha do F5-P2 entra na promoção do F5-P3, como nos lotes anteriores: nenhum lote se declara GREEN antes do próprio gate.
- **Paginação:** o README passa a dizer onde há paginação (projetos, responsáveis e secretarias). Os indicadores agrupados e os prazos são agregados e não paginam. O item "paginação em listagens" do PDF continua atendido: as listagens de registros paginam.
- **Interpretações novas** (tabela "Interpretações do enunciado"):
  - **`PUT` × tabela:** a tabela e as confirmações valem para a mudança de status pedida. O `PUT` edita dados, e o status é recalculado das datas. É o comportamento da `v2.0.0`, agora declarado.
  - **Término previsto vazio nas linhas 1, 11 e 12:** 422 `BUSINESS_RULE_VIOLATION`, porque falta um dado para classificar o projeto, e não é a tabela que recusa. Decisão do F5-L2, agora declarada.
  - Os testes citados são conferidos no gate.
- **Diferenciais com texto errado:**
  - `AI_USAGE`: o GitHub Actions é citado como diferencial de CI/CD, não "pedido";
  - `CHANGELOG` 1.0.0: "CI/CD" vira "CI", porque não há deploy.
  - A entrada da 1.0.0 é corrigida no lugar, com registro na `[2.0.1]`, porque o texto era factualmente errado.
- **Auditoria:**
  - "auditoria independente" vira "análise própria confrontada com um agente independente";
  - `ADERENCIA-V2.0.0.md` versionado (decisão 2 do usuário). Única alteração em relação ao relatório entregue: a referência a um arquivo de trabalho local (`desafio.txt:200`) virou "seção de diferenciais" do PDF.
- **Evidências do F5-L5:**
  - `GATE.md`, `MATRIZ.md` e `AUDITORIA.md` diziam "pendente" dentro da tag;
  - passam a registrar o freeze e a release GREEN, com os SHAs e as runs de CI da execução do usuário;
  - a correção do script de verificação e a `SAIDA-RELEASE.txt` estão na `develop` (depois da tag) e chegam à `hotfix` no back-merge do F5-P3.
- **AI_USAGE:**
  - a terceira auditoria e a patch 2.0.1 entram na tabela de ferramentas e nas specs;
  - dois erros novos da IA entram na seção 5: documentação desatualizada na tag e o exemplo de lista do F5-P1 rev1.
- **Conferência do Swagger no navegador (F5-P1, seção 3):** pedida antes dos commits do F5-P1 e registrada no F5-P3, para não declarar aqui algo ainda não executado.
