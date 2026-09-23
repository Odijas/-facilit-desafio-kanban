# F4 — RISCOS E LACUNAS

Data: 2026-09-23

- `[DESCONHECIDO]` execução do gate de freeze, do CI da release e da verificação da release. Resolve: `RELEASE.md`, passos 4 a 7.
- `[HIPÓTESE]` o Maven 3.9.16 com JDK 25 pode imprimir avisos de depreciação da própria ferramenta. O gate os lista sem falhar; o código do projeto continua coberto por `-Xlint:all -Werror`.
- `[HIPÓTESE]` `pnpm audit --prod` depende do registro npm no momento da execução. É informativo; um alerta novo seria analisado sem bloquear o freeze por fator externo.
- `[VERIFICADO]` o gate para o projeto Compose padrão para liberar as portas; volte a subi-lo depois, se precisar (`docker compose up -d`).
- `[VERIFICADO]` riscos de segurança residuais listados em `REVISAO-SEGURANCA.md` (A02, A03, A04, A06, A07 e A09).
- `[VERIFICADO]` aviso não bloqueante mantido: bundle Vite acima de 500 kB.
