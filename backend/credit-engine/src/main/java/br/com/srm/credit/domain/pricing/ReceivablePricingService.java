package br.com.srm.credit.domain.pricing;

import static br.com.srm.credit.domain.shared.DomainValidation.require;
import static br.com.srm.credit.domain.shared.DomainValidation.requireNonNull;

import br.com.srm.credit.domain.pricing.strategy.PricingStrategyResolver;
import br.com.srm.credit.domain.shared.StructuredLog;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class ReceivablePricingService {

    private static final BigDecimal DAYS_IN_MONTH = BigDecimal.valueOf(30);
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    private final PricingStrategyResolver pricingStrategyResolver;

    public ReceivablePricingService(PricingStrategyResolver pricingStrategyResolver) {
        this.pricingStrategyResolver = pricingStrategyResolver;
    }

    public CreditPricingResponse price(CreditPricingRequest request, ReceivableTypePricingProfile receivableType) {
        requireNonNull(request, () -> new PricingValidationException(PricingMessage.PRICING_REQUEST_INVALID));
        requireNonNull(receivableType, () -> new PricingValidationException(PricingMessage.RECEIVABLE_TYPE_INVALID));
        require(
                request.receivablePricingRuleCode().equals(receivableType.pricingRuleCode()),
                () -> new PricingBusinessException(PricingMessage.PRICING_RULE_MISMATCH));

        StructuredLog.debug()
                .step("processing")
                .append(
                        request,
                        "operationReference",
                        "receivablePricingRuleCode",
                        "faceCurrencyCode",
                        "paymentCurrencyCode",
                        "termDays")
                .append("typeActive", receivableType.active())
                .log();

        var strategy = pricingStrategyResolver.resolve(receivableType);
        var appliedSpread = strategy.resolveSpread(receivableType);
        var monthlyPeriods = BigDecimal.valueOf(request.termDays()).divide(DAYS_IN_MONTH, 8, RoundingMode.HALF_UP);
        var effectiveRate = ONE.add(request.baseTaxRate()).add(appliedSpread);
        var discountFactor =
                new BigDecimal(Math.pow(effectiveRate.doubleValue(), monthlyPeriods.doubleValue()), MATH_CONTEXT);
        var discountedAmount = request.faceAmount().divide(discountFactor, 10, RoundingMode.HALF_UP);
        var netAmount = discountedAmount;

        var crossCurrency = !request.faceCurrencyCode().equalsIgnoreCase(request.paymentCurrencyCode());
        if (crossCurrency) {
            netAmount = discountedAmount.multiply(request.exchangeRate()).setScale(2, RoundingMode.HALF_UP);
        } else {
            netAmount = discountedAmount.setScale(2, RoundingMode.HALF_UP);
        }

        StructuredLog.info()
                .step("end")
                .append(request, "operationReference")
                .append("crossCurrency", crossCurrency)
                .append("appliedSpread", appliedSpread.setScale(4, RoundingMode.HALF_UP))
                .append("discountedAmount", discountedAmount.setScale(2, RoundingMode.HALF_UP))
                .append("netAmount", netAmount)
                .log();

        return new CreditPricingResponse(
                request.operationReference(),
                receivableType.pricingRuleCode().code(),
                request.faceAmount().setScale(2, RoundingMode.HALF_UP),
                request.baseTaxRate().setScale(4, RoundingMode.HALF_UP),
                appliedSpread.setScale(4, RoundingMode.HALF_UP),
                request.termDays(),
                discountedAmount.setScale(2, RoundingMode.HALF_UP),
                request.exchangeRate().setScale(8, RoundingMode.HALF_UP),
                netAmount,
                crossCurrency);
    }
}
