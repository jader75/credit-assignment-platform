package br.com.srm.credit.domain.batch;

public class BatchImportValidationException extends RuntimeException {

    public BatchImportValidationException(BatchImportMessage message) {
        super(message.message());
    }
}
