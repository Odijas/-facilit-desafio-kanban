# F0-L2 — EXECUÇÃO

Data: 2026-09-21.

## TDD interno — RED

[EXECUTADO: compilação de probe temporário antes da implementação · 2026-09-21] O probe referenciando `Project`, `Responsible`, `Secretariat`, `ProjectDates`, `ProjectStatus` e `AuditMetadata` falhou porque os pacotes ainda não existiam.

```text
RED_STATUS=1
error: package br.com.facilit.kanban.domain.common does not exist
error: package br.com.facilit.kanban.domain.project does not exist
error: package br.com.facilit.kanban.domain.responsible does not exist
error: package br.com.facilit.kanban.domain.secretariat does not exist
```

## TDD interno — GREEN disponível neste runtime

[EXECUTADO: `javac -Xlint:all -Werror ... && java F0L2DomainProbe` · 2026-09-21] O domínio puro compilou sem warnings no Java disponível neste runtime e o probe legítimo + ataques passou.

```text
F0_L2_DOMAIN_PROBE_GREEN
```

[VERIFICADO: probe temporário · 2026-09-21] Foram exercitados: criação válida; relacionamento Projeto→Responsável; cópia defensiva/não modificável do conjunto; rejeição de nome vazio; rejeição de projeto sem responsável; rejeição de email vazio; rejeição de Secretaria sem nome; rejeição de auditoria temporalmente inválida.

[DESCONHECIDO] O runtime da IA possui Java 21 e não possui Maven; portanto `mvn clean verify` com Java 25 e a inicialização Docker do snapshot F0-L2 precisam ser executados pelo usuário no ambiente real.

## Gate local final

[EXECUTADO: ambiente do usuário · 2026-09-21] `mvn -B -ntp clean verify` concluiu com `BUILD SUCCESS`; 3 testes, 0 falhas, 0 erros, 0 ignorados.

[EXECUTADO: ambiente do usuário · 2026-09-21] O probe persistente documentado em `VERIFICACAO-USUARIO.md` retornou `F0_L2_DOMAIN_PROBE_GREEN`.

[EXECUTADO: ambiente do usuário · 2026-09-21] Docker Compose iniciou PostgreSQL healthy, backend e frontend; REST e GraphQL retornaram `UP`.

[EXECUTADO: ambiente do usuário · 2026-09-21] O gate terminou com exit code 0 e `=== F0-L2 GREEN ===`.
