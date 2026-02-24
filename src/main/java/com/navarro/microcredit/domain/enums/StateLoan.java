package com.navarro.microcredit.domain.enums;

import lombok.Getter;

@Getter
public enum StateLoan {

    PENDING("Pendente", "O pedido de empréstimo está aguardando análise de crédito."),
    APPROVED("Aprovado", "Empréstimo aprovado. O valor será liberado em breve."),
    DENIED("Negado", "Empréstimo negado devido às políticas de crédito atuais."),
    CANCELED("Cancelado", "O pedido de empréstimo foi cancelado pelo cliente."),
    RAID("Quitado", "O empréstimo foi totalmente pago.");

    private final String title;
    private final String description;

    StateLoan(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public static StateLoan fromString(String value) {
        for (StateLoan state : StateLoan.values()) {
            if (state.name().equalsIgnoreCase(value)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Status de empréstimo inválido: " + value);
    }
}
