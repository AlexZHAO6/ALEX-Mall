package com.alex.mallorder.config;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyRabbitConfig {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @PostConstruct
    public void initRabbitTemplate() {
        rabbitTemplate.setConfirmCallback((msg, ack, cause) -> {
            System.out.println("Message sent to exchange: " + msg.getId());
            if (ack) {
                System.out.println("Message successfully received by exchange");
            } else {
                System.out.println("Message failed to reach exchange: " + cause);
            }
        });

        rabbitTemplate.setReturnsCallback(returned -> {
            System.out.println("Message lost");
            System.out.println("Exchange: " + returned.getExchange());
            System.out.println("Routing Key: " + returned.getRoutingKey());
            System.out.println("Reply Code: " + returned.getReplyCode());
            System.out.println("Reply Text: " + returned.getReplyText());
            System.out.println("Message: " + returned.getMessage());
        });
    }
}
