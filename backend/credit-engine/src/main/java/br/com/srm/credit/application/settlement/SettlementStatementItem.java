package br.com.srm.credit.application.settlement;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SettlementStatementItem(
        String operationReference,
        String batchReference,
        String assignorDocumentNumber,
        String assignorName,
        String paymentCurrencyCode,
        BigDecimal faceAmount,
        BigDecimal netAmount,
        OffsetDateTime liquidatedAt,
        String status) {}
