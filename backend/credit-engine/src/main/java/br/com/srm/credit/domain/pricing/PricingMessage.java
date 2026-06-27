package br.com.srm.credit.domain.pricing;

public enum PricingMessage {
    TERM_DAYS_INVALID("P001", "O prazo deve ser maior ou igual a zero."),
    FACE_AMOUNT_INVALID("P002", "O valor de face deve ser maior que zero."),
    BASE_TAX_RATE_INVALID("P003", "A taxa base deve ser maior ou igual a zero."),
    EXCHANGE_RATE_INVALID("P004", "A taxa de câmbio deve ser maior que zero."),
    FACE_CURRENCY_INVALID("P005", "A moeda do título é obrigatória."),
    PAYMENT_CURRENCY_INVALID("P006", "A moeda de pagamento é obrigatória."),
    OPERATION_REFERENCE_INVALID("P007", "A referência da operação é obrigatória."),
    RECEIVABLE_TYPE_CODE_INVALID("P008", "O código do tipo de recebível é obrigatório."),
    PRICING_RULE_CODE_INVALID("P009", "O código da regra de precificação é obrigatório."),
    BASE_SPREAD_INVALID("P010", "O spread base deve ser maior ou igual a zero."),
    PRICING_REQUEST_INVALID("P011", "A solicitação de precificação é obrigatória."),
    RECEIVABLE_TYPE_INVALID("P012", "O tipo de recebível é obrigatório."),
    PRICING_RULE_NOT_FOUND("P013", "Nenhuma estratégia de precificação foi encontrada para a regra selecionada."),
    PRICING_RULE_MISMATCH("P014", "A estratégia de precificação da operação não confere com o tipo do recebível.");

    private final String code;
    private final String message;

    PricingMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
