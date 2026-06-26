package br.com.srm.credit.application.settlement;

import br.com.srm.credit.domain.shared.StructuredLog;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.transaction.annotation.Transactional;

public class SettlementStatementApplicationService {

    private final SettlementStatementReadRepository settlementStatementReadRepository;
    private final MeterRegistry meterRegistry;

    public SettlementStatementApplicationService(
            SettlementStatementReadRepository settlementStatementReadRepository, MeterRegistry meterRegistry) {
        this.settlementStatementReadRepository = settlementStatementReadRepository;
        this.meterRegistry = meterRegistry;
    }

    @Transactional(readOnly = true)
    public SettlementStatementPage search(SettlementStatementFilter filter) {
        var sample = Timer.start(meterRegistry);
        try {
            StructuredLog.info()
                    .step("start")
                    .append(
                            filter,
                            "startDate",
                            "endDate",
                            "assignorDocumentNumber",
                            "paymentCurrencyCode",
                            "page",
                            "size")
                    .log();

            var result = settlementStatementReadRepository.search(filter);
            meterRegistry
                    .counter("credit.settlement.statement.requests", "outcome", "success")
                    .increment();
            StructuredLog.info()
                    .step("end")
                    .append(result, "page", "size", "totalElements", "totalPages")
                    .append("items", result.items().size())
                    .log();
            return result;
        } catch (RuntimeException exception) {
            meterRegistry
                    .counter("credit.settlement.statement.requests", "outcome", "failure")
                    .increment();
            StructuredLog.warn()
                    .step("error")
                    .append(filter, "assignorDocumentNumber", "paymentCurrencyCode")
                    .append("reason", exception.getMessage())
                    .log();
            throw exception;
        } finally {
            sample.stop(meterRegistry.timer("credit.settlement.statement.duration"));
        }
    }
}
