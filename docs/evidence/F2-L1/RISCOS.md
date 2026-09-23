# F2-L1 — RISCOS E LACUNAS

Data: 2026-09-22

- `[DESCONHECIDO]` A compilação exata Spring Security 6.5.11/Java 25 e o comportamento runtime aguardam o gate local; este ambiente não possui Maven/Docker.
- `[VERIFICADO]` `SESSION_COOKIE_SECURE=false` é apenas default local. Em implantação HTTPS deve ser configurado como `true`; o lote não inventa infraestrutura TLS inexistente.
- `[VERIFICADO]` O bootstrap não rotaciona senha de um administrador já existente; mudança de credencial após criação requer fluxo explícito posterior, fora do F2-L1.
- `[VERIFICADO]` F2-L1 possui somente papel `ADMIN`; gestão/autenticação de responsáveis é diferencial posterior e não foi antecipada.
- `[VERIFICADO]` O frontend ainda não implementa login/CSRF; isso pertence ao F2-L2. Health continua público, portanto a regressão frontend atual permanece testável.
- `[VERIFICADO]` O warning conhecido de auto-attach Mockito permanece pendência técnica não bloqueante da baseline.
