package br.com.srm.credit.domain.pricing;

public enum PricingRuleCode {
    TRADE_RECEIVABLE("TRADE_RECEIVABLE"),
    POST_DATED_CHECK("POST_DATED_CHECK");

    private final String code;

    PricingRuleCode(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static PricingRuleCode fromCode(String code) {
        for (var value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }

        throw new PricingBusinessException(PricingMessage.PRICING_RULE_NOT_FOUND);
    }
}
