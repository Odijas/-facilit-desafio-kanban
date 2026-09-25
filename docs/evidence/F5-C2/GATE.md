# F5-C2 — GATE

Data: 2026-09-24

Estado: **CANDIDATE**, aguardando o gate local.

Escopo: `docs/governance/PLANO-CORRECAO-RELEASE-2.0.0.md`, lote F5-C2 (rigor de testes e validação), mais a promoção do F5-C1, na `release/2.0.0`.

Critérios:

- `F5_C2_PRECONDITIONS_GREEN`:
  - branch `bugfix/2.0.0-*` cuja base tem o F5-C1 commitado (Dockerfile com `jacoco.skip` e evidências do F5-C1);
  - arquivos alterados iguais à lista do pacote;
  - frontend intocado.
- `F5_C2_BACKEND_GREEN`:
  - `mvn clean verify` sem falha, sem `[deprecation]` e sem autoanexo do Mockito;
  - unitários/BDD: 25 relatórios e 174 testes; integração: 12 relatórios e 49 testes; sem falha, erro ou teste ignorado;
  - contagem exata nas classes do lote;
  - relatório Cucumber com 12 cenários, 48 passos aprovados e o passo de métricas em todos;
  - JaCoCo ≥ 95%.
- `F5_C2_STATIC_GREEN`:
  - espaços e linha final nos arquivos do lote e `git diff --check`;
  - colunas de métricas no `.feature`;
  - limites no código;
  - README com os limites;
  - F5-C1 promovido a GREEN;
  - seções exigidas pelo freeze.
- `F5_C2_DOCKER_GREEN`: `up --build` com banco novo (imagem do backend com o código do lote), migrations 1 a 7.
- `F5_C2_API_GREEN`:
  - fronteiras aceitas: nome com 200 caracteres e e-mail com 254;
  - acima do limite → 400 `VALIDATION_ERROR` com a mensagem em pt-BR: nome com 201, e-mail com 255, 51 responsáveis;
  - texto com 101 caracteres → 400 `INVALID_REQUEST`;
  - GraphQL acima do limite → `VALIDATION_ERROR`;
  - `/api-docs` com `maxLength` e `maxItems`.
- `F5_C1_CI_GREEN`: CI da `release/2.0.0` com o F5-C1 (3 jobs em sucesso e o passo `Imagem Docker do backend`).
- Marcador final `=== F5-C2 GREEN ===` e `Resultado: exit code 0`.
