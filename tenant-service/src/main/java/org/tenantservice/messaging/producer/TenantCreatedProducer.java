package org.tenantservice.messaging.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tenantservice.messaging.event.TenantCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantCreatedProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.tenant}")
    private String tenantExchange;

    @Value("${rabbitmq.routing-key.tenant-created}")
    private String tenantCreatedRoutingKey;

    public void publish(TenantCreatedEvent event) {
        log.info("Publishing TenantCreatedEvent: tenantId={}, schema={}, eventId={}",
                event.getTenantId(), event.getSchemaName(), event.getEventId());
        try {
            rabbitTemplate.convertAndSend(tenantExchange, tenantCreatedRoutingKey, event);
            log.info("TenantCreatedEvent published successfully for tenantId={}", event.getTenantId());
        } catch (Exception ex) {
            log.error("Failed to publish TenantCreatedEvent for tenantId={}: {}",
                    event.getTenantId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to publish TenantCreatedEvent", ex);
        }
    }
}