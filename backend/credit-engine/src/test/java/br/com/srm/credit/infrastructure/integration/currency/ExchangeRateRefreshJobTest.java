package br.com.srm.credit.infrastructure.integration.currency;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.srm.credit.application.currency.ExchangeRateCacheClient;
import br.com.srm.credit.application.currency.ExchangeRateQuote;
import br.com.srm.credit.domain.shared.ExchangeRateSource;
import br.com.srm.credit.infrastructure.persistence.entity.CurrencyEntity;
import br.com.srm.credit.infrastructure.persistence.entity.ExchangeRateEntity;
import br.com.srm.credit.infrastructure.persistence.repository.CurrencyJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.ExchangeRateJpaRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExchangeRateRefreshJobTest {

    private final CurrencyJpaRepository currencyJpaRepository = mock(CurrencyJpaRepository.class);
    private final ExchangeRateJpaRepository exchangeRateJpaRepository = mock(ExchangeRateJpaRepository.class);
    private final br.com.srm.credit.application.currency.ExchangeRateQuoteClient exchangeRateQuoteClient =
            mock(br.com.srm.credit.application.currency.ExchangeRateQuoteClient.class);
    private final ExchangeRateCacheClient exchangeRateCacheClient = mock(ExchangeRateCacheClient.class);
    private final ExchangeRateRefreshJob job = new ExchangeRateRefreshJob(
            currencyJpaRepository, exchangeRateJpaRepository, exchangeRateQuoteClient, exchangeRateCacheClient);

    @Test
    void shouldRefreshRedisWithExternalQuoteAndFallbackToDatabaseWhenNeeded() {
        when(currencyJpaRepository.findAll())
                .thenReturn(List.of(
                        new CurrencyEntity("BRL", "Real brasileiro", "R$"), new CurrencyEntity("USD", "Dollar", "$")));
        when(exchangeRateQuoteClient.fetch("BRL", "USD"))
                .thenReturn(new ExchangeRateQuote(
                        "BRL", "USD", new BigDecimal("0.19320000"), OffsetDateTime.parse("2026-06-27T00:00:00Z")));
        when(exchangeRateQuoteClient.fetch("USD", "BRL"))
                .thenThrow(new IllegalStateException("Frankfurter indisponivel"));
        when(exchangeRateJpaRepository.findFirstByFromCurrencyCodeAndToCurrencyCodeAndSourceInOrderByQuotedAtDescIdDesc(
                        "USD", "BRL", List.of(ExchangeRateSource.MANUAL, ExchangeRateSource.MOCK)))
                .thenReturn(Optional.of(new ExchangeRateEntity(
                        "USD",
                        "BRL",
                        new BigDecimal("5.20000000"),
                        OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                        ExchangeRateSource.MANUAL)));

        job.refreshCache();

        verify(exchangeRateCacheClient)
                .put(
                        "BRL",
                        "USD",
                        new BigDecimal("0.19320000"),
                        OffsetDateTime.parse("2026-06-27T00:00:00Z"),
                        ExchangeRateSource.INTEGRATION);
        verify(exchangeRateCacheClient)
                .put(
                        "USD",
                        "BRL",
                        new BigDecimal("5.20000000"),
                        OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                        ExchangeRateSource.MANUAL);
    }
}
