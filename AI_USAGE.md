# Uso de IA no desenvolvimento

Este documento atende à Etapa 4 do desafio. Ele cobre:

- quais ferramentas de IA foram usadas;
- como o trabalho foi estruturado;
- o que foi delegado e o que ficou com o desenvolvedor;
- sugestões da IA rejeitadas e por quê;
- erros da IA que o processo pegou;
- um trecho de prompt representativo.

A proposta de camada de IA para o produto (Etapa 5) está no ADR [`docs/adr/0001-camada-ia-agente-rag.md`](docs/adr/0001-camada-ia-agente-rag.md).

## 1. Ferramentas

| Ferramenta | Uso |
|---|---|
| ChatGPT (OpenAI) | uso em fases anteriores (F0–F2), sob os mesmos prompts de governança |
| Claude (Anthropic), em conversas e sessões de agente no claude.ai | implementação de backend e frontend, testes, scripts de gate, documentação e pesquisa em fontes oficiais; conduziu toda a F3 |
| Ambiente de execução da IA (contêiner Linux) | verificações possíveis sem as dependências completas: `javac` sobre domínio e aplicação, suíte do frontend, validação de schema GraphQL, `promtool`, `actionlint`, `newman` contra servidor simulado |
| Máquina do desenvolvedor (Linux Mint) | execução real e decisiva: `mvn clean verify` com JDK 25 e Testcontainers, Docker Compose, suíte do frontend em Node 24 e os gates de cada lote |

As evidências registram, desde o F0-L1, que o ambiente de execução da IA não alcançava o Maven Central nem o Docker e não tinha o JDK 25. Por isso, nenhum build Java, teste de integração ou subida de contêiner foi dado como aprovado pela IA. Todos passaram pelo gate executado pelo desenvolvedor.

## 2. Como o trabalho foi estruturado

O desenvolvimento seguiu uma forma de *spec-driven development*, com três documentos versionados em `docs/governance/`:

1. **`PROMPT-EXECUTIVO-BASE-v1.1.md`.** Regras invioláveis, independentes de projeto e de ferramenta de IA:
   - RAG em fontes oficiais da versão exata;
   - protocolo anti-alucinação;
   - marcação de toda afirmação técnica como `[VERIFICADO]`, `[EXECUTADO]`, `[HIPÓTESE]` ou `[DESCONHECIDO]`;
   - leitura declarada e consumidores mapeados;
   - TDD;
   - diff mínimo.
2. **`PROMPT-EXECUTIVO-KANBAN-v1.0.md`.** Regras do projeto:
   - stack;
   - arquitetura (Clean Architecture pragmática, com REST e GraphQL sobre os mesmos casos de uso);
   - plano F0–F4 em lotes;
   - prioridade de corte;
   - "Pacote Anti-Alucinação" obrigatório por lote.
3. **`REPLANEJAMENTO-F3.md`.** Emenda ao plano, com as decisões do desenvolvedor registradas antes da implementação.

Ciclo de cada lote:

```text
pacote do projeto (tar.gz) → IA lê o código real e implementa (TDD) → pacote candidato (tar.gz + SHA-256)
  + evidências em docs/evidence/<LOTE>/ (livro-razão, fontes, decisões, consumidores, matriz, riscos)
  + gate executável (VERIFICACAO-USUARIO.md)
→ desenvolvedor aplica e roda o gate → GREEN: promove o lote | RED: correção no mesmo lote, nova revisão
```

Nenhum lote começou com o anterior em RED. A promoção documental de um lote GREEN entra no pacote do lote seguinte.

## 3. Divisão de responsabilidades

- **Desenvolvedor:**
  - escopo e prioridades;
  - aprovação de cada plano e emenda;
  - decisões de produto e segurança:
    - o responsável faz CRUD só dos próprios projetos e vê o quadro inteiro;
    - nenhuma credencial padrão é versionada;
    - só GitHub Actions, com migração do GitLab para o GitHub;
  - execução dos gates;
  - aceite final.
- **IA:**
  - leitura do código e das fontes oficiais;
  - implementação e testes;
  - scripts de verificação;
  - documentação;
  - registro de riscos e erros próprios.
- **Regra comum:** resultado previsto não vale como resultado executado. A IA declara o que não pôde executar e entrega o comando que resolve.

## 4. Sugestões da IA rejeitadas

1. **Pipeline duplicado (GitLab CI + GitHub Actions).**
   - **Proposta da IA:** manter CI no GitLab, onde o repositório estava, e acrescentar GitHub Actions.
   - **Rejeitada:** o desafio pede GitHub Actions e repositório no GitHub, e dois pipelines iguais dobram a manutenção sem ganho.
   - **Decisão:** migrar o repositório para o GitHub com o histórico (`git clone --bare` + `git push --mirror`) e manter só GitHub Actions, antes do freeze.
2. **Pacote separado só para promover documentação.**
   - **Proposta da IA:** depois do gate GREEN do F3-L1, gerar um pacote extra com a documentação promovida.
   - **Rejeitada:** reaplicar um pacote sobre um lote já validado é retrabalho e risco de sobrescrita.
   - **Decisão:** a promoção documental passa a viajar no pacote do lote seguinte.

## 5. Erros da IA pegos pelo processo

Todos estão registrados em `docs/evidence/<LOTE>/EXECUCAO.md` e foram corrigidos dentro do próprio lote:

- **F3-L1 rev2.**
  - **Erro:** uma consulta JPQL com parâmetros anuláveis quebrou no PostgreSQL (`lower(bytea)`, "could not determine type"), acusada pelos testes de integração.
  - **Correção:** filtros reescritos com JPA `Specification`.
  - **Causa registrada:** a IA leu menos do que o escopo da mudança.
- **F3-L1 rev3.** O próprio gate declarava uma variável GraphQL como `ID` onde o schema exige `ID!`.
- **F3-L3 rev1.**
  - **Erro:** o gate exigia o corpo exato `{"status":"UP"}` no health. Com os grupos `liveness`/`readiness` ativos, o Spring Boot lista os nomes dos grupos.
  - **Correção:** a asserção foi ajustada depois de conferida no código-fonte do Spring Boot 3.5.16.
- **Gates antigos.**
  - **Erro:** checagens escritas como `! grep ...` não bloqueiam sob `set -e` do bash. A própria IA encontrou a falha ao revisar o gate do F3-L3.
  - **Correção:** as checagens passaram a usar `if grep ...; then exit 1; fi`.

## 6. Trecho de prompt representativo

Do `PROMPT-EXECUTIVO-BASE-v1.1.md`, cláusula 2.1, que governou todas as sessões:

```text
- PROIBIDO citar API, assinatura, opção de configuração ou comportamento de memória.
- PROIBIDO reportar teste verde, build ok ou lint limpo sem execução real. Toda entrega cola a saída real dos comandos de verificação do projeto.
- PROIBIDO apresentar previsão como resultado.
- Se a ferramenta de IA não puder executar comandos, ela declara isso e entrega instruções de verificação para o usuário executar — nunca simula resultados.
```

As instruções de sessão eram curtas, porque as regras já estavam nos documentos de governança. Por exemplo, ao devolver a saída de um gate:

```text
analise. use seus prompts. corrija. pare.
```

```text
se estiver OK, avance para F3-L3. pare.
```

## 7. Limites do uso de IA neste projeto

- Toda afirmação de "verde" vem da execução do desenvolvedor ou de comando com saída registrada. Nenhuma vem de previsão da IA.
- O código gerado passou pelos mesmos testes, lint e verificações estritas que qualquer código do projeto, incluindo:
  - `-Xlint:all -Werror` no Java;
  - TypeScript strict;
  - proibição de `any`, `as` e `!`.
- As decisões de escopo, segurança e entrega foram do desenvolvedor e estão registradas antes da implementação.
