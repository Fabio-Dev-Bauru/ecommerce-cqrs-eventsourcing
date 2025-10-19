package com.ecommerce.saga.entity;

import com.ecommerce.saga.domain.SagaStatus;
import com.ecommerce.saga.domain.SagaStep;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "saga_instance", indexes = {
    @Index(name = "idx_correlation_id", columnList = "correlationId"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class SagaInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID correlationId;

    @Column(nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStep currentStep;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<SagaStep> completedSteps = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> sagaData = new HashMap<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Instant completedAt;

    private String errorMessage;

    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addCompletedStep(SagaStep step) {
        if (completedSteps == null) {
            completedSteps = new ArrayList<>();
        }
        if (!completedSteps.contains(step)) {
            completedSteps.add(step);
        }
    }

    public void putSagaData(String key, Object value) {
        if (sagaData == null) {
            sagaData = new HashMap<>();
        }
        sagaData.put(key, value);
    }

    public Object getSagaData(String key) {
        return sagaData != null ? sagaData.get(key) : null;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}

