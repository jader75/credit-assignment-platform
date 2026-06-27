package br.com.srm.credit.domain.settlement;

public class SettlementValidationException extends RuntimeException {

    public SettlementValidationException(SettlementMessage message) {
        super(message.message());
    }
}
