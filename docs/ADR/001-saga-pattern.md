# ADR 001: Saga Pattern - Orchestration vs Choreography

## Contexto

Em arquiteturas de microserviços com transações distribuídas, precisamos garantir consistência eventual entre múltiplos serviços. Para o processo de criação de pedido, envolvemos:
- **Payment Service**: Autorização de pagamento
- **Inventory Service**: Reserva de estoque
- **Shipping Service**: Agendamento de envio

Caso algum desses passos falhe, precisamos compensar (reverter) as ações já realizadas.

## Decisão

Implementamos o **Saga Pattern com Orchestration** ao invés de Choreography.

### Saga Orchestration

Um serviço central (Saga Orchestrator) coordena todos os passos da transação distribuída:

```
Order Created → Saga Orchestrator → Payment Command
                     ↓
                Payment Success
                     ↓
              Inventory Command
                     ↓
              Inventory Reserved
                     ↓
              Shipping Command
                     ↓
              Shipping Scheduled
                     ↓
              Order Confirmed
```

### Fluxo de Compensação

Em caso de falha em qualquer etapa:

```
Shipping Failed → Compensate Inventory → Compensate Payment → Order Cancelled
```

## Vantagens da Orchestration

### Positivas
1. **Centralização**: Lógica de coordenação em um único lugar
2. **Visibilidade**: Estado da saga facilmente consultável
3. **Debugging**: Mais fácil rastrear e debugar problemas
4. **Timeout Handling**: Controle centralizado de timeouts
5. **Retry Logic**: Fácil implementar retry com backoff
6. **Compensação**: Ordem de compensação controlada
7. **Evolução**: Adicionar/remover passos é mais simples

### Negativas
1. **Acoplamento**: Orchestrator conhece todos os serviços
2. **Single Point of Failure**: Se o orchestrator cair, sagas param
3. **Performance**: Latência adicional na coordenação

## Alternativa: Choreography

Cada serviço publica eventos e outros serviços reagem:

```
Order Created Event → Payment Service
Payment Success Event → Inventory Service
Inventory Reserved Event → Shipping Service
```

### Por que NÃO escolhemos?

1. **Complexidade**: Lógica distribuída entre múltiplos serviços
2. **Debugging**: Difícil rastrear fluxo completo
3. **Compensação**: Mais complexa de implementar
4. **Dependência Cíclica**: Serviços precisam conhecer outros serviços
5. **Evolução**: Mudanças impactam múltiplos serviços

## Implementação

### Componentes

1. **SagaInstance**: Entidade que persiste estado da saga
2. **OrderSagaOrchestrator**: Coordena os passos
3. **SagaTimeoutScheduler**: Detecta timeouts e executa compensações
4. **Kafka Topics**:
   - `order-events`: Eventos de pedidos
   - `payment-commands`, `payment-events`
   - `inventory-commands`, `inventory-events`
   - `shipping-commands`, `shipping-events`

### Estados da Saga

```
STARTED → PAYMENT_PENDING → PAYMENT_AUTHORIZED → 
INVENTORY_PENDING → INVENTORY_RESERVED → 
SHIPPING_PENDING → SHIPPING_SCHEDULED → COMPLETED

Se falha em qualquer ponto:
COMPENSATING → COMPENSATION_COMPLETED
```

### Garantias

- **Atomicidade**: Cada passo é atômico
- **Consistência Eventual**: Sistema converge para estado consistente
- **Idempotência**: Comandos podem ser reprocessados
- **Durabilidade**: Estado persistido no PostgreSQL

## Consequências

### Operacionais
- Necessidade de monitorar o Saga Orchestrator
- Configurar alertas para sagas em timeout
- Dashboard para visualizar estado das sagas
- Logs estruturados com correlationId

### Desenvolvimento
- Testes devem simular falhas em cada etapa
- Compensações devem ser testadas
- Idempotência deve ser garantida em todos os handlers

## Referências

- [Microservices Patterns - Chris Richardson](https://microservices.io/patterns/data/saga.html)
- [Event-Driven Microservices - Adam Bellemare]
- [Building Event-Driven Microservices - O'Reilly]

