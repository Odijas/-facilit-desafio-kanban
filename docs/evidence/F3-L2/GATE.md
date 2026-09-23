# F3-L2 — GATE

Data: 2026-09-23

Estado: **GREEN** (gate local em 2026-09-23, exit code 0).

Escopo aprovado: `docs/governance/REPLANEJAMENTO-F3.md`, seções 4.1 e 4.2.

Critérios:

- `F3_L2_FRONTEND_GREEN`: Biome write, lint, typecheck, 30 testes e build;
- `F3_L2_BACKEND_GREEN`: `mvn clean verify` com unitários e ITs (Testcontainers) verdes;
- `F3_L2_DOCKER_GREEN`: Compose com db healthy, backend e frontend;
- `F3_L2_DEMO_BOOTSTRAP_GREEN`: admin e responsável de demonstração autenticam pelas variáveis de ambiente;
- `F3_L2_FIXTURES_GREEN`;
- `F3_L2_RESPONSIBLE_LEGIT_GREEN`: responsável cria e transiciona o próprio projeto e vê o quadro inteiro;
- `F3_L2_RESPONSIBLE_ATTACK_BLOCKED_GREEN`: projeto alheio, responsáveis, secretarias, credenciais e mutation GraphQL respondem 403 `FORBIDDEN`, sem alterar o projeto alheio;
- `F3_L2_CREDENTIALS_GREEN`: senha curta 400; definição 204; login; revogação 204; login seguinte 401;
- `F3_L2_SAFE_ERROR_GREEN`: rota inexistente 404, não 403;
- `F3_L2_CLEANUP_GREEN`;
- `F3_L2_STRICT_TYPES_GREEN`, ausência de armazenamento de navegador, `git diff --check` e `F3_L2_STATIC_GREEN`;
- marcador final `=== F3-L2 GREEN ===` e exit code 0.

Evidência final recebida do usuário:

```text
[EXECUTADO PELO USUÁRIO · 2026-09-23] gate F3-L2 completo, saída enviada na conversa com todos os marcadores GREEN e exit code 0:
  F3_L2_FRONTEND_GREEN · F3_L2_BACKEND_GREEN · F3_L2_DOCKER_GREEN · F3_L2_DEMO_BOOTSTRAP_GREEN
  F3_L2_FIXTURES_GREEN · F3_L2_RESPONSIBLE_LEGIT_GREEN · F3_L2_RESPONSIBLE_ATTACK_BLOCKED_GREEN
  F3_L2_CREDENTIALS_GREEN · F3_L2_SAFE_ERROR_GREEN · F3_L2_CLEANUP_GREEN
  F3_L2_STRICT_TYPES_GREEN · F3_L2_STATIC_GREEN
  === F3-L2 GREEN ===
  Resultado: exit code 0
[VERIFICADO: script com `set -euo pipefail`] a lista acima é a sequência de marcadores do gate; o exit code 0 final só ocorre se todas as etapas terminarem com sucesso.
[LIMITE] o texto literal da saída não foi transcrito para este arquivo; a fonte primária é a mensagem do usuário de 2026-09-23.
```

Conclusão: F3-L2 promovido para GREEN em 2026-09-23.
