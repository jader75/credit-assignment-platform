package br.com.srm.credit.application.currency;

public interface ExchangeRateQuoteClient {

    ExchangeRateQuote fetch(String fromCurrencyCode, String toCurrencyCode);
}
