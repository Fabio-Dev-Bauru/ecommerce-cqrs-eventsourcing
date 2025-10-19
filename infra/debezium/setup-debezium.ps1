# Script PowerShell para configurar Debezium Connector

Write-Host "Aguardando Kafka Connect estar pronto..." -ForegroundColor Yellow

do {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8083/connectors" -UseBasicParsing -ErrorAction Stop
        break
    }
    catch {
        Write-Host "Kafka Connect não está pronto ainda. Aguardando..." -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
} while ($true)

Write-Host "Kafka Connect está pronto!" -ForegroundColor Green

Write-Host "`nVerificando conectores existentes..." -ForegroundColor Cyan
Invoke-RestMethod -Uri "http://localhost:8083/connectors" -Method Get

Write-Host "`nRegistrando Outbox Connector..." -ForegroundColor Cyan
$connectorConfig = Get-Content -Path "register-connector.json" -Raw

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8083/connectors/" `
        -Method Post `
        -ContentType "application/json" `
        -Body $connectorConfig
    
    Write-Host "`nConector registrado com sucesso!" -ForegroundColor Green
}
catch {
    Write-Host "`nErro ao registrar conector: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`nVerificando status do conector..." -ForegroundColor Cyan
$status = Invoke-RestMethod -Uri "http://localhost:8083/connectors/outbox-connector/status"
$status | ConvertTo-Json -Depth 10

Write-Host "`nConectores ativos:" -ForegroundColor Cyan
$connectors = Invoke-RestMethod -Uri "http://localhost:8083/connectors"
$connectors | ConvertTo-Json

Write-Host "`nSetup do Debezium concluído!" -ForegroundColor Green

