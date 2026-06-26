package br.com.srm.credit.domain.pricing.strategy;

import static br.com.srm.credit.domain.shared.DomainValidation.requireNonNull;

import br.com.srm.credit.domain.pricing.PricingMessage;
import br.com.srm.credit.domain.pricing.PricingRuleCode;
import br.com.srm.credit.domain.pricing.PricingValidationException;
import br.com.srm.credit.domain.pricing.ReceivableTypePricingProfile;
import java.math.BigDecimal;

public class PostDatedCheckPricingStrategy implements PricingStrategy {

    @Override
    public PricingRuleCode ruleCode() {
        return PricingRuleCode.POST_DATED_CHECK;
    }

    @Override
    public BigDecimal resolveSpread(ReceivableTypePricingProfile receivableType) {
        requireNonNull(receivableType, () -> new PricingValidationException(PricingMessage.RECEIVABLE_TYPE_INVALID));
        return receivableType.baseSpread();
    }
}
