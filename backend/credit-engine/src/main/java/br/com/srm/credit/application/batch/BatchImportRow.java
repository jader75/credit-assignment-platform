package br.com.srm.credit.application.batch;

import java.math.BigDecimal;
import java.time.LocalDate;

record BatchImportRow(
        String operationReference,
        String assignorDocumentNumber,
        String assignorName,
        String riskRating,
        String receivableTypeCode,
        String receivableTypeName,
        String faceCurrencyCode,
        BigDecimal faceAmount,
        LocalDate dueDate,
        BigDecimal baseTaxRate,
        Integer termDays,
        String paymentCurrencyCode) {}
