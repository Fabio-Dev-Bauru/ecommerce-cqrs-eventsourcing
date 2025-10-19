#!/bin/bash

set -e

echo "========================================="
echo "Setup da Infraestrutura do E-commerce"
echo "========================================="

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}1. Iniciando containers com Docker Compose...${NC}"
docker-compose up -d

echo -e "${YELLOW}2. Aguardando serviços estarem prontos...${NC}"
sleep 30

echo -e "${YELLOW}3. Verificando status dos containers...${NC}"
docker-compose ps

echo -e "${YELLOW}4. Verificando health dos serviços...${NC}"

# Check PostgreSQL
until docker exec postgres-command pg_isready -U postgres > /dev/null 2>&1; do
  echo "Aguardando PostgreSQL Command..."
  sleep 2
done
echo -e "${GREEN}✓ PostgreSQL Command está pronto${NC}"

until docker exec postgres-query pg_isready -U postgres > /dev/null 2>&1; do
  echo "Aguardando PostgreSQL Query..."
  sleep 2
done
echo -e "${GREEN}✓ PostgreSQL Query está pronto${NC}"

# Check Kafka
until docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; do
  echo "Aguardando Kafka..."
  sleep 2
done
echo -e "${GREEN}✓ Kafka está pronto${NC}"

# Check Redis
until docker exec redis redis-cli ping > /dev/null 2>&1; do
  echo "Aguardando Redis..."
  sleep 2
done
echo -e "${GREEN}✓ Redis está pronto${NC}"

# Check Kafka Connect
until curl -s http://localhost:8083 > /dev/null; do
  echo "Aguardando Kafka Connect..."
  sleep 2
done
echo -e "${GREEN}✓ Kafka Connect está pronto${NC}"

echo -e "${YELLOW}5. Configurando Debezium Connector...${NC}"
cd infra/debezium
./setup-debezium.sh
cd ../..

echo -e "${YELLOW}6. Criando tópicos Kafka...${NC}"
docker exec kafka kafka-topics --create --if-not-exists \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --partitions 3 \
  --replication-factor 1

docker exec kafka kafka-topics --create --if-not-exists \
  --bootstrap-server localhost:9092 \
  --topic payment-commands \
  --partitions 3 \
  --replication-factor 1

docker exec kafka kafka-topics --create --if-not-exists \
  --bootstrap-server localhost:9092 \
  --topic payment-events \
  --partitions 3 \
  --replication-factor 1

docker exec kafka kafka-topics --create --if-not-exists \
  --bootstrap-server localhost:9092 \
  --topic inventory-commands \
  --partitions 3 \
  --replication-factor 1

docker exec kafka kafka-topics --create --if-not-exists \
  --bootstrap-server localhost:9092 \
  --topic inventory-events \
  --partitions 3 \
  --replication-factor 1

docker exec kafka kafka-topics --create --if-not-exists \
  --bootstrap-server localhost:9092 \
  --topic shipping-commands \
  --partitions 3 \
  --replication-factor 1

docker exec kafka kafka-topics --create --if-not-exists \
  --bootstrap-server localhost:9092 \
  --topic shipping-events \
  --partitions 3 \
  --replication-factor 1

echo -e "${GREEN}✓ Tópicos Kafka criados${NC}"

echo -e "${YELLOW}7. Listando tópicos Kafka...${NC}"
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}Setup concluído com sucesso!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo -e "${YELLOW}Serviços disponíveis:${NC}"
echo "  - PostgreSQL Command: localhost:5432"
echo "  - PostgreSQL Query: localhost:5433"
echo "  - Kafka: localhost:9093"
echo "  - Kafka UI: http://localhost:8090"
echo "  - Kafka Connect: http://localhost:8083"
echo "  - Redis: localhost:6379"
echo "  - Prometheus: http://localhost:9090"
echo "  - Grafana: http://localhost:3000 (admin/admin)"
echo "  - Kibana: http://localhost:5601"
echo "  - Zipkin: http://localhost:9411"
echo ""
echo -e "${YELLOW}Próximos passos:${NC}"
echo "  1. Compilar: mvn clean install"
echo "  2. Executar: mvn spring-boot:run -pl order-command-service"
echo ""

