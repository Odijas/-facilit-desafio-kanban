# F0-L2 — MATRIZ REQUISITO → IMPLEMENTAÇÃO → VERIFICAÇÃO → EVIDÊNCIA

Data: 2026-09-21.

| Requisito | Implementação | Verificação | Evidência |
|---|---|---|---|
| Projeto | `domain/project/Project.java` | compile + probe | `[EXECUTADO] F0_L2_DOMAIN_PROBE_GREEN` |
| Responsável | `domain/responsible/Responsible.java` | compile + probe | `[EXECUTADO] F0_L2_DOMAIN_PROBE_GREEN` |
| Um ou mais responsáveis | `Project.responsibleIds` | caminho legítimo + conjunto vazio | `[EXECUTADO] probe` |
| Secretaria-base | `domain/secretariat/Secretariat.java` | compile + probe | `[EXECUTADO] probe` |
| Datas previstas/realizadas | `domain/project/ProjectDates.java` | compile + criação válida | `[EXECUTADO] probe` |
| Status estrutural | `domain/project/ProjectStatus.java` | compile | `[EXECUTADO] javac -Xlint:all -Werror` |
| Auditoria mínima | `domain/common/AuditMetadata.java` | válida + `updatedAt < createdAt` | `[EXECUTADO] probe` |
| Domínio sem framework | novos arquivos `domain/**` | inspeção integral de imports | `[VERIFICADO] somente `java.*` + imports do próprio domínio` |
| Build/testes Maven Java 25 | projeto completo | `mvn -B -ntp clean verify` | `[EXECUTADO] BUILD SUCCESS; 3 testes, 0 falhas` |
| Regressão runtime | Compose + health REST/GraphQL | gate local | `[EXECUTADO] PostgreSQL healthy; REST/GraphQL UP; exit code 0` |
