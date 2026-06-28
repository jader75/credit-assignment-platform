package br.com.srm.credit.application.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PricingValueObjectTest {

    @Test
    void shouldExposePricingCommandAndResultRecords() {
        var command = new PricingSimulationCommand(
                "OP-1", "TRADE_RECEIVABLE", "BRL", "USD", new BigDecimal("1000.00"), new BigDecimal("0.0200"), 30);
        var result = new PricingSimulationResult(
                "OP-1",
                "TRADE_RECEIVABLE",
                new BigDecimal("1000.00"),
                new BigDecimal("0.0200"),
                new BigDecimal("0.0150"),
                30,
                new BigDecimal("966.18"),
                new BigDecimal("5.20000000"),
                new BigDecimal("500.00"),
                true);

        assertThat(command.operationReference()).isEqualTo("OP-1");
        assertThat(command.termDays()).isEqualTo(30);
        assertThat(result.appliedSpread()).isEqualByComparingTo("0.0150");
        assertThat(result.crossCurrency()).isTrue();
    }
}
