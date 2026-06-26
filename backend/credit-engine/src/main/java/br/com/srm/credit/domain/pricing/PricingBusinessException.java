package br.com.srm.credit.domain.pricing;

public final class PricingBusinessException extends PricingException {

    public PricingBusinessException(PricingMessage pricingMessage) {
        super(pricingMessage);
    }
}
