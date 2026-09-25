# F5-C2 — FONTES RAG

Data: 2026-09-24

1. PDF do desafio:
   - Etapa 2: "Testes cobrindo: casos felizes, bloqueios (erros), confirmações obrigatórias e cálculo de status/métricas — linha a linha da tabela de transição";
   - Requisitos não funcionais: "Segurança: validação de inputs".
2. `ADERENCIA-FINAL.md`: lacunas 4 (métricas por linha) e 5 (sem limite de tamanho).
3. `docs/governance/PLANO-CORRECAO-RELEASE-2.0.0.md`: lote F5-C2 e limites aprovados.
4. Hibernate Validator 8.0.2.Final (GitHub): `ValidationMessages_pt.properties`: `Size.message = tamanho deve ser entre {min} e {max}`. O `_pt_BR` não redefine essa mensagem.
5. Spring Boot 3.5.16 (GitHub): imports do `@AutoConfigureGraphQl` incluem `ValidationAutoConfiguration`, então o `@GraphQlTest` valida os `@Argument @Valid`.
6. Execução do domínio real (harness javac 21) para os valores esperados de atraso e percentual de cada linha.
