# F5-P3 — MATRIZ ETAPA → VERIFICAÇÃO → EVIDÊNCIA

Data: 2026-09-25

| Etapa | Verificação | Evidência |
|---|---|---|
| Freeze da `hotfix/2.0.1` | `VERIFICACAO-USUARIO.md`: build, testes, estática, Docker, Swagger com exemplos, API, segurança, observabilidade e CI | `SAIDA-GATE.txt` (neste diretório) |
| Obrigatório #40 (Swagger com exemplos) na entrega final | regra do `OpenApiContractIT` aplicada ao `/api-docs` do Docker | `F5_P3_UI_SWAGGER_GREEN` |
| Busca literal (decisão 1) na entrega final | `text=_` sem resultado com nomes sem `_` | `F5_P3_API_GREEN` |
| Release `v2.0.1` | `VERIFICACAO-RELEASE.md`: refs, tag, back-merge, GitHub, CI da `main` e conteúdo da tag | `SAIDA-RELEASE.txt`, na `develop` |
| Aderência final | auditoria da tag `v2.0.1` (análise própria confrontada com um agente independente) | `docs/evidence/F5/ADERENCIA-V2.0.1.md`, na `develop` |
