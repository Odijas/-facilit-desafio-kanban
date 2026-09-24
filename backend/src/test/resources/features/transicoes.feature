# language: pt
Funcionalidade: Tabela de transição de status do Kanban
  Como API de projetos
  Quero aplicar as doze linhas da tabela do desafio
  Para manter ações automáticas, bloqueios e confirmações coerentes

  Esquema do Cenário: linha <linha> — <origem> para <destino>
    Dado um projeto no cenário "<cenario>" classificado como "<origem>"
    Quando solicito a transição para "<destino>" com confirmação "<confirmacao>"
    Então o resultado da transição é "<resultado>"

    Exemplos:
      | linha | cenario             | origem      | destino      | confirmacao | resultado    |
      | 01    | not_started_future  | NOT_STARTED | IN_PROGRESS  | false       | IN_PROGRESS  |
      | 02    | not_started_future  | NOT_STARTED | OVERDUE      | false       | BLOCKED      |
      | 03    | not_started_future  | NOT_STARTED | COMPLETED    | false       | COMPLETED    |
      | 04    | in_progress_future  | IN_PROGRESS | NOT_STARTED  | true        | NOT_STARTED  |
      | 05    | in_progress_future  | IN_PROGRESS | OVERDUE      | false       | BLOCKED      |
      | 06    | in_progress_future  | IN_PROGRESS | COMPLETED    | false       | COMPLETED    |
      | 07    | overdue_not_started | OVERDUE     | NOT_STARTED  | false       | BLOCKED      |
      | 08    | overdue_not_started | OVERDUE     | IN_PROGRESS  | false       | BLOCKED      |
      | 09    | overdue_not_started | OVERDUE     | COMPLETED    | false       | COMPLETED    |
      | 10    | completed_future    | COMPLETED   | NOT_STARTED  | false       | BLOCKED      |
      | 11    | completed_future    | COMPLETED   | IN_PROGRESS  | true        | IN_PROGRESS  |
      | 12    | completed_overdue   | COMPLETED   | OVERDUE      | true        | OVERDUE      |
