# E-commerce CQRS Event Sourcing

Sistema de e-commerce enterprise utilizando **CQRS**, **Event Sourcing**, **Saga Pattern** e **Debezium CDC**.

## 🎯 Arquitetura

### Padrões Implementados
- **CQRS** (Command Query Responsibility Segregation)
- **Event Sourcing** com Event Store completo
- **Outbox Pattern** para garantia de entrega de eventos
- **Saga Pattern** para transações distribuídas
- **CDC** (Change Data Capture) com Debezium

### Stack Tecnológica

#### Backend
- Java 17
- Spring Boot 3.0.0
- Spring Data JPA
- Lombok
- Bean Validation

#### Mensageria & Streaming
- Apache Kafka 7.5.0
- Kafka Connect
- Debezium 2.4
- Schema Registry

#### Bancos de Dados
- PostgreSQL 15 (Write & Read Models)
- Redis 7 (Cache)

#### Observabilidade
- Prometheus (Métricas)
- Grafana (Dashboards)
- ELK Stack (Logs)
  - Elasticsearch 8.10
  - Logstash 8.10
  - Kibana 8.10
- Zipkin (Distributed Tracing)

## 📋 Pré-requisitos

- **Java 17+**
- **Maven 3.8+**
- **Docker & Docker Compose**
- **8GB RAM** mínimo recomendado

## 🚀 Como Executar

### 1. Iniciar Infraestrutura (Automático)

**Linux/Mac:**
```bash
chmod +x infra/scripts/setup-infrastructure.sh
./infra/scripts/setup-infrastructure.sh
```

**Windows:**
```powershell
.\infra\scripts\setup-infrastructure.ps1
```

O script automático irá:
- ✅ Subir todos os containers
- ✅ Aguardar serviços estarem prontos
- ✅ Configurar Debezium Connector
- ✅ Criar tópicos Kafka
- ✅ Validar health de todos os serviços

### 1.1 Iniciar Infraestrutura (Manual)

```bash
# Subir todos os serviços de infraestrutura
docker-compose up -d

# Verificar status dos containers
docker-compose ps

# Ver logs
docker-compose logs -f [service-name]

# Configurar Debezium (após containers estarem prontos)
cd infra/debezium
./setup-debezium.sh  # Linux/Mac
# ou
.\setup-debezium.ps1  # Windows
```

### 2. Compilar e Executar Aplicação

```bash
# Compilar o projeto
mvn clean install

# Executar order-command-service
mvn spring-boot:run -pl order-command-service

# Executar order-query-service (quando implementado)
mvn spring-boot:run -pl order-query-service

# Executar saga-orchestrator (quando implementado)
mvn spring-boot:run -pl saga-orchestrator
```

## 🏗️ Estrutura do Projeto

```
ecommerce-cqrs-eventsourcing/
├── shared/                          # Módulo compartilhado
│   └── src/main/java/.../
│       ├── events/                  # Eventos de domínio
│       └── response/                # DTOs de resposta
│
├── order-command-service/           # Serviço de comandos (Write Model)
│   └── src/main/java/.../
│       ├── controller/              # REST Controllers
│       ├── service/                 # Lógica de negócio
│       ├── entity/                  # Entidades JPA (Event, Outbox)
│       ├── repository/              # Repositórios
│       ├── dto/                     # Data Transfer Objects
│       ├── exception/               # Exception Handlers
│       └── util/                    # Utilitários
│
├── order-query-service/             # Serviço de consultas (Read Model) [EM DESENVOLVIMENTO]
├── saga-orchestrator/               # Orquestrador de Sagas [EM DESENVOLVIMENTO]
│
├── infra/                           # Configurações de infraestrutura
│   ├── prometheus/                  # Config Prometheus
│   ├── grafana/                     # Config Grafana
│   └── logstash/                    # Config Logstash
│
└── docker-compose.yml               # Orquestração de containers
```

## 🔌 Portas e Endpoints

### Aplicações
- **order-command-service**: http://localhost:8080
  - API: `/api/v1/orders`
  - Health: `/actuator/health`
  - Metrics: `/actuator/prometheus`
- **order-query-service**: http://localhost:8081 (em desenvolvimento)
- **saga-orchestrator**: http://localhost:8082 (em desenvolvimento)

### Infraestrutura
- **PostgreSQL Command**: `localhost:5432`
- **PostgreSQL Query**: `localhost:5433`
- **Kafka**: `localhost:9093` (external) / `9092` (internal)
- **Zookeeper**: `localhost:2181`
- **Schema Registry**: http://localhost:8081
- **Kafka Connect**: http://localhost:8083
- **Kafka UI**: http://localhost:8090
- **Redis**: `localhost:6379`
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601
- **Zipkin**: http://localhost:9411

## 📡 Exemplos de API

### Criar Pedido

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-123",
    "items": [
      {
        "productId": "PROD-001",
        "productName": "Laptop Dell",
        "quantity": 1,
        "unitPrice": 3500.00
      },
      {
        "productId": "PROD-002",
        "productName": "Mouse Logitech",
        "quantity": 2,
        "unitPrice": 89.90
      }
    ]
  }'
```

### Resposta

```json
{
  "message": "Order created successfully",
  "status": "CREATED",
  "data": {
    "orderId": "550e8400-e29b-41d4-a716-446655440000"
  },
  "timestamp": "2024-10-19T22:00:00Z"
}
```

## 🔧 Configuração de Ambiente

Copie o arquivo `.env.example` para `.env` e ajuste as variáveis conforme necessário:

```bash
cp .env.example .env
```

## 📊 Monitoramento

### Prometheus
Acesse http://localhost:9090 para visualizar métricas e criar queries.

### Grafana
1. Acesse http://localhost:3000 (admin/admin)
2. Os datasources já estão provisionados automaticamente
3. Importe dashboards ou crie os seus

### Kibana
Acesse http://localhost:5601 para visualizar logs centralizados.

### Zipkin
Acesse http://localhost:9411 para rastreamento distribuído.

## 🧪 Testes

```bash
# Executar todos os testes
mvn test

# Executar testes de um módulo específico
mvn test -pl order-command-service

# Executar com cobertura
mvn test jacoco:report
```

## 🐛 Troubleshooting

### Containers não sobem
```bash
# Verificar logs
docker-compose logs -f

# Limpar volumes e reiniciar
docker-compose down -v
docker-compose up -d
```

### Aplicação não conecta no banco
Verifique se o PostgreSQL está rodando e acessível:
```bash
docker-compose ps postgres-command
```

### Kafka não está recebendo eventos
Verifique o Kafka UI em http://localhost:8090

## 📝 Próximos Passos

- [ ] Implementar order-query-service
- [ ] Implementar saga-orchestrator
- [ ] Configurar Debezium Connector
- [ ] Adicionar autenticação JWT
- [ ] Implementar testes de integração
- [ ] Adicionar testes de carga

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT.
