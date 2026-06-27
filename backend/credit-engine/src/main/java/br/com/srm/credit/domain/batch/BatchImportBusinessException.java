package br.com.srm.credit.domain.batch;

public class BatchImportBusinessException extends RuntimeException {

    public BatchImportBusinessException(BatchImportMessage message) {
        super(message.message());
    }
}
