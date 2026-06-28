package br.com.srm.credit.application.currency;

import br.com.srm.credit.domain.currency.CurrencyBusinessException;
import br.com.srm.credit.domain.currency.CurrencyMessage;
import br.com.srm.credit.domain.shared.ExchangeRateSource;
import br.com.srm.credit.domain.shared.StructuredLog;
import br.com.srm.credit.infrastructure.persistence.repository.CurrencyJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.ExchangeRateJpaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

public class ExchangeRateQueryService {

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final List<ExchangeRateSource> FALLBACK_SOURCES =
            List.of(ExchangeRateSource.MANUAL, ExchangeRateSource.MOCK);

    private final CurrencyJpaRepository currencyJpaRepository;
    private final ExchangeRateJpaRepository exchangeRateJpaRepository;
    private final ExchangeRateCacheClient exchangeRateCacheClient;

    public ExchangeRateQueryService(
            CurrencyJpaRepository currencyJpaRepository,
            ExchangeRateJpaRepository exchangeRateJpaRepository,
            ExchangeRateCacheClient exchangeRateCacheClient) {
        this.currencyJpaRepository = currencyJpaRepository;
        this.exchangeRateJpaRepository = exchangeRateJpaRepository;
        this.exchangeRateCacheClient = exchangeRateCacheClient;
    }

    public BigDecimal resolve(String fromCurrencyCode, String toCurrencyCode) {
        if (fromCurrencyCode == null || toCurrencyCode == null) {
            throw new CurrencyBusinessException(CurrencyMessage.EXCHANGE_RATE_NOT_FOUND);
        }

        var from = normalizeCurrencyCode(fromCurrencyCode);
        var to = normalizeCurrencyCode(toCurrencyCode);

        validateCurrencyExists(from);
        if (from.equals(to)) {
            return ONE;
        }
        validateCurrencyExists(to);

        var cacheKey = from + "/" + to;
        var cached = exchangeRateCacheClient.get(from, to);
        if (cached.isPresent()) {
            StructuredLog.info()
                    .step("exchange-rate-cache-hit")
                    .append("pair", cacheKey)
                    .append("rate", cached.get().rate())
                    .append("quotedAt", cached.get().quotedAt())
                    .append("source", cached.get().source())
                    .log();
            return cached.get().rate().setScale(8, RoundingMode.HALF_UP);
        }

        StructuredLog.warn()
                .step("exchange-rate-cache-miss")
                .append("pair", cacheKey)
                .append("fallback", "database")
                .log();

        var fallbackRate = resolveFromDatabase(from, to);
        StructuredLog.info()
                .step("exchange-rate-fallback-used")
                .append("pair", cacheKey)
                .append("rate", fallbackRate)
                .log();
        return fallbackRate;
    }

    private BigDecimal resolveFromDatabase(String fromCurrencyCode, String toCurrencyCode) {
        return exchangeRateJpaRepository
                .findFirstByFromCurrencyCodeAndToCurrencyCodeAndSourceInOrderByQuotedAtDescIdDesc(
                        fromCurrencyCode, toCurrencyCode, FALLBACK_SOURCES)
                .map(entity -> entity.getRate().setScale(8, RoundingMode.HALF_UP))
                .orElseThrow(() -> new CurrencyBusinessException(CurrencyMessage.EXCHANGE_RATE_NOT_FOUND));
    }

    private void validateCurrencyExists(String currencyCode) {
        if (!currencyJpaRepository.existsById(currencyCode)) {
            throw new CurrencyBusinessException(CurrencyMessage.CURRENCY_NOT_FOUND);
        }
    }

    private static String normalizeCurrencyCode(String currencyCode) {
        return currencyCode.trim().toUpperCase(Locale.ROOT);
    }
}
