package br.com.srm.credit.domain.batch;

public enum BatchImportMessage {
    BATCH_REFERENCE_INVALID("Referencia do lote invalida."),
    BATCH_ALREADY_EXISTS("Ja existe um lote com esta referencia."),
    BATCH_FILE_INVALID("Arquivo de lote invalido."),
    BATCH_FILE_EMPTY("Arquivo de lote vazio."),
    BATCH_HEADER_INVALID("Cabecalho do arquivo de lote invalido."),
    BATCH_ROW_INVALID("Linha do arquivo de lote invalida."),
    BATCH_OPERATION_REFERENCE_DUPLICATED("Referencia da operacao duplicada no lote."),
    BATCH_OPERATION_REFERENCE_ALREADY_EXISTS("Ja existe uma operacao com esta referencia."),
    BATCH_ASSIGNOR_INVALID("Cedente invalido."),
    BATCH_RECEIVABLE_TYPE_INVALID("Tipo de recebivel invalido."),
    BATCH_CURRENCY_INVALID("Moeda invalida."),
    BATCH_RULE_INVALID("Regra de precificacao invalida."),
    BATCH_NAME_MISMATCH("Cadastro do arquivo nao confere com o registro existente.");

    private final String message;

    BatchImportMessage(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
