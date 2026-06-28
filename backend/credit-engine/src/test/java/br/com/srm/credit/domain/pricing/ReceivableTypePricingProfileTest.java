package br.com.srm.credit.domain.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ReceivableTypePricingProfileTest {

    @Test
    void shouldCreateProfileAndRejectInvalidData() {
        var profile =
                ReceivableTypePricingProfile.of("TRADE_RECEIVABLE", "TRADE_RECEIVABLE", new BigDecimal("0.0150"), true);

        assertThat(profile.code()).isEqualTo("TRADE_RECEIVABLE");
        assertThat(profile.pricingRuleCode()).isEqualTo(PricingRuleCode.TRADE_RECEIVABLE);
        assertThat(profile.baseSpread()).isEqualByComparingTo("0.0150");
        assertThat(profile.active()).isTrue();

        assertThatThrownBy(
                        () -> ReceivableTypePricingProfile.of(null, "TRADE_RECEIVABLE", new BigDecimal("0.0150"), true))
                .isInstanceOf(PricingValidationException.class)
                .hasMessage(PricingMessage.RECEIVABLE_TYPE_CODE_INVALID.message());
        assertThatThrownBy(
                        () -> ReceivableTypePricingProfile.of("TRADE_RECEIVABLE", null, new BigDecimal("0.0150"), true))
                .isInstanceOf(PricingBusinessException.class)
                .hasMessage(PricingMessage.PRICING_RULE_NOT_FOUND.message());
        assertThatThrownBy(() -> ReceivableTypePricingProfile.of("TRADE_RECEIVABLE", "TRADE_RECEIVABLE", null, true))
                .isInstanceOf(PricingValidationException.class)
                .hasMessage(PricingMessage.BASE_SPREAD_INVALID.message());
        assertThatThrownBy(() -> ReceivableTypePricingProfile.of(
                        "TRADE_RECEIVABLE", "TRADE_RECEIVABLE", new BigDecimal("-0.0100"), true))
                .isInstanceOf(PricingValidationException.class)
                .hasMessage(PricingMessage.BASE_SPREAD_INVALID.message());
    }
}
