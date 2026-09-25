# language: pt
Funcionalidade: Tabela de transição de status do Kanban
  Como API de projetos
  Quero aplicar as doze linhas da tabela do desafio
  Para manter ações automáticas, bloqueios e confirmações coerentes

  Esquema do Cenário: linha <linha> — <origem> para <destino>
    Dado um projeto no cenário "<cenario>" classificado como "<origem>"
    Quando solicito a transição para "<destino>" com confirmação "<confirmacao>"
    Então o resultado da transição é "<resultado>"
    E as métricas depois da transição são atraso "<atraso>" e percentual restante "<percentual>"

    # Hoje = 2026-09-24. Cenários (previsto início/término, realizado início/término):
    #   not_started_future  25/09–04/10, sem realizados                (A iniciar)
    #   in_progress_future  24/09–04/10, início realizado 24/09        (Em andamento)
    #   overdue_not_started 19/09–23/09, sem realizados                (Atrasado, 1 dia)
    #   completed_future    22/09–02/10, realizados 22/09 e 24/09      (Concluído)
    #   completed_overdue   14/09–23/09, realizados 14/09 e 24/09      (Concluído)
    # Nas linhas bloqueadas, "-" significa que nenhuma métrica nova foi calculada: o projeto não muda.
    Exemplos:
      | linha | cenario             | origem      | destino      | confirmacao | resultado    | atraso | percentual |
      | 01    | not_started_future  | NOT_STARTED | IN_PROGRESS  | false       | IN_PROGRESS  | 0      | 100        |
      | 02    | not_started_future  | NOT_STARTED | OVERDUE      | false       | BLOCKED      | -      | -          |
      | 03    | not_started_future  | NOT_STARTED | COMPLETED    | false       | COMPLETED    | 0      | 0          |
      | 04    | in_progress_future  | IN_PROGRESS | NOT_STARTED  | true        | NOT_STARTED  | 0      | 100        |
      | 05    | in_progress_future  | IN_PROGRESS | OVERDUE      | false       | BLOCKED      | -      | -          |
      | 06    | in_progress_future  | IN_PROGRESS | COMPLETED    | false       | COMPLETED    | 0      | 0          |
      | 07    | overdue_not_started | OVERDUE     | NOT_STARTED  | false       | BLOCKED      | -      | -          |
      | 08    | overdue_not_started | OVERDUE     | IN_PROGRESS  | false       | BLOCKED      | -      | -          |
      | 09    | overdue_not_started | OVERDUE     | COMPLETED    | false       | COMPLETED    | 0      | 0          |
      | 10    | completed_future    | COMPLETED   | NOT_STARTED  | false       | BLOCKED      | -      | -          |
      | 11    | completed_future    | COMPLETED   | IN_PROGRESS  | true        | IN_PROGRESS  | 0      | 80         |
      | 12    | completed_overdue   | COMPLETED   | OVERDUE      | true        | OVERDUE      | 1      | 0          |
