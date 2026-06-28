package br.com.srm.credit.application.currency;

import br.com.srm.credit.domain.shared.ExchangeRateSource;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface ExchangeRateCacheClient {

    Optional<ExchangeRateSnapshot> get(String fromCurrencyCode, String toCurrencyCode);

    void put(
            String fromCurrencyCode,
            String toCurrencyCode,
            BigDecimal rate,
            OffsetDateTime quotedAt,
            ExchangeRateSource source);
}
