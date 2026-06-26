package br.com.srm.credit.domain.pricing.strategy;

import br.com.srm.credit.domain.pricing.ReceivableTypePricingProfile;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class PostDatedCheckPricingStrategy implements PricingStrategy {

    @Override
    public String ruleCode() {
        return "POST_DATED_CHECK";
    }

    @Override
    public BigDecimal resolveSpread(ReceivableTypePricingProfile receivableType) {
        Objects.requireNonNull(receivableType, "receivableType must not be null");
        return receivableType.baseSpread();
    }
}
