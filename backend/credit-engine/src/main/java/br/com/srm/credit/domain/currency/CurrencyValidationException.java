package br.com.srm.credit.domain.currency;

public final class CurrencyValidationException extends CurrencyException {

    public CurrencyValidationException(CurrencyMessage currencyMessage) {
        super(currencyMessage);
    }
}
