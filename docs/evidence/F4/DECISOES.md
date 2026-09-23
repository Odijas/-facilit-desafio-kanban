# F4 — DECISÕES

Data: 2026-09-23

- `[VERIFICADO: KANBAN v1.0, F4]` freeze sem feature nova. A F4 prova a entrega; falha gera só correção mínima na própria `release/1.0.0`.
- `[VERIFICADO: Gitflow já adotado no F3-L4]` release em `release/1.0.0` a partir da `develop`, `merge --no-ff` na `main`, tag anotada `v1.0.0` e merge de volta na `develop`, publicado nos dois remotos.
- `[VERIFICADO]` versão `1.0.0` na release.
  - **Arquivos:** `backend/pom.xml` (era `0.0.1-SNAPSHOT`), `backend/Dockerfile` (nome do jar copiado) e `frontend/package.json` (era `0.0.1`).
  - **Consumidores:** o único que depende do nome do jar é o `Dockerfile` (busca por `0.0.1` no repositório).
  - **Lockfile:** o `pnpm-lock.yaml` não guarda a versão do próprio pacote. `pnpm install --frozen-lockfile` segue válido (executado neste ambiente).
- `[VERIFICADO]` banco limpo sem destruir dados locais: o gate usa `docker compose -p facilit-kanban-f4` (o `-p` tem precedência sobre o `name:` do arquivo) e remove só os volumes desse projeto. O projeto padrão apenas para, sem `-v`.
- `[VERIFICADO]` saída do gate como evidência versionada (`SAIDA-GATE.txt`), normalizada, sem cores, retornos de carro nem espaços no fim, para passar no `git diff --check` do CI. O nome temporário `/tmp/saida-gate-f4.txt` fica fora do padrão `/tmp/f4-*` que o gate apaga na partida.
- `[VERIFICADO]` "nada depreciado" (decisão do usuário no F3-L4):
  - **Frontend:** qualquer aviso de depreciação faz o gate falhar.
  - **Código Java:** `[deprecation]` faz o gate falhar, além do `-Xlint:all -Werror` que já impede o build.
  - **Ferramentas (Maven, JVM, bibliotecas de teste):** os avisos são listados como informativos, porque não dependem do código do projeto.
- `[VERIFICADO: top10.owasp.org/2025]` revisão de segurança pelas 10 categorias da edição 2025, com riscos residuais declarados e não corrigidos no freeze:
  - senha de bootstrap do ADMIN sem tamanho mínimo;
  - login sem limite de tentativas;
  - GraphiQL ligado no Compose;
  - sem varredura de dependências Maven;
  - sem alertas.
- `[VERIFICADO]` a auditoria declara as lacunas em relação ao texto do desafio: BDD formal, Mockito, `@WebMvcTest` dos controllers de CRUD e datas do histórico reconstruído.
