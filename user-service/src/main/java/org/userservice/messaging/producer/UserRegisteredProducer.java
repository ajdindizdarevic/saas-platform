package org.userservice.messaging.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.userservice.messaging.event.UserRegisteredEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.tenant}")
    private String exchange;

    @Value("${rabbitmq.routing-key.user-registered}")
    private String routingKey;

    public void publish(UserRegisteredEvent event) {
        log.info("Publishing UserRegisteredEvent for user={}", event.getEmail());
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}