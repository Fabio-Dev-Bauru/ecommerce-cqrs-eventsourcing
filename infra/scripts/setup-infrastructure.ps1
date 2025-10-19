# Script PowerShell para setup da infraestrutura

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Setup da Infraestrutura do E-commerce" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

Write-Host "`n1. Iniciando containers com Docker Compose..." -ForegroundColor Yellow
docker-compose up -d

Write-Host "`n2. Aguardando serviços estarem prontos..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "`n3. Verificando status dos containers..." -ForegroundColor Yellow
docker-compose ps

Write-Host "`n4. Verificando health dos serviços..." -ForegroundColor Yellow

# Check PostgreSQL
do {
    try {
        docker exec postgres-command pg_isready -U postgres 2>$null | Out-Null
        break
    }
    catch {
        Write-Host "Aguardando PostgreSQL Command..." -ForegroundColor Gray
        Start-Sleep -Seconds 2
    }
} while ($true)
Write-Host "✓ PostgreSQL Command está pronto" -ForegroundColor Green

do {
    try {
        docker exec postgres-query pg_isready -U postgres 2>$null | Out-Null
        break
    }
    catch {
        Write-Host "Aguardando PostgreSQL Query..." -ForegroundColor Gray
        Start-Sleep -Seconds 2
    }
} while ($true)
Write-Host "✓ PostgreSQL Query está pronto" -ForegroundColor Green

# Check Redis
do {
    try {
        docker exec redis redis-cli ping 2>$null | Out-Null
        break
    }
    catch {
        Write-Host "Aguardando Redis..." -ForegroundColor Gray
        Start-Sleep -Seconds 2
    }
} while ($true)
Write-Host "✓ Redis está pronto" -ForegroundColor Green

# Check Kafka Connect
do {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8083" -UseBasicParsing -ErrorAction Stop
        break
    }
    catch {
        Write-Host "Aguardando Kafka Connect..." -ForegroundColor Gray
        Start-Sleep -Seconds 2
    }
} while ($true)
Write-Host "✓ Kafka Connect está pronto" -ForegroundColor Green

Write-Host "`n5. Configurando Debezium Connector..." -ForegroundColor Yellow
Set-Location infra\debezium
.\setup-debezium.ps1
Set-Location ..\..

Write-Host "`n6. Criando tópicos Kafka..." -ForegroundColor Yellow

$topics = @(
    "order-events",
    "payment-commands",
    "payment-events",
    "inventory-commands",
    "inventory-events",
    "shipping-commands",
    "shipping-events"
)

foreach ($topic in $topics) {
    docker exec kafka kafka-topics --create --if-not-exists `
        --bootstrap-server localhost:9092 `
        --topic $topic `
        --partitions 3 `
        --replication-factor 1 2>$null
}

Write-Host "✓ Tópicos Kafka criados" -ForegroundColor Green

Write-Host "`n7. Listando tópicos Kafka..." -ForegroundColor Yellow
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

Write-Host "`n=========================================" -ForegroundColor Green
Write-Host "Setup concluído com sucesso!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

Write-Host "`nServiços disponíveis:" -ForegroundColor Yellow
Write-Host "  - PostgreSQL Command: localhost:5432"
Write-Host "  - PostgreSQL Query: localhost:5433"
Write-Host "  - Kafka: localhost:9093"
Write-Host "  - Kafka UI: http://localhost:8090"
Write-Host "  - Kafka Connect: http://localhost:8083"
Write-Host "  - Redis: localhost:6379"
Write-Host "  - Prometheus: http://localhost:9090"
Write-Host "  - Grafana: http://localhost:3000 (admin/admin)"
Write-Host "  - Kibana: http://localhost:5601"
Write-Host "  - Zipkin: http://localhost:9411"

Write-Host "`nPróximos passos:" -ForegroundColor Yellow
Write-Host "  1. Compilar: mvn clean install"
Write-Host "  2. Executar: mvn spring-boot:run -pl order-command-service"
Write-Host ""

