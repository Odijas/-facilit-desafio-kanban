# F5-L3 — GATE

Data: 2026-09-24

Estado: **GREEN — FECHADO**, após execução local do gate pelo usuário em 2026-09-24.

Escopo: `docs/governance/PLANO-CONFORMIDADE-F5.md`, lote F5-L3 (camadas de teste completas e transação por caso de uso), sobre a base F5-L2 já encerrada e promovida.

Critérios:

- `F5_L3_PRECONDITIONS_GREEN`:
  - branch `feature/f5-l3-*` que já contém o F5-L2 commitado;
  - arquivos alterados iguais à lista do pacote;
  - frontend intocado.
- `F5_L3_BACKEND_GREEN`:
  - `mvn clean verify` sem falha e sem `[deprecation]`;
  - Mockito carregado como agente (nenhum aviso de autoanexo do Mockito nem de agente carregado dinamicamente);
  - unitários: 23 classes e 140 testes; integração: 9 classes e 45 testes; nenhuma falha, erro ou teste ignorado;
  - classes do lote com a contagem exata: `ProjectRestControllerTest` (17), `ResponsibleRestControllerTest` (9), `SecretariatRestControllerTest` (6), `ProjectIndicatorsRestControllerTest` (2), `ProjectGraphQlControllerTest` (5), `ResponsibleGraphQlControllerTest` (3), `SecretariatGraphQlControllerTest` (2), `ProjectServiceMockitoTest` (5), `ProjectPersistenceAdapterIT` (5), `TransactionIT` (4), `StatusTransitionApiIT` (16); e as do F5-L2 sem regressão: `ProjectServiceTest` (9), `ProjectStatusTransitionTest` (24).
- `F5_L3_STATIC_GREEN`:
  - espaços e linha final nos arquivos do lote;
  - `git diff --check`;
  - seções do README;
  - F5-L2 promovido a GREEN;
  - agente do Mockito no Surefire e no Failsafe; V7 e `@Version`;
  - nenhum `@MockBean` (depreciado).
- `F5_L3_DOCKER_GREEN`: projeto Compose próprio (`facilit-kanban-f5l3`) com banco novo; migrations 1 a 7; `projects.version` `bigint NOT NULL DEFAULT 0`.
- `F5_L3_API_GREEN`:
  - versão 0 ao criar, +1 a cada gravação (edição e transições);
  - recusas (bloqueio e falta de confirmação) não gravam: a versão não muda;
  - linhas 1, 4, 5, 6, 11 e 12 da tabela pela API, com confirmação nas linhas 4 e 11.
- Marcador final `=== F5-L3 GREEN ===` e `Resultado: exit code 0`.

## Resultado executado pelo usuário

```text
[EXECUTADO PELO USUÁRIO · 2026-09-24] F5_L3_PRECONDITIONS_GREEN — 40 arquivos do lote; frontend intocado; branch feature/f5-l3-camadas-de-teste sobre F5-L2 commitado.
[EXECUTADO PELO USUÁRIO · 2026-09-24] F5_L3_BACKEND_GREEN — 23 classes/140 testes unitários + 9 classes/45 testes de integração; 0 falhas, 0 erros, 0 ignorados; Mockito como agente.
[EXECUTADO PELO USUÁRIO · 2026-09-24] F5_L3_STATIC_GREEN — README, F5-L2 GREEN, pom, V7 e @Version validados; sem @MockBean.
[EXECUTADO PELO USUÁRIO · 2026-09-24] F5_L3_DOCKER_GREEN — banco novo; migrations V1–V7; projects.version bigint NOT NULL DEFAULT 0.
[EXECUTADO PELO USUÁRIO · 2026-09-24] F5_L3_API_GREEN — 18 cenários de versão, recusas sem gravação, transições e exclusões.
[EXECUTADO PELO USUÁRIO · 2026-09-24] === F5-L3 GREEN ===; Resultado: exit code 0.
```
