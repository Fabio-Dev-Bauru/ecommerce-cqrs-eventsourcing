-- Script de inicialização do PostgreSQL para Debezium CDC

-- Criar banco de dados para saga orchestrator
CREATE DATABASE saga_db;

-- Conectar ao banco order_command_db
\c order_command_db;

-- Verificar configuração de replicação
SELECT name, setting 
FROM pg_settings 
WHERE name IN ('wal_level', 'max_replication_slots', 'max_wal_senders');

-- Nota: Índices e funções serão criados após Spring JPA criar as tabelas
-- O Spring JPA com ddl-auto=update criará as tabelas automaticamente

