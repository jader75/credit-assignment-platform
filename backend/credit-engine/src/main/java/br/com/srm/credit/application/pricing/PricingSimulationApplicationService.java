package br.com.srm.credit.application.pricing;

import br.com.srm.credit.domain.pricing.CreditPricingRequest;
import br.com.srm.credit.domain.pricing.ReceivablePricingService;
import br.com.srm.credit.domain.pricing.ReceivableTypePricingProfile;
import br.com.srm.credit.domain.shared.StructuredLog;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class PricingSimulationApplicationService {

    private final ReceivablePricingService receivablePricingService;
    private final MeterRegistry meterRegistry;

    public PricingSimulationApplicationService(
            ReceivablePricingService receivablePricingService, MeterRegistry meterRegistry) {
        this.receivablePricingService = receivablePricingService;
        this.meterRegistry = meterRegistry;
    }

    public PricingSimulationResult simulate(PricingSimulationCommand command) {
        var sample = Timer.start(meterRegistry);
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

        try {
            StructuredLog.info()
                    .step("start")
                    .append(
                            command,
                            "operationReference",
                            "receivablePricingRuleCode",
                            "faceCurrencyCode",
                            "paymentCurrencyCode",
                            "termDays")
                    .log();

            var response = receivablePricingService.price(request, receivableType);

            meterRegistry
                    .counter("credit.pricing.simulation.requests", "outcome", "success")
                    .increment();
            StructuredLog.info()
                    .step("end")
                    .append(response, "operationReference", "crossCurrency", "netAmount", "appliedSpread")
                    .log();

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
        } catch (RuntimeException exception) {
            meterRegistry
                    .counter("credit.pricing.simulation.requests", "outcome", "failure")
                    .increment();
            StructuredLog.warn()
                    .step("error")
                    .append(command, "operationReference", "receivablePricingRuleCode")
                    .append("reason", exception.getMessage())
                    .log();
            throw exception;
        } finally {
            sample.stop(meterRegistry.timer("credit.pricing.simulation.duration"));
        }
    }
}
