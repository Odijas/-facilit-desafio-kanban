# F2-L1 — EXECUÇÃO

Data: 2026-09-22

## Evidência anterior promovida

```text
[EXECUTADO PELO USUÁRIO · 2026-09-22] F1-L3: health REST UP; etapa Flyway V3 + índices alcançada; gate terminou com `Resultado: exit code 0`.
[CONCLUSÃO] F1-L3 e F1 são GREEN e constituem a baseline funcional anterior ao F2-L1.
```

## Executado neste ambiente no F2-L1

```text
[EXECUTADO] leitura integral dos arquivos modificados/criados e consumidores diretamente afetados.
[EXECUTADO] comparação integral F1-L3 → F2-L1 para delimitar o diff.
[EXECUTADO] parse XML do `backend/pom.xml`.
[EXECUTADO] parse YAML de `application.yml` e `compose.yaml`.
[EXECUTADO] busca de consumidores REST/GraphQL/health e símbolos de segurança.
[EXECUTADO] inspeção estática para ausência de segredo padrão versionado e ausência de logging no pacote de segurança/auth.
[CORREÇÃO DE VERIFICAÇÃO] a primeira regex do scan de segredo tratou o espaço antes da expressão `${...}` como valor e gerou falso positivo; o scan foi substituído por parse estrutural de `.env.example`/YAML e passou como `NO_VERSIONED_ADMIN_SECRET_GREEN`. Nenhum arquivo de produção precisou ser alterado por esse falso positivo.
[EXECUTADO] parse XML do POM: `POM_XML_GREEN`; parse YAML: `YAML_GREEN`; scan de logging: `SECURITY_NO_LOGGER_GREEN`; configuração sem senha administrativa versionada: `NO_VERSIONED_ADMIN_SECRET_GREEN`; migration estática: `MIGRATION_STATIC_GREEN`; gate: `GATE_BASH_SYNTAX_GREEN`; diff F1-L3→F2-L1 sem erro de whitespace: `DIFF_WHITESPACE_GREEN`.
[CORREÇÃO DE LEITURA] uma tentativa de abrir `graphql/schema.graphqls` usou caminho inexistente; os recursos reais `graphql/health.graphqls` e `graphql/kanban.graphqls` foram localizados e lidos integralmente.
```

## Não executado neste ambiente

`[DESCONHECIDO]` Maven/Spring em Java 25, Testcontainers/Docker/PostgreSQL, autenticação real, sessão/cookies, CSRF e frontend dependem do gate no ambiente do usuário. Este ambiente possui Java 21, não possui Maven/Docker e não substitui a execução local prevalente.
