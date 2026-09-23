# F1-L3 — EXECUÇÃO

Data: 2026-09-22

## Executado neste ambiente

```text
[EXECUTADO] leitura integral dos arquivos modificados/criados e consumidores diretamente relacionados.
[EXECUTADO] `javac -Xlint:all -Werror` sobre 23 fontes puras de `domain` + `application`: `PURE_JAVAC_GREEN`.
[EXECUTADO] parse XML de `backend/pom.xml`: `POM_XML_GREEN`.
[EXECUTADO] busca `grep -RIn` dos consumidores de handlers/códigos/controllers registrada em `CONSUMIDORES.md`.
[EXECUTADO] fontes oficiais verificadas para Spring Boot 3.5.16, Spring GraphQL 1.4, Testcontainers 1.21.4, springdoc 2.8.x/2.8.17 e Failsafe.
```

## Erro próprio corrigido

A primeira tentativa de escrever os documentos F1-L3 usou um delimitador de heredoc que colidiu com um heredoc interno do gate. O Python abortou por erro de sintaxe antes de escrever os documentos; fragmentos seguintes tentaram rodar no shell deste ambiente, onde Corepack não tinha rede e Docker não existe. A tentativa foi descartada, a documentação foi regenerada de forma segura e o conteúdo foi relido. Nenhuma dessas saídas é tratada como validação do projeto.

## Não executado neste ambiente

`[DESCONHECIDO]` Maven/Spring Java 25, Testcontainers/Docker, PostgreSQL real, springdoc runtime e frontend dependem do gate no ambiente do usuário.

## Gate final no ambiente do usuário

```text
[EXECUTADO PELO USUÁRIO · 2026-09-22] `docs/evidence/F1-L3/VERIFICACAO-USUARIO.md` chegou ao final com `Resultado: exit code 0`.
[EXECUTADO PELO USUÁRIO] health REST retornou `{"status":"UP"}`.
[EXECUTADO PELO USUÁRIO] etapa `FLYWAY V3 + INDEXES NO COMPOSE` passou.
[CONCLUSÃO] F1-L3 e F1 promovidos para GREEN.
```
