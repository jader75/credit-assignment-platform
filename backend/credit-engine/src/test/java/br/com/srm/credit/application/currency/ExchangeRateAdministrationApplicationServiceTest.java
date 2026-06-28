package br.com.srm.credit.application.currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.srm.credit.domain.currency.CurrencyBusinessException;
import br.com.srm.credit.domain.currency.CurrencyMessage;
import br.com.srm.credit.domain.currency.CurrencyValidationException;
import br.com.srm.credit.domain.shared.ExchangeRateSource;
import br.com.srm.credit.infrastructure.persistence.entity.ExchangeRateEntity;
import br.com.srm.credit.infrastructure.persistence.repository.CurrencyJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.ExchangeRateJpaRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExchangeRateAdministrationApplicationServiceTest {

    private final CurrencyJpaRepository currencyJpaRepository = mock(CurrencyJpaRepository.class);
    private final ExchangeRateJpaRepository exchangeRateJpaRepository = mock(ExchangeRateJpaRepository.class);
    private final ExchangeRateAdministrationApplicationService service =
            new ExchangeRateAdministrationApplicationService(currencyJpaRepository, exchangeRateJpaRepository);

    @Test
    void shouldListOnlyManualAndMockExchangeRatesOrderedByQuotedAtDesc() {
        when(exchangeRateJpaRepository.findAllBySourceInOrderByQuotedAtDescIdDesc(
                        List.of(ExchangeRateSource.MANUAL, ExchangeRateSource.MOCK)))
                .thenReturn(List.of(new ExchangeRateEntity(
                        "USD",
                        "BRL",
                        new BigDecimal("5.20000000"),
                        OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                        ExchangeRateSource.MANUAL)));

        var items = service.list();

        assertThat(items).hasSize(1);
        assertThat(items.get(0).fromCurrencyCode()).isEqualTo("USD");
        assertThat(items.get(0).toCurrencyCode()).isEqualTo("BRL");
        assertThat(items.get(0).rate()).isEqualByComparingTo("5.20000000");
        assertThat(items.get(0).source()).isEqualTo(ExchangeRateSource.MANUAL);
    }

    @Test
    void shouldCreateManualRateUsingNormalizedCurrencies() {
        when(currencyJpaRepository.existsById("USD")).thenReturn(true);
        when(currencyJpaRepository.existsById("BRL")).thenReturn(true);
        when(exchangeRateJpaRepository.save(any(ExchangeRateEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var item = service.create(new ExchangeRateCommand(
                " usd ",
                " brl ",
                new BigDecimal("5.20000000"),
                OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                ExchangeRateSource.MANUAL));

        assertThat(item.fromCurrencyCode()).isEqualTo("USD");
        assertThat(item.toCurrencyCode()).isEqualTo("BRL");
        assertThat(item.rate()).isEqualByComparingTo("5.20000000");
        assertThat(item.source()).isEqualTo(ExchangeRateSource.MANUAL);
        verify(exchangeRateJpaRepository, times(1)).save(any(ExchangeRateEntity.class));
        verify(currencyJpaRepository).existsById("USD");
        verify(currencyJpaRepository).existsById("BRL");
    }

    @Test
    void shouldRejectIntegrationSourceForManualCrud() {
        when(currencyJpaRepository.existsById("USD")).thenReturn(true);
        when(currencyJpaRepository.existsById("BRL")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new ExchangeRateCommand(
                        "USD",
                        "BRL",
                        new BigDecimal("5.20000000"),
                        OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                        ExchangeRateSource.INTEGRATION)))
                .isInstanceOf(CurrencyBusinessException.class)
                .hasMessage(CurrencyMessage.EXCHANGE_RATE_SOURCE_NOT_SUPPORTED.message());

        verify(exchangeRateJpaRepository, never()).save(any());
    }

    @Test
    void shouldUpdateExchangeRate() {
        when(currencyJpaRepository.existsById("USD")).thenReturn(true);
        when(currencyJpaRepository.existsById("BRL")).thenReturn(true);
        when(exchangeRateJpaRepository.findById(10L))
                .thenReturn(Optional.of(new ExchangeRateEntity(
                        "EUR",
                        "USD",
                        new BigDecimal("6.00000000"),
                        OffsetDateTime.parse("2026-06-25T10:00:00Z"),
                        ExchangeRateSource.MOCK)));
        when(exchangeRateJpaRepository.save(any(ExchangeRateEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var item = service.update(
                10L,
                new ExchangeRateCommand(
                        " usd ",
                        " brl ",
                        new BigDecimal("5.10000000"),
                        OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                        ExchangeRateSource.MANUAL));

        assertThat(item.fromCurrencyCode()).isEqualTo("USD");
        assertThat(item.toCurrencyCode()).isEqualTo("BRL");
        assertThat(item.rate()).isEqualByComparingTo("5.10000000");
        verify(exchangeRateJpaRepository).findById(10L);
        verify(exchangeRateJpaRepository, times(1)).save(any(ExchangeRateEntity.class));
    }

    @Test
    void shouldDeleteExchangeRate() {
        when(exchangeRateJpaRepository.existsById(10L)).thenReturn(true);

        service.delete(10L);

        verify(exchangeRateJpaRepository).deleteById(10L);
    }

    @Test
    void shouldRejectNullIdentifierOnCrudOperations() {
        assertThatThrownBy(() -> service.update(
                        null,
                        new ExchangeRateCommand(
                                "USD",
                                "BRL",
                                new BigDecimal("5.10000000"),
                                OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                                ExchangeRateSource.MANUAL)))
                .isInstanceOf(CurrencyValidationException.class)
                .hasMessage(CurrencyMessage.EXCHANGE_RATE_ID_INVALID.message());

        assertThatThrownBy(() -> service.delete(null))
                .isInstanceOf(CurrencyValidationException.class)
                .hasMessage(CurrencyMessage.EXCHANGE_RATE_ID_INVALID.message());
    }

    @Test
    void shouldRejectMissingExchangeRateOnUpdateAndDelete() {
        when(currencyJpaRepository.existsById("USD")).thenReturn(true);
        when(currencyJpaRepository.existsById("BRL")).thenReturn(true);
        when(exchangeRateJpaRepository.findById(10L)).thenReturn(Optional.empty());
        when(exchangeRateJpaRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> service.update(
                        10L,
                        new ExchangeRateCommand(
                                "USD",
                                "BRL",
                                new BigDecimal("5.10000000"),
                                OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                                ExchangeRateSource.MANUAL)))
                .isInstanceOf(CurrencyBusinessException.class)
                .hasMessage(CurrencyMessage.EXCHANGE_RATE_NOT_FOUND.message());

        assertThatThrownBy(() -> service.delete(10L))
                .isInstanceOf(CurrencyBusinessException.class)
                .hasMessage(CurrencyMessage.EXCHANGE_RATE_NOT_FOUND.message());
    }

    @Test
    void shouldRejectInvalidCurrencyPairOnCrud() {
        when(currencyJpaRepository.existsById("BRL")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new ExchangeRateCommand(
                        "BRL",
                        "BRL",
                        new BigDecimal("5.10000000"),
                        OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                        ExchangeRateSource.MANUAL)))
                .isInstanceOf(CurrencyBusinessException.class)
                .hasMessage(CurrencyMessage.CURRENCY_PAIR_INVALID.message());

        verify(currencyJpaRepository, times(2)).existsById("BRL");
    }
}
