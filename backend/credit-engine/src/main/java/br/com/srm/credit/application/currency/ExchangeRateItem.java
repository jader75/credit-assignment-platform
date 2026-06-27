package br.com.srm.credit.application.currency;

import br.com.srm.credit.domain.shared.ExchangeRateSource;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ExchangeRateItem(
        Long id,
        String fromCurrencyCode,
        String toCurrencyCode,
        BigDecimal rate,
        OffsetDateTime quotedAt,
        ExchangeRateSource source,
        OffsetDateTime createdAt) {}
