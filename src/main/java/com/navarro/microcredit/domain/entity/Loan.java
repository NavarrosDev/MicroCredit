package com.navarro.microcredit.domain.entity;

import com.navarro.microcredit.domain.enums.StateLoan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_loan")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private BigDecimal requestedAmount;

    @Column(nullable = false)
    private BigDecimal totalValueIncludingInterest;

    @Column(nullable = false)
    private Integer numberOfInstallments;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StateLoan stateLoan;
}
