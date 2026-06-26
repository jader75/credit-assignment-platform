package br.com.srm.credit.domain.pricing;

import br.com.srm.credit.domain.pricing.strategy.PricingStrategyResolver;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class ReceivablePricingService {

    private static final BigDecimal DAYS_IN_MONTH = BigDecimal.valueOf(30);
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    private final PricingStrategyResolver pricingStrategyResolver;

    public ReceivablePricingService(PricingStrategyResolver pricingStrategyResolver) {
        this.pricingStrategyResolver = pricingStrategyResolver;
    }

    public CreditPricingResponse price(CreditPricingRequest request, ReceivableTypePricingProfile receivableType) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(receivableType, "receivableType must not be null");

        validateRequest(request);

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

        return new CreditPricingResponse(
                request.operationReference(),
                receivableType.pricingRuleCode(),
                request.faceAmount().setScale(2, RoundingMode.HALF_UP),
                request.baseTaxRate().setScale(4, RoundingMode.HALF_UP),
                appliedSpread.setScale(4, RoundingMode.HALF_UP),
                request.termDays(),
                discountedAmount.setScale(2, RoundingMode.HALF_UP),
                request.exchangeRate().setScale(8, RoundingMode.HALF_UP),
                netAmount,
                crossCurrency);
    }

    private static void validateRequest(CreditPricingRequest request) {
        if (request.termDays() < 0) {
            throw new IllegalArgumentException("termDays must be greater than or equal to zero");
        }
        if (request.faceAmount() == null || request.faceAmount().signum() <= 0) {
            throw new IllegalArgumentException("faceAmount must be greater than zero");
        }
        if (request.baseTaxRate() == null || request.baseTaxRate().signum() < 0) {
            throw new IllegalArgumentException("baseTaxRate must be greater than or equal to zero");
        }
        if (request.exchangeRate() == null || request.exchangeRate().signum() <= 0) {
            throw new IllegalArgumentException("exchangeRate must be greater than zero");
        }
        if (request.faceCurrencyCode() == null || request.faceCurrencyCode().isBlank()) {
            throw new IllegalArgumentException("faceCurrencyCode must not be blank");
        }
        if (request.paymentCurrencyCode() == null
                || request.paymentCurrencyCode().isBlank()) {
            throw new IllegalArgumentException("paymentCurrencyCode must not be blank");
        }
        if (request.operationReference() == null || request.operationReference().isBlank()) {
            throw new IllegalArgumentException("operationReference must not be blank");
        }
    }
}
