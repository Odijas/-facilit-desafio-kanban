# F4 — EXECUÇÃO

Data: 2026-09-23

## Base

```text
[EXECUTADO PELO USUÁRIO · 2026-09-23] F3-L4 GREEN (quatro etapas; pipeline 35894739171 com sucesso nos três jobs).
[VERIFICADO] base da F4 = develop 16ae837 (árvore da área de trabalho verificada no F3-L4) + este pacote.
```

## Executado neste ambiente

```text
[EXECUTADO · 2026-09-23] frontend com a versão 1.0.0 (Node 22.22.2, pnpm 12.5.1): install --frozen-lockfile, format, lint, typecheck, check:strict, test, build
  → STRICT_TYPES_GREEN 31; Test Files 6 passed (6); Tests 30 passed (30); 0 linhas com "deprecat" no log
[EXECUTADO · 2026-09-23] pnpm audit --prod → "No known vulnerabilities found"
[EXECUTADO · 2026-09-23] busca por "0.0.1" fora de docs/evidence e do lockfile → frontend/package.json, backend/pom.xml, backend/Dockerfile (e trechos não relacionados do README/compose, como 127.0.0.1)
[EXECUTADO · 2026-09-23] simulação do Gitflow completo num repositório Git temporário:
  reconstrução do histórico (script do F3-L4) → develop; release/1.0.0 + pacote da F4 → git status mostrou só os arquivos de CONSUMIDORES.md; 3 commits da release
  trechos do gate de freeze (versão, workflow com actionlint 1.7.12, repositório e histórico, Conventional Commits, documentação):
    versão 1.0.0 OK; WORKFLOW_POLICY_OK; WORKTREE_OK files=302; HISTORY_SECRETS_OK; Conventional Commits: 38 commits; docs OK; F4_STATIC_GREEN; exit 0
  normalização da saída (ANSI, \r de progresso, CRLF, espaços e linhas finais) → arquivo limpo
  passo 6 com dois repositórios bare como origin e gitlab; trechos REFS e REMOTES de VERIFICACAO-RELEASE.md → F4_RELEASE_REFS_GREEN, F4_RELEASE_REMOTES_GREEN (12 refs)
  controle negativo: tag leve no lugar da anotada → "FALHA: git fetch falhou (tag local diferente da publicada?)", exit 1
[EXECUTADO · 2026-09-23] `bash -n` em VERIFICACAO-USUARIO.md e VERIFICACAO-RELEASE.md extraídos por awk → sem erro.
```

## Correções próprias antes do empacotamento

```text
[VERIFICADO] a primeira versão da verificação da release exigia que a develop contivesse a main. No Gitflow, a develop recebe a release/1.0.0, não o merge da main; a simulação acusou ("develop não contém a main"). Corrigido para exigir a release/1.0.0 na develop.
[VERIFICADO] a primeira versão da normalização abria a saída em modo texto e convertia \r em quebra de linha, duplicando as linhas de progresso. Corrigido com newline='' e tratamento de CRLF; testado.
[VERIFICADO] a primeira versão do gate de freeze falhava com qualquer aviso de depreciação no log do Maven, inclusive os da própria ferramenta. Passou a falhar só com [deprecation] do compilador e a listar os demais como informativos.
[VERIFICADO] o nome temporário da saída foi mudado para /tmp/saida-gate-f4.txt, porque o gate apaga /tmp/f4-* na partida e apagaria o arquivo que o tee ainda estaria escrevendo.
[VERIFICADO] AUDITORIA.md dizia "16 transições" para ProjectStatusTransitionTest; o correto é 17 testes cobrindo as 12 transições da tabela e a rejeição do mesmo status.
```

## rev1 — RED no gate do usuário (2026-09-23)

```text
[EXECUTADO PELO USUÁRIO · 2026-09-23] release/1.0.0 com 3 commits (f49090e build, 2161f2c docs(F3-L4), b8d7017 docs(F4)), publicada no GitHub e no GitLab.
[EXECUTADO PELO USUÁRIO · 2026-09-23] gate de freeze rev1:
  F4_PRECONDITIONS_GREEN (HEAD b8d7017722680826f4f34d61fabab258a864dc09)
  F4_FRONTEND_GREEN: STRICT_TYPES_GREEN 31; Test Files 6 passed (6); pnpm audit --prod: No known vulnerabilities found
  F4_BACKEND_GREEN: unitários 13 classes / 64 testes; integração 3 classes / 13 testes; 0 falhas, 0 erros, 0 ignorados
    aviso informativo da ferramenta: "WARNING: A terminally deprecated method in sun.misc.Unsafe has been called" (1 linha, Maven no JDK 25)
  F4_STATIC_GREEN: WORKTREE_OK files=302; HISTORY_SECRETS_OK; 32 Conventional Commits desde 283ce5d
  F4_DOCKER_CLEAN_DB_GREEN: projeto facilit-kanban-f4; migrations 1 2 3 4 5
  F4_UI_SWAGGER_GREEN: OpenAPI com 13 caminhos
  F4_API_GREEN: ok 1 … ok 21
  F4_RESPONSIBLE_AUTH_GREEN: ok 22 … ok 34
  REVISÃO DE SEGURANÇA: "FALHA: PostgreSQL publicado no host"; exit code 1
[VERIFICADO] as checagens de segurança anteriores à falha passaram (set -e): flags do cookie, nosniff, X-Frame-Options DENY, CSRF, 404 sem detalhe interno, bcrypt, usuários de bootstrap, métricas 401, endpoints do Actuator, Prometheus e Grafana em 127.0.0.1.
[ERRO PRÓPRIO] a checagem usava `docker compose port db 5432` e esperava saída vazia para porta não publicada. O compose.yaml não publica a porta do banco, e o `docker compose ps` do F3-L3 mostrou `5432/tcp` sem mapeamento. Logo, o comando imprimiu algo mesmo sem porta publicada; `:0` é a hipótese, a saída exata não foi vista. Eu não tinha verificado esse comportamento.
[CORREÇÃO rev2] porta lida no contêiner: `docker inspect` de `HostConfig.PortBindings` e `NetworkSettings.Ports`; falha só se alguma porta tiver mapeamento para o host (porta exposta sem mapeamento aparece como `null`). O resumo do frontend passa a remover códigos de cor antes do grep (a linha "Tests 30 passed" não apareceu no rev1).
```

## rev2 — verificação local da correção

```text
[EXECUTADO · 2026-09-23] trecho novo da checagem de porta contra JSON de docker inspect:
  porta exposta sem mapeamento ({"5432/tcp": null}, bindings {}) → "banco sem porta no host", exit 0
  bindings null → exit 0
  porta publicada (HostPort 5432) → "FALHA: PostgreSQL publicado no host: {...}", exit 1
[EXECUTADO · 2026-09-23] filtro do resumo do frontend contra linha colorida do Vitest → "Tests  30 passed (30)" exibida.
[EXECUTADO · 2026-09-23] `bash -n` no gate extraído por awk → sem erro.
```

## Não executado neste ambiente

```text
[DESCONHECIDO] mvn clean verify com JDK 25, Compose real do freeze, CI da release e verificação da release. Resolve: RELEASE.md, passos 4 a 7.
```
