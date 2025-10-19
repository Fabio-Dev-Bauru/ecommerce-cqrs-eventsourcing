package com.ecommerce.shared.domain;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID getAggregateId();
    UUID getCorrelationId();
    UUID getCausationId();
    Instant getTimestamp();
    Integer getVersion();
    String getEventType();
}

