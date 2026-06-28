package br.com.srm.credit.infrastructure.integration.currency;

import br.com.srm.credit.application.currency.ExchangeRateCacheClient;
import br.com.srm.credit.application.currency.ExchangeRateQuoteClient;
import br.com.srm.credit.domain.shared.ExchangeRateSource;
import br.com.srm.credit.domain.shared.StructuredLog;
import br.com.srm.credit.infrastructure.persistence.repository.CurrencyJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.ExchangeRateJpaRepository;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "credit.exchange-rate.redis.refresh-enabled", havingValue = "true")
public class ExchangeRateRefreshJob {

    private static final List<ExchangeRateSource> FALLBACK_SOURCES =
            List.of(ExchangeRateSource.MANUAL, ExchangeRateSource.MOCK);

    private final CurrencyJpaRepository currencyJpaRepository;
    private final ExchangeRateJpaRepository exchangeRateJpaRepository;
    private final ExchangeRateQuoteClient exchangeRateQuoteClient;
    private final ExchangeRateCacheClient exchangeRateCacheClient;

    public ExchangeRateRefreshJob(
            CurrencyJpaRepository currencyJpaRepository,
            ExchangeRateJpaRepository exchangeRateJpaRepository,
            ExchangeRateQuoteClient exchangeRateQuoteClient,
            ExchangeRateCacheClient exchangeRateCacheClient) {
        this.currencyJpaRepository = currencyJpaRepository;
        this.exchangeRateJpaRepository = exchangeRateJpaRepository;
        this.exchangeRateQuoteClient = exchangeRateQuoteClient;
        this.exchangeRateCacheClient = exchangeRateCacheClient;
    }

    @Scheduled(fixedDelayString = "${credit.exchange-rate.redis.refresh-interval-ms:30000}")
    public void refreshCache() {
        var currencies = currencyJpaRepository.findAll().stream()
                .map(item -> item.getCode().trim().toUpperCase())
                .toList();
        for (var from : currencies) {
            for (var to : currencies) {
                if (from.equals(to)) {
                    continue;
                }
                refreshPair(from, to);
            }
        }
    }

    private void refreshPair(String fromCurrencyCode, String toCurrencyCode) {
        try {
            var quote = exchangeRateQuoteClient.fetch(fromCurrencyCode, toCurrencyCode);
            exchangeRateCacheClient.put(
                    quote.fromCurrencyCode(),
                    quote.toCurrencyCode(),
                    quote.rate().setScale(8, RoundingMode.HALF_UP),
                    quote.quotedAt(),
                    ExchangeRateSource.INTEGRATION);
            StructuredLog.info()
                    .step("exchange-rate-cache-refresh-success")
                    .append("fromCurrencyCode", fromCurrencyCode)
                    .append("toCurrencyCode", toCurrencyCode)
                    .append("rate", quote.rate().setScale(8, RoundingMode.HALF_UP))
                    .append("quotedAt", quote.quotedAt())
                    .append("source", ExchangeRateSource.INTEGRATION)
                    .log();
        } catch (RuntimeException externalException) {
            StructuredLog.warn()
                    .step("exchange-rate-cache-refresh-external-failure")
                    .append("fromCurrencyCode", fromCurrencyCode)
                    .append("toCurrencyCode", toCurrencyCode)
                    .append("reason", externalException.getMessage())
                    .log();
            var fallback = exchangeRateJpaRepository
                    .findFirstByFromCurrencyCodeAndToCurrencyCodeAndSourceInOrderByQuotedAtDescIdDesc(
                            fromCurrencyCode, toCurrencyCode, FALLBACK_SOURCES)
                    .orElse(null);
            if (fallback == null) {
                StructuredLog.warn()
                        .step("exchange-rate-cache-refresh-db-miss")
                        .append("fromCurrencyCode", fromCurrencyCode)
                        .append("toCurrencyCode", toCurrencyCode)
                        .append("reason", "Nenhuma taxa manual ou mock encontrada.")
                        .log();
                return;
            }

            exchangeRateCacheClient.put(
                    fallback.getFromCurrencyCode(),
                    fallback.getToCurrencyCode(),
                    fallback.getRate().setScale(8, RoundingMode.HALF_UP),
                    fallback.getQuotedAt(),
                    fallback.getSource());
            StructuredLog.info()
                    .step("exchange-rate-cache-refresh-db-fallback")
                    .append("fromCurrencyCode", fromCurrencyCode)
                    .append("toCurrencyCode", toCurrencyCode)
                    .append("rate", fallback.getRate().setScale(8, RoundingMode.HALF_UP))
                    .append("quotedAt", fallback.getQuotedAt())
                    .append("source", fallback.getSource())
                    .log();
        }
    }
}
