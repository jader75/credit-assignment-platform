package br.com.srm.credit.application.currency;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.srm.credit.domain.shared.ExchangeRateSource;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class CurrencyValueObjectTest {

    @Test
    void shouldExposeCurrencyAndExchangeRateRecords() {
        var createdAt = OffsetDateTime.parse("2026-06-27T10:00:00Z");
        var currencyItem = new CurrencyItem("BRL", "Real brasileiro", "R$", createdAt);
        var exchangeRateItem = new ExchangeRateItem(
                1L, "USD", "BRL", new BigDecimal("5.20000000"), createdAt, ExchangeRateSource.MANUAL, createdAt);
        var exchangeRateCommand = new ExchangeRateCommand(
                "USD", "BRL", new BigDecimal("5.20000000"), createdAt, ExchangeRateSource.MANUAL);

        assertThat(currencyItem.code()).isEqualTo("BRL");
        assertThat(currencyItem.name()).isEqualTo("Real brasileiro");
        assertThat(exchangeRateItem.fromCurrencyCode()).isEqualTo("USD");
        assertThat(exchangeRateItem.toCurrencyCode()).isEqualTo("BRL");
        assertThat(exchangeRateItem.rate()).isEqualByComparingTo("5.20000000");
        assertThat(exchangeRateCommand.source()).isEqualTo(ExchangeRateSource.MANUAL);
    }
}
