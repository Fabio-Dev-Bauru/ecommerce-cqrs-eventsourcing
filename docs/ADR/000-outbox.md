# ADR 000: Uso do Padrão Outbox

## Contexto

No desenvolvimento de sistemas distribuídos, garantir a consistência entre o armazenamento de eventos e a publicação de mensagens é um desafio. O padrão Outbox é uma solução que permite a gravação de eventos e mensagens em uma única transação, garantindo que ambos sejam processados de forma consistente.

## Decisão

Implementar o padrão Outbox para garantir que eventos e mensagens sejam gravados e publicados de forma atômica. Isso será feito utilizando uma tabela de outbox no banco de dados PostgreSQL, onde eventos serão armazenados antes de serem publicados no Kafka.

## Consequências

- **Positivas:**
  - Consistência garantida entre o armazenamento de eventos e a publicação de mensagens.
  - Redução de complexidade na gestão de falhas de publicação.

- **Negativas:**
  - Aumento da complexidade no gerenciamento da tabela de outbox.
  - Necessidade de implementar um mecanismo de polling ou Debezium para processar a tabela de outbox.

## Alternativas Consideradas

- **Transações Distribuídas:**
  - **Prós:** Consistência forte entre serviços.
  - **Contras:** Complexidade e overhead significativos.

- **Publicação Direta:**
  - **Prós:** Simplicidade na implementação.
  - **Contras:** Risco de inconsistência entre eventos e mensagens em caso de falha.
