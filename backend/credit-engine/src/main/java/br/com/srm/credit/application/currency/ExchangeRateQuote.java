package br.com.srm.credit.application.currency;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ExchangeRateQuote(
        String fromCurrencyCode, String toCurrencyCode, BigDecimal rate, OffsetDateTime quotedAt) {}
