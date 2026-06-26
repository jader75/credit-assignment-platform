package br.com.srm.credit.domain.pricing;

public abstract class PricingException extends RuntimeException {

    private final PricingMessage pricingMessage;

    protected PricingException(PricingMessage pricingMessage) {
        super(pricingMessage.message());
        this.pricingMessage = pricingMessage;
    }

    public PricingMessage pricingMessage() {
        return pricingMessage;
    }

    public String code() {
        return pricingMessage.code();
    }

    public String messageKey() {
        return pricingMessage.message();
    }
}
