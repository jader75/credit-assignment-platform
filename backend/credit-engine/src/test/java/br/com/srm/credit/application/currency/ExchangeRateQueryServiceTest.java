package br.com.srm.credit.application.currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.srm.credit.domain.currency.CurrencyBusinessException;
import br.com.srm.credit.domain.currency.CurrencyMessage;
import br.com.srm.credit.domain.shared.ExchangeRateSource;
import br.com.srm.credit.infrastructure.persistence.entity.ExchangeRateEntity;
import br.com.srm.credit.infrastructure.persistence.repository.ExchangeRateJpaRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExchangeRateQueryServiceTest {

    private final ExchangeRateJpaRepository exchangeRateJpaRepository = mock(ExchangeRateJpaRepository.class);
    private final ExchangeRateQueryService service = new ExchangeRateQueryService(exchangeRateJpaRepository);

    @Test
    void shouldReturnOneForSameCurrencyPair() {
        assertThat(service.resolve("BRL", "BRL")).isEqualByComparingTo("1");
        verify(exchangeRateJpaRepository, never())
                .findFirstByFromCurrencyCodeAndToCurrencyCodeOrderByQuotedAtDescIdDesc("BRL", "BRL");
    }

    @Test
    void shouldResolveLatestRateForCurrencyPair() {
        when(exchangeRateJpaRepository.findFirstByFromCurrencyCodeAndToCurrencyCodeOrderByQuotedAtDescIdDesc(
                        "USD", "BRL"))
                .thenReturn(Optional.of(new ExchangeRateEntity(
                        "USD",
                        "BRL",
                        new BigDecimal("5.20000000"),
                        OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                        ExchangeRateSource.MANUAL)));

        assertThat(service.resolve("USD", "BRL")).isEqualByComparingTo("5.20000000");
    }

    @Test
    void shouldFailWhenExchangeRateIsMissing() {
        when(exchangeRateJpaRepository.findFirstByFromCurrencyCodeAndToCurrencyCodeOrderByQuotedAtDescIdDesc(
                        "USD", "BRL"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolve("USD", "BRL"))
                .isInstanceOf(CurrencyBusinessException.class)
                .hasMessage(CurrencyMessage.EXCHANGE_RATE_NOT_FOUND.message());
    }

    @Test
    void shouldNotInvertExchangeRateAutomatically() {
        when(exchangeRateJpaRepository.findFirstByFromCurrencyCodeAndToCurrencyCodeOrderByQuotedAtDescIdDesc(
                        "BRL", "USD"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolve("BRL", "USD"))
                .isInstanceOf(CurrencyBusinessException.class)
                .hasMessage(CurrencyMessage.EXCHANGE_RATE_NOT_FOUND.message());
    }
}
