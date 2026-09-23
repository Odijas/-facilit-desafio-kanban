# F3-L2 — DECISÕES

Data: 2026-09-23

- `[VERIFICADO: REPLANEJAMENTO-F3.md §4.1]` papéis `ADMIN` e `RESPONSIBLE`; migration V5 com `app_users.responsible_id` (FK com `ON DELETE CASCADE`, `UNIQUE`) e CHECK que amarra papel e vínculo.
- `[VERIFICADO]` regra de posse na camada de aplicação: `Actor` (record) informado a `ProjectService`, `ResponsibleService`, `SecretariatService` e `ResponsibleCredentialService`; REST e GraphQL chamam os mesmos casos de uso. `ForbiddenOperationException` vira 403 `FORBIDDEN` nos dois protocolos.
- `[VERIFICADO]` o responsável precisa estar incluído ao criar e continuar incluído ao editar; transição e exclusão exigem que ele seja responsável pelo projeto.
- `[VERIFICADO]` `AuthenticatedActorResolver` (infraestrutura de segurança) converte o `Principal` autenticado em `Actor`: `ROLE_ADMIN` → admin; `ROLE_RESPONSIBLE` → busca `responsible_id` em `app_users` pelo e-mail autenticado.
- `[VERIFICADO]` credenciais: `ResponsibleCredentialService` valida senha (mínimo 12 caracteres, máximo 72 bytes UTF-8, limite do bcrypt), tamanho do e-mail de login (320) e colisão com outro login (409). O hash fica no adapter (`PasswordEncoder` existente); a aplicação não conhece o algoritmo.
- `[VERIFICADO]` sincronização do e-mail de login dentro de `ResponsiblePersistenceAdapter.save`, na mesma transação da gravação do responsável, porque a camada de aplicação é pura e não abre transação. A pré-checagem de colisão fica em `ResponsibleService.update` (409).
- `[VERIFICADO]` responsável de demonstração: `ResponsibleDemoBootstrap` no mesmo padrão do `SecurityAdminBootstrap`, com as quatro variáveis obrigatoriamente juntas, idempotente pelo e-mail de login e em transação única (responsável + credencial). Reutiliza os casos de uso com ator de sistema ADMIN.
- `[VERIFICADO: Spring Security 6.5.11]` `dispatcherTypeMatchers(FORWARD, ERROR).permitAll()`; negócio passa de `hasRole("ADMIN")` para `hasAnyRole("ADMIN","RESPONSIBLE")`, com a autorização fina nos casos de uso.
- `[VERIFICADO: Spring Framework ErrorResponse]` o tratador genérico REST devolve exceções do framework (`ErrorResponse`) com o próprio status e só as demais viram 500 `INTERNAL_ERROR` com `incidentId`. O log registra tipo, incidente e origem (primeiro frame), sem mensagem da exceção, para não gravar dados pessoais.
- `[VERIFICADO]` GraphQL ganhou tratador genérico com `extensions.code = INTERNAL_ERROR` e `incidentId`.
- `[VERIFICADO]` `/auth/me` e o login devolvem `responsibleId` (nulo para ADMIN); o frontend usa `authorities` e `responsibleId` para exibir ações.
- `[VERIFICADO]` frontend: ações administrativas (novo responsável, CRUD de secretaria) ocultas para RESPONSIBLE; cartões alheios sem editar/excluir e não arrastáveis; no diálogo de projeto o próprio responsável vem marcado e travado. A proteção real continua no backend.
- `[VERIFICADO]` sem nova dependência no backend nem no frontend.
- `[VERIFICADO]` arquivos de teste novos autorizados pelo item 7 do escopo aprovado: `ResponsibleCredentialServiceTest`, `InMemoryResponsibleCredentialRepository`, `RestExceptionHandlerTest`; os demais testes foram mantidos e ampliados.
