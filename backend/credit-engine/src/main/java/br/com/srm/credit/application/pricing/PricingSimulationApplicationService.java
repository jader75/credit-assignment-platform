package br.com.srm.credit.application.pricing;

import br.com.srm.credit.application.currency.ExchangeRateQueryService;
import br.com.srm.credit.domain.pricing.CreditPricingRequest;
import br.com.srm.credit.domain.pricing.PricingRuleCode;
import br.com.srm.credit.domain.pricing.ReceivablePricingService;
import br.com.srm.credit.domain.shared.StructuredLog;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class PricingSimulationApplicationService {

    private final ReceivablePricingService receivablePricingService;
    private final ExchangeRateQueryService exchangeRateQueryService;
    private final MeterRegistry meterRegistry;

    public PricingSimulationApplicationService(
            ReceivablePricingService receivablePricingService,
            ExchangeRateQueryService exchangeRateQueryService,
            MeterRegistry meterRegistry) {
        this.receivablePricingService = receivablePricingService;
        this.exchangeRateQueryService = exchangeRateQueryService;
        this.meterRegistry = meterRegistry;
    }

    public PricingSimulationResult simulate(PricingSimulationCommand command) {
        var sample = Timer.start(meterRegistry);

        try {
            var exchangeRate =
                    exchangeRateQueryService.resolve(command.faceCurrencyCode(), command.paymentCurrencyCode());
            var request = new CreditPricingRequest(
                    command.operationReference(),
                    PricingRuleCode.fromCode(command.receivableTypeCode()),
                    command.faceCurrencyCode(),
                    command.paymentCurrencyCode(),
                    command.faceAmount(),
                    command.baseTaxRate(),
                    command.termDays(),
                    exchangeRate);

            StructuredLog.info()
                    .step("start")
                    .append(
                            command,
                            "operationReference",
                            "receivableTypeCode",
                            "faceCurrencyCode",
                            "paymentCurrencyCode",
                            "termDays")
                    .log();

            var response = receivablePricingService.price(request);

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
                    .append(command, "operationReference", "receivableTypeCode")
                    .append("reason", exception.getMessage())
                    .log();
            throw exception;
        } finally {
            sample.stop(meterRegistry.timer("credit.pricing.simulation.duration"));
        }
    }
}
