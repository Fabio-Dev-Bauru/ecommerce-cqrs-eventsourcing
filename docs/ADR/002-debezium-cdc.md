# ADR 002: Debezium CDC para Outbox Pattern

## Contexto

Implementamos o Outbox Pattern para garantir atomicidade entre persistência de eventos e publicação no Kafka. Precisamos de um mecanismo confiável para ler a tabela `outbox` e publicar os eventos no Kafka.

## Decisão

Utilizar **Debezium CDC (Change Data Capture)** com PostgreSQL para implementar o Outbox Relay.

## Como Funciona

### Arquitetura

```
PostgreSQL (outbox table)
    ↓ (WAL - Write-Ahead Log)
Debezium Connector (Kafka Connect)
    ↓ (EventRouter SMT)
Kafka Topics (order-events)
    ↓
Consumers (Saga, Query Service, etc)
```

### Fluxo Detalhado

1. **Aplicação escreve na tabela outbox** (mesma transação do Event Store)
2. **PostgreSQL WAL** registra a mudança
3. **Debezium** lê o WAL via replication slot
4. **EventRouter SMT** transforma o registro em evento
5. **Kafka** recebe o evento no tópico correto
6. **Consumers** processam o evento

### Configuração do Connector

```json
{
  "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
  "table.include.list": "public.outbox",
  "plugin.name": "pgoutput",
  "transforms": "outbox",
  "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter"
}
```

### EventRouter SMT (Single Message Transform)

Transforma registros da tabela outbox em eventos Kafka:

| Campo Outbox | Destino Kafka |
|--------------|---------------|
| `aggregate_id` | Message Key |
| `event_type` | Header eventType / Topic routing |
| `event_data` | Message Payload |
| `created_at` | Timestamp |

## Vantagens

### Técnicas

1. **Zero Application Code**: Não precisa de polling na aplicação
2. **Performance**: CDC é mais eficiente que polling
3. **Latência Baixa**: Eventos publicados em ~ms após commit
4. **Ordering**: Garante ordem dos eventos por aggregate_id
5. **Exactly-Once**: Com transações Kafka
6. **No Duplicates**: Debezium garante entrega única

### Operacionais

1. **Separação de Responsabilidades**: App não se preocupa com publicação
2. **Resiliência**: Debezium gerencia offset e retry
3. **Monitoramento**: Kafka Connect tem métricas prontas
4. **Scaling**: Pode escalar Kafka Connect independentemente

## Desvantagens

1. **Complexidade Adicional**: Precisa gerenciar Kafka Connect
2. **Dependência Externa**: Debezium como componente crítico
3. **Debugging**: Mais difícil debugar problemas de CDC
4. **PostgreSQL WAL**: Precisa configurar replication
5. **Storage**: WAL pode crescer se conector ficar down

## Alternativas Consideradas

### 1. Polling com Scheduler

**Prós:**
- Simples de implementar
- Sem dependências externas

**Contras:**
- Performance ruim
- Polling interval vs latência tradeoff
- Mais código para manter
- Pode perder eventos em caso de falha

### 2. Listen/Notify do PostgreSQL

**Prós:**
- Notificação em tempo real
- Sem polling

**Contras:**
- Não garante entrega
- Não funciona com múltiplas instâncias
- Mensagens não são persistentes

### 3. Triggers no PostgreSQL

**Prós:**
- Automático

**Contras:**
- Lógica de negócio no banco
- Difícil de testar
- Performance impact

## Implementação

### Pré-requisitos no PostgreSQL

```sql
-- Habilitar replicação lógica
ALTER SYSTEM SET wal_level = 'logical';
ALTER SYSTEM SET max_replication_slots = 4;
ALTER SYSTEM SET max_wal_senders = 4;

-- Reiniciar PostgreSQL
```

### Deploy do Connector

```bash
# Via REST API
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @register-connector.json

# Via script
./infra/debezium/setup-debezium.sh
```

### Verificação

```bash
# Status do connector
curl http://localhost:8083/connectors/outbox-connector/status

# Topics criados
kafka-topics.sh --list --bootstrap-server localhost:9092

# Consumir eventos
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic order-events --from-beginning
```

## Monitoramento

### Métricas Importantes

- `debezium.connector.outbox.TotalNumberOfEventsSeen`
- `debezium.connector.outbox.NumberOfEventsFiltered`
- `kafka.connect.task.status`
- `kafka.connect.task.offset.commit.completion.rate`

### Health Checks

```bash
# Connector health
curl http://localhost:8083/connectors/outbox-connector/status

# WAL slot
SELECT * FROM pg_replication_slots WHERE slot_name = 'debezium_outbox_slot';

# Lag
SELECT pg_wal_lsn_diff(pg_current_wal_lsn(), restart_lsn) 
FROM pg_replication_slots WHERE slot_name = 'debezium_outbox_slot';
```

## Considerações de Segurança

1. **Credentials**: Usar secrets management (Vault, AWS Secrets Manager)
2. **Network**: Kafka Connect deve ter acesso ao PostgreSQL
3. **SSL/TLS**: Configurar conexões encriptadas em produção
4. **RBAC**: Usuário do Debezium com permissões mínimas

```sql
-- Criar usuário específico para Debezium
CREATE USER debezium WITH REPLICATION LOGIN PASSWORD 'secure_password';
GRANT SELECT ON TABLE outbox TO debezium;
GRANT USAGE ON SCHEMA public TO debezium;
```

## Disaster Recovery

### Cenário: Connector Down

1. **WAL acumula** no PostgreSQL
2. **Ao religar**, Debezium retoma do último offset
3. **Eventos são publicados** na ordem correta
4. **Sem perda de dados**

### Cenário: PostgreSQL Crash

1. **Após recovery**, WAL é replicado
2. **Debezium detecta** e retoma
3. **Eventos republicados** se necessário (idempotência)

## Evolução Futura

1. **Multi-table Outbox**: Suportar múltiplas tabelas
2. **Schema Evolution**: Versionamento com Schema Registry
3. **Compaction**: Limpar outbox de eventos antigos
4. **Multi-tenancy**: Filtros por tenant

## Referências

- [Debezium Documentation](https://debezium.io/documentation/)
- [Outbox Pattern - Debezium](https://debezium.io/documentation/reference/transformations/outbox-event-router.html)
- [PostgreSQL Logical Replication](https://www.postgresql.org/docs/current/logical-replication.html)

