package br.com.srm.credit.application.settlement;

public class SettlementStatementApplicationService {

    private final SettlementStatementReadRepository settlementStatementReadRepository;

    public SettlementStatementApplicationService(SettlementStatementReadRepository settlementStatementReadRepository) {
        this.settlementStatementReadRepository = settlementStatementReadRepository;
    }

    public SettlementStatementPage search(SettlementStatementFilter filter) {
        return settlementStatementReadRepository.search(filter);
    }
}
