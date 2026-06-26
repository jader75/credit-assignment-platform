package br.com.srm.credit.application.settlement;

import java.time.LocalDate;

public record SettlementStatementFilter(
        LocalDate startDate,
        LocalDate endDate,
        String assignorDocumentNumber,
        String paymentCurrencyCode,
        int page,
        int size) {}
