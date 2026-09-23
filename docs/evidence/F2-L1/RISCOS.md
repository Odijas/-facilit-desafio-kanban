# F2-L1 — RISCOS E LACUNAS

Data: 2026-09-22

- `[EXECUTADO PELO USUÁRIO · 2026-09-22]` compilação/runtime do F2-L1 foram validados pelo gate local com exit code 0.
- `[VERIFICADO]` `SESSION_COOKIE_SECURE=false` é apenas default local. Em implantação HTTPS deve ser configurado como `true`; o lote não inventa infraestrutura TLS inexistente.
- `[VERIFICADO]` O bootstrap não rotaciona senha de um administrador já existente; mudança de credencial após criação requer fluxo explícito posterior, fora do F2-L1.
- `[VERIFICADO]` F2-L1 possui somente papel `ADMIN`; gestão/autenticação de responsáveis é diferencial posterior e não foi antecipada.
- `[VERIFICADO]` O frontend autenticado pertence ao F2-L2 e não fazia parte do escopo do F2-L1.
- `[VERIFICADO]` O warning conhecido de auto-attach Mockito permanece pendência técnica não bloqueante da baseline.
