package org.tenantservice.messaging.config;

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

    /* ── exchanges ── */
    @Value("${rabbitmq.exchange.tenant}")
    private String tenantExchange;

    @Value("${rabbitmq.exchange.dead-letter}")
    private String deadLetterExchange;

    /* ── queues consumed by this service ── */
    @Value("${rabbitmq.queue.tenant-approved}")
    private String tenantApprovedQueue;

    @Value("${rabbitmq.queue.tenant-approved-dlq}")
    private String tenantApprovedDlq;

    @Value("${rabbitmq.routing-key.tenant-approved}")
    private String tenantApprovedRoutingKey;

    /* ── queues produced by this service ── */
    @Value("${rabbitmq.queue.tenant-created}")
    private String tenantCreatedQueue;

    @Value("${rabbitmq.routing-key.tenant-created}")
    private String tenantCreatedRoutingKey;

    // ── message converter ─────────────────────────────────────────────────────

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

    // ── inbound: tenant-approved ──────────────────────────────────────────────

    @Bean
    public Queue tenantApprovedQueue() {
        return QueueBuilder.durable(tenantApprovedQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", tenantApprovedDlq)
                .build();
    }

    @Bean
    public Queue tenantApprovedDlq() {
        return QueueBuilder.durable(tenantApprovedDlq).build();
    }

    @Bean
    public Binding tenantApprovedBinding(Queue tenantApprovedQueue, TopicExchange tenantExchange) {
        return BindingBuilder.bind(tenantApprovedQueue).to(tenantExchange).with(tenantApprovedRoutingKey);
    }

    @Bean
    public Binding tenantApprovedDlqBinding(Queue tenantApprovedDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(tenantApprovedDlq).to(deadLetterExchange).with(this.tenantApprovedDlq);
    }

    // ── outbound: tenant-created ──────────────────────────────────────────────

    @Bean
    public Queue tenantCreatedQueue() {
        return QueueBuilder.durable(tenantCreatedQueue).build();
    }

    @Bean
    public Binding tenantCreatedBinding(Queue tenantCreatedQueue, TopicExchange tenantExchange) {
        return BindingBuilder.bind(tenantCreatedQueue).to(tenantExchange).with(tenantCreatedRoutingKey);
    }
}