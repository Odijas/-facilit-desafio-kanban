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

## Não executado neste ambiente

```text
[DESCONHECIDO] mvn clean verify com JDK 25, Compose real do freeze, CI da release e verificação da release. Resolve: RELEASE.md, passos 4 a 7.
```
