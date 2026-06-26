package br.com.srm.credit.domain.pricing;

public final class PricingValidationException extends PricingException {

    public PricingValidationException(PricingMessage pricingMessage) {
        super(pricingMessage);
    }
}
