# F0-L1 — RISCOS E LACUNAS

1. **F0-L1 funcionalmente GREEN.** Frontend, backend, Docker, banco, REST e GraphQL foram executados com sucesso em 2026-09-21.
2. **Mockito/JDK 25.** O backend está GREEN, porém a execução emitiu aviso de self-attach do Mockito/Byte Buddy. Não bloqueia F0-L1; acompanhar antes de eventual mudança do JDK que impeça carregamento dinâmico.
3. **Repositório remoto.** O repositório recebido aponta para GitLab, enquanto o desafio pede entrega pública no GitHub. Pendência de entrega, sem impacto no bootstrap.
4. **Healthcheck do backend no Compose.** O gate prova readiness por sondagem HTTP limitada. Um healthcheck de aplicação pode ser incorporado quando houver requisito operacional concreto; não é necessário para promover este lote.
5. **[DESCONHECIDO] Ambiente final de avaliação.** Versões de Docker/Compose e disponibilidade de portas do avaliador não são conhecidas; a entrega deve permanecer reproduzível via README e Compose.
