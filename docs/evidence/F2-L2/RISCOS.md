# F2-L2 — RISCOS E LACUNAS

Data: 2026-09-22

- `[EXECUTADO PELO USUÁRIO]` o primeiro gate F2-L2 ficou RED por formatação, tipagem MUI v9 e isolamento de testes; estes pontos foram corrigidos no candidato revisado. O GREEN depende da reexecução completa.
- `[DESCONHECIDO]` o fluxo real navegador → Vite proxy → Spring Security depende do gate Docker local.
- `[VERIFICADO]` o roteamento nativo cobre somente `/login` e `/`; expansão de rotas deve ser reavaliada no F2-L3, sem antecipar biblioteca.
- `[VERIFICADO]` o frontend Docker ainda executa o Vite dev server; produção estática permanece fora deste lote e deve ser tratada no fechamento final se exigida.
- `[VERIFICADO]` o warning conhecido de auto-attach Mockito continua como pendência técnica não bloqueante.
