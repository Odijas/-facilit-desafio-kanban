# F5-Q1 — FONTES RAG

Data: 2026-09-25

1. `[VERIFICADO]` `docs/governance/PROMPT-EXECUTIVO-BASE-v1.1.md`: protocolo de entrada, marcação obrigatória, leitura integral, consumidores, execução real e diff mínimo.
2. `[VERIFICADO]` `docs/governance/PROMPT-EXECUTIVO-KANBAN-v1.0.md:390-415`: pacote anti-alucinação obrigatório por lote.
3. `[VERIFICADO]` `docs/governance/PLANO-CORRECAO-RELEASE-2.0.2.md:62-89`: escopo F5-Q1 — versão 2.0.2, E2–E5, D1, testes e gate.
4. `[VERIFICADO]` Código real da v2.0.1 no pacote base: serviços, domínio, persistência, segurança, DTOs, OpenAPI e testes listados em `LEITURA.md`.
5. `[VERIFICADO]` `docs/evidence/F5-P3/SAIDA-GATE.txt`: baseline v2.0.1 executada com 174 testes unitários/BDD e 51 de integração.
6. `[EXECUTADO]` `javac --release 21` sobre `application` + `domain` do candidato: exit 0. É verificação parcial com JDK 21, não substitui o Maven/Java 25 do gate.
7. `[EXECUTADO]` harnesses temporários fora do projeto para E2, E4 e E5: caminhos alterados exercitados com exit 0.
8. `[DESCONHECIDO]` Resultado de `mvn clean verify`, Docker, Testcontainers e `/api-docs` real no candidato. O ambiente da IA não possui Maven, pnpm nem Docker; `VERIFICACAO-USUARIO.md` resolve a lacuna.

Nenhuma API nova, dependência nova ou comportamento de biblioteca foi inferido de memória. A alteração do OpenAPI reutiliza as classes e o padrão já existentes em `ApiErrorDocumentation`.

9. `[EXECUTADO: saída do usuário em 2026-09-25]` primeiro `mvn clean verify` do candidato: 52 testes de integração, 1 falha em `OpenApiContractIT.errorExamplesMatchTheOperationResourceInputAndAuthorizationRule` para `POST /api/v1/projects`.
10. `[VERIFICADO]` `ProjectRestController.java`: `POST /api/v1/projects` já declara um exemplo 400 próprio; `ApiErrorDocumentation.add` não substitui resposta existente.
