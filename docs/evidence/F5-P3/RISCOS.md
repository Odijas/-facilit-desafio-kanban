# F5-P3 — RISCOS E LACUNAS

Data: 2026-09-25

- `[HIPÓTESE]` O freeze da 2.0.0, adaptado, roda igual na 2.0.1. As partes novas foram ensaiadas aqui (ver `EXECUCAO.md`); Maven, Docker, Grafana, Prometheus e CI são reais só na máquina do usuário.
- `[HIPÓTESE]` O back-merge na `develop` sai sem conflito. O gate do F5-P2 não achou arquivo em comum. Se houver conflito, o roteiro manda abortar sem tocar na `main` e na tag já publicadas.
- `[VERIFICADO]` O `#9` (histórico) continua Parcial e declarado, por decisão do usuário. A nota esperada dos obrigatórios é 39,5/40 = 98,75%, a confirmar na auditoria final.
- `[DESCONHECIDO]` Conferência visual do Swagger UI: não relatada (ver `DECISOES.md`).
