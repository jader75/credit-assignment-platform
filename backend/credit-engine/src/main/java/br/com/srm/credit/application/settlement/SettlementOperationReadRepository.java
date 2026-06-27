package br.com.srm.credit.application.settlement;

public interface SettlementOperationReadRepository {

    SettlementOperationPage search(SettlementOperationFilter filter);
}
