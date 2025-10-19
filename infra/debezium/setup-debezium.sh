#!/bin/bash

# Script para configurar Debezium Connector

echo "Aguardando Kafka Connect estar pronto..."
until curl -f -s http://localhost:8083/connectors > /dev/null; do
    echo "Kafka Connect não está pronto ainda. Aguardando..."
    sleep 5
done

echo "Kafka Connect está pronto!"

echo "Verificando conectores existentes..."
curl -s http://localhost:8083/connectors

echo -e "\n\nRegistrando Outbox Connector..."
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" \
  http://localhost:8083/connectors/ \
  -d @register-connector.json

echo -e "\n\nConector registrado com sucesso!"

echo -e "\nVerificando status do conector..."
curl -s http://localhost:8083/connectors/outbox-connector/status | jq

echo -e "\n\nConectores ativos:"
curl -s http://localhost:8083/connectors | jq

echo -e "\n\nSetup do Debezium concluído!"

