# F4 — FONTES RAG

Data: 2026-09-23

1. Código e evidências do projeto (F0 a F3-L4).
2. OWASP Top 10:2025, nomes das 10 categorias: https://top10.owasp.org/2025/
3. Docker Compose — precedência do nome do projeto (`-p` sobre `name:` do arquivo): https://docs.docker.com/compose/how-tos/project-name/ (ordem: `-p` > `COMPOSE_PROJECT_NAME` > `name:` do arquivo > diretório)
4. GitHub REST — workflow runs (`head_sha`, `event`, `head_branch`) e jobs: https://docs.github.com/en/rest/actions/workflow-runs?apiVersion=2022-11-28
5. Execução local: suíte do frontend com a versão 1.0.0, `pnpm audit --prod`, `bash -n` dos gates, trechos estáticos do gate num repositório Git simulado.
