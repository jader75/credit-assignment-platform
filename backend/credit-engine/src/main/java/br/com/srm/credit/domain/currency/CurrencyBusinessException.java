package br.com.srm.credit.domain.currency;

public final class CurrencyBusinessException extends CurrencyException {

    public CurrencyBusinessException(CurrencyMessage currencyMessage) {
        super(currencyMessage);
    }
}
