# F5-C3 — DECISÕES

Data: 2026-09-24

Escopo: `docs/governance/PLANO-CORRECAO-RELEASE-2.0.0.md`, lote F5-C3 (documentação de entrega), mais a promoção do F5-C2. O usuário pediu seguir depois do F5-C2 GREEN ("prossiga F5-C3, incluindo a rotina de gitflow semântico", 2026-09-24 22h14).

## Coleção de API (lacuna 6)

- 4 requisições novas na pasta "04 — Kanban, filtros e indicadores", depois de "Indicadores". Cada uma confere o dado do próprio cenário, não só o status HTTP:
  - `by-secretariat`: a secretaria do cenário com 1 projeto;
  - `by-responsible`: o responsável do cenário com 1 projeto;
  - `deadlines?withinDays=10`: o projeto do cenário vence em 10 dias (o fim previsto do cenário é hoje + 10, e a janela é inclusiva: `between :from and :to`);
  - `deadlines?withinDays=0` → 400 `INVALID_REQUEST`.
- `[VERIFICADO: ProjectIndicatorsRestController, ProjectService.deadlines, ProjectJpaRepository.findDeadlines]` formatos de resposta e a regra 1–90.
- A coleção passa de 23 para 27 requisições. O JSON foi regravado com a mesma indentação, e o diff só tem inserções.
- **Prova:** o gate roda a coleção inteira com o newman 6.2.2 contra a aplicação real, com banco novo. `[VERIFICADO: registry.npmjs.org]` 6.2.2 é a versão mais recente, não depreciada, com Node ≥ 16.

## AI_USAGE (lacuna 7)

- Honesto e verificável:
  - ferramentas: Claude, com o que ele conduziu na F5 (auditorias, planos, F5-L1 a F5-L3 e F5-C1 a F5-C3);
  - os planos F5 como specs derivadas de auditoria;
  - erros da IA pegos pelo processo: status congelado da v1.0.0, gate do F5-L1 rev1, imagem Docker da release candidata, Swagger sem CSRF;
  - trecho representativo da spec de correção.
- Não afirma autoria de lotes feitos fora desta condução (F5-L4 e o preparo do F5-L5).
- As seções exigidas pelo freeze continuam com os mesmos títulos.

## README (lacuna 8 e inconsistências)

- **Texto de trabalho removido:** "o F5-L5 prepara a release". Entraram duas limitações reais: token CSRF à mão no GraphiQL e limite de tamanho só na borda da API.
- **Mantido de propósito:** `F5-L5 — Release \`v2.0.0\``. O gate `VERIFICACAO-RELEASE.md` procura esse texto no README da tag.
- **GraphQL:** deixa de dizer que "espelha" o REST. `[VERIFICADO: kanban.graphqls]` as páginas GraphQL não têm `totalElements`.
- **Estrutura de pastas:** cita o ADR 0002.
- **Governança:** o plano de correção e a auditoria independente entram na lista, e o histórico reconstruído de F2-L1 a F3-L4 fica declarado (lacuna 9: não reescrever o histórico).

## CHANGELOG e auditoria

- **`[2.0.0]`:** registra os limites de entrada, as métricas no BDD, a imagem no CI e a coleção. O CSRF do Swagger entra como `Fixed`, porque já faltava na v1.0.0. A imagem quebrada **não** entra como `Fixed`: o defeito nunca chegou a uma release publicada.
- **`docs/evidence/F5/ADERENCIA-FINAL.md`:** a auditoria independente de 24/09 fica versionada (decisão do usuário), com uma nota de estado no topo. O corpo não muda: é o registro do que foi encontrado.
- **`AUDITORIA.md`:** incorpora os lotes C, mantendo os títulos exigidos pelo freeze.
- **`GATE.md` e `RELEASE.md` do F5:** a condição "a release contém somente o L5" passa a aceitar os lotes C, listados com suas branches.
