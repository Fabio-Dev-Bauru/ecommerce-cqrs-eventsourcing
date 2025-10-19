# Modelo de Eventos

Este documento descreve os eventos utilizados no sistema de e-commerce baseado em Event Sourcing e CQRS.

## Eventos

### OrderCreated
- **Descrição:** Evento disparado quando um pedido é criado.
- **Atributos:**
  - `orderId`: Identificador único do pedido.
  - `customerId`: Identificador do cliente.
  - `orderItems`: Lista de itens do pedido.
  - `totalAmount`: Valor total do pedido.
  - `timestamp`: Data e hora da criação do pedido.
  - `correlationId`: Identificador de correlação para rastreamento.
  - `causationId`: Identificador de causa para rastreamento.
  - `version`: Versão do evento.

### InventoryReserved
- **Descrição:** Evento disparado quando o inventário é reservado para um pedido.
- **Atributos:**
  - `orderId`: Identificador único do pedido.
  - `reservedItems`: Lista de itens reservados.
  - `timestamp`: Data e hora da reserva.
  - `correlationId`: Identificador de correlação para rastreamento.
  - `causationId`: Identificador de causa para rastreamento.
  - `version`: Versão do evento.

### PaymentAuthorized
- **Descrição:** Evento disparado quando o pagamento é autorizado para um pedido.
- **Atributos:**
  - `orderId`: Identificador único do pedido.
  - `paymentId`: Identificador do pagamento.
  - `amount`: Valor autorizado.
  - `timestamp`: Data e hora da autorização.
  - `correlationId`: Identificador de correlação para rastreamento.
  - `causationId`: Identificador de causa para rastreamento.
  - `version`: Versão do evento.

### OrderConfirmed
- **Descrição:** Evento disparado quando um pedido é confirmado.
- **Atributos:**
  - `orderId`: Identificador único do pedido.
  - `timestamp`: Data e hora da confirmação.
  - `correlationId`: Identificador de correlação para rastreamento.
  - `causationId`: Identificador de causa para rastreamento.
  - `version`: Versão do evento.

### OrderCancelled
- **Descrição:** Evento disparado quando um pedido é cancelado.
- **Atributos:**
  - `orderId`: Identificador único do pedido.
  - `reason`: Motivo do cancelamento.
  - `timestamp`: Data e hora do cancelamento.
  - `correlationId`: Identificador de correlação para rastreamento.
  - `causationId`: Identificador de causa para rastreamento.
  - `version`: Versão do evento.
