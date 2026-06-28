package br.com.srm.credit.application.currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import br.com.srm.credit.domain.currency.CurrencyBusinessException;
import br.com.srm.credit.domain.currency.CurrencyMessage;
import br.com.srm.credit.domain.shared.ExchangeRateSource;
import br.com.srm.credit.infrastructure.persistence.entity.ExchangeRateEntity;
import br.com.srm.credit.infrastructure.persistence.repository.CurrencyJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.ExchangeRateJpaRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExchangeRateQueryServiceTest {

    private final CurrencyJpaRepository currencyJpaRepository = mock(CurrencyJpaRepository.class);
    private final ExchangeRateJpaRepository exchangeRateJpaRepository = mock(ExchangeRateJpaRepository.class);
    private final ExchangeRateCacheClient exchangeRateCacheClient = mock(ExchangeRateCacheClient.class);
    private final ExchangeRateQueryService service =
            new ExchangeRateQueryService(currencyJpaRepository, exchangeRateJpaRepository, exchangeRateCacheClient);

    @Test
    void shouldReturnOneForSameCurrencyPair() {
        when(currencyJpaRepository.existsById("BRL")).thenReturn(true);

        assertThat(service.resolve("BRL", "BRL")).isEqualByComparingTo("1");
        verify(currencyJpaRepository).existsById("BRL");
        verify(exchangeRateCacheClient, never()).get(anyString(), anyString());
        verifyNoMoreInteractions(exchangeRateJpaRepository);
    }

    @Test
    void shouldResolveFromRedisCacheWhenPresent() {
        when(currencyJpaRepository.existsById("USD")).thenReturn(true);
        when(currencyJpaRepository.existsById("BRL")).thenReturn(true);
        when(exchangeRateCacheClient.get("USD", "BRL"))
                .thenReturn(Optional.of(new ExchangeRateSnapshot(
                        "USD",
                        "BRL",
                        new BigDecimal("5.20000000"),
                        OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                        ExchangeRateSource.INTEGRATION)));

        assertThat(service.resolve("USD", "BRL")).isEqualByComparingTo("5.20000000");
        verify(exchangeRateCacheClient).get("USD", "BRL");
        verifyNoMoreInteractions(exchangeRateJpaRepository);
    }

    @Test
    void shouldFallbackToLatestManualOrMockRateWhenRedisMisses() {
        when(currencyJpaRepository.existsById("USD")).thenReturn(true);
        when(currencyJpaRepository.existsById("BRL")).thenReturn(true);
        when(exchangeRateCacheClient.get("USD", "BRL")).thenReturn(Optional.empty());
        when(exchangeRateJpaRepository.findFirstByFromCurrencyCodeAndToCurrencyCodeAndSourceInOrderByQuotedAtDescIdDesc(
                        "USD", "BRL", java.util.List.of(ExchangeRateSource.MANUAL, ExchangeRateSource.MOCK)))
                .thenReturn(Optional.of(new ExchangeRateEntity(
                        "USD",
                        "BRL",
                        new BigDecimal("5.20000000"),
                        OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                        ExchangeRateSource.MANUAL)));

        assertThat(service.resolve("USD", "BRL")).isEqualByComparingTo("5.20000000");
        verify(exchangeRateCacheClient).get("USD", "BRL");
        verify(exchangeRateJpaRepository)
                .findFirstByFromCurrencyCodeAndToCurrencyCodeAndSourceInOrderByQuotedAtDescIdDesc(
                        "USD", "BRL", java.util.List.of(ExchangeRateSource.MANUAL, ExchangeRateSource.MOCK));
    }

    @Test
    void shouldFailWhenBothRedisAndLocalRatesAreMissing() {
        when(currencyJpaRepository.existsById("USD")).thenReturn(true);
        when(currencyJpaRepository.existsById("BRL")).thenReturn(true);
        when(exchangeRateCacheClient.get("USD", "BRL")).thenReturn(Optional.empty());
        when(exchangeRateJpaRepository.findFirstByFromCurrencyCodeAndToCurrencyCodeAndSourceInOrderByQuotedAtDescIdDesc(
                        "USD", "BRL", java.util.List.of(ExchangeRateSource.MANUAL, ExchangeRateSource.MOCK)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolve("USD", "BRL"))
                .isInstanceOf(CurrencyBusinessException.class)
                .hasMessage(CurrencyMessage.EXCHANGE_RATE_NOT_FOUND.message());
    }

    @Test
    void shouldFailWhenCurrencyDoesNotExist() {
        when(currencyJpaRepository.existsById("USD")).thenReturn(false);

        assertThatThrownBy(() -> service.resolve("USD", "BRL"))
                .isInstanceOf(CurrencyBusinessException.class)
                .hasMessage(CurrencyMessage.CURRENCY_NOT_FOUND.message());
        verifyNoMoreInteractions(exchangeRateCacheClient, exchangeRateJpaRepository);
    }
}
