-- Scripts úteis para monitorar o Outbox Pattern e Debezium

-- 1. Verificar eventos não processados
SELECT 
    id,
    aggregate_id,
    event_type,
    created_at,
    AGE(NOW(), created_at) as age
FROM outbox 
WHERE processed = false
ORDER BY created_at DESC
LIMIT 100;

-- 2. Estatísticas do outbox
SELECT 
    event_type,
    COUNT(*) as total,
    COUNT(*) FILTER (WHERE processed = true) as processed,
    COUNT(*) FILTER (WHERE processed = false) as pending,
    MIN(created_at) as oldest,
    MAX(created_at) as newest
FROM outbox
GROUP BY event_type;

-- 3. Taxa de processamento (últimas 24h)
SELECT 
    DATE_TRUNC('hour', created_at) as hour,
    COUNT(*) as created,
    COUNT(*) FILTER (WHERE processed = true) as processed,
    ROUND(AVG(EXTRACT(EPOCH FROM (processed_at - created_at))) * 1000, 2) as avg_latency_ms
FROM outbox
WHERE created_at > NOW() - INTERVAL '24 hours'
GROUP BY hour
ORDER BY hour DESC;

-- 4. Verificar replication slots do Debezium
SELECT 
    slot_name,
    plugin,
    slot_type,
    database,
    active,
    restart_lsn,
    confirmed_flush_lsn,
    pg_wal_lsn_diff(pg_current_wal_lsn(), restart_lsn) as lag_bytes,
    pg_size_pretty(pg_wal_lsn_diff(pg_current_wal_lsn(), restart_lsn)) as lag_size
FROM pg_replication_slots
WHERE slot_name LIKE '%debezium%';

-- 5. Verificar WAL acumulado
SELECT 
    pg_size_pretty(
        pg_wal_lsn_diff(pg_current_wal_lsn(), restart_lsn)
    ) as replication_lag
FROM pg_replication_slots 
WHERE slot_name = 'debezium_outbox_slot';

-- 6. Eventos mais antigos não processados
SELECT 
    id,
    event_type,
    aggregate_id,
    created_at,
    AGE(NOW(), created_at) as age
FROM outbox
WHERE processed = false
ORDER BY created_at ASC
LIMIT 10;

-- 7. Limpar eventos processados antigos (> 7 dias)
-- DELETE FROM outbox 
-- WHERE processed = true 
--   AND processed_at < NOW() - INTERVAL '7 days';

-- 8. Performance do Event Store
SELECT 
    event_type,
    COUNT(*) as total,
    MIN(created_at) as first_event,
    MAX(created_at) as last_event,
    COUNT(DISTINCT aggregate_id) as unique_aggregates
FROM events
GROUP BY event_type;

-- 9. Verificar consistência entre Event Store e Outbox
SELECT 
    e.event_type,
    COUNT(DISTINCT e.aggregate_id) as events_count,
    COUNT(DISTINCT o.aggregate_id) as outbox_count
FROM events e
LEFT JOIN outbox o ON e.aggregate_id = o.aggregate_id AND e.version = o.version
GROUP BY e.event_type;

-- 10. Alertas - Eventos pendentes por muito tempo (> 5 minutos)
SELECT 
    id,
    event_type,
    aggregate_id,
    created_at,
    AGE(NOW(), created_at) as age
FROM outbox
WHERE processed = false
  AND created_at < NOW() - INTERVAL '5 minutes'
ORDER BY created_at ASC;

