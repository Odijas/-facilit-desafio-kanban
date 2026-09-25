# F5-P2 — EXECUÇÃO

Data: 2026-09-25

## Base

```text
[EXECUTADO PELO USUÁRIO · 2026-09-25 08:44] F5-P1 rev2 GREEN (exit code 0), docs/evidence/F5-P1/SAIDA-GATE.txt.
[VERIFICADO] base do F5-P2 = árvore do F5-P1 rev2 (pacote SHA-256 aa61b22d…fb78e448).
```

## RED (o que o lote corrige)

```text
[VERIFICADO: tag v2.0.0] README:314 "a tag só é criada após o freeze GREEN"; README:309 F4 sem estado; README:213
  "em todas as listagens"; README:286 "auditoria independente"; AI_USAGE:82 "o desafio pede GitHub Actions";
  CHANGELOG:39 "CI/CD"; docs/evidence/F5/GATE.md "PENDENTE DE EXECUÇÃO"; MATRIZ.md "PENDENTE L5" (6 linhas);
  AUDITORIA.md "continua pendente" e "ainda não foram executados".
```

## Ensaio do gate

```text
[EXECUTADO · 2026-09-25] repositório Git montado com a v2.0.0 (tag anotada), develop com 2 commits de documentação
  depois da release (como a real), remotos locais; F5-P1 rev2 aplicado e commitado pelas seções 0/1/4 do F5-P1 e
  seção 0 deste lote; API do GitHub simulada:
  seção 1 → sha256 OK, PREPARO_OK · bash -n → sintaxe OK
  F5_P2_PRECONDITIONS_GREEN (19 arquivos, só documentação)
  F5_P2_DOCS_GREEN (38 links e caminhos resolvidos; testes citados existem)
  F5_P2_HISTORY_GREEN (4 commits do F5-P1 em Conventional Commits)
  aviso de sobreposição: develop com SAIDA-RELEASE.txt e VERIFICACAO-RELEASE.md; sem sobreposição com a hotfix
  F5_P1_CI_GREEN · === F5-P2 GREEN === · Resultado: exit code 0
  seção 3 → 5 commits semânticos + merge --no-ff na hotfix/2.0.1 + push nos dois remotos; árvore limpa.
[EXECUTADO · casos negativos, cada um com exit code 1]
  frase antiga devolvida ao README → README.md ainda com "em todas as listagens", e a frase nova ausente;
  link para arquivo inexistente → "link quebrado em README.md";
  backend/pom.xml alterado → "alteração fora do pacote: backend/pom.xml".
[CORRIGIDO NO ENSAIO] a primeira versão do gate (1) acusava a frase antiga citada de propósito na seção de erros do
  AI_USAGE, e passou a procurar a frase no contexto original ("**Rejeitada:** o desafio pede GitHub Actions"); (2) exigia
  existência de docs/api/openapi.json, que o relatório de aderência cita como correção proposta, e passou a conferir
  só os links dos relatórios ADERENCIA-*.
```

## Não executado neste ambiente

```text
[NÃO EXECUTADO] CI real da hotfix/2.0.1 e API do GitHub. Resolve: gate local (seção 2).
```
