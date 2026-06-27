package br.com.srm.credit.application.currency;

import br.com.srm.credit.domain.currency.CurrencyBusinessException;
import br.com.srm.credit.domain.currency.CurrencyMessage;
import br.com.srm.credit.infrastructure.persistence.repository.ExchangeRateJpaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExchangeRateQueryService {

    private static final BigDecimal ONE = BigDecimal.ONE;

    private final ExchangeRateJpaRepository exchangeRateJpaRepository;

    public ExchangeRateQueryService(ExchangeRateJpaRepository exchangeRateJpaRepository) {
        this.exchangeRateJpaRepository = exchangeRateJpaRepository;
    }

    public BigDecimal resolve(String fromCurrencyCode, String toCurrencyCode) {
        if (fromCurrencyCode == null || toCurrencyCode == null) {
            throw new CurrencyBusinessException(CurrencyMessage.EXCHANGE_RATE_NOT_FOUND);
        }

        var from = fromCurrencyCode.trim().toUpperCase();
        var to = toCurrencyCode.trim().toUpperCase();
        if (from.equals(to)) {
            return ONE;
        }

        return exchangeRateJpaRepository
                .findFirstByFromCurrencyCodeAndToCurrencyCodeOrderByQuotedAtDescIdDesc(from, to)
                .map(entity -> entity.getRate().setScale(8, RoundingMode.HALF_UP))
                .orElseThrow(() -> new CurrencyBusinessException(CurrencyMessage.EXCHANGE_RATE_NOT_FOUND));
    }
}
