package org.onboardingservice.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {

    @Value("${rabbitmq.exchange.tenant}")
    private String tenantExchange;

    @Value("${rabbitmq.queue.tenant-approved}")
    private String tenantApprovedQueue;

    @Value("${rabbitmq.routing-key.tenant-approved}")
    private String tenantApprovedRoutingKey;

    @Value("${rabbitmq.queue.tenant-approved-dlq}")
    private String tenantApprovedDlq;

    @Value("${rabbitmq.exchange.dead-letter}")
    private String deadLetterExchange;

    @Value("${rabbitmq.queue.user-registered}")
    private String userRegisteredQueue;

    @Value("${rabbitmq.queue.user-registered-dlq}")
    private String userRegisteredDlq;

    @Value("${rabbitmq.routing-key.user-registered}")
    private String userRegisteredRoutingKey;

    @Bean
    public TopicExchange tenantExchange() {
        return ExchangeBuilder.topicExchange(tenantExchange).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(deadLetterExchange).durable(true).build();
    }

    @Bean
    public Queue tenantApprovedQueue() {
        return QueueBuilder.durable(tenantApprovedQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", tenantApprovedDlq)
                .build();
    }

    @Bean
    public Queue tenantApprovedDeadLetterQueue() {
        return QueueBuilder.durable(tenantApprovedDlq).build();
    }

    @Bean
    public Binding tenantApprovedBinding(Queue tenantApprovedQueue, TopicExchange tenantExchange) {
        return BindingBuilder.bind(tenantApprovedQueue)
                .to(tenantExchange)
                .with(tenantApprovedRoutingKey);
    }

    @Bean
    public Binding deadLetterBinding(Queue tenantApprovedDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(tenantApprovedDeadLetterQueue)
                .to(deadLetterExchange)
                .with(tenantApprovedDlq);
    }

    // ── user-registered ───────────────────────────────────────────────────────

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(userRegisteredQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", userRegisteredDlq)
                .build();
    }

    @Bean
    public Queue userRegisteredDeadLetterQueue() {
        return QueueBuilder.durable(userRegisteredDlq).build();
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange tenantExchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(tenantExchange).with(userRegisteredRoutingKey);
    }

    @Bean
    public Binding userRegisteredDlqBinding(Queue userRegisteredDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(userRegisteredDeadLetterQueue).to(deadLetterExchange).with(this.userRegisteredDlq);
    }
}