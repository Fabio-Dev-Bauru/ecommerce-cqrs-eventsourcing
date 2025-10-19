package com.ecommerce.saga.repository;

import com.ecommerce.saga.domain.SagaStatus;
import com.ecommerce.saga.entity.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstance, Long> {

    Optional<SagaInstance> findByCorrelationId(UUID correlationId);

    Optional<SagaInstance> findByOrderId(UUID orderId);

    List<SagaInstance> findByStatus(SagaStatus status);

    @Query("SELECT s FROM SagaInstance s WHERE s.status IN :statuses AND s.updatedAt < :timeout")
    List<SagaInstance> findTimeoutSagas(@Param("statuses") List<SagaStatus> statuses, 
                                        @Param("timeout") Instant timeout);

    @Query("SELECT s FROM SagaInstance s WHERE s.status = :status AND s.retryCount < :maxRetries")
    List<SagaInstance> findRetryableSagas(@Param("status") SagaStatus status,
                                          @Param("maxRetries") Integer maxRetries);
}

