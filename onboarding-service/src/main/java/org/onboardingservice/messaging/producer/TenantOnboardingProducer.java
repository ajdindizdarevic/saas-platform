package org.onboardingservice.messaging.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onboardingservice.messaging.event.TenantApprovedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantOnboardingProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.tenant}")
    private String tenantExchange;

    @Value("${rabbitmq.routing-key.tenant-approved}")
    private String tenantApprovedRoutingKey;

    public void publishTenantApprovedEvent(TenantApprovedEvent event) {
        log.info("Publishing TenantApprovedEvent for company: {}, requestId: {}",
                event.getCompanyName(), event.getOnboardingRequestId());
        try {
            rabbitTemplate.convertAndSend(tenantExchange, tenantApprovedRoutingKey, event);
            log.info("Successfully published TenantApprovedEvent with eventId: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish TenantApprovedEvent for requestId: {}",
                    event.getOnboardingRequestId(), e);
            throw new RuntimeException("Failed to publish tenant approved event", e);
        }
    }
}