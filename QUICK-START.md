# ⚡ Quick Start - E-commerce CQRS Event Sourcing

Guia rápido para executar o projeto em 5 minutos!

## 🎯 Pré-requisitos

- ✅ Java 17+
- ✅ Maven 3.8+
- ✅ Docker Desktop rodando
- ✅ 8GB RAM disponível

## 🚀 Passos

### 1️⃣ Iniciar Infraestrutura (3 minutos)

**Windows:**
```powershell
.\infra\scripts\setup-infrastructure.ps1
```

**Linux/Mac:**
```bash
chmod +x infra/scripts/setup-infrastructure.sh
./infra/scripts/setup-infrastructure.sh
```

Aguarde a mensagem: **"Setup concluído com sucesso!"**

### 2️⃣ Compilar Projeto (1 minuto)

```bash
mvn clean install -DskipTests
```

### 3️⃣ Executar Serviços (3 terminais)

**Terminal 1:**
```bash
mvn spring-boot:run -pl order-command-service
```

**Terminal 2:**
```bash
mvn spring-boot:run -pl order-query-service
```

**Terminal 3:**
```bash
mvn spring-boot:run -pl saga-orchestrator
```

Aguarde: `Started [Service]Application in X seconds`

### 4️⃣ Testar! 🎉

#### A. Fazer Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Salve o token** da resposta!

#### B. Criar Pedido

```bash
# Substitua <SEU_TOKEN> pelo token recebido
TOKEN="<SEU_TOKEN>"

curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-123",
    "items": [
      {
        "productId": "PROD-001",
        "productName": "Laptop Dell XPS 15",
        "quantity": 1,
        "unitPrice": 7500.00
      },
      {
        "productId": "PROD-002",
        "productName": "Mouse Logitech MX Master",
        "quantity": 2,
        "unitPrice": 399.90
      }
    ]
  }'
```

**Salve o orderId** da resposta!

#### C. Consultar Pedido

```bash
# Substitua <ORDER_ID>
ORDER_ID="<ORDER_ID>"

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/v1/orders/$ORDER_ID
```

#### D. Verificar Saga

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8082/api/v1/sagas/order/$ORDER_ID
```

---

## 🎨 Acessar Dashboards

| Ferramenta | URL | Descrição |
|------------|-----|-----------|
| **Grafana** | http://localhost:3000 | Métricas e dashboards (admin/admin) |
| **Kibana** | http://localhost:5601 | Logs centralizados |
| **Zipkin** | http://localhost:9411 | Distributed tracing |
| **Kafka UI** | http://localhost:8090 | Monitoramento Kafka |
| **Prometheus** | http://localhost:9090 | Métricas raw |

---

## 📊 Verificar Status

### Health Checks

```bash
# Command Service
curl http://localhost:8080/actuator/health

# Query Service
curl http://localhost:8081/actuator/health

# Saga Orchestrator
curl http://localhost:8082/actuator/health
```

### Logs

```bash
# Ver logs do Command Service
docker-compose logs -f order-command-service

# Ou direto na aplicação Spring Boot
```

### Kafka Topics

Abra **Kafka UI**: http://localhost:8090
- Verifique tópico `order-events`
- Veja mensagens sendo publicadas

### Debezium Connector

```bash
# Status
curl http://localhost:8083/connectors/outbox-connector/status

# Ou via Kafka UI
```

---

## 🐛 Troubleshooting

### Containers não sobem

```bash
docker-compose ps
docker-compose logs [service-name]

# Restart
docker-compose down
docker-compose up -d
```

### Porta já em uso

```bash
# Verificar porta 8080
netstat -ano | findstr :8080  # Windows
lsof -i :8080                  # Linux/Mac

# Matar processo ou mudar porta via env var:
export SERVER_PORT=8085
mvn spring-boot:run -pl order-command-service
```

### Erro de conexão com banco

```bash
# Verificar PostgreSQL
docker exec -it postgres-command psql -U postgres -d order_command_db

# Recriar banco
docker-compose down -v
docker-compose up -d
```

---

## 📱 Postman Collection

**Importar collection:** `docs/postman/ecommerce-cqrs.postman_collection.json` (TODO)

---

## 🎓 Próximos Passos

1. ✅ Leia o `README.md` para documentação completa
2. ✅ Consulte `docs/PROJECT-STATUS.md` para arquitetura
3. ✅ Veja `docs/ADR/` para decisões arquiteturais
4. ✅ Explore os dashboards no Grafana
5. ✅ Experimente criar múltiplos pedidos e ver no Kafka UI

---

## 🎯 Comandos Úteis

```bash
# Build completo
mvn clean install

# Rodar todos os testes
mvn test

# Cobertura de código
mvn test jacoco:report

# Parar infraestrutura
docker-compose down

# Parar e limpar volumes
docker-compose down -v

# Ver logs
docker-compose logs -f

# Restart serviço específico
docker-compose restart kafka
```

---

**🎉 Pronto! Você tem um sistema de e-commerce enterprise rodando localmente!**

**Explore, teste e divirta-se! 🚀**

