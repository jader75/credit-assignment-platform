package br.com.srm.credit.domain.settlement;

public class SettlementBusinessException extends RuntimeException {

    public SettlementBusinessException(SettlementMessage message) {
        super(message.message());
    }
}
