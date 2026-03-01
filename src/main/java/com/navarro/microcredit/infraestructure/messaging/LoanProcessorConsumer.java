package com.navarro.microcredit.infraestructure.messaging;

import com.navarro.microcredit.domain.entity.Loan;
import com.navarro.microcredit.domain.enums.StateLoan;
import com.navarro.microcredit.infraestructure.configuration.rabbitmq.RabbitMQConfig;
import com.navarro.microcredit.infraestructure.external.serasa.SerasaMockService;
import com.navarro.microcredit.infraestructure.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanProcessorConsumer {

    private final LoanRepository loanRepository;
    private final SerasaMockService serasaMockService;

    @RabbitListener(queues = RabbitMQConfig.LOAN_REQUEST_QUEUE)
    public void processLoan(String loanIdString) {
        log.info("Recebendo empréstimo para processamento. ID: {}", loanIdString);

        try {
            UUID loanId = UUID.fromString(loanIdString);
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado no banco!"));

            if (!serasaMockService.isEligible(loan.getClient().getCpf())) {
                finalizeLoan(loan, StateLoan.DENIED);
                log.warn("Empréstimo {} NEGADO pelo Serasa.", loanId);
                return;
            }

            BigDecimal installmentValue =
                    loan.getTotalValueIncludingInterest()
                            .divide(BigDecimal.valueOf(loan.getNumberOfInstallments()), 2, RoundingMode.HALF_UP);
            BigDecimal maxAllowedInstallment = loan.getClient().getMonthlyIncome().multiply(new BigDecimal("0.30"));

            if (installmentValue.compareTo(maxAllowedInstallment) > 0) {
                finalizeLoan(loan, StateLoan.DENIED);
                log.warn("Empréstimo {} NEGADO. O valor da parcela compromete mais de 30% da renda do cliente..", loanId);
                return;
            }

            finalizeLoan(loan, StateLoan.APPROVED);
            log.info("Empréstimo {} APROVADO com sucesso!", loanId);

        } catch (Exception e) {
            log.error("Erro crítico ao processar empréstimo: {}", loanIdString, e);
            // Futuramente mandar para uma "Dead Letter Queue" (fila de erros)
        }
    }

    private void finalizeLoan(Loan loan, StateLoan state) {
        loan.setStateLoan(state);
        loanRepository.save(loan);

        // Simulação do envio de e-mail (futuro AWS SES ou Java Mail)
        log.info("EMAIL: Enviando notificação para {}, status: {}",
                loan.getClient().getName(), state);
    }
}
