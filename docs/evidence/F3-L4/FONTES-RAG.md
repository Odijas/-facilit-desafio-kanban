# F3-L4 — FONTES RAG

Data: 2026-09-23

1. Código do projeto e evidências dos lotes anteriores.
2. Fontes oficiais:
   - Tags e SHAs: `git ls-remote https://github.com/actions/{checkout,setup-node,setup-java}.git`.
   - `action.yml` nas tags exatas:
     - https://raw.githubusercontent.com/actions/checkout/v7.0.1/action.yml
     - https://raw.githubusercontent.com/actions/setup-node/v7.0.0/action.yml
     - https://raw.githubusercontent.com/actions/setup-java/v6.0.1/action.yml
   - Imagem do runner Ubuntu 24.04 (Maven 3.9.16, Docker 28, Compose 2.38): https://github.com/actions/runner-images/blob/main/images/ubuntu/Ubuntu2404-Readme.md
   - GitHub REST — workflow runs (`head_sha`, acesso público sem autenticação): https://docs.github.com/en/rest/actions/workflow-runs?apiVersion=2022-11-28
   - GitHub — duplicação de repositório e segurança de Actions: referências já registradas em `REPLANEJAMENTO-F3.md` §5.
   - actionlint v1.7.12 (binário oficial e imagem `rhysd/actionlint:1.7.12`, Docker Hub ativo em 2026-03-30): https://github.com/rhysd/actionlint
   - newman 6.2.2 (`npm view newman version` → 6.2.2); imagem `postman/newman` com última tag de 2024-06 (Docker Hub).
   - Postman Collection v2.1 schema: https://schema.getpostman.com/json/collection/v2.1.0/collection.json
3. Execução local: actionlint, newman contra API simulada, Biome/TypeScript/Vitest/Vite, scanner de histórico com controles positivo e negativo, `bash -n` nos dois gates.
