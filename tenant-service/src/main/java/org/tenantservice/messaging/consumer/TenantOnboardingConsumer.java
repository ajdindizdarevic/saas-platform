package org.tenantservice.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tenantservice.messaging.event.TenantApprovedEvent;
import org.tenantservice.service.TenantInitializationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantOnboardingConsumer {

    private final TenantInitializationService tenantInitializationService;

    /**
     * Listens on the tenant-approved queue. On receipt, drives the full
     * tenant provisioning pipeline (schema → resources → publish TenantCreatedEvent).
     * If an exception escapes the handler the message is routed to the DLQ
     * (defaultRequeueRejected = false set in RabbitMQConfig).
     */
    @RabbitListener(queues = "${rabbitmq.queue.tenant-approved}",
            containerFactory = "rabbitListenerContainerFactory")
    public void handleTenantApproved(TenantApprovedEvent event) {
        log.info("Received TenantApprovedEvent: eventId={}, company={}, requestId={}",
                event.getEventId(), event.getCompanyName(), event.getOnboardingRequestId());
        try {
            tenantInitializationService.initialize(event);
            log.info("Tenant initialization completed for onboardingRequestId={}",
                    event.getOnboardingRequestId());
        } catch (Exception ex) {
            log.error("Tenant initialization failed for eventId={}: {}",
                    event.getEventId(), ex.getMessage(), ex);
            // re-throw so Spring AMQP routes to DLQ
            throw ex;
        }
    }
}