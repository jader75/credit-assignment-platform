package br.com.srm.credit.domain.pricing.strategy;

import br.com.srm.credit.domain.pricing.ReceivableTypePricingProfile;
import java.math.BigDecimal;

public interface PricingStrategy {

    String ruleCode();

    BigDecimal resolveSpread(ReceivableTypePricingProfile receivableType);
}
