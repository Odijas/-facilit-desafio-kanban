# Changelog

Todas as mudanças relevantes deste projeto são registradas aqui.

## [2.0.0] — 2026-09-25

### Breaking changes

- Transições bloqueadas deixam de usar erro genérico 400 e passam a responder 422 `TRANSITION_BLOCKED`.
- Regras de domínio inválidas passam a responder 422 `BUSINESS_RULE_VIOLATION`.
- As transições Em andamento → A iniciar, Concluído → Em andamento e Concluído → Atrasado exigem `confirm: true` quando a ação automática apaga uma data registrada; sem confirmação, respondem 422 `CONFIRMATION_REQUIRED`.
- O mesmo contrato de erro e confirmação é exposto no GraphQL por `extensions.code` e pelo argumento `confirm`.

### Added

- Recálculo diário de status, dias de atraso e percentual restante no fuso de negócio, incluindo atualização antes das leituras e job de aquecimento.
- Concorrência otimista de projetos com coluna `version` e resposta 409 `CONFLICT`.
- Transação por caso de uso de escrita.
- Indicadores por secretaria, responsável e janela de prazo em REST e GraphQL.
- BDD formal em Cucumber para as 12 linhas da tabela de transição.
- JaCoCo integrado ao `mvn clean verify` com mínimo global de 95% de linhas.
- Testes isolados de controllers REST/GraphQL com Mockito e novos testes de persistência/transação/API.
- Documentação de erro no OpenAPI e logs de negócio sem dados pessoais.
- Limites de tamanho nas entradas REST e GraphQL: nomes e cargo até 200 caracteres, e-mail até 254, texto de busca até 100 e de 1 a 50 responsáveis por projeto (400, publicados no OpenAPI).
- BDD confere atraso e percentual restante em cada linha da tabela de transição.
- O CI constrói a imagem Docker do backend. O build da imagem roda os testes unitários sem o limite do JaCoCo, que é medido com unitários e integração no `mvn clean verify` do CI.
- Coleção Postman com os indicadores por secretaria, por responsável e de prazos.

### Fixed

- Status e métricas deixam de ficar congelados quando o calendário avança sem edição do projeto.
- Transições passam a partir do status calculado para o dia atual.
- Datas realizadas no futuro são rejeitadas.
- Corridas de unicidade e atualização otimista deixam de resultar em erro interno genérico.
- O Swagger UI envia o token CSRF exigido pela API; login e operações autenticadas funcionam pelo "Try it out".

## [1.0.0] — 2026-09-23

Primeira release pública do desafio técnico: CRUD, Kanban, REST, GraphQL, UI, autenticação, segurança, indicadores básicos, filtros, Secretaria, observabilidade, CI/CD, documentação e proposta de arquitetura de IA/RAG.
