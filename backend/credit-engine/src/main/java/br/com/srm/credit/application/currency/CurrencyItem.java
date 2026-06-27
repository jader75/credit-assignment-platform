package br.com.srm.credit.application.currency;

import java.time.OffsetDateTime;

public record CurrencyItem(String code, String name, String symbol, OffsetDateTime createdAt) {}
