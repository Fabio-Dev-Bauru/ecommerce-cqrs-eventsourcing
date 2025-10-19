package com.ecommerce.saga.scheduler;

import com.ecommerce.saga.domain.SagaStatus;
import com.ecommerce.saga.entity.SagaInstance;
import com.ecommerce.saga.repository.SagaInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaTimeoutScheduler {

    private final SagaInstanceRepository sagaRepository;

    @Value("${saga.timeout-minutes:15}")
    private Integer timeoutMinutes;

    @Scheduled(fixedDelay = 60000) // Executar a cada 1 minuto
    @Transactional
    public void checkTimeoutSagas() {
        log.debug("Checking for timeout sagas...");

        Instant timeoutThreshold = Instant.now().minusSeconds(timeoutMinutes * 60L);

        List<SagaStatus> pendingStatuses = Arrays.asList(
                SagaStatus.STARTED,
                SagaStatus.PAYMENT_PENDING,
                SagaStatus.INVENTORY_PENDING,
                SagaStatus.SHIPPING_PENDING
        );

        List<SagaInstance> timeoutSagas = sagaRepository.findTimeoutSagas(pendingStatuses, timeoutThreshold);

        if (!timeoutSagas.isEmpty()) {
            log.warn("Found {} timeout sagas", timeoutSagas.size());

            for (SagaInstance saga : timeoutSagas) {
                log.error("Saga timeout: {} - Status: {} - Step: {} - Created: {}",
                        saga.getCorrelationId(), saga.getStatus(), 
                        saga.getCurrentStep(), saga.getCreatedAt());

                saga.setStatus(SagaStatus.FAILED);
                saga.setErrorMessage("Saga timeout after " + timeoutMinutes + " minutes");
                saga.setCompletedAt(Instant.now());
                sagaRepository.save(saga);
            }
        }
    }

    @Scheduled(fixedDelay = 120000) // Executar a cada 2 minutos
    @Transactional
    public void retryFailedSagas() {
        log.debug("Checking for retryable sagas...");

        Integer maxRetries = 3;
        List<SagaInstance> retryableSagas = sagaRepository.findRetryableSagas(SagaStatus.FAILED, maxRetries);

        if (!retryableSagas.isEmpty()) {
            log.info("Found {} retryable sagas", retryableSagas.size());

            for (SagaInstance saga : retryableSagas) {
                log.info("Retrying saga: {} - Retry count: {}", 
                        saga.getCorrelationId(), saga.getRetryCount());

                saga.incrementRetryCount();
                saga.setUpdatedAt(Instant.now());
                sagaRepository.save(saga);

                // TODO: Implementar lógica de retry baseada no currentStep
            }
        }
    }
}

