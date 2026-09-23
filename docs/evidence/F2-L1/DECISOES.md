# F2-L1 — DECISÕES

Data: 2026-09-22

## D01 — Sessão HTTP em vez de token próprio

O lote usa a sessão nativa do Spring Security com `HttpSessionSecurityContextRepository`. Não foi criado JWT ou protocolo proprietário. O login customizado salva explicitamente o `SecurityContext`, conforme Spring Security 6.5.

## D02 — Senha com DelegatingPasswordEncoder

A senha administrativa de bootstrap é recebida somente por configuração local/ambiente e persistida codificada por `PasswordEncoderFactories.createDelegatingPasswordEncoder()`. Não existe senha padrão versionada.

## D03 — Bootstrap administrativo mínimo

F2-L1 cria somente o papel `ADMIN`, suficiente para provar autenticação/autorização sem antecipar gestão de usuários. Se as duas variáveis de bootstrap estiverem vazias, nenhum usuário é criado; se apenas uma estiver presente, o startup falha sem revelar valor sensível.

## D04 — CSRF preservado para SPA

CSRF não foi desabilitado. O cookie `XSRF-TOKEN` é legível pelo JavaScript e o handler segue o padrão SPA da documentação oficial: header `X-XSRF-TOKEN` usa token cru; outros caminhos preservam XOR/BREACH. Após login o token anterior é invalidado e o cliente deve obter outro por `GET /api/v1/auth/csrf`.

## D05 — Autorização mínima e explícita

Health, bootstrap de CSRF e documentação ficam públicos. CRUD REST e `/graphql` exigem `ROLE_ADMIN`. Demais rotas não mapeadas são negadas por padrão.

## D06 — Erros de segurança não revelam credenciais

Falha de login retorna mensagem genérica; 401/403 do filtro usam o mesmo vocabulário estável de `ApiErrorCode`. Nenhum logger foi introduzido no fluxo de autenticação/bootstrap.

## D07 — Testes de segurança em fronteira real

`SecurityApiIT` usa PostgreSQL 18.6 via Testcontainers e exercita ataque e caminho legítimo: anonimato, credencial inválida, hash persistido, login/sessão, CSRF bloqueado/permitido e logout. `KanbanApiIT` foi adaptado para consumir as APIs agora protegidas.
