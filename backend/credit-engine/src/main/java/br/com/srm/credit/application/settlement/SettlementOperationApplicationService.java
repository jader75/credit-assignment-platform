package br.com.srm.credit.application.settlement;

import br.com.srm.credit.domain.settlement.SettlementBusinessException;
import br.com.srm.credit.domain.settlement.SettlementMessage;
import br.com.srm.credit.domain.settlement.SettlementValidationException;
import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import br.com.srm.credit.domain.shared.StructuredLog;
import br.com.srm.credit.infrastructure.persistence.repository.CreditAssignmentJpaRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class SettlementOperationApplicationService {

    private final SettlementOperationReadRepository settlementOperationReadRepository;
    private final CreditAssignmentJpaRepository creditAssignmentJpaRepository;
    private final MeterRegistry meterRegistry;

    public SettlementOperationApplicationService(
            SettlementOperationReadRepository settlementOperationReadRepository,
            CreditAssignmentJpaRepository creditAssignmentJpaRepository,
            MeterRegistry meterRegistry) {
        this.settlementOperationReadRepository = settlementOperationReadRepository;
        this.creditAssignmentJpaRepository = creditAssignmentJpaRepository;
        this.meterRegistry = meterRegistry;
    }

    @Transactional(readOnly = true)
    public SettlementOperationPage search(SettlementOperationFilter filter) {
        var sample = Timer.start(meterRegistry);
        try {
            var normalized = normalizeFilter(filter);
            StructuredLog.info()
                    .step("start")
                    .append(normalized, "batchReference", "assignorDocumentNumber", "page", "size")
                    .append("statuses", normalized.statuses())
                    .log();

            var result = settlementOperationReadRepository.search(normalized);
            meterRegistry
                    .counter("credit.settlement.operations.requests", "outcome", "success")
                    .increment();
            StructuredLog.info()
                    .step("end")
                    .append(result, "page", "size", "totalElements", "totalPages")
                    .append("items", result.items().size())
                    .log();
            return result;
        } catch (RuntimeException exception) {
            meterRegistry
                    .counter("credit.settlement.operations.requests", "outcome", "failure")
                    .increment();
            StructuredLog.warn()
                    .step("error")
                    .append("reason", exception.getMessage())
                    .log();
            throw exception;
        } finally {
            sample.stop(meterRegistry.timer("credit.settlement.operations.duration"));
        }
    }

    @Transactional
    public SettlementOperationItem updateStatus(SettlementOperationStatusCommand command) {
        var sample = Timer.start(meterRegistry);
        try {
            var normalized = normalizeCommand(command);
            StructuredLog.info()
                    .step("start")
                    .append("operationReference", normalized.operationReference())
                    .append("targetStatus", normalized.targetStatus())
                    .log();

            var assignment = creditAssignmentJpaRepository
                    .findByOperationReference(normalized.operationReference())
                    .orElseThrow(() -> new SettlementBusinessException(SettlementMessage.OPERATION_NOT_FOUND));

            if (assignment.getStatus() == CreditAssignmentStatus.LIQUIDATED
                    && normalized.targetStatus() != CreditAssignmentStatus.LIQUIDATED) {
                throw new SettlementBusinessException(SettlementMessage.OPERATION_ALREADY_LIQUIDATED);
            }

            var now = OffsetDateTime.now();
            assignment.changeStatus(normalized.targetStatus(), now);
            var saved = creditAssignmentJpaRepository.save(assignment);

            meterRegistry
                    .counter("credit.settlement.operations.status.requests", "outcome", "success")
                    .increment();
            var result = map(saved);
            StructuredLog.info()
                    .step("end")
                    .append(result, "operationReference", "status", "liquidatedAt")
                    .log();
            return result;
        } catch (RuntimeException exception) {
            meterRegistry
                    .counter("credit.settlement.operations.status.requests", "outcome", "failure")
                    .increment();
            StructuredLog.warn()
                    .step("error")
                    .append("reason", exception.getMessage())
                    .log();
            throw exception;
        } finally {
            sample.stop(meterRegistry.timer("credit.settlement.operations.status.duration"));
        }
    }

    private static SettlementOperationFilter normalizeFilter(SettlementOperationFilter filter) {
        if (filter == null) {
            return new SettlementOperationFilter(
                    List.of(CreditAssignmentStatus.PENDING, CreditAssignmentStatus.PRICED), null, null, 0, 20);
        }

        var statuses = filter.statuses();
        if (statuses == null || statuses.isEmpty()) {
            statuses = List.of(CreditAssignmentStatus.PENDING, CreditAssignmentStatus.PRICED);
        }

        return new SettlementOperationFilter(
                statuses, filter.batchReference(), filter.assignorDocumentNumber(), filter.page(), filter.size());
    }

    private static SettlementOperationStatusCommand normalizeCommand(SettlementOperationStatusCommand command) {
        if (command == null
                || command.operationReference() == null
                || command.operationReference().isBlank()) {
            throw new SettlementValidationException(SettlementMessage.OPERATION_REFERENCE_INVALID);
        }
        if (command.targetStatus() == null) {
            throw new SettlementValidationException(SettlementMessage.TARGET_STATUS_INVALID);
        }
        return new SettlementOperationStatusCommand(
                command.operationReference().trim().toUpperCase(), command.targetStatus());
    }

    private static SettlementOperationItem map(
            br.com.srm.credit.infrastructure.persistence.entity.CreditAssignmentEntity entity) {
        return new SettlementOperationItem(
                entity.getOperationReference(),
                entity.getBatch().getBatchReference(),
                entity.getAssignor().getDocumentNumber(),
                entity.getAssignor().getName(),
                entity.getReceivableType().getCode(),
                entity.getFaceCurrencyCode(),
                entity.getPaymentCurrencyCode(),
                entity.getFaceAmount(),
                entity.getNetAmount(),
                entity.getDueDate(),
                entity.getPricingAt(),
                entity.getLiquidatedAt(),
                entity.getStatus());
    }
}
