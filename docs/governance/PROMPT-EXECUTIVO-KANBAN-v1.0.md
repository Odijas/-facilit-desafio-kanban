# PROMPT EXECUTIVO — KANBAN v1.0

## Subordinação

O **PROMPT EXECUTIVO — BASE v1.1** prevalece integralmente sobre este documento.

Este prompt específico apenas adiciona regras próprias do projeto Kanban e não pode relaxar, substituir ou contradizer o documento-pai.

---

## Objetivo

Entregar até **25/09/2026** uma solução para o **Desafio Técnico Backend — Kanban**, com nível compatível com uma vaga de Desenvolvedor(a) Back-End Sênior, priorizando:

- funcionamento correto;
- segurança;
- testes automatizados;
- clareza arquitetural;
- documentação;
- rastreabilidade das decisões;
- diferenciais demonstráveis;
- qualidade acima de quantidade;
- ausência de regressões;
- execução verificável por evidência real.

O objetivo é maximizar os diferenciais do desafio sem sacrificar o núcleo obrigatório.

---

## Governança

Toda execução permanece subordinada ao **PROMPT EXECUTIVO — BASE v1.1**, incluindo:

- RAG obrigatório;
- protocolo anti-alucinação;
- leitura declarada;
- marcação das afirmações técnicas;
- TDD interno;
- Clean Architecture pragmática;
- SOLID;
- diffs mínimos;
- proibição de over-engineering;
- consumidores mapeados;
- execução real de testes e verificações;
- gates entre fases e lotes.

Nenhum resultado previsto pode ser apresentado como resultado executado.

---

## Arquitetura governante

A solução seguirá **Clean Architecture pragmática**.

Regras:

1. O domínio e as regras de negócio não dependem de REST, GraphQL, banco, UI ou framework.
2. REST e GraphQL reutilizam os mesmos casos de uso.
3. Não deve existir duplicação de regra de negócio entre adaptadores.
4. Controllers/resolvers devem permanecer finos.
5. Persistência, segurança e infraestrutura permanecem nas bordas.
6. Nenhuma abstração adicional será criada sem requisito concreto.
7. A solução deve permanecer simples o suficiente para ser concluída, testada e auditada dentro do prazo.

---

## Stack planejada

### Backend

- Java 25 LTS
- Spring Boot 3.5.x
- Maven
- Spring MVC
- Spring for GraphQL
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Spring Security
- springdoc OpenAPI / Swagger
- Spring Boot Actuator
- Micrometer / Prometheus
- JUnit 5
- Mockito
- Spring Boot Test
- GraphQlTester
- Testcontainers com PostgreSQL

### Frontend

- React 19
- TypeScript
- Vite 8
- Material UI
- React Router
- React Query
- Zustand somente se houver necessidade real de estado global
- Vitest
- Testing Library
- @testing-library/user-event
- Biome
- pnpm

### Infraestrutura e entrega

- Docker
- Docker Compose
- Git
- GitHub
- GitHub Actions
- Prometheus
- Grafana

As versões exatas das dependências devem ser verificadas e congeladas na fundação antes da primeira baseline.

---

# Plano de execução

## F0 — Fundação + domínio — 21/09/2026

Objetivo: criar a fundação executável e estabelecer a primeira baseline confiável.

### Lote F0-L1 — Bootstrap

- criar backend;
- criar frontend;
- definir estrutura arquitetural;
- configurar PostgreSQL;
- configurar Docker/Docker Compose;
- configurar migrations;
- configurar qualidade estática;
- fundar REST;
- fundar GraphQL;
- criar primeira execução integrada.

### Lote F0-L2 — Domínio

Implementar:

- Projeto;
- Responsável;
- relacionamento entre Projeto e Responsável;
- estrutura inicial para Secretaria quando necessária;
- datas previstas e realizadas;
- auditoria mínima;
- regras fundamentais do domínio.

### Lote F0-L3 — Motor de regras

Implementar e validar:

- cálculo automático de status;
- percentual de tempo restante;
- dias de atraso;
- regras de consistência temporal;
- testes unitários do domínio;
- persistência mínima;
- primeira baseline `tar.gz`.

Gate F0:

- aplicação sobe;
- banco sobe;
- migrations executam;
- testes executam;
- domínio principal possui cobertura;
- REST e GraphQL estão fundados;
- primeira baseline é gerada.

---

## F1 — Backend funcional — 22/09/2026

Objetivo: concluir integralmente o núcleo backend exigido pelo desafio.

### Lote F1-L1 — CRUD

Implementar:

- CRUD de Projeto;
- CRUD de Responsável;
- REST;
- GraphQL;
- validação de e-mail único;
- auditoria;
- paginação.

### Lote F1-L2 — Kanban e transições

Implementar integralmente:

- listagem por status;
- transição entre estados;
- ações automáticas;
- bloqueios;
- mensagens claras;
- recálculo após cada transição;
- validação de correspondência entre status solicitado e status resultante.

A tabela de transições do desafio deve ser coberta linha a linha.

### Lote F1-L3 — API e qualidade

Implementar:

- tratamento padronizado de erros;
- validação de entrada;
- paginação;
- filtros necessários;
- índices coerentes;
- Swagger/OpenAPI;
- exemplos de request/response;
- testes unitários;
- testes de integração;
- testes de API;
- testes GraphQL.

Gate F1:

- todos os requisitos obrigatórios de backend funcionam;
- tabela de transições está coberta;
- REST e GraphQL reutilizam os mesmos casos de uso;
- testes estão verdes;
- Swagger está acessível;
- banco real é exercitado nos testes de integração.

---

## F2 — Segurança + UI — 23/09/2026

Objetivo: transformar a solução em produto demonstrável sem degradar a qualidade do backend.

### Lote F2-L1 — Segurança

Implementar:

- Spring Security;
- autenticação;
- login;
- logout;
- autorização;
- armazenamento seguro de senha;
- sessão/cookie;
- CSRF compatível com SPA;
- tratamento seguro de erros;
- ausência de credenciais ou PII em logs.

### Lote F2-L2 — Fundação frontend

Implementar:

- React;
- TypeScript;
- Vite;
- Material UI;
- roteamento;
- integração com API;
- fluxo de autenticação;
- estados de loading;
- estados de erro;
- estrutura visual consistente.

### Lote F2-L3 — Kanban UI

Implementar:

- login;
- quadro Kanban;
- drag-and-drop;
- listagem de projetos por status;
- CRUD essencial;
- responsáveis;
- feedback de erros de transição;
- atualização consistente após operações.

Testes unitários obrigatórios no frontend, incluindo ao menos:

- componentes críticos;
- formulários;
- validações;
- autenticação;
- Kanban;
- interações;
- loading;
- erro.

Gate F2:

- login/logout funcionam;
- endpoints estão protegidos;
- UI consome a API;
- Kanban funciona por drag-and-drop;
- erros de domínio são exibidos corretamente;
- suíte unitária frontend está verde.

---

## F3 — Diferenciais — 24/09/2026

Objetivo: maximizar valor técnico sem comprometer a baseline.

### Lote F3-L1 — Funcionalidades diferenciais

Implementar:

- indicadores;
- média de dias de atraso por status;
- quantidade de projetos por status;
- outros resumos úteis de baixo risco;
- CRUD de Secretaria;
- filtros avançados por:
  - secretaria;
  - período;
  - responsável;
  - texto.

### Lote F3-L2 — Observabilidade

Implementar:

- Spring Boot Actuator;
- métricas;
- Prometheus;
- Grafana;
- logs estruturados, se o custo permanecer seguro.

### Lote F3-L3 — Engenharia de entrega

Implementar/documentar:

- GitHub Actions;
- build;
- testes;
- análise automatizada aplicável;
- README;
- AI_USAGE.md;
- ADR de arquitetura de agente/RAG;
- diagrama;
- contrato proposto;
- montagem de contexto;
- erros/timeouts de provedor;
- trade-offs;
- limitações e próximos passos.

Gate F3:

- nenhum diferencial pode deixar o baseline vermelho;
- CI executa;
- observabilidade funciona;
- documentação está atualizada;
- AI_USAGE.md atende integralmente ao desafio;
- ADR de IA/RAG está completo.

---

## F4 — Freeze — 25/09/2026

Objetivo: provar a entrega.

Nenhuma feature nova será adicionada.

Executar:

- build limpo;
- testes completos;
- análise estática;
- `git diff --check`;
- subida integral por Docker Compose;
- banco limpo;
- migrations do zero;
- smoke tests;
- REST;
- GraphQL;
- autenticação;
- UI;
- Swagger;
- Prometheus/Grafana;
- revisão de segurança;
- revisão de README;
- revisão de AI_USAGE.md;
- revisão de diagramas;
- revisão do histórico de commits;
- auditoria requisito → implementação → teste → evidência.

Qualquer falha encontrada durante F4 gera somente correção mínima e nova execução do gate afetado.

---

# Pacote Anti-Alucinação obrigatório por lote

Cada lote deve produzir obrigatoriamente:

1. snapshot `tar.gz`;
2. SHA-256 do snapshot;
3. livro-razão integral dos arquivos lidos;
4. `FONTES-RAG.md`;
5. `DECISOES.md`;
6. consumidores mapeados;
7. comandos executados;
8. saídas reais dos comandos;
9. matriz:
   - requisito;
   - implementação;
   - teste;
   - evidência;
10. riscos e lacunas marcados como `[DESCONHECIDO]`;
11. separação explícita entre:
   - `[VERIFICADO]`;
   - `[EXECUTADO]`;
   - `[HIPÓTESE]`;
   - `[DESCONHECIDO]`;
12. gate explícito antes do próximo lote.

Nenhum lote seguinte começa com gate RED.

---

# Regra contra rodadas infinitas

Cada fase deve ser dividida em poucos lotes fechados, objetivos e verificáveis.

É proibido:

- decompor indefinidamente em microtarefas;
- abrir novo lote para corrigir algo que pertence ao lote corrente;
- avançar com pendência conhecida não classificada;
- criar rodadas RED/GREEN separadas para o usuário quando a implementação do lote pode ser consolidada;
- promover baseline parcialmente validada.

Correções necessárias para fechar o lote permanecem dentro do próprio lote até o gate final.

---

# Critério de prioridade

A ordem de prioridade é:

1. núcleo obrigatório correto;
2. segurança e testes;
3. documentação e execução reproduzível;
4. UI demonstrável;
5. autenticação;
6. GraphQL;
7. indicadores;
8. filtros;
9. Secretaria;
10. observabilidade;
11. CI/CD;
12. arquitetura de IA/RAG.

Caso o prazo force corte, o item de menor prioridade e maior risco é removido antes que qualquer requisito obrigatório ou baseline verde seja sacrificado.

---

# Regra final

A entrega deve demonstrar maturidade de engenharia, e não apenas quantidade de funcionalidades.

Toda decisão deve ser:

- justificável;
- verificável;
- testável;
- proporcional ao prazo;
- coerente com o desafio;
- subordinada ao PROMPT EXECUTIVO — BASE.
