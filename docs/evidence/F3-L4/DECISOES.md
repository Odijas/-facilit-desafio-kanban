# F3-L4 — DECISÕES

Data: 2026-09-23

- `[VERIFICADO: REPLANEJAMENTO-F3.md §5]` só GitHub Actions, sem GitLab CI. Migração com `git clone --bare` + `git push --mirror`. Workflow com `permissions: contents: read`, actions por SHA completo, sem `pull_request_target`, sem runner próprio e sem segredos.
- `[VERIFICADO: git ls-remote em github.com · 2026-09-23]` actions fixadas por SHA das tags mais recentes:
  - `actions/checkout` v7.0.1 → `3d3c42e5aac5ba805825da76410c181273ba90b1`;
  - `actions/setup-node` v7.0.0 → `820762786026740c76f36085b0efc47a31fe5020`;
  - `actions/setup-java` v6.0.1 → `de7274f081f381c8f8158605e0321c36c376e2e6`.

  As três são tags leves; o SHA é o próprio commit. Todas rodam em `node24` (lido no `action.yml` de cada tag).
- `[VERIFICADO: action.yml das tags]` entradas usadas:
  - `persist-credentials: false` no checkout (o padrão é `true`);
  - `node-version: "24"` no setup-node;
  - `distribution: temurin`, `java-version: "25"` e `cache: maven` no setup-java.

  O cache automático do setup-node só vale para npm (`package-manager-cache`). O projeto usa pnpm via corepack e segue sem cache, para não depender do pnpm antes do setup.
- `[VERIFICADO: actions/runner-images, Ubuntu2404-Readme.md, imagem 20260907]` o runner `ubuntu-24.04` traz Maven 3.9.16 (a mesma versão do `Dockerfile`) e Docker 28 (necessário ao Testcontainers). O runner fica fixado em `ubuntu-24.04`, e não em `ubuntu-latest`.
- `[VERIFICADO]` três jobs:
  - `frontend` com o mesmo roteiro do gate, sem `format` com escrita;
  - `backend` com `mvn -B -ntp clean verify`;
  - `repository` com `git diff --check` da árvore inteira, armazenamento de navegador e arquivos sensíveis.

  Gatilhos: `push`, `pull_request` e `workflow_dispatch`, com `concurrency` por ref.
- `[VERIFICADO]` análise automatizada aplicável sem ferramenta nova: Biome, TypeScript strict, `javac -Xlint:all -Werror` e a verificação de tipagem estrita dos gates. Essa verificação virou `frontend/scripts/check-strict-types.mjs` (`pnpm check:strict`) para rodar também no CI.
- `[VERIFICADO]` CodeQL ficou fora, porque exige `security-events: write`, contra a decisão de token só de leitura.
- `[VERIFICADO]` a publicação no GitHub é irreversível e fica com o usuário (`MIGRACAO-GITHUB.md`). O lote fecha em quatro etapas: gate local, reconstrução do histórico, migração e gate do pipeline pela API pública do GitHub, sem token.
- `[VERIFICADO]` a varredura de segredos no histórico (`git log --all -p`) vem antes da publicação e cobre:
  - chave privada;
  - tokens GitLab, GitHub e AWS;
  - atribuições com valor das variáveis de senha do projeto;
  - arquivos `.env`, `.pem`, `.key`, `.p12` e `.jks` em qualquer commit.

  Valores vazios, `change-me-local-only` e expansões `$(...)`/`${...}` são aceitos.
- `[VERIFICADO: Docker Hub, npm]` newman 6.2.2 (npm, versão mais recente) via `npx --yes`. A imagem `postman/newman` foi descartada: a última atualização é de 2024-06, na versão 6.1.3. O `npx` roda fora do workspace pnpm (`minimumReleaseAgeStrict`).
- `[VERIFICADO: Docker Hub]` `rhysd/actionlint:1.7.12` (tag ativa, 2026-03-30), a mesma versão do binário usado aqui.
- `[VERIFICADO]` README reorganizado nas seções pedidas pelo desafio. O histórico por lote foi preservado no fim, e as seções de ambiente e observabilidade do F3-L3 foram mantidas.
- `[VERIFICADO]` `AI_USAGE.md` registra só o que consta no repositório e nesta condução:
  - duas sugestões rejeitadas: CI duplicado e pacote extra de promoção documental;
  - erros da IA pegos pelos gates (F3-L1 rev2 e rev3, F3-L3 rev1, `! grep`).
- `[VERIFICADO]` ADR de IA como proposta, sem código:
  - agente somente leitura sobre os casos de uso existentes, mais RAG da documentação com `pgvector`;
  - portas na aplicação e adaptador na infraestrutura;
  - contrato REST e GraphQL, códigos de erro, prazos, retry, circuit breaker e modo degradado.
- `[VERIFICADO]` coleção de API sem credencial versionada. `adminEmail`/`adminPassword` vêm do ambiente do Postman/Insomnia ou de `--env-var`; nomes e e-mails levam `runId` para não colidir; a limpeza está dentro da coleção e também no cleanup do gate.

## rev2 — decisões do usuário e histórico Git (2026-09-23)

- `[DECISÃO DO USUÁRIO]` o `AI_USAGE.md` declara o uso de IA como o desafio exige, citando ChatGPT (OpenAI), com uso em fases anteriores (F0–F2) sob os mesmos prompts de governança, e Claude (Anthropic).
- `[DECISÃO DO USUÁRIO]` histórico em Gitflow com Conventional Commits. O GitLab tinha só a `main`.
- `[EXECUTADO PELO USUÁRIO · 2026-09-23]` estado do repositório:
  - `main` em `283ce5d` (`feat: add backend authentication and security foundation`), cujo README marca o F2-L1 como CANDIDATE;
  - área de trabalho com as alterações do F2-L1 GREEN ao F3-L4 sem commit.
- `[EXECUTADO PELO USUÁRIO · 2026-09-23]` inspeção dos pacotes em `~/Downloads`: todos completos, com prefixo `./`, sem entradas indevidas. O único arquivo do `HEAD` ausente a partir do F3-L1 é o `SecretariatRepository` órfão, removido de propósito.
- `[VERIFICADO]` reconstrução por fotografia de pacote validado:
  - um commit por área (backend, testes do backend, frontend, testes do frontend, infraestrutura, docs);
  - uma `feature/<lote>` por lote, com `merge --no-ff` na `develop`;
  - o F3-L4 sai da área de trabalho verificada, com 7 commits por assunto;
  - a `main` fica no F2-L1 até a release da F4.

  Alternativa descartada: `git add -p` por trecho, porque os mesmos arquivos foram alterados em vários lotes e haveria risco de erro manual.
- `[VERIFICADO]` as datas dos commits não são retroagidas; retroagir falsearia o histórico.
- `[VERIFICADO]` o F2-L3 usa o snapshot local de 2026-09-22 (estado real após o gate, com formatação do Biome) no lugar do `F2-L3-corrigido-v3`, que é anterior à formatação.

## rev3 — sem ferramenta depreciada e base sem `docs/` (2026-09-23)

- `[DECISÃO DO USUÁRIO]` nada depreciado na entrega.
- `[EXECUTADO PELO USUÁRIO]` o newman 6.2.2, a versão mais recente, emite `DEP0176` (`fs.F_OK`) no Node 24 e puxa dependências depreciadas (`har-validator`, `uuid@3`, `uuid@8`, `@faker-js/faker@5`). Não há versão mais nova, e suprimir o aviso esconderia o problema.
  - **Decisão:** retirar o newman do gate e do README e rodar os mesmos 21 cenários por `curl`. A coleção continua como entregável para Postman e Insomnia, e as duas execuções verdes de hoje ficam registradas.
  - **Segurança:** o gate deixa de baixar e executar pacotes do npm a cada rodada.
- `[VERIFICADO: frontend/pnpm-lock.yaml, lockfileVersion 9.0]` o lockfile não tem nenhum campo `deprecated:` (0 ocorrências). `[EXECUTADO PELO USUÁRIO]` `pnpm install` sem aviso de depreciação. No Java, `-Xlint:all -Werror` impede uso de API depreciada.
- `[EXECUTADO PELO USUÁRIO]` o `283ce5d` não contém `docs/`. O pacote F2-L1 só acrescenta arquivos ali, e o código é idêntico.
  - **Decisão:** a base aceita apenas arquivos **novos** em `docs/`, commitados como `docs: adiciona governança e evidências de F0 a F2-L1` em `feature/f2-l1-documentacao`.
  - Qualquer arquivo alterado, removido ou novo fora de `docs/` continua parando o script.
