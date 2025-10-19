package com.ecommerce.shared.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public abstract class AggregateRoot {
    
    protected UUID id;
    protected Integer version;
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();

    protected AggregateRoot() {
        this.version = 0;
    }

    protected AggregateRoot(UUID id) {
        this.id = id;
        this.version = 0;
    }

    protected void applyNewEvent(DomainEvent event) {
        apply(event);
        uncommittedEvents.add(event);
    }

    protected abstract void apply(DomainEvent event);

    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }

    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(uncommittedEvents);
    }

    public void loadFromHistory(List<DomainEvent> history) {
        for (DomainEvent event : history) {
            apply(event);
            version++;
        }
    }

    protected void incrementVersion() {
        this.version++;
    }
}

