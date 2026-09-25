# F5-L1 — GATE

Data: 2026-09-24

Estado: **GREEN** (rev2, gate local em 2026-09-24).

Histórico:

- **rev1 RED (2026-09-24):** a pré-condição exigia a tag `v1.0.0` como ancestral da branch. Em Gitflow, a tag fica no merge da release na `main`, e a `develop` recebe a branch release (2º pai desse merge), não a `main`.
- **rev2:** aceita a tag ou o 2º pai do merge da tag, e confere que o conteúdo é o mesmo da tag. O passo 1 da verificação passou a ser repetível e a parar no primeiro erro.

Escopo: `docs/governance/PLANO-CONFORMIDADE-F5.md`, lote F5-L1 (regras sempre corretas).

Critérios:

- `F5_L1_PRECONDITIONS_GREEN`:
  - branch `feature/f5-l1-*` que contém a release `v1.0.0` (a tag ou a branch release mesclada, com o mesmo conteúdo da tag);
  - pacote aplicado;
  - arquivos alterados iguais à lista do pacote;
  - frontend intocado.
- `F5_L1_BACKEND_GREEN`:
  - `mvn clean verify` sem falha e sem `[deprecation]` no código;
  - relatórios com `ProjectScheduleRefresherTest` (8), `ProjectDatesTest` (4), `ProjectStatusTransitionTest` (19), `ScheduleFreshnessIT` (3) e `ScheduleCalculationDateMigrationIT` (1);
  - todas as classes sem falha, erro ou teste ignorado.
- `F5_L1_STATIC_GREEN`:
  - espaços e linha final nos arquivos do lote;
  - `git diff --check`;
  - YAML e JSON válidos;
  - seções do README e do ADR 0002;
  - links relativos;
  - variável `today` da coleção em UTC−3.
- `F5_L1_DOCKER_CLEAN_DB_GREEN`:
  - projeto Compose próprio (`facilit-kanban-f5l1`) com banco novo;
  - migrations `1 2 3 4 5 6`;
  - coluna `schedule_calculated_on NOT NULL`;
  - log `gatilho=startup`.
- `F5_L1_API_GREEN`:
  - projeto envelhecido 10 dias no banco vira OVERDUE com 8 dias de atraso e 0% no GET, no GraphQL, na listagem por status e nos indicadores;
  - `updated_at` intacto;
  - Em andamento desatualizado → Atrasado recusado;
  - `actualStart` = data de São Paulo;
  - início realizado futuro → 400.
- Marcador final `=== F5-L1 GREEN ===` e `Resultado: exit code 0`.

Evidência final recebida do usuário:

```text
[EXECUTADO PELO USUÁRIO · 2026-09-24 09:07] gate F5-L1 rev2 (pacote SHA-256 ef481a2a…4d65 conferido: SUCESSO):
  42 arquivos alterados, todos do pacote F5-L1; frontend intocado
  branch feature/f5-l1-regras-sempre-corretas contém a release v1.0.0 (eef7a4a, mesmo conteúdo da tag)
  F5_L1_PRECONDITIONS_GREEN
  unitários: 15 classes, 79 testes, 0 falhas, 0 erros, 0 ignorados
  integração: 5 classes, 17 testes, 0 falhas, 0 erros, 0 ignorados
  ProjectScheduleRefresherTest 8 · ProjectDatesTest 4 · ProjectStatusTransitionTest 19 · ScheduleFreshnessIT 3 · ScheduleCalculationDateMigrationIT 1
  F5_L1_BACKEND_GREEN
  F5_L1_STATIC_GREEN
  banco novo (facilit-kanban-f5l1); migrations V1 2 3 4 5 6; schedule_calculated_on NOT NULL; recálculo na subida registrado
  F5_L1_DOCKER_CLEAN_DB_GREEN
  hoje em America/Sao_Paulo: 2026-09-24 · 24 verificações da API ok (GET após 10 dias: OVERDUE 8 0; updated_at intacto;
  Em andamento desatualizado → Atrasado recusado; actualStart = data de São Paulo; início realizado futuro → 400)
  F5_L1_API_GREEN
  === F5-L1 GREEN ===
  Resultado: exit code 0
[INFORMATIVO] 1 linha com "deprecat" no log do Maven: "WARNING: A terminally deprecated method in sun.misc.Unsafe has been
  called". [HIPÓTESE] vem de uma ferramenta do build (Maven ou plugin) no JDK 25, não do código do projeto: o gate checa
  que não há aviso [deprecation] na compilação (-Xlint:all -Werror).
```

Conclusão: F5-L1 promovido para GREEN em 2026-09-24.
