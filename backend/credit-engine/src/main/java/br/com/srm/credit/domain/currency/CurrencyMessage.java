package br.com.srm.credit.domain.currency;

public enum CurrencyMessage {
    CURRENCY_CODE_INVALID("C001", "O codigo da moeda e obrigatorio."),
    CURRENCY_NAME_INVALID("C002", "O nome da moeda e obrigatorio."),
    CURRENCY_SYMBOL_INVALID("C003", "O simbolo da moeda e obrigatorio."),
    EXCHANGE_RATE_ID_INVALID("C004", "A taxa de cambio deve ser informada."),
    EXCHANGE_RATE_VALUE_INVALID("C005", "A taxa de cambio deve ser maior que zero."),
    EXCHANGE_RATE_SOURCE_INVALID("C006", "A origem da taxa de cambio e obrigatoria."),
    FROM_CURRENCY_INVALID("C007", "A moeda de origem e obrigatoria."),
    TO_CURRENCY_INVALID("C008", "A moeda de destino e obrigatoria."),
    CURRENCY_PAIR_INVALID("C009", "A moeda de origem e destino devem ser diferentes."),
    EXCHANGE_RATE_NOT_FOUND("C010", "A taxa de cambio nao foi encontrada."),
    CURRENCY_NOT_FOUND("C011", "A moeda nao foi encontrada.");

    private final String code;
    private final String message;

    CurrencyMessage(String code, String message) {
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
