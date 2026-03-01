package com.navarro.microcredit.infraestructure.configuration.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String LOAN_REQUEST_QUEUE = "loan-request-queue";

    @Bean
    public Queue loanRequestQueue() {
        return new Queue(LOAN_REQUEST_QUEUE, true);
    }
}
