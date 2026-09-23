# F1-L1 — DECISÕES

Data: 2026-09-22

1. REST e GraphQL usam `ResponsibleService` e `ProjectService`; nenhuma regra de negócio é duplicada nos adaptadores de entrega.
2. A camada `application` permanece sem dependência de Spring/JPA; persistência e transações ficam nos adapters JPA.
3. Foram criadas portas específicas (`ResponsibleRepository`, `ProjectRepository`, `SecretariatRepository`), sem repositório genérico.
4. Paginação entra na aplicação como `PageQuery`/`PageResult`; `Pageable` permanece restrito à infraestrutura.
5. Listagem paginada de projetos faz paginação dos projetos primeiro e busca os responsáveis do lote em consulta separada, evitando fetch-join de coleção sobre consulta paginada.
6. E-mail do responsável é normalizado e validado no domínio, de forma compartilhada por REST e GraphQL; V2 reforça unicidade case-insensitive no PostgreSQL. O REST usa Bean Validation apenas para obrigatoriedade estrutural, evitando duas regras semânticas divergentes de e-mail.
7. Auditoria é calculada na aplicação via `Clock`, com `Clock.systemUTC()` no composition root para comportamento reproduzível.
8. O tratamento de erro deste lote é mínimo: 400/404/409 no REST e categorias equivalentes no GraphQL. O contrato padronizado definitivo continua reservado para F1-L3.
9. Secretaria continua sem CRUD neste lote; apenas sua referência opcional em Responsável é validada quando informada.
10. Não foi introduzida autenticação, Swagger, filtros avançados ou transições Kanban; permanecem nos lotes planejados.

11. Correção pós-gate: os slice tests de health importam somente `HealthQuery`, em vez da composition root `ApplicationBeans`. Isso preserva o isolamento de `@WebMvcTest`/`@GraphQlTest` e evita carregar serviços/repositórios fora do slice.
