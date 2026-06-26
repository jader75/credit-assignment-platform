package br.com.srm.credit.application.settlement;

public interface SettlementStatementReadRepository {

    SettlementStatementPage search(SettlementStatementFilter filter);
}
