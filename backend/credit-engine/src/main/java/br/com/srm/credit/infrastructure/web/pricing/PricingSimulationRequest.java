package br.com.srm.credit.infrastructure.web.pricing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(name = "PricingSimulationRequest", description = "Payload para simular a precificacao de um recebivel.")
public record PricingSimulationRequest(
        @NotBlank @Schema(description = "Referencia da operacao.", example = "OP-001") String operationReference,
        @NotBlank @Schema(description = "Codigo do tipo de recebivel.", example = "TRADE_RECEIVABLE")
                String receivableTypeCode,
        @NotBlank @Schema(description = "Regra de precificacao aplicada ao tipo.", example = "TRADE_RECEIVABLE")
                String receivablePricingRuleCode,
        @NotNull
                @DecimalMin(value = "0.00000000", inclusive = true)
                @Schema(description = "Spread base do tipo de recebivel.", example = "0.0150")
                BigDecimal receivableTypeBaseSpread,
        @Schema(description = "Indica se o tipo de recebivel esta ativo.", example = "true")
                boolean receivableTypeActive,
        @NotBlank @Schema(description = "Moeda do titulo.", example = "BRL") String faceCurrencyCode,
        @NotBlank @Schema(description = "Moeda de pagamento.", example = "BRL") String paymentCurrencyCode,
        @NotNull
                @DecimalMin(value = "0.01", inclusive = true)
                @Schema(description = "Valor de face do titulo.", example = "1000.00")
                BigDecimal faceAmount,
        @NotNull
                @DecimalMin(value = "0.00", inclusive = true)
                @Schema(description = "Taxa base da operacao.", example = "0.0200")
                BigDecimal baseTaxRate,
        @Min(0) @Schema(description = "Prazo da operacao em dias.", example = "30") int termDays,
        @NotNull
                @DecimalMin(value = "0.00000001", inclusive = true)
                @Schema(description = "Taxa de cambio da moeda de pagamento.", example = "1.00000000")
                BigDecimal exchangeRate) {}
