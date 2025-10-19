# Fluxo da Saga de Pedido

Este documento descreve o fluxo completo da saga de criação de pedido.

## Fluxo Normal (Happy Path)

```
1. Order Command Service
   ↓ OrderCreatedEvent (Kafka: order-events)
   
2. Saga Orchestrator
   - Cria SagaInstance (status: STARTED)
   - Envia ProcessPaymentCommand (Kafka: payment-commands)
   
3. Payment Service
   - Processa pagamento
   - Publica PaymentProcessedEvent (status: SUCCESS)
   
4. Saga Orchestrator
   - Atualiza saga (status: PAYMENT_AUTHORIZED)
   - Envia ReserveInventoryCommand (Kafka: inventory-commands)
   
5. Inventory Service
   - Reserva itens
   - Publica InventoryReservedEvent (status: SUCCESS)
   
6. Saga Orchestrator
   - Atualiza saga (status: INVENTORY_RESERVED)
   - Envia ScheduleShippingCommand (Kafka: shipping-commands)
   
7. Shipping Service
   - Agenda envio
   - Publica ShippingScheduledEvent (status: SUCCESS)
   
8. Saga Orchestrator
   - Atualiza saga (status: COMPLETED)
   - Publica OrderConfirmedEvent (Kafka: order-events)
   
9. Order Query Service
   - Atualiza projeção (status: CONFIRMED)
```

## Fluxo de Compensação (Unhappy Path)

### Cenário 1: Falha no Payment

```
1. OrderCreatedEvent
2. ProcessPaymentCommand
3. PaymentProcessedEvent (status: FAILED)
4. Saga → FAILED
5. OrderCancelledEvent
```

### Cenário 2: Falha no Inventory

```
1-3. Payment OK
4. ReserveInventoryCommand
5. InventoryReservedEvent (status: FAILED)
6. Saga → COMPENSATING
7. Compensate Payment (estorno)
8. Saga → COMPENSATION_COMPLETED
9. OrderCancelledEvent
```

### Cenário 3: Falha no Shipping

```
1-5. Payment + Inventory OK
6. ScheduleShippingCommand
7. ShippingScheduledEvent (status: FAILED)
8. Saga → COMPENSATING
9. Compensate Inventory (liberar reserva)
10. Compensate Payment (estorno)
11. Saga → COMPENSATION_COMPLETED
12. OrderCancelledEvent
```

## Timeouts

Se uma saga não completar em **15 minutos**:

```
SagaTimeoutScheduler (a cada 1 min)
  ↓
Detecta sagas antigas em estado pendente
  ↓
Marca como FAILED
  ↓
Trigger compensação (se necessário)
```

## Retry Strategy

Sagas falhadas são automaticamente retriadas:

- **Max Retries**: 3 tentativas
- **Backoff**: 1 segundo inicial
- **Scheduler**: Verifica a cada 2 minutos

## Idempotência

Todos os handlers devem ser idempotentes para suportar reprocessamento:

- **Payment Service**: Verifica se paymentId já existe
- **Inventory Service**: Verifica se reservationId já existe
- **Shipping Service**: Verifica se trackingNumber já existe

## Monitoramento

### Métricas Importantes

- `saga.started.total`: Total de sagas iniciadas
- `saga.completed.total`: Total de sagas completadas
- `saga.failed.total`: Total de sagas falhadas
- `saga.compensated.total`: Total de compensações
- `saga.duration.seconds`: Duração das sagas
- `saga.timeout.total`: Total de timeouts

### Alertas Recomendados

1. Taxa de falha > 5%
2. Duração média > 10 minutos
3. Sagas em COMPENSATING por > 5 minutos
4. Queue lag > 1000 mensagens

## Consultas Úteis

```sql
-- Sagas em andamento
SELECT * FROM saga_instance 
WHERE status NOT IN ('COMPLETED', 'FAILED', 'COMPENSATION_COMPLETED');

-- Sagas falhadas nas últimas 24h
SELECT * FROM saga_instance 
WHERE status = 'FAILED' 
  AND created_at > NOW() - INTERVAL '24 hours';

-- Taxa de sucesso
SELECT 
  status,
  COUNT(*) as total,
  ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM saga_instance
GROUP BY status;
```

## Tópicos Kafka

| Tópico | Tipo | Descrição |
|--------|------|-----------|
| `order-events` | Event | Eventos de pedido (Created, Confirmed, Cancelled) |
| `payment-commands` | Command | Comandos de pagamento |
| `payment-events` | Event | Respostas do Payment Service |
| `inventory-commands` | Command | Comandos de inventário |
| `inventory-events` | Event | Respostas do Inventory Service |
| `shipping-commands` | Command | Comandos de envio |
| `shipping-events` | Event | Respostas do Shipping Service |

