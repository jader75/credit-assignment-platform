package br.com.srm.credit.infrastructure.web.settlement;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(name = "SettlementStatementItemResponse", description = "Linha do extrato de liquidacao.")
public record SettlementStatementItemResponse(
        @Schema(description = "Referencia da operacao.", example = "OP-1001") String operationReference,
        @Schema(description = "Referencia do lote.", example = "BATCH-001") String batchReference,
        @Schema(description = "Documento do cedente.", example = "12345678000199") String assignorDocumentNumber,
        @Schema(description = "Nome do cedente.", example = "Cedente ABC Ltda") String assignorName,
        @Schema(description = "Moeda de pagamento.", example = "BRL") String paymentCurrencyCode,
        @Schema(description = "Valor de face.", example = "1000.00") BigDecimal faceAmount,
        @Schema(description = "Valor liquido.", example = "956.94") BigDecimal netAmount,
        @Schema(description = "Data e hora da liquidacao.", example = "2026-06-26T10:30:00-03:00")
                OffsetDateTime liquidatedAt,
        @Schema(description = "Status da liquidacao.", example = "LIQUIDATED") String status) {}
