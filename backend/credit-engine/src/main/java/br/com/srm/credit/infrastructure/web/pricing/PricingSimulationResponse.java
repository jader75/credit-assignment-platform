package br.com.srm.credit.infrastructure.web.pricing;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "PricingSimulationResponse", description = "Resultado da simulacao de precificacao.")
public record PricingSimulationResponse(
        @Schema(description = "Referencia da operacao.", example = "OP-001") String operationReference,
        @Schema(description = "Codigo da regra de precificacao.", example = "TRADE_RECEIVABLE")
                String receivablePricingRuleCode,
        @Schema(description = "Valor de face do titulo.", example = "1000.00") BigDecimal faceAmount,
        @Schema(description = "Taxa base da operacao.", example = "0.0200") BigDecimal baseTaxRate,
        @Schema(description = "Spread aplicado ao recebivel.", example = "0.0150") BigDecimal appliedSpread,
        @Schema(description = "Prazo da operacao em dias.", example = "30") int termDays,
        @Schema(description = "Valor descontado antes da conversao cambial.", example = "966.18")
                BigDecimal discountedAmount,
        @Schema(description = "Taxa de cambio aplicada.", example = "1.00000000") BigDecimal exchangeRate,
        @Schema(description = "Valor liquido final.", example = "966.18") BigDecimal netAmount,
        @Schema(description = "Indica se houve conversao cross-currency.", example = "false") boolean crossCurrency) {}
