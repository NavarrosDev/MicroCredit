package com.navarro.microcredit.domain.event;

import java.util.UUID;

public record LoanCreateEvent(UUID loanId) { }
