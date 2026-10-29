package org.userservice.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.tenant}")
    private String tenantExchange;

    @Value("${rabbitmq.exchange.dead-letter}")
    private String deadLetterExchange;

    @Value("${rabbitmq.queue.tenant-created}")
    private String tenantCreatedQueue;

    @Value("${rabbitmq.queue.tenant-created-dlq}")
    private String tenantCreatedDlq;

    @Value("${rabbitmq.routing-key.tenant-created}")
    private String tenantCreatedRoutingKey;

    @Value("${rabbitmq.queue.user-registered}")
    private String userRegisteredQueue;

    @Value("${rabbitmq.queue.user-registered-dlq}")
    private String userRegisteredDlq;

    @Value("${rabbitmq.routing-key.user-registered}")
    private String userRegisteredRoutingKey;

    // ── converter / template ──────────────────────────────────────────────────

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(jsonMessageConverter());
        return tpl;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory cf) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    // ── exchanges ─────────────────────────────────────────────────────────────

    @Bean
    public TopicExchange tenantExchange() {
        return ExchangeBuilder.topicExchange(tenantExchange).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(deadLetterExchange).durable(true).build();
    }

    // ── queues ────────────────────────────────────────────────────────────────

    @Bean
    public Queue tenantCreatedQueue() {
        return QueueBuilder.durable(tenantCreatedQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", tenantCreatedDlq)
                .build();
    }

    @Bean
    public Queue tenantCreatedDlq() {
        return QueueBuilder.durable(tenantCreatedDlq).build();
    }

    // ── bindings ──────────────────────────────────────────────────────────────

    @Bean
    public Binding tenantCreatedBinding(Queue tenantCreatedQueue, TopicExchange tenantExchange) {
        return BindingBuilder.bind(tenantCreatedQueue).to(tenantExchange).with(tenantCreatedRoutingKey);
    }

    @Bean
    public Binding tenantCreatedDlqBinding(Queue tenantCreatedDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(tenantCreatedDlq).to(deadLetterExchange).with(this.tenantCreatedDlq);
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
    public Queue userRegisteredDlq() {
        return QueueBuilder.durable(userRegisteredDlq).build();
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange tenantExchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(tenantExchange).with(userRegisteredRoutingKey);
    }

    @Bean
    public Binding userRegisteredDlqBinding(Queue userRegisteredDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(userRegisteredDlq).to(deadLetterExchange).with(this.userRegisteredDlq);
    }
}