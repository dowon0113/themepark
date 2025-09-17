package com.business.productservice.infrastructure.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.mq.exchange}")   private String exchange;
    @Value("${app.mq.routingKey}") private String routingKey;
    @Value("${app.mq.queue}")      private String queue;
    @Value("${app.mq.dlx}")        private String dlx;
    @Value("${app.mq.dlq}")        private String dlq;

    @Bean
    public DirectExchange stockExchange() { return new DirectExchange(exchange, true, false); }

    @Bean
    public DirectExchange deadLetterExchange() { return new DirectExchange(dlx, true, false); }

    @Bean
    public Queue stockQueue() {
        return QueueBuilder.durable(queue)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", dlq)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() { return QueueBuilder.durable(dlq).build(); }

    @Bean
    public Binding stockBinding() {
        return BindingBuilder.bind(stockQueue()).to(stockExchange()).with(routingKey);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(dlq);
    }

    @Bean
    public org.springframework.amqp.support.converter.Jackson2JsonMessageConverter messageConverter() {
        return new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory manualAckFactory(
            ConnectionFactory cf,
            org.springframework.amqp.support.converter.Jackson2JsonMessageConverter converter
    ) {
        var f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        f.setConcurrentConsumers(4);
        f.setMaxConcurrentConsumers(12);
        f.setMessageConverter(converter); // ★ JSON → 객체 변환
        return f;
    }

}
