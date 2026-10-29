package org.onboardingservice.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onboardingservice.messaging.event.UserRegisteredEvent;
import org.onboardingservice.service.OnboardingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final OnboardingService onboardingService;

    @RabbitListener(queues = "${rabbitmq.queue.user-registered}")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for user={}", event.getEmail());
        try {
            onboardingService.createRequestFromRegistration(event);
        } catch (Exception ex) {
            log.error("Failed to create onboarding request for {}: {}", event.getEmail(), ex.getMessage());
            throw ex;  // rethrow → message goes to DLQ
        }
    }
}