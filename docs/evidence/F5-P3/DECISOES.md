# F5-P3 — DECISÕES

Data: 2026-09-25

Escopo: lote F5-P3 de `docs/governance/PLANO-CORRECAO-RELEASE-2.0.1.md`, mais a promoção do F5-P2. O usuário pediu seguir depois do F5-P2 GREEN ("prossiga para P3 com o gitflow semântico e a finalização correta e coesa", 9h00).

- **Gitflow de hotfix:**
  - o pacote do F5-P3 é só documentação e é aplicado direto na `hotfix/2.0.1` (promoção do P2, gate e roteiro), como o preparo do L5 na `release/2.0.0`;
  - depois do freeze, merge `--no-ff` na `main`, tag anotada `v2.0.1` e back-merge `--no-ff` na `develop`.
- **Freeze = freeze da 2.0.0 adaptado**, e não um gate novo. Mudanças:
  - branch `hotfix/2.0.1`, versão 2.0.1 e projeto Compose `facilit-kanban-f5p3`;
  - contagens exatas de testes (174/51);
  - pré-condições dos lotes P e tag `v2.0.1` ainda inexistente;
  - CHANGELOG `[2.0.1]` no topo e ausência das frases desatualizadas;
  - regra de exemplos do OpenAPI e CSRF do Swagger UI;
  - busca literal no smoke da API.
  - A limpeza inicial preserva o próprio arquivo do gate, correção levada do F5-P1.
- **Nenhum lote se declara GREEN antes do próprio gate:**
  - a linha do F5-P3 no README e o estado GREEN do `GATE.md` entram no commit que registra a saída do freeze (passo 4 do `RELEASE.md`), por um script que recusa saída não GREEN;
  - a verificação pública e a auditoria final ficam na `develop`, depois da tag, como na 2.0.0.
- **Estado no `GATE.md` e na `MATRIZ.md`:** o `GATE.md` diz o estado de forma verificável (CANDIDATE até o passo 4, GREEN depois), e a matriz aponta para as evidências, sem coluna de estado que ficaria desatualizada dentro da tag.
- **Verificação da release:**
  - já nasce com a correção do falso negativo da 2.0.0: baixa para arquivo antes do `grep`, sem `curl | grep -q` nem `grep | grep -q` sob `pipefail`;
  - confere também que as refs publicadas são iguais às locais e que a tag publicada aponta para a `main`.
- **Conferência visual do Swagger UI (F5-P1, seção 3):**
  - não foi relatada pelo usuário até o preparo deste lote;
  - não é declarada como executada;
  - a prova automática do conteúdo (exemplos nas 26 operações e CSRF do Swagger UI) roda no gate do F5-P1 e de novo neste freeze.
