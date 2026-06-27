package br.com.srm.credit.domain.currency;

public class CurrencyException extends RuntimeException {

    private final CurrencyMessage currencyMessage;

    protected CurrencyException(CurrencyMessage currencyMessage) {
        super(currencyMessage.message());
        this.currencyMessage = currencyMessage;
    }

    public CurrencyMessage getCurrencyMessage() {
        return currencyMessage;
    }
}
