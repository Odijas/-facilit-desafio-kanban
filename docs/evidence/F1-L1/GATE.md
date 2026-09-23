# F1-L1 — GATE

Data: 2026-09-22

Estado: **GREEN**.

Evidência final fornecida pelo usuário após a correção dos slice tests de health:

```text
{"status":"UP"}
Resultado: exit code 0
Resultado: exit code 0
```

O comando de validação pós-correção executava `mvn -B -ntp clean verify` e, em seguida, o gate integral de `VERIFICACAO-USUARIO.md` dentro de `bash` com `set -euo pipefail`. O exit code final 0 fecha o lote sem promover a regressão encontrada na primeira tentativa.

Critérios comprovados pelo gate executado:

- backend Maven verde;
- migrations e startup integrados verdes;
- CRUD REST de Projeto/Responsável;
- CRUD GraphQL de Projeto/Responsável;
- unicidade case-insensitive de e-mail;
- bloqueio de responsável em uso;
- paginação REST/GraphQL;
- regressão health preservada;
- `git diff --check` verde;
- containers ativos.
