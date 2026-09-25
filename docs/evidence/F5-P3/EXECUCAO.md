# F5-P3 — EXECUÇÃO

Data: 2026-09-25

## Base

```text
[EXECUTADO PELO USUÁRIO · 2026-09-25 09:00] F5-P2 GREEN (exit code 0), docs/evidence/F5-P2/SAIDA-GATE.txt;
  CI da hotfix/2.0.1 com o F5-P1 na run 36131837374 (3 jobs e imagem Docker em sucesso).
[VERIFICADO] base do F5-P3 = árvore do F5-P2 (pacote SHA-256 4bd62f3b…ff619f47).
```

## Ensaio

```text
[EXECUTADO · 2026-09-25] repositório Git montado: main = v2.0.0 (tag anotada), develop com 2 commits de documentação
  depois da release, hotfix/2.0.1 com o F5-P1 e o F5-P2 mesclados de bugfix/*, remotos locais origin e gitlab.
  bash -n → freeze, verificação da release e blocos do roteiro com sintaxe OK.
  RELEASE passo 1 → sha256 OK, git status com README, 3 arquivos do F5-P2 e o diretório F5-P3, PREPARO_OK;
  passo 2 → 2 commits semânticos e push nos dois remotos, árvore limpa.
  Freeze, por trechos (Maven, pnpm, Docker e API simulados):
    pré-condições → versão 2.0.1, v2.0.0 ancestral, P1/P2 GREEN, sem tag v2.0.1 → F5_P3_PRECONDITIONS_GREEN;
    backend com relatórios simulados 25/174 e 12/51 → F5_P3_JACOCO_BDD_GREEN, F5_P3_BACKEND_GREEN;
    estática sobre a árvore real do pacote (workflow, 474 arquivos, segredos, Conventional Commits, README, AI_USAGE,
      CHANGELOG 2.0.1, ADR, auditorias, links, coleção; actionlint fora do ensaio) → F5_P3_STATIC_GREEN;
    UI + Swagger com /api-docs simulado (26 operações) → F5_P3_UI_SWAGGER_GREEN; negativo: item de responsibleIds
      sem exemplo → "ProjectRequest.responsibleIds[] sem exemplo".
  Passo 4 com saída GREEN simulada (com códigos de cor e \r) → REGISTRO_OK; README com a linha do F5-P3; GATE.md
    GREEN; SAIDA-GATE.txt sem códigos de cor; commit e push. Negativo: saída com exit code 1 → "PARE: a saída do freeze
    não é GREEN", nada alterado.
  Passo 5 → merge --no-ff na main, tag anotada v2.0.1, back-merge na develop sem conflito, push nos dois remotos.
  Passo 6 (verificação pública, API do GitHub e arquivos da tag simulados a partir do próprio repositório) →
    F5_P3_RELEASE_REFS_GREEN, F5_P3_RELEASE_GITHUB_GREEN, F5_P3_RELEASE_CI_GREEN, F5_P3_RELEASE_PAGE_GREEN,
    === F5-P3 RELEASE GREEN ===, exit code 0.
  Passo 7 → REGISTRO_OK, commit na develop e push.
[NÃO ENSAIADO] trechos do freeze iguais aos da 2.0.0 (frontend, Docker, smoke da API, autenticação do responsável,
  segurança, observabilidade e CI), que já rodaram GREEN na máquina do usuário em 2026-09-24; a linha nova do smoke
  (busca text=_) só teve a sintaxe conferida.
```

## Não executado neste ambiente

```text
[NÃO EXECUTADO] freeze real (pnpm, Maven, Docker, Prometheus, Grafana, actionlint), merge/tag/push reais, CI e API do
  GitHub. Resolve: passos 3 a 7 de RELEASE.md na máquina do usuário.
```
