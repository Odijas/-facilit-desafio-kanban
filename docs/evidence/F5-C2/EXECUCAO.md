# F5-C2 — EXECUÇÃO

Data: 2026-09-24

## Base

```text
[EXECUTADO PELO USUÁRIO · 2026-09-24 21:59] F5-C1 GREEN (exit code 0), docs/evidence/F5-C1/SAIDA-GATE.txt:
  RED do Dockerfile anterior reproduzido (cobertura 59% só com unitários); imagem nova, banco novo e Swagger com CSRF OK.
[VERIFICADO] base do F5-C2 = árvore do F5-C1 (pacote SHA-256 f247ca99…0533d9f226) + promoção documental do F5-C1.
```

## RED (o que o lote corrige)

```text
[VERIFICADO: transicoes.feature e StatusTransitionSteps] o BDD conferia só o status resultante de cada linha; atraso e
  percentual restante não eram conferidos por linha (o PDF pede "cálculo de status/métricas — linha a linha").
[VERIFICADO: grep "@Size|@Max" em backend/src/main/java → vazio] nome, cargo, e-mail, texto de busca e lista de responsáveis
  sem limite de tamanho; colunas TEXT.
```

## GREEN neste ambiente (limitado)

```text
[EXECUTADO · 2026-09-24] javac 21 -Xlint:all -Werror sobre domain + application + delivery/common/InputLimits → exit 0.
[EXECUTADO · 2026-09-24] harness com as classes reais do domínio: atraso e percentual de cada linha de sucesso
  (01: 0/100 · 03: 0/0 · 04: 0/100 · 06: 0/0 · 09: 0/0 · 11: 0/80 · 12: 1/0) e origem dos cenários ajustados
  (overdue_not_started: Atrasado com 1 dia; completed_future: Concluído); linhas 02, 05, 07, 08 e 10 bloqueadas.
[EXECUTADO · 2026-09-24] harness do ProjectFilter: texto de 100 (depois de aparado) aceito; 101 → mensagem esperada;
  texto em branco → null.
[EXECUTADO · 2026-09-24] javac sobre os testes e controllers alterados: só erros de símbolos de bibliotecas ausentes aqui
  (JUnit, AssertJ, Mockito, Spring, Cucumber); nenhum erro de sintaxe nem de símbolo do projeto.
```

## Ensaio do gate

```text
[EXECUTADO · 2026-09-24] repositório Git montado com a release/2.0.0 já com o F5-C1 commitado, remoto local e a branch
  bugfix/2.0.0-c2-testes-validacao + os 28 arquivos do pacote; Maven, Docker e API do GitHub simulados; API do Kanban
  simulada por um servidor local com CSRF, validações de tamanho e /api-docs:
  bash -n → sintaxe OK
  F5_C2_PRECONDITIONS_GREEN · F5_C2_BACKEND_GREEN (parser de relatórios, cucumber.json e jacoco.xml)
  F5_C2_STATIC_GREEN · F5_C2_DOCKER_GREEN · F5_C2_API_GREEN (14 verificações; os valores de fronteira passam pelo shell
  sem perder as aspas: 51 UUIDs, e-mails de 254/255) · F5_C1_CI_GREEN
  === F5-C2 GREEN === · Resultado: exit code 0
```

## Não executado neste ambiente

```text
[NÃO EXECUTADO] mvn clean verify, Cucumber, imagem, API real e CI (Maven Central 403, sem JDK 25 e sem Docker aqui).
  Resolve: gate local (VERIFICACAO-USUARIO.md, seção 2).
```
