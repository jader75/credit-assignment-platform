package br.com.srm.credit.application.pricing;

import br.com.srm.credit.domain.pricing.CreditPricingRequest;
import br.com.srm.credit.domain.pricing.ReceivablePricingService;
import br.com.srm.credit.domain.pricing.ReceivableTypePricingProfile;

public class PricingSimulationApplicationService {

    private final ReceivablePricingService receivablePricingService;

    public PricingSimulationApplicationService(ReceivablePricingService receivablePricingService) {
        this.receivablePricingService = receivablePricingService;
    }

    public PricingSimulationResult simulate(PricingSimulationCommand command) {
        var request = new CreditPricingRequest(
                command.operationReference(),
                command.receivablePricingRuleCode(),
                command.faceCurrencyCode(),
                command.paymentCurrencyCode(),
                command.faceAmount(),
                command.baseTaxRate(),
                command.termDays(),
                command.exchangeRate());

        var receivableType = new ReceivableTypePricingProfile(
                command.receivableTypeCode(),
                command.receivablePricingRuleCode(),
                command.receivableTypeBaseSpread(),
                command.receivableTypeActive());

        var response = receivablePricingService.price(request, receivableType);

        return new PricingSimulationResult(
                response.operationReference(),
                response.receivablePricingRuleCode(),
                response.faceAmount(),
                response.baseTaxRate(),
                response.appliedSpread(),
                response.termDays(),
                response.discountedAmount(),
                response.exchangeRate(),
                response.netAmount(),
                response.crossCurrency());
    }
}
