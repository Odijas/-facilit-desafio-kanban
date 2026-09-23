# F3-L4 — RISCOS E LACUNAS

Data: 2026-09-23

- `[DESCONHECIDO]` primeiro pipeline real no GitHub. Não pode ser executado antes da migração. Resolve: `VERIFICACAO-CI.md`.
- `[HIPÓTESE]` Testcontainers funciona no runner `ubuntu-24.04` com o Docker 28 pré-instalado (documentado na imagem, não executado). Resolve: job `backend` do primeiro pipeline.
- `[HIPÓTESE]` corepack incluído no Node 24 instalado pelo `setup-node` ativa o pnpm 12.5.1, como no gate local e no `Dockerfile` do frontend. Resolve: job `frontend`.
- `[DESCONHECIDO]` conteúdo do histórico Git local, que o ambiente da IA não tem. A varredura de segredos roda no gate local antes da publicação. Um falso positivo aparece com commit e arquivo, sem o valor, e se resolve no próprio lote.
- `[VERIFICADO: decisão do usuário · 2026-09-23]` a tabela "Ferramentas" do `AI_USAGE.md` cita ChatGPT (OpenAI) nas fases anteriores e Claude (Anthropic), como informado pelo usuário.
- `[VERIFICADO]` a limpeza do gate local remove o admin temporário e as sobras da coleção (`Projeto Postman %`, `postman-%@example.invalid`, `Secretaria Postman %`) caso o newman pare no meio.
- `[VERIFICADO]` o limite da API do GitHub sem token é de 60 requisições por hora por IP; o gate CI usa no máximo 45.
- `[VERIFICADO]` a migração é irreversível (repositório público). Por isso ela não está no gate e é executada pelo usuário depois do GREEN local.
- `[VERIFICADO]` achado próprio já registrado no F3-L3: gates antigos com `! grep` não bloqueavam. O gate do F3-L4 e o CI usam `if grep …; then exit 1; fi`.
- `[VERIFICADO]` aviso não bloqueante mantido: bundle Vite acima de 500 kB.
- `[HIPÓTESE]` os pacotes F2-L1, F2-L2 e F3-L1-local correspondem aos estados validados em cada gate. O script confere o F2-L1 contra o `283ce5d` e para se houver diferença; para os demais, o SHA-256 fixado impede troca de arquivo.
- `[VERIFICADO]` a `main` publicada fica no F2-L1 até a F4. Não enviar o link aos avaliadores antes da release `v1.0.0`.
- `[VERIFICADO]` o backup da etapa 2 inclui o `.env` e deve permanecer só na máquina local.
- `[HIPÓTESE]` a coleção deixa de ser executada automaticamente e pode divergir da API. Mitigações: o smoke em `curl` cobre os mesmos 21 cenários; a F4 é freeze (sem mudança de API); a checagem estática de schema e credenciais continua no gate.
- `[VERIFICADO]` o smoke usa apenas `curl`, `python3` e `date` GNU, já presentes nos gates anteriores.
