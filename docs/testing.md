# Guia de Testes

## Estratégia de Testes

O projeto implementa múltiplas camadas de testes para garantir qualidade e confiabilidade:

### Pirâmide de Testes

```
           /\
          /  \  E2E Tests (Poucos)
         /____\
        /      \  Integration Tests (Moderados)
       /________\
      /          \  Unit Tests (Muitos)
     /__________\
```

## Tipos de Testes

### 1. Testes Unitários

**Objetivo**: Testar componentes isolados

**Exemplos Implementados:**
- `OrderTest`: Agregado Order e lógica de negócio
- `MoneyTest`: Value Object Money e operações
- `OrderServiceIntegrationTest`: Service com repositórios reais

**Executar:**
```bash
mvn test -pl order-command-service
```

**Cobertura:**
```bash
mvn test jacoco:report
# Relatório em: target/site/jacoco/index.html
```

### 2. Testes de Integração

**Objetivo**: Testar integração entre componentes

**Cobertura:**
- Controllers com MockMvc
- Repositories com banco H2
- Consumers Kafka com EmbeddedKafka
- Cache Redis com Testcontainers

**Exemplo: OrderServiceIntegrationTest**
```java
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceIntegrationTest {
    // Testa persistência em Event Store + Outbox atomicamente
}
```

### 3. Testes de Contrato (Contract Tests)

**Objetivo**: Garantir compatibilidade entre serviços

**Frameworks:**
- Spring Cloud Contract
- Pact

**Exemplo:**
```java
// Producer: order-command-service
@AutoConfigureMessageVerifier
class OrderEventContractTest {
    // Define contrato do evento OrderCreatedEvent
}

// Consumer: saga-orchestrator
@PactTest
class OrderEventConsumerPactTest {
    // Verifica que consome contrato corretamente
}
```

### 4. Testes End-to-End

**Objetivo**: Testar fluxo completo do sistema

**Cenários:**
1. Happy Path: Order → Payment → Inventory → Shipping → Confirmed
2. Payment Failure → Order Cancelled
3. Inventory Failure → Compensation → Order Cancelled
4. Timeout → Saga Compensation

### 5. Testes de Carga

**Objetivo**: Avaliar performance sob carga

**Ferramentas:**
- Gatling
- JMeter
- K6

**Cenário Exemplo:**
```scala
// Gatling scenario
scenario("Create Orders")
  .exec(
    http("Create Order")
      .post("/api/v1/orders")
      .header("Authorization", "Bearer ${token}")
      .body(StringBody(orderJson))
  )
  .inject(
    rampUsersPerSec(1) to 100 during (1 minute),
    constantUsersPerSec(100) during (5 minutes)
  )
```

## Executar Testes

### Todos os Testes

```bash
# Todos os módulos
mvn test

# Módulo específico
mvn test -pl order-command-service

# Com cobertura
mvn test jacoco:report
```

### Apenas Testes Unitários

```bash
mvn test -Dgroups=unit
```

### Apenas Testes de Integração

```bash
mvn test -Dgroups=integration
```

### Testes Paralelos (Mais rápido)

```bash
mvn test -T 4  # 4 threads
```

## Cobertura de Código

### Meta de Cobertura

| Camada | Meta |
|--------|------|
| Domain (Aggregates, VOs) | > 90% |
| Service | > 80% |
| Controller | > 70% |
| Repository | > 60% |
| Geral | > 75% |

### Verificar Cobertura

```bash
mvn test jacoco:report
open order-command-service/target/site/jacoco/index.html
```

### CI/CD Gate

```bash
mvn verify jacoco:check
```

## Testes Implementados

### order-command-service

| Classe | Tipo | Descrição |
|--------|------|-----------|
| `OrderTest` | Unit | Agregado Order e Event Sourcing |
| `MoneyTest` | Unit | Value Object Money |
| `OrderServiceIntegrationTest` | Integration | Service + Repositories |
| `OrderControllerTest` | Integration | Controller + Security |
| `OrderCommandServiceApplicationTests` | Integration | Context loading |

### order-query-service

| Classe | Tipo | Descrição |
|--------|------|-----------|
| `OrderProjectionServiceTest` | Integration | Projection service + Cache |
| `OrderQueryServiceApplicationTests` | Integration | Context loading |

### saga-orchestrator

| Classe | Tipo | Descrição |
|--------|------|-----------|
| `OrderSagaOrchestratorTest` | Integration | Saga orchestration logic |
| `SagaOrchestratorApplicationTests` | Integration | Context loading |

## Mocks e Stubs

### MockBean vs SpyBean

```java
// MockBean - comportamento completamente controlado
@MockBean
private PaymentService paymentService;

when(paymentService.process(any())).thenReturn(paymentId);

// SpyBean - usa implementação real, sobrescreve apenas quando necessário
@SpyBean
private EventRepository eventRepository;
```

### Testcontainers (Futuro)

Para testes mais realistas:

```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

@Container
static KafkaContainer kafka = new KafkaContainer(
    DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
);
```

## Dados de Teste

### Builders

```java
// OrderRequestBuilder
public class OrderRequestBuilder {
    public static OrderRequest validOrderRequest() {
        return OrderRequest.builder()
            .customerId("CUST-123")
            .items(List.of(validOrderItem()))
            .build();
    }
}
```

### Fixtures

```java
@TestConfiguration
public class TestFixtures {
    public static UUID TEST_ORDER_ID = UUID.fromString("...");
    public static String TEST_CUSTOMER_ID = "CUST-TEST";
}
```

## Testes de Performance

### Benchmarking com JMH

```java
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class OrderServiceBenchmark {
    @Benchmark
    public void createOrder(Blackhole blackhole) {
        UUID orderId = orderService.createOrder(request);
        blackhole.consume(orderId);
    }
}
```

### Resultados Esperados

| Operação | Throughput | Latência p95 |
|----------|-----------|--------------|
| Create Order | > 500 ops/s | < 100ms |
| Query Order | > 2000 ops/s | < 50ms |
| Process Event | > 1000 msg/s | < 10ms |

## Testes de Resiliência

### Chaos Engineering

Simular falhas:
- Network delays
- Database unavailable
- Kafka down
- Memory pressure

```java
@Test
void shouldHandleDatabaseFailureGracefully() {
    // Simular falha do banco
    doThrow(new DataAccessException("DB Down")).when(repository).save(any());
    
    // Sistema deve fazer retry ou compensar
    assertThatThrownBy(() -> service.createOrder(request))
        .isInstanceOf(BusinessException.class);
}
```

## CI/CD Pipeline

```yaml
# GitHub Actions / GitLab CI
stages:
  - build
  - test
  - quality
  - deploy

test:
  script:
    - mvn clean test
    - mvn jacoco:report
    - mvn sonar:sonar
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    reports:
      junit: '**/target/surefire-reports/TEST-*.xml'
      cobertura: '**/target/site/jacoco/jacoco.xml'
```

## Boas Práticas

### 1. Nomenclatura

```java
// Padrão: should[Behavior]When[Condition]
@Test
void shouldCreateOrderSuccessfullyWhenValidRequest()

@Test
void shouldThrowExceptionWhenCustomerIdIsNull()
```

### 2. Given-When-Then

```java
@Test
void testName() {
    // Given (Arrange)
    OrderRequest request = createValidRequest();
    
    // When (Act)
    UUID orderId = service.createOrder(request);
    
    // Then (Assert)
    assertThat(orderId).isNotNull();
}
```

### 3. Testes Independentes

Cada teste deve:
- Ser independente de outros
- Poder rodar em qualquer ordem
- Limpar dados antes/depois
- Não depender de estado externo

### 4. Assertions Claras

```java
// ❌ Ruim
assertTrue(order.getItems().size() > 0);

// ✅ Bom
assertThat(order.getItems()).isNotEmpty();
assertThat(order.getTotalAmount()).isEqualByComparingTo("1500.00");
```

## Testes Não Funcionais

### Segurança

```java
@Test
void shouldReturn401WhenNoToken() {
    mockMvc.perform(post("/api/v1/orders"))
        .andExpect(status().isUnauthorized());
}

@Test
@WithMockUser(roles = "USER")
void shouldReturn403WhenInsufficientPermissions() {
    mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
        .andExpect(status().isForbidden());
}
```

### Performance

```java
@Test
@Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
void shouldRespondQuickly() {
    UUID orderId = service.createOrder(request);
    assertThat(orderId).isNotNull();
}
```

## Relatórios

### JaCoCo

```bash
mvn clean test jacoco:report
# HTML: target/site/jacoco/index.html
# XML: target/site/jacoco/jacoco.xml (para CI)
```

### Surefire

```bash
# XML reports em: target/surefire-reports/
# Útil para CI/CD integrations
```

## Debugging Testes

```bash
# Rodar teste específico
mvn test -Dtest=OrderServiceTest

# Com debug remoto
mvn test -Dmaven.surefire.debug

# Verbose output
mvn test -X
```

## Referências

- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

