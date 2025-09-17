package com.sparta.orderservice.order.infrastructure.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitResultConfig {

    @Value("${app.mq.result-exchange}")
    private String resultExchangeName;

    @Value("${app.mq.result-queue}")
    private String resultQueueName;

    @Bean
    public DirectExchange stockResultExchange() {
        return new DirectExchange(resultExchangeName, true, false);
    }

    @Bean
    public Queue stockResultQueue() {
        return QueueBuilder.durable(resultQueueName).build();
    }

    // 상품 서비스가 발행하는 두 라우팅키를 같은 큐에 바인딩
    @Bean
    public Binding bindDecreased() {
        return BindingBuilder.bind(stockResultQueue())
                .to(stockResultExchange()).with("stock.decreased");
    }

    @Bean
    public Binding bindRejected() {
        return BindingBuilder.bind(stockResultQueue())
                .to(stockResultExchange()).with("stock.rejected");
    }

    // 주문 서비스에서 결과 이벤트를 JSON으로 받기 위한 컨버터 (필요 시)
//    @Bean
//    public org.springframework.amqp.support.converter.Jackson2JsonMessageConverter messageConverter() {
//        var c = new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
//        c.setAlwaysConvertToInferredType(true);
//        c.setTrustedPackages("com.sparta.orderservice"); // 최소 범위로
//        return c;
//    }

    // (선택) 리스너 팩토리: 결과 리스너는 AUTO ack로도 충분
    @Bean
    public SimpleRabbitListenerContainerFactory resultListenerFactory(
            ConnectionFactory cf,
            org.springframework.amqp.support.converter.Jackson2JsonMessageConverter converter
    ) {
        var f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setAcknowledgeMode(AcknowledgeMode.AUTO);
        f.setMessageConverter(converter);
        return f;
    }
}

