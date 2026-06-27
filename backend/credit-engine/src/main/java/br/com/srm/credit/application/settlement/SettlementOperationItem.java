package br.com.srm.credit.application.settlement;

import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record SettlementOperationItem(
        String operationReference,
        String batchReference,
        String assignorDocumentNumber,
        String assignorName,
        String receivableTypeCode,
        String faceCurrencyCode,
        String paymentCurrencyCode,
        BigDecimal faceAmount,
        BigDecimal netAmount,
        LocalDate dueDate,
        OffsetDateTime pricingAt,
        OffsetDateTime liquidatedAt,
        CreditAssignmentStatus status) {}
