# F0-L3 — EXECUÇÃO

Data: 2026-09-21.

## Baseline de entrada

[EXECUTADO: evidência enviada pelo usuário · 2026-09-21] F0-L2 foi validado com Maven `BUILD SUCCESS`, 3 testes sem falhas, Docker Compose funcional, REST/GraphQL `UP`, `git diff --check` limpo e probe `F0_L2_DOMAIN_PROBE_GREEN`.

## TDD interno — RED

[EXECUTADO: compilação de probe temporário contra a baseline F0-L2 intacta · 2026-09-21] O probe que exigia `ProjectScheduleCalculator` falhou com `RED_STATUS=1` porque a classe ainda não existia.

```text
error: cannot find symbol
import br.com.facilit.kanban.domain.project.ProjectScheduleCalculator;
```

## TDD interno — GREEN

[EXECUTADO: `javac -Xlint:all -Werror` sobre todos os arquivos `domain/**` após a implementação · 2026-09-21] Compilação do domínio puro concluída sem warnings no Java 21 disponível no runtime da IA.

[EXECUTADO: `java F0L3DomainProbe` · 2026-09-21] Caminhos legítimos e de ataque do motor foram exercitados e retornaram:

```text
F0_L3_DOMAIN_PROBE_GREEN
```

[VERIFICADO: probe temporário · 2026-09-21] Foram exercitados: A iniciar, Atrasado por início, Atrasado por término, Em andamento, igualdade com término previsto, Concluído, datas ausentes, duração zero, início futuro, intervalos temporais invertidos, projeto iniciado sem término previsto e limites das métricas.

## Verificações estáticas adicionais

[EXECUTADO: parser XML/YAML · 2026-09-21] `backend/pom.xml`, `compose.yaml` e `application.yml` foram parseados com sucesso.

[EXECUTADO: busca de whitespace/comentários · 2026-09-21] Nenhum trailing whitespace nos arquivos do pacote e nenhum comentário inline em `backend/src/main/java`.

[EXECUTADO: contagem de testes versionados · 2026-09-21] A suíte backend contém 18 métodos `@Test`: 3 preexistentes e 15 de domínio adicionados no F0-L3.

[EXECUTADO: `bash -n` no bloco de gate · 2026-09-21] O script documentado em `VERIFICACAO-USUARIO.md` possui sintaxe Bash válida.

## Execução local do usuário — gate final

[EXECUTADO: ambiente do usuário · 2026-09-21] Frontend concluído com Biome sem correções, typecheck sem erros, 2 testes aprovados e build Vite concluído.

[EXECUTADO: `mvn -B -ntp clean verify` · 2026-09-21] 18 testes executados; 0 falhas; 0 erros; 0 ignorados; `BUILD SUCCESS`.

[EXECUTADO: Docker Compose · 2026-09-21] Build de backend/frontend concluído; banco PostgreSQL 18.6 ficou `healthy`; backend e frontend iniciaram.

[EXECUTADO: Flyway/schema · 2026-09-21] `1:true`, `core_tables=4` e `explicit_indexes=5`.

[EXECUTADO: persistência · 2026-09-21] Caminho legítimo aceito com rollback (`DB_LEGITIMATE_PATH_GREEN`) e tentativa de persistir `delay_days=-1`/`remaining_time_percentage=101` foi rejeitada por `ck_projects_delay_days`, resultando em `DB_CONSTRAINT_ATTACK_BLOCKED`.

[EXECUTADO: regressão runtime · 2026-09-21] REST `UP`, GraphQL `UP`, frontend HTTP 200.

[EXECUTADO: `git diff --check` · 2026-09-21] Sem saída de erro. O `git status --short` listou apenas arquivos modificados/novos esperados do lote ainda não commitado.

## Resultado

[EXECUTADO: gate F0-L3/F0 · 2026-09-21] O script terminou com `=== F0-L3 / F0 GREEN ===` e `Resultado: exit code 0`.
