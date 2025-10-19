-- Script de inicialização do PostgreSQL para Debezium CDC

-- Habilitar replicação lógica (se não estiver habilitado)
-- Nota: Isso requer reinicialização do PostgreSQL
-- ALTER SYSTEM SET wal_level = 'logical';
-- ALTER SYSTEM SET max_replication_slots = 4;
-- ALTER SYSTEM SET max_wal_senders = 4;

-- Criar banco de dados para saga orchestrator
CREATE DATABASE saga_db;

-- Conectar ao banco order_command_db
\c order_command_db;

-- Verificar configuração de replicação
SELECT name, setting 
FROM pg_settings 
WHERE name IN ('wal_level', 'max_replication_slots', 'max_wal_senders');

-- Criar índices adicionais para performance
CREATE INDEX IF NOT EXISTS idx_outbox_not_processed 
ON outbox (created_at) 
WHERE processed = false;

-- Criar função para limpar outbox antiga (eventos já processados)
CREATE OR REPLACE FUNCTION cleanup_old_outbox_entries()
RETURNS void AS $$
BEGIN
    DELETE FROM outbox 
    WHERE processed = true 
      AND processed_at < NOW() - INTERVAL '7 days';
    
    RAISE NOTICE 'Cleaned up old outbox entries';
END;
$$ LANGUAGE plpgsql;

-- Criar job para executar cleanup (requer pg_cron extension)
-- SELECT cron.schedule('cleanup-outbox', '0 2 * * *', 'SELECT cleanup_old_outbox_entries()');

-- Grants para usuário Debezium (em produção, criar usuário específico)
-- CREATE USER debezium WITH REPLICATION PASSWORD 'debezium_password';
-- GRANT SELECT ON TABLE outbox TO debezium;
-- GRANT USAGE ON SCHEMA public TO debezium;

-- Verificar replication slots
SELECT slot_name, plugin, slot_type, database, active
FROM pg_replication_slots;

-- Verificar tamanho do WAL
SELECT pg_size_pretty(pg_wal_lsn_diff(pg_current_wal_lsn(), restart_lsn)) as wal_size
FROM pg_replication_slots 
WHERE slot_name = 'debezium_outbox_slot';

