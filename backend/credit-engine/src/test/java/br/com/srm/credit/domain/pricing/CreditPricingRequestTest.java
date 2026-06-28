package br.com.srm.credit.domain.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CreditPricingRequestTest {

    @Test
    void shouldCreateRequestAndRejectInvalidInputs() {
        var request = CreditPricingRequest.of(
                "OP-1",
                "TRADE_RECEIVABLE",
                "BRL",
                "USD",
                new BigDecimal("1000.00"),
                new BigDecimal("0.0200"),
                30,
                new BigDecimal("5.20000000"));

        assertThat(request.operationReference()).isEqualTo("OP-1");
        assertThat(request.receivablePricingRuleCode()).isEqualTo(PricingRuleCode.TRADE_RECEIVABLE);
        assertThat(request.exchangeRate()).isEqualByComparingTo("5.20000000");

        assertThatThrownBy(() -> CreditPricingRequest.of(
                        null,
                        "TRADE_RECEIVABLE",
                        "BRL",
                        "USD",
                        new BigDecimal("1000.00"),
                        new BigDecimal("0.0200"),
                        30,
                        new BigDecimal("5.20000000")))
                .isInstanceOf(PricingValidationException.class)
                .hasMessage(PricingMessage.OPERATION_REFERENCE_INVALID.message());
        assertThatThrownBy(() -> CreditPricingRequest.of(
                        "OP-1",
                        "TRADE_RECEIVABLE",
                        "BRL",
                        "USD",
                        new BigDecimal("0.00"),
                        new BigDecimal("0.0200"),
                        30,
                        new BigDecimal("5.20000000")))
                .isInstanceOf(PricingValidationException.class)
                .hasMessage(PricingMessage.FACE_AMOUNT_INVALID.message());
        assertThatThrownBy(() -> CreditPricingRequest.of(
                        "OP-1",
                        "TRADE_RECEIVABLE",
                        "BRL",
                        "USD",
                        new BigDecimal("1000.00"),
                        new BigDecimal("-0.0200"),
                        30,
                        new BigDecimal("5.20000000")))
                .isInstanceOf(PricingValidationException.class)
                .hasMessage(PricingMessage.BASE_TAX_RATE_INVALID.message());
        assertThatThrownBy(() -> CreditPricingRequest.of(
                        "OP-1",
                        "TRADE_RECEIVABLE",
                        "BRL",
                        "USD",
                        new BigDecimal("1000.00"),
                        new BigDecimal("0.0200"),
                        -1,
                        new BigDecimal("5.20000000")))
                .isInstanceOf(PricingValidationException.class)
                .hasMessage(PricingMessage.TERM_DAYS_INVALID.message());
        assertThatThrownBy(() -> CreditPricingRequest.of(
                        "OP-1",
                        "TRADE_RECEIVABLE",
                        "BRL",
                        "USD",
                        new BigDecimal("1000.00"),
                        new BigDecimal("0.0200"),
                        30,
                        new BigDecimal("0.00")))
                .isInstanceOf(PricingValidationException.class)
                .hasMessage(PricingMessage.EXCHANGE_RATE_INVALID.message());
    }
}
