# 🗺️ Roadmap - Futuras Implementações

Documento de planejamento para evolução do projeto **E-commerce CQRS Event Sourcing**.

---

## 📊 Status Atual do Projeto

### ✅ Implementado e Funcionando (v1.0)

#### Core Architecture
- ✅ **CQRS** - Command e Query separados
- ✅ **Event Sourcing** - Event Store completo
- ✅ **Outbox Pattern** - Garantia de entrega de eventos
- ✅ **Debezium CDC** - Change Data Capture funcionando
- ✅ **DDD** - Aggregates, Value Objects, Domain Events

#### Serviços
- ✅ **Order Command Service** (porta 8080)
  - Criação de pedidos
  - Persistência no Event Store
  - Persistência no Outbox
  - Spring Security configurado

- ✅ **Order Query Service** (porta 8081) - CÓDIGO PRONTO
  - Projeções implementadas
  - Redis Cache configurado
  - Kafka Consumer implementado
  - ⚠️ **NÃO TESTADO**

- ✅ **Saga Orchestrator** (porta 8082) - CÓDIGO PRONTO
  - Orquestração de saga implementada
  - Compensações definidas
  - Scheduler de timeout
  - ⚠️ **COMPENSAÇÕES COM TODOs**

#### Infraestrutura
- ✅ PostgreSQL (Write + Read Models)
- ✅ Kafka + Zookeeper + Schema Registry
- ✅ Kafka Connect + Debezium
- ✅ Kafka UI (http://localhost:8090)
- ✅ Redis

---

## 🚀 Fase 1: Completar Funcionalidades Básicas (Alta Prioridade)

### 1.1 Testar e Ajustar Order Query Service
**Tempo Estimado:** 2-4 horas  
**Prioridade:** 🔴 ALTA

**Tarefas:**
- [ ] Iniciar order-query-service
- [ ] Criar pedido via order-command-service
- [ ] Verificar se consumer processou evento
- [ ] Verificar se projeção foi criada no PostgreSQL
- [ ] Testar endpoints GET:
  - `GET /api/v1/orders` - Listar todos
  - `GET /api/v1/orders/{id}` - Buscar por ID
  - `GET /api/v1/orders/customer/{customerId}` - Por cliente
- [ ] Validar cache do Redis funcionando
- [ ] Ajustar deserialização se necessário
- [ ] Documentar fluxo completo CQRS

**Arquivos a revisar:**
- `order-query-service/src/main/java/com/ecommerce/order/query/consumer/OrderEventConsumer.java`
- `order-query-service/src/main/java/com/ecommerce/order/query/service/OrderProjectionService.java`

---

### 1.2 Implementar Autenticação Completa
**Tempo Estimado:** 4-6 horas  
**Prioridade:** 🔴 ALTA

**Tarefas:**
- [ ] Criar entidade `User` (id, username, password, email, roles)
- [ ] Criar `UserRepository`
- [ ] Criar `UserService` com:
  - Registro de usuário (com hash de senha)
  - Login (validação de credenciais)
  - Geração de JWT token
- [ ] Habilitar `AuthController` em order-command-service
- [ ] Criar endpoints:
  - `POST /api/v1/auth/register` - Registrar usuário
  - `POST /api/v1/auth/login` - Login (retorna JWT)
  - `GET /api/v1/auth/me` - Obter usuário autenticado
- [ ] Testar fluxo completo:
  - Registrar → Login → Obter token → Usar token em requisições

**Exemplo de Request/Response:**
```json
POST /api/v1/auth/register
{
  "username": "john.doe",
  "email": "john@example.com",
  "password": "StrongPass123!",
  "roles": ["ROLE_USER"]
}

POST /api/v1/auth/login
{
  "username": "john.doe",
  "password": "StrongPass123!"
}
Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresAt": "2025-10-21T02:00:00Z"
}
```

**Arquivos a criar/modificar:**
- `order-command-service/src/main/java/com/ecommerce/order/command/entity/User.java`
- `order-command-service/src/main/java/com/ecommerce/order/command/repository/UserRepository.java`
- `order-command-service/src/main/java/com/ecommerce/order/command/service/UserService.java`
- `order-command-service/src/main/java/com/ecommerce/order/command/controller/AuthController.java.disabled` → renomear

---

### 1.3 Atualizar Testes Unitários
**Tempo Estimado:** 3-4 horas  
**Prioridade:** 🟠 MÉDIA-ALTA

**Tarefas:**
- [ ] Atualizar `OrderCommandServiceApplicationTests.java`
- [ ] Criar testes para DDD (Order aggregate, Value Objects)
- [ ] Criar testes para OrderService
- [ ] Criar testes para UserService (quando implementado)
- [ ] Criar testes para OrderProjectionService
- [ ] Criar testes para OrderSagaOrchestrator
- [ ] Atingir cobertura mínima de 70%

**Frameworks:**
- JUnit 5
- Mockito
- Testcontainers (para testes de integração)

---

## 🔄 Fase 2: Completar Saga Pattern (Média Prioridade)

### 2.1 Implementar Serviços Mock
**Tempo Estimado:** 6-8 horas  
**Prioridade:** 🟠 MÉDIA

Criar serviços mock para completar a saga:

#### Payment Service (porta 8083)
- [ ] Criar módulo `payment-service`
- [ ] Implementar endpoints:
  - `POST /api/v1/payments/process` - Processar pagamento
  - `POST /api/v1/payments/refund` - Estornar pagamento
  - `GET /api/v1/payments/{id}` - Consultar pagamento
- [ ] Consumer para `payment-commands`
- [ ] Producer para `PaymentProcessedEvent` e `PaymentFailedEvent`
- [ ] Lógica mock (simular sucesso/falha aleatória ou baseado em valor)

#### Inventory Service (porta 8084)
- [ ] Criar módulo `inventory-service`
- [ ] Implementar endpoints:
  - `POST /api/v1/inventory/reserve` - Reservar estoque
  - `POST /api/v1/inventory/release` - Liberar estoque
  - `GET /api/v1/inventory/{productId}` - Consultar estoque
- [ ] Consumer para `inventory-commands`
- [ ] Producer para `InventoryReservedEvent` e `InventoryFailedEvent`
- [ ] Banco de dados com produtos e quantidades

#### Shipping Service (porta 8085)
- [ ] Criar módulo `shipping-service`
- [ ] Implementar endpoints:
  - `POST /api/v1/shipping/schedule` - Agendar envio
  - `POST /api/v1/shipping/cancel` - Cancelar envio
  - `GET /api/v1/shipping/{id}` - Rastrear envio
- [ ] Consumer para `shipping-commands`
- [ ] Producer para `ShippingScheduledEvent` e `ShippingFailedEvent`

---

### 2.2 Completar Compensações da Saga
**Tempo Estimado:** 2-3 horas  
**Prioridade:** 🟠 MÉDIA

**Arquivos a modificar:**
- `saga-orchestrator/src/main/java/com/ecommerce/saga/service/OrderSagaOrchestrator.java`

**TODOs a implementar:**
```java
// Linha 227
if (completedSteps.contains(SagaStep.SHIPPING_PROCESSING)) {
    saga.setCurrentStep(SagaStep.COMPENSATION_SHIPPING);
    CancelShippingCommand command = CancelShippingCommand.builder()
        .correlationId(saga.getCorrelationId())
        .orderId(saga.getOrderId())
        .reason(reason)
        .build();
    kafkaTemplate.send("shipping-commands", command.getCorrelationId().toString(), command);
    log.info("Shipping cancellation command sent for saga: {}", saga.getCorrelationId());
}

// Linha 234
if (completedSteps.contains(SagaStep.INVENTORY_PROCESSING)) {
    saga.setCurrentStep(SagaStep.COMPENSATION_INVENTORY);
    ReleaseInventoryCommand command = ReleaseInventoryCommand.builder()
        .correlationId(saga.getCorrelationId())
        .orderId(saga.getOrderId())
        .items(getOrderItems(saga))
        .build();
    kafkaTemplate.send("inventory-commands", command.getCorrelationId().toString(), command);
    log.info("Inventory release command sent for saga: {}", saga.getCorrelationId());
}

// Linha 241
if (completedSteps.contains(SagaStep.PAYMENT_PROCESSING)) {
    saga.setCurrentStep(SagaStep.COMPENSATION_PAYMENT);
    RefundPaymentCommand command = RefundPaymentCommand.builder()
        .correlationId(saga.getCorrelationId())
        .orderId(saga.getOrderId())
        .amount(getPaymentAmount(saga))
        .build();
    kafkaTemplate.send("payment-commands", command.getCorrelationId().toString(), command);
    log.info("Payment refund command sent for saga: {}", saga.getCorrelationId());
}
```

**Classes a criar em `shared/src/main/java/com/ecommerce/shared/commands/`:**
- `CancelShippingCommand.java`
- `ReleaseInventoryCommand.java`
- `RefundPaymentCommand.java`

---

### 2.3 Testes de Saga Completos
**Tempo Estimado:** 3-4 horas  
**Prioridade:** 🟠 MÉDIA

**Cenários a testar:**
- [ ] Saga happy path (todos os passos com sucesso)
- [ ] Falha no payment → compensação
- [ ] Falha no inventory → compensação payment
- [ ] Falha no shipping → compensação inventory + payment
- [ ] Timeout da saga → compensação automática
- [ ] Retry após falha transiente

---

## 📊 Fase 3: Observabilidade Completa (Média Prioridade)

### 3.1 Configurar Prometheus + Grafana
**Tempo Estimado:** 4-6 horas  
**Prioridade:** 🟠 MÉDIA

**Tarefas:**
- [ ] Subir Prometheus e Grafana
- [ ] Importar dashboards prontos:
  - JVM Micrometer Dashboard
  - Spring Boot Statistics
  - Kafka Metrics
  - PostgreSQL Metrics
- [ ] Criar dashboard customizado com:
  - Taxa de criação de pedidos
  - Latência de comandos
  - Taxa de sucesso/falha de sagas
  - Tamanho do outbox
  - Throughput do Kafka
- [ ] Configurar alertas:
  - Outbox muito grande (> 1000 eventos)
  - Saga com timeout
  - Erro rate > 5%
  - Latência > 1s

**URLs:**
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

**Arquivos a criar:**
- `infra/grafana/dashboards/order-service-dashboard.json`
- `infra/prometheus/alerts.yml`

---

### 3.2 Configurar ELK Stack
**Tempo Estimado:** 3-4 horas  
**Prioridade:** 🟡 MÉDIA

**Tarefas:**
- [ ] Subir Elasticsearch, Logstash, Kibana
- [ ] Configurar Logstash pipeline para consumir logs
- [ ] Configurar índices no Elasticsearch
- [ ] Criar dashboards no Kibana:
  - Logs por severidade
  - Logs por serviço
  - Erros em tempo real
  - Trace de requisições
- [ ] Configurar alertas de erro
- [ ] Implementar log correlation com trace ID

**URLs:**
- Kibana: http://localhost:5601
- Elasticsearch: http://localhost:9200

**Arquivos a revisar:**
- `infra/logstash/logstash.conf`
- `order-command-service/src/main/resources/logback-spring.xml`

---

### 3.3 Configurar Zipkin (Distributed Tracing)
**Tempo Estimado:** 2-3 horas  
**Prioridade:** 🟡 MÉDIA

**Tarefas:**
- [ ] Subir Zipkin
- [ ] Adicionar dependências de tracing:
  ```xml
  <dependency>
      <groupId>io.micrometer</groupId>
      <artifactId>micrometer-tracing-bridge-brave</artifactId>
  </dependency>
  <dependency>
      <groupId>io.zipkin.reporter2</groupId>
      <artifactId>zipkin-reporter-brave</artifactId>
  </dependency>
  ```
- [ ] Configurar application.yml com sampling
- [ ] Testar trace de requisições entre serviços
- [ ] Visualizar latência por serviço
- [ ] Identificar gargalos de performance

**URL:**
- Zipkin: http://localhost:9411

---

## 🔐 Fase 4: Segurança Avançada (Alta Prioridade)

### 4.1 Implementar Autenticação/Autorização Completa
**Tempo Estimado:** 6-8 horas  
**Prioridade:** 🔴 ALTA

**Tarefas:**

#### Backend
- [ ] Criar entidade `User`:
  ```java
  @Entity
  @Table(name = "users")
  public class User {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
      
      @Column(unique = true, nullable = false)
      private String username;
      
      @Column(unique = true, nullable = false)
      private String email;
      
      @Column(nullable = false)
      private String password; // BCrypt hash
      
      @ElementCollection(fetch = FetchType.EAGER)
      @CollectionTable(name = "user_roles")
      private Set<String> roles;
      
      private boolean enabled;
      private Instant createdAt;
      private Instant lastLoginAt;
  }
  ```

- [ ] Criar `UserRepository` com Spring Data JPA
- [ ] Criar `UserService` com:
  - `register(RegisterRequest)` - Hash senha com BCrypt
  - `login(LoginRequest)` - Validar e gerar JWT
  - `getUserByUsername(String)`
  - `updateRoles(Long userId, Set<String> roles)` - Admin only

- [ ] Criar DTOs:
  - `RegisterRequest` (username, email, password)
  - `LoginRequest` (username, password)
  - `AuthResponse` (token, expiresAt, user)

- [ ] Habilitar `AuthController`:
  ```java
  @PostMapping("/register")
  public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request)
  
  @PostMapping("/login")
  public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request)
  
  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse> getCurrentUser()
  ```

#### Segurança
- [ ] Ajustar `SecurityConfig` para:
  - `/api/v1/auth/**` → Public
  - `/actuator/health` → Public
  - `/api/v1/orders` POST → Authenticated (ROLE_USER)
  - `/api/v1/orders/**` PUT/DELETE → Authenticated (ROLE_ADMIN)

- [ ] Adicionar validações:
  - Senha mínima: 8 caracteres
  - Email válido
  - Username único

- [ ] Implementar refresh token (opcional)

#### Testes
- [ ] Testes unitários para UserService
- [ ] Testes de integração:
  - Registrar usuário
  - Login com sucesso
  - Login com senha incorreta
  - Acessar endpoint protegido sem token (401)
  - Acessar endpoint protegido com token válido (200)
  - Acessar endpoint admin sem role (403)

---

### 4.2 Adicionar Rate Limiting
**Tempo Estimado:** 2-3 horas  
**Prioridade:** 🟡 MÉDIA

**Tarefas:**
- [ ] Adicionar Bucket4j:
  ```xml
  <dependency>
      <groupId>com.github.vladimir-bukhtoyarov</groupId>
      <artifactId>bucket4j-core</artifactId>
      <version>8.1.0</version>
  </dependency>
  ```
- [ ] Criar `RateLimitingFilter`
- [ ] Configurar limites:
  - 100 requisições/minuto por IP
  - 1000 requisições/minuto por usuário autenticado
- [ ] Retornar `429 Too Many Requests` quando exceder
- [ ] Adicionar headers: `X-RateLimit-Remaining`, `X-RateLimit-Reset`

---

## 🧪 Fase 5: Testes Completos (Alta Prioridade)

### 5.1 Testes Unitários
**Tempo Estimado:** 8-10 horas  
**Prioridade:** 🔴 ALTA

**Cobertura mínima:** 70%

**Módulos a testar:**

#### order-command-service
- [ ] `Order` aggregate (regras de negócio)
- [ ] Value Objects (Money, OrderItem, etc)
- [ ] `OrderService` (mock repositories)
- [ ] `OrderController` (MockMvc)
- [ ] `DomainEventMapper`
- [ ] Exception handlers

#### order-query-service
- [ ] `OrderProjectionService`
- [ ] `OrderEventConsumer`
- [ ] `OrderQueryController`
- [ ] Cache behavior (Redis)

#### saga-orchestrator
- [ ] `OrderSagaOrchestrator` - happy path
- [ ] Compensação por falha em cada step
- [ ] `SagaTimeoutScheduler`

#### shared
- [ ] `JwtUtil` (geração e validação de tokens)
- [ ] `JwtAuthenticationFilter`

---

### 5.2 Testes de Integração
**Tempo Estimado:** 6-8 horas  
**Prioridade:** 🟠 MÉDIA

**Usar Testcontainers:**
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.19.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>kafka</artifactId>
    <version>1.19.0</version>
    <scope>test</scope>
</dependency>
```

**Cenários:**
- [ ] Criar pedido → Verificar Event Store + Outbox
- [ ] Criar pedido → Verificar evento no Kafka (Testcontainers Kafka)
- [ ] Criar pedido → Verificar projeção criada (CQRS)
- [ ] Fluxo completo de autenticação
- [ ] Saga completa (com mocks de serviços externos)

---

### 5.3 Testes E2E
**Tempo Estimado:** 4-6 horas  
**Prioridade:** 🟡 MÉDIA

**Ferramentas:** REST Assured ou MockMvc

**Cenários:**
1. **Happy Path Completo:**
   - Register → Login → Create Order → Query Order → Verify in DB
   
2. **Erro de Validação:**
   - Criar pedido com dados inválidos → 400 Bad Request
   
3. **Autenticação:**
   - Criar pedido sem token → 401 Unauthorized
   - Criar pedido com token expirado → 401
   - Deletar pedido sem role ADMIN → 403 Forbidden

4. **CQRS:**
   - Criar pedido → Aguardar 2s → Query deve retornar pedido

---

## 🏗️ Fase 6: Features Avançadas (Baixa Prioridade)

### 6.1 API Gateway
**Tempo Estimado:** 8-10 horas  
**Prioridade:** 🟢 BAIXA

**Tecnologia:** Spring Cloud Gateway

**Tarefas:**
- [ ] Criar módulo `api-gateway` (porta 8000)
- [ ] Configurar rotas:
  - `/commands/**` → order-command-service:8080
  - `/queries/**` → order-query-service:8081
  - `/saga/**` → saga-orchestrator:8082
  - `/auth/**` → order-command-service:8080
- [ ] Implementar:
  - Rate Limiting global
  - Authentication filter
  - Request logging
  - CORS configuration
  - Circuit Breaker (Resilience4j)
  - Load Balancing (se múltiplas instâncias)

**Exemplo de application.yml:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-commands
          uri: http://localhost:8080
          predicates:
            - Path=/api/v1/orders
          filters:
            - name: CircuitBreaker
              args:
                name: orderCommandService
                fallbackUri: forward:/fallback
```

---

### 6.2 Circuit Breaker com Resilience4j
**Tempo Estimado:** 3-4 horas  
**Prioridade:** 🟢 BAIXA

**Tarefas:**
- [ ] Adicionar dependências:
  ```xml
  <dependency>
      <groupId>io.github.resilience4j</groupId>
      <artifactId>resilience4j-spring-boot3</artifactId>
  </dependency>
  ```
- [ ] Configurar circuit breakers
- [ ] Implementar fallbacks para serviços críticos
- [ ] Adicionar retry policies
- [ ] Monitorar estado dos circuit breakers no Actuator

---

### 6.3 API Documentation (Swagger/OpenAPI)
**Tempo Estimado:** 2-3 horas  
**Prioridade:** 🟡 MÉDIA

**Tarefas:**
- [ ] Adicionar SpringDoc OpenAPI:
  ```xml
  <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.2.0</version>
  </dependency>
  ```
- [ ] Adicionar anotações nos controllers:
  - `@Operation`, `@ApiResponse`, `@Schema`
- [ ] Configurar security scheme para JWT
- [ ] Acessar: http://localhost:8080/swagger-ui.html

---

### 6.4 Notification Service
**Tempo Estimado:** 6-8 horas  
**Prioridade:** 🟢 BAIXA

**Tarefas:**
- [ ] Criar módulo `notification-service`
- [ ] Consumer para eventos:
  - `OrderCreatedEvent` → Email de confirmação
  - `OrderCompletedEvent` → Email de conclusão
  - `OrderCancelledEvent` → Email de cancelamento
- [ ] Integrar com provedor de email (SendGrid, AWS SES, ou mock)
- [ ] Criar templates de email
- [ ] Implementar retry para falhas de envio

---

## 🚢 Fase 7: Deployment & DevOps (Baixa Prioridade)

### 7.1 Containerizar Aplicações
**Tempo Estimado:** 4-5 horas  
**Prioridade:** 🟡 MÉDIA

**Tarefas:**
- [ ] Criar `Dockerfile` para cada serviço:
  ```dockerfile
  FROM eclipse-temurin:17-jre-alpine
  WORKDIR /app
  COPY target/*.jar app.jar
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```
- [ ] Multi-stage build para otimizar imagens
- [ ] Adicionar serviços ao docker-compose.yml:
  ```yaml
  order-command-service:
    build: ./order-command-service
    ports:
      - "8080:8080"
    depends_on:
      - postgres-command
      - kafka
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-command:5432/order_command_db
  ```

---

### 7.2 Kubernetes Deployment
**Tempo Estimado:** 12-16 horas  
**Prioridade:** 🟢 BAIXA

**Tarefas:**
- [ ] Criar namespaces: `ecommerce-dev`, `ecommerce-prod`
- [ ] Criar manifests para cada serviço:
  - Deployment
  - Service
  - ConfigMap
  - Secret
  - HPA (Horizontal Pod Autoscaler)
  - PVC (Persistent Volume Claims) para bancos
- [ ] Configurar Ingress para roteamento
- [ ] Implementar Health Checks (liveness, readiness)
- [ ] Configurar Resources (requests/limits)

**Estrutura:**
```
k8s/
├── base/
│   ├── namespace.yaml
│   ├── postgres-command/
│   ├── postgres-query/
│   ├── kafka/
│   └── redis/
├── services/
│   ├── order-command-service/
│   ├── order-query-service/
│   └── saga-orchestrator/
└── overlays/
    ├── dev/
    └── prod/
```

---

### 7.3 CI/CD Pipeline
**Tempo Estimado:** 8-10 horas  
**Prioridade:** 🟢 BAIXA

**GitHub Actions - Tarefas:**
- [ ] Criar `.github/workflows/ci.yml`:
  - Build em cada push
  - Rodar testes unitários
  - Rodar testes de integração
  - Análise de código (SonarQube)
  - Build Docker images
  - Push para Docker Hub/GitHub Container Registry

- [ ] Criar `.github/workflows/cd.yml`:
  - Deploy automático em dev (branch develop)
  - Deploy manual em staging (branch main + approval)
  - Deploy manual em prod (tags + approval)

**Exemplo de workflow:**
```yaml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build with Maven
        run: mvn clean install
      - name: Run Tests
        run: mvn test
      - name: Build Docker Image
        run: docker build -t ${{ secrets.DOCKER_USERNAME }}/order-command-service:${{ github.sha }} ./order-command-service
```

---

## 🎨 Fase 8: Melhorias de Código (Média Prioridade)

### 8.1 Refactoring e Code Quality
**Tempo Estimado:** 6-8 horas  
**Prioridade:** 🟠 MÉDIA

**Tarefas:**
- [ ] Adicionar SonarQube analysis
- [ ] Resolver code smells
- [ ] Adicionar JavaDoc em classes públicas
- [ ] Extrair constantes mágicas
- [ ] Melhorar tratamento de exceções
- [ ] Adicionar validações de negócio customizadas

---

### 8.2 Performance Optimization
**Tempo Estimado:** 4-6 horas  
**Prioridade:** 🟡 MÉDIA

**Tarefas:**
- [ ] Adicionar índices no PostgreSQL:
  ```sql
  CREATE INDEX idx_events_aggregate_version ON events(aggregate_id, version);
  CREATE INDEX idx_outbox_not_processed_created ON outbox(created_at) WHERE processed = false;
  ```
- [ ] Implementar paginação em queries:
  ```java
  Page<OrderProjection> findAll(Pageable pageable);
  ```
- [ ] Configurar connection pool:
  ```yaml
  spring:
    datasource:
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5
        connection-timeout: 30000
  ```
- [ ] Otimizar queries N+1
- [ ] Implementar cache multi-level (Redis L1, Local L2)
- [ ] Batch processing para outbox cleanup

---

### 8.3 Adicionar Validation Customizada
**Tempo Estimado:** 2-3 horas  
**Prioridade:** 🟡 MÉDIA

**Tarefas:**
- [ ] Criar validações customizadas:
  ```java
  @ValidCPF
  private String customerCpf;
  
  @ValidCreditCard
  private String cardNumber;
  
  @MinValue(value = "0.01", message = "Total must be greater than zero")
  private BigDecimal totalAmount;
  ```

- [ ] Implementar validators:
  - CPF/CNPJ brasileiro
  - Cartão de crédito (Luhn algorithm)
  - CEP
  - Telefone

---

## 📈 Fase 9: Features de Negócio (Baixa Prioridade)

### 9.1 Gestão de Estoque
**Tempo Estimado:** 10-12 horas  
**Prioridade:** 🟢 BAIXA

**Tarefas:**
- [ ] Criar `inventory-service`
- [ ] Entidade `Product` (id, name, sku, quantity, reservedQuantity)
- [ ] Endpoints:
  - `GET /api/v1/products` - Listar produtos
  - `GET /api/v1/products/{id}` - Detalhes
  - `POST /api/v1/products` - Criar (admin)
  - `PUT /api/v1/products/{id}` - Atualizar (admin)
- [ ] Implementar reserva de estoque:
  - Decrementar `quantity`
  - Incrementar `reservedQuantity`
- [ ] Implementar liberação (compensação):
  - Decrementar `reservedQuantity`
  - Incrementar `quantity`
- [ ] Eventos:
  - `InventoryReservedEvent`
  - `InventoryReleasedEvent`
  - `InventoryInsufficientEvent`

---

### 9.2 Processamento de Pagamentos
**Tempo Estimado:** 10-12 horas  
**Prioridade:** 🟢 BAIXA

**Tarefas:**
- [ ] Criar `payment-service`
- [ ] Entidade `Payment` (id, orderId, amount, status, method, transactionId)
- [ ] Integrar com gateway de pagamento mock ou real:
  - Stripe
  - PayPal
  - PagSeguro/PicPay (Brasil)
- [ ] Implementar processamento assíncrono
- [ ] Implementar estorno (compensação)
- [ ] Webhook para notificações do gateway
- [ ] Eventos:
  - `PaymentAuthorizedEvent`
  - `PaymentCapturedEvent`
  - `PaymentRefundedEvent`
  - `PaymentFailedEvent`

---

### 9.3 Logística e Envio
**Tempo Estimado:** 8-10 horas  
**Prioridade:** 🟢 BAIXA

**Tarefas:**
- [ ] Criar `shipping-service`
- [ ] Entidade `Shipment` (id, orderId, trackingCode, carrier, status)
- [ ] Integrar com APIs de transportadoras (mock ou real):
  - Correios
  - Loggi
  - Melhor Envio
- [ ] Calcular frete por CEP
- [ ] Rastreamento de envio
- [ ] Eventos:
  - `ShippingScheduledEvent`
  - `ShippingDispatchedEvent`
  - `ShippingDeliveredEvent`
  - `ShippingCancelledEvent`

---

### 9.4 Gestão de Pedidos Avançada
**Tempo Estimado:** 6-8 horas  
**Prioridade:** 🟢 BAIXA

**Tarefas:**
- [ ] Implementar estados adicionais:
  - `PENDING_PAYMENT`
  - `PAYMENT_CONFIRMED`
  - `PREPARING_SHIPMENT`
  - `SHIPPED`
  - `DELIVERED`
  - `CANCELLED`
  - `RETURNED`

- [ ] Comandos adicionais:
  - `CancelOrderCommand`
  - `UpdateOrderCommand`
  - `ReturnOrderCommand`

- [ ] Eventos correspondentes:
  - `OrderCancelledEvent`
  - `OrderUpdatedEvent`
  - `OrderReturnedEvent`

- [ ] Endpoints:
  - `PUT /api/v1/orders/{id}/cancel`
  - `PUT /api/v1/orders/{id}/status`
  - `GET /api/v1/orders/status/{status}`

---

## 🔍 Fase 10: Monitoramento Avançado (Média Prioridade)

### 10.1 Alerting Completo
**Tempo Estimado:** 4-6 horas  
**Prioridade:** 🟠 MÉDIA

**Tarefas:**
- [ ] Configurar Alertmanager (Prometheus)
- [ ] Criar regras de alerta:
  ```yaml
  groups:
    - name: order_service_alerts
      rules:
        - alert: HighErrorRate
          expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
          annotations:
            summary: "High error rate detected"
        
        - alert: OutboxBacklog
          expr: count(outbox_not_processed) > 1000
          annotations:
            summary: "Outbox backlog is growing"
  ```
- [ ] Integrar com canais de notificação:
  - Slack
  - Email
  - PagerDuty
  - Discord

---

### 10.2 Business Metrics
**Tempo Estimado:** 3-4 horas  
**Prioridade:** 🟡 MÉDIA

**Tarefas:**
- [ ] Criar métricas customizadas:
  ```java
  @Component
  public class BusinessMetrics {
      private final Counter ordersCreated;
      private final Counter ordersCompleted;
      private final Counter ordersCancelled;
      private final Timer orderProcessingTime;
      private final Gauge outboxSize;
  }
  ```

- [ ] Dashboard do Grafana com:
  - Pedidos por hora/dia
  - Taxa de conversão (criados vs completados)
  - Receita total
  - Tempo médio de processamento
  - Taxa de cancelamento
  - Top produtos mais vendidos

---

## 📚 Fase 11: Documentação (Alta Prioridade)

### 11.1 Documentação Técnica
**Tempo Estimado:** 4-6 horas  
**Prioridade:** 🔴 ALTA

**Tarefas:**
- [ ] Criar ADRs (Architecture Decision Records):
  - `001-event-sourcing.md` - Por que Event Sourcing?
  - `002-saga-pattern.md` - Escolha entre Saga vs 2PC
  - `003-debezium-cdc.md` - CDC vs Dual Writes
  - `004-postgresql-jsonb.md` - JSONB para saga data

- [ ] Documentar fluxos:
  - Diagrama de sequência: Criar pedido
  - Diagrama de sequência: Saga completa
  - Diagrama de sequência: Compensação
  - Arquitetura C4 Model (Context, Container, Component, Code)

- [ ] Guias de desenvolvimento:
  - Como adicionar novo comando
  - Como adicionar novo evento
  - Como criar nova projeção
  - Como adicionar step na saga

---

### 11.2 Guias Operacionais
**Tempo Estimado:** 2-3 horas  
**Prioridade:** 🟠 MÉDIA

**Tarefas:**
- [ ] Criar `docs/OPERATIONS.md`:
  - Como fazer backup do Event Store
  - Como replay de eventos
  - Como limpar outbox antiga
  - Como resetar consumer offset
  - Como adicionar novo conector Debezium

- [ ] Criar `docs/TROUBLESHOOTING.md`:
  - Saga travada → Como resolver
  - Outbox crescendo → Diagnóstico
  - Kafka Connect parado → Recovery
  - Performance degradada → Checklist

---

## 🎯 Fase 12: Otimizações de Produção

### 12.1 Event Store Optimizations
**Tempo Estimado:** 4-6 horas  
**Prioridade:** 🟡 MÉDIA

**Tarefas:**
- [ ] Implementar Snapshots:
  ```java
  @Entity
  public class Snapshot {
      @Id
      private UUID aggregateId;
      private Integer version;
      private String aggregateData; // JSON do estado atual
      private Instant createdAt;
  }
  ```
- [ ] Criar snapshot a cada 50 eventos
- [ ] Reconstruir aggregate a partir do snapshot + eventos delta
- [ ] Implementar archiving de eventos antigos

---

### 12.2 Outbox Cleanup
**Tempo Estimado:** 2-3 horas  
**Prioridade:** 🟠 MÉDIA

**Tarefas:**
- [ ] Criar scheduled task para limpar outbox:
  ```java
  @Scheduled(cron = "0 0 2 * * *") // 2 AM diariamente
  public void cleanupOldOutboxEntries() {
      Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);
      outboxRepository.deleteByProcessedTrueAndProcessedAtBefore(cutoff);
  }
  ```
- [ ] Adicionar métricas de cleanup
- [ ] Configurar retenção por ambiente:
  - Dev: 1 dia
  - Staging: 7 dias
  - Prod: 30 dias

---

### 12.3 Read Model Optimization
**Tempo Estimado:** 3-4 horas  
**Prioridade:** 🟡 MÉDIA

**Tarefas:**
- [ ] Implementar projeções especializadas:
  - `CustomerOrdersSummary` - Agregado por cliente
  - `ProductSalesStats` - Estatísticas de vendas
  - `DailySalesReport` - Relatório diário
- [ ] Adicionar índices otimizados
- [ ] Implementar cache warming na inicialização
- [ ] Configurar TTL diferenciado por tipo de query

---

## 🧩 Fase 13: Recursos Extras

### 13.1 Multi-tenancy
**Tempo Estimado:** 10-12 horas  
**Prioridade:** 🟢 BAIXA

**Tarefas:**
- [ ] Adicionar `tenantId` em todas as entidades
- [ ] Implementar `TenantContext` com ThreadLocal
- [ ] Criar filtros SQL automáticos por tenant
- [ ] Separar dados por tenant no Event Store
- [ ] Configurar Kafka topics por tenant

---

### 13.2 Event Replay & Time Travel
**Tempo Estimado:** 6-8 horas  
**Prioridade:** 🟢 BAIXA

**Tarefas:**
- [ ] Criar endpoint para replay de eventos:
  ```java
  POST /api/v1/admin/replay
  {
    "aggregateId": "uuid",
    "fromVersion": 1,
    "toVersion": 10
  }
  ```
- [ ] Implementar reconstrução de projeções
- [ ] Criar endpoint de "time travel":
  ```java
  GET /api/v1/admin/orders/{id}/history?timestamp=2025-01-01T00:00:00Z
  ```
- [ ] Visualização de audit trail completo

---

### 13.3 GraphQL API
**Tempo Estimado:** 8-10 horas  
**Prioridade:** 🟢 BAIXA

**Tarefas:**
- [ ] Adicionar Spring GraphQL
- [ ] Criar schema GraphQL:
  ```graphql
  type Order {
    id: ID!
    customerId: String!
    items: [OrderItem!]!
    totalAmount: Float!
    status: OrderStatus!
    createdAt: DateTime!
  }
  
  type Query {
    order(id: ID!): Order
    orders(page: Int, size: Int): OrderConnection
    customerOrders(customerId: String!): [Order!]!
  }
  
  type Mutation {
    createOrder(input: CreateOrderInput!): Order!
    cancelOrder(orderId: ID!): Order!
  }
  ```
- [ ] Implementar resolvers
- [ ] Adicionar DataLoader para N+1 queries
- [ ] GraphQL Playground

---

## 📦 Artefatos Pendentes

### Scripts
- [ ] `infra/scripts/setup-infrastructure.sh` (Linux/Mac)
- [ ] `infra/scripts/setup-infrastructure.ps1` (Windows)
- [ ] `infra/scripts/create-topics.sh` - Criar tópicos Kafka
- [ ] `infra/scripts/register-connectors.sh` - Registrar conectores
- [ ] `infra/scripts/cleanup.sh` - Limpar ambiente

### Configurações
- [ ] `infra/grafana/dashboards/` - Dashboards prontos
- [ ] `infra/prometheus/alerts.yml` - Regras de alerta
- [ ] `infra/elasticsearch/index-templates.json` - Templates de índices
- [ ] `.env.example` - Exemplo de variáveis de ambiente

### Dados Mock
- [ ] `infra/data/seed-products.sql` - Produtos de exemplo
- [ ] `infra/data/seed-users.sql` - Usuários de teste
- [ ] Postman Collection completa
- [ ] JMeter scripts para testes de carga

---

## 🎓 Melhorias de Arquitetura

### Long-term Improvements

#### 1. Event Store Dedicado
- Migrar de PostgreSQL para:
  - EventStoreDB
  - Apache Kafka Streams
  - AWS EventBridge

#### 2. Message Broker Alternativo
- Avaliar:
  - RabbitMQ com plugins de Event Sourcing
  - AWS SQS/SNS
  - Google Pub/Sub

#### 3. Cache Distribuído
- Implementar cache multi-região com:
  - Redis Cluster
  - Hazelcast
  - AWS ElastiCache

#### 4. Serverless Components
- Migrar consumers para:
  - AWS Lambda
  - Azure Functions
  - Google Cloud Functions

---

## 📋 Checklist de Produção

Antes de considerar o projeto "production-ready":

### Segurança
- [ ] Implementar autenticação completa
- [ ] Adicionar rate limiting
- [ ] Configurar HTTPS/TLS
- [ ] Implementar CORS adequado
- [ ] Secrets management (Vault, AWS Secrets Manager)
- [ ] Audit logging de ações sensíveis
- [ ] Input sanitization
- [ ] SQL injection prevention (já tem com JPA)
- [ ] OWASP Top 10 compliance

### Performance
- [ ] Testes de carga (JMeter/Gatling)
- [ ] Objetivos: > 1000 req/s, < 200ms p95
- [ ] Connection pooling otimizado
- [ ] Cache hit rate > 80%
- [ ] Índices de banco otimizados

### Reliability
- [ ] Cobertura de testes > 70%
- [ ] Circuit breakers configurados
- [ ] Retry policies com backoff
- [ ] Dead letter queues
- [ ] Health checks completos
- [ ] Graceful shutdown

### Observability
- [ ] Logs estruturados (JSON)
- [ ] Trace distribuído configurado
- [ ] Métricas de negócio
- [ ] Dashboards principais
- [ ] Alertas críticos configurados
- [ ] SLIs/SLOs definidos

### Operacional
- [ ] Backup automatizado
- [ ] Disaster recovery plan
- [ ] Runbooks documentados
- [ ] Incident response plan
- [ ] Capacity planning
- [ ] Cost optimization

---

## 🎯 Recomendações por Cenário

### Para Portfolio/Demonstração (Atual)
✅ **Projeto está ÓTIMO!** Demonstra:
- Arquitetura avançada
- Padrões enterprise
- Conhecimento de DDD, CQRS, Event Sourcing
- Infraestrutura complexa

**Sugestões adicionais:**
1. Completar Order Query Service (2h)
2. Adicionar Swagger/OpenAPI (2h)
3. Criar alguns testes (3h)
4. **Total: 7 horas para "portfolio perfeito"**

---

### Para MVP de Startup
**Priorizar:**
1. Autenticação completa (6h)
2. Order Query Service funcionando (2h)
3. Testes básicos (4h)
4. API Gateway (8h)
5. **Total: 20 horas para MVP**

---

### Para Produção Enterprise
**Implementar tudo da Fase 1-5:**
- Fase 1: Funcionalidades Básicas (12-18h)
- Fase 2: Saga Completa (16-20h)
- Fase 3: Observabilidade (10-14h)
- Fase 4: Segurança (10-12h)
- Fase 5: Testes (18-24h)
- **Total: 66-88 horas (~2-3 sprints)**

---

## 📌 Notas Importantes

### Decisões Arquiteturais Tomadas
1. **PostgreSQL para Event Store** - Adequado para esse tamanho, EventStoreDB seria overkill
2. **Debezium CDC** - Mais robusto que polling manual do outbox
3. **Saga Orchestration** - Melhor que Choreography para esse caso (visibilidade centralizada)
4. **Redis Cache** - Simples e eficaz, pode escalar para Redis Cluster depois
5. **Spring Security** - Padrão da indústria, bem suportado

### Dívidas Técnicas Conhecidas
1. Compensações da saga têm TODOs (não afeta demo)
2. AuthController desabilitado (pode habilitar quando implementar User entity)
3. Testes desatualizados (não afeta funcionalidade)
4. Observabilidade não configurada (infraestrutura existe)

### Quick Wins (< 2 horas cada)
- [ ] Adicionar Swagger UI
- [ ] Criar Postman Collection
- [ ] Adicionar health checks customizados
- [ ] Implementar paginação
- [ ] Adicionar CORS configuration
- [ ] Criar script de setup automatizado

---

## 🎉 Conclusão

Este projeto demonstra **arquitetura enterprise de alto nível** com padrões modernos. 

**Estado atual:** ⭐⭐⭐⭐ (4/5)
- Arquitetura sólida
- Código limpo e organizado
- Pronto para demonstração
- Fácil de evoluir

**Para chegar a 5/5:**
- Completar autenticação
- Testar query service
- Adicionar testes de integração
- Configurar observabilidade

---

**Documento criado em:** 2025-10-20  
**Versão:** 1.0  
**Autor:** Sistema Automatizado  
**Última atualização:** Após implementação do Debezium CDC

