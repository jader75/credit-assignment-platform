package br.com.srm.credit.domain.settlement;

public enum SettlementMessage {
    OPERATION_REFERENCE_INVALID("Referencia da operacao invalida."),
    TARGET_STATUS_INVALID("Status de destino invalido."),
    OPERATION_NOT_FOUND("Operacao nao encontrada."),
    OPERATION_ALREADY_LIQUIDATED("Operacao ja liquidada."),
    OPERATION_INVALID_TRANSITION("Transicao de status invalida."),
    OPERATION_STATUS_INVALID("Status da operacao invalido.");

    private final String message;

    SettlementMessage(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
