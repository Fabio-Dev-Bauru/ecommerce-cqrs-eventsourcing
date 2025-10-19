# Guia de Monitoramento e Observabilidade

## Visão Geral

O sistema possui observabilidade completa com três pilares:

### 📊 Métricas (Prometheus + Grafana)
- Request rate, latency, errors
- JVM metrics (heap, threads, GC)
- Saga metrics (status, duration)
- Kafka consumer lag
- Cache hit rate

### 📝 Logs (ELK Stack)
- Logs estruturados em JSON
- Correlation ID para rastreamento
- Níveis configuráveis por ambiente
- Pesquisa e agregação no Kibana

### 🔍 Tracing (Zipkin)
- Distributed tracing end-to-end
- Latência por span
- Dependências entre serviços
- Debug de problemas de performance

## Métricas Importantes

### HTTP Metrics

```promql
# Request rate por serviço
sum(rate(http_server_requests_seconds_count[1m])) by (application)

# Latência p95
histogram_quantile(0.95, 
  sum(rate(http_server_requests_seconds_bucket[5m])) by (application, le)
)

# Taxa de erro (5xx)
sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) 
  / 
sum(rate(http_server_requests_seconds_count[1m]))
```

### JVM Metrics

```promql
# Heap usage
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}

# GC time
rate(jvm_gc_pause_seconds_sum[1m])

# Threads
jvm_threads_live_threads
```

### Kafka Metrics

```promql
# Consumer lag
sum(kafka_consumer_fetch_manager_records_lag) by (topic)

# Messages consumed per second
rate(kafka_consumer_fetch_manager_records_consumed_total[1m])
```

### Saga Metrics

```promql
# Sagas completadas
increase(saga_completed_total[1h])

# Taxa de falha
saga_failed_total / saga_started_total

# Duração média
avg(saga_duration_seconds)
```

### Cache Metrics

```promql
# Hit rate
cache_gets_total{result="hit"} / cache_gets_total

# Evictions
rate(cache_evictions_total[5m])
```

## Dashboards Grafana

### 1. E-commerce Overview
- Visão geral do sistema
- Total de pedidos
- Request rate e latency
- Taxa de erro
- Status das sagas

### 2. Saga Monitoring
- Sagas ativas, completadas, falhadas
- Taxa de sucesso
- Duração (p50, p95, p99)
- Distribuição por step

### 3. Kafka Monitoring
- Consumer lag por tópico
- Messages processed/sec
- Offset commit rate
- Rebalances

### 4. JVM Monitoring
- Heap usage
- GC activity
- Thread count
- CPU usage

## Alertas Recomendados

### Críticos (PagerDuty)

```yaml
- alert: HighErrorRate
  expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
  for: 2m
  annotations:
    summary: "Taxa de erro alta: {{ $value }}%"

- alert: SagaFailureRate
  expr: saga_failed_total / saga_started_total > 0.1
  for: 5m
  annotations:
    summary: "Taxa de falha de sagas > 10%"

- alert: KafkaConsumerLag
  expr: kafka_consumer_fetch_manager_records_lag > 10000
  for: 5m
  annotations:
    summary: "Consumer lag muito alto"
```

### Warnings (Slack)

```yaml
- alert: HighLatency
  expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
  for: 5m
  annotations:
    summary: "Latência p95 > 2s"

- alert: HighHeapUsage
  expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.85
  for: 10m
  annotations:
    summary: "Heap usage > 85%"
```

## Logs

### Kibana Queries Úteis

```
# Buscar por correlationId
correlationId: "550e8400-e29b-41d4-a716-446655440000"

# Errors nas últimas 24h
level: ERROR AND @timestamp: [now-24h TO now]

# Sagas falhadas
message: "Saga failed" AND level: ERROR

# Slow queries (> 1s)
duration: >1000 AND logger_name: *Repository
```

### Log Patterns

Todos os logs incluem:
- `timestamp`: ISO-8601
- `level`: DEBUG, INFO, WARN, ERROR
- `logger`: Nome da classe
- `message`: Mensagem
- `correlationId`: Para rastreamento
- `application`: Nome do serviço

## Distributed Tracing

### Zipkin UI

**URL:** http://localhost:9411

### Buscar Traces

1. Por service name: `order-command-service`
2. Por span name: `POST /api/v1/orders`
3. Por tags: `http.status_code=500`
4. Por duração: `> 1000ms`

### Traces Importantes

- **Order Creation**: Command → Event Store → Outbox → Saga
- **Saga Execution**: Saga → Payment → Inventory → Shipping
- **Query Execution**: Query Service → PostgreSQL → Redis

### Análise de Performance

```
Trace: Create Order
├─ HTTP POST /api/v1/orders (150ms)
│  ├─ OrderService.createOrder (120ms)
│  │  ├─ Validate request (5ms)
│  │  ├─ Create aggregate (10ms)
│  │  ├─ Save to Event Store (50ms)
│  │  └─ Save to Outbox (50ms)
│  └─ Response serialization (5ms)
└─ Kafka publish (25ms)
```

## Health Checks

### Endpoints

```bash
# Command Service
curl http://localhost:8080/actuator/health

# Query Service
curl http://localhost:8081/actuator/health

# Saga Orchestrator
curl http://localhost:8082/actuator/health
```

### Response

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" },
    "kafka": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

## Métricas Customizadas

### Exemplo: Order Metrics

```java
@Component
public class OrderMetrics {
    private final Counter ordersCreated;
    private final Timer orderCreationTime;
    
    public OrderMetrics(MeterRegistry registry) {
        this.ordersCreated = Counter.builder("orders.created")
            .tag("type", "order")
            .register(registry);
            
        this.orderCreationTime = Timer.builder("orders.creation.time")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry);
    }
}
```

## Runbooks

### High Error Rate

1. Verificar logs no Kibana
2. Identificar stack traces
3. Verificar traces no Zipkin
4. Correlacionar com deploys recentes
5. Rollback se necessário

### Saga Failures

1. Consultar sagas falhadas: `GET /api/v1/sagas?status=FAILED`
2. Verificar errorMessage
3. Analisar logs com correlationId
4. Verificar serviços downstream
5. Retry manual se necessário

### Kafka Consumer Lag

1. Identificar tópico problemático
2. Verificar se consumer está ativo
3. Aumentar paralelismo (partitions/threads)
4. Escalar consumers se necessário

## Capacity Planning

### Métricas para Monitorar

- CPU usage
- Memory usage
- Disk I/O
- Network I/O
- Database connections
- Thread pool usage

### Thresholds

| Métrica | Warning | Critical |
|---------|---------|----------|
| CPU | > 70% | > 85% |
| Memory | > 80% | > 90% |
| Kafka Lag | > 1000 | > 10000 |
| Response Time p95 | > 1s | > 3s |
| Error Rate | > 1% | > 5% |

## Ferramentas

| Ferramenta | URL | Uso |
|------------|-----|-----|
| Grafana | http://localhost:3000 | Dashboards e alertas |
| Prometheus | http://localhost:9090 | Query de métricas |
| Kibana | http://localhost:5601 | Análise de logs |
| Zipkin | http://localhost:9411 | Distributed tracing |
| Kafka UI | http://localhost:8090 | Monitoramento Kafka |

