package com.navarro.microcredit.infraestructure.messaging;

import com.navarro.microcredit.domain.event.LoanCreateEvent;
import com.navarro.microcredit.infraestructure.configuration.rabbitmq.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LoanEventListener {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLoanCreated(LoanCreateEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.LOAN_REQUEST_QUEUE,
                event.loanId().toString());
    }
}
