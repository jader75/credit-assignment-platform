package br.com.srm.credit.infrastructure.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.srm.credit.domain.settlement.SettlementBusinessException;
import br.com.srm.credit.domain.shared.BatchStatus;
import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class CreditAssignmentEntityTest {

    @Test
    void shouldChangeStatusAndRejectInvalidTransitions() {
        var assignment = new CreditAssignmentEntity(
                new CreditBatchEntity("BATCH-001", BatchStatus.PROCESSED),
                new AssignorEntity("12345678000199", "Alfa Comercio LTDA", "A"),
                new ReceivableTypeEntity(
                        "TRADE_RECEIVABLE", "Duplicata Mercantil", "TRADE_RECEIVABLE", new BigDecimal("0.0150"), true),
                "OP-001",
                "BRL",
                new BigDecimal("1000.00"),
                LocalDate.parse("2026-07-20"),
                new BigDecimal("0.0200"),
                new BigDecimal("0.0150"),
                30,
                "BRL",
                new BigDecimal("1.00000000"),
                new BigDecimal("966.18"),
                OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                CreditAssignmentStatus.PRICED);

        assignment.changeStatus(CreditAssignmentStatus.REJECTED, OffsetDateTime.parse("2026-06-28T10:00:00Z"));
        assertThat(assignment.getStatus()).isEqualTo(CreditAssignmentStatus.REJECTED);
        assertThat(assignment.getLiquidatedAt()).isNull();

        assignment.liquidate(OffsetDateTime.parse("2026-06-28T11:00:00Z"));
        assertThat(assignment.getStatus()).isEqualTo(CreditAssignmentStatus.LIQUIDATED);
        assertThat(assignment.getLiquidatedAt()).isNotNull();

        assertThatThrownBy(() -> assignment.changeStatus(CreditAssignmentStatus.PRICED, OffsetDateTime.now()))
                .isInstanceOf(SettlementBusinessException.class);
        assertThatThrownBy(() -> assignment.changeStatus(null, OffsetDateTime.now()))
                .isInstanceOf(SettlementBusinessException.class);
    }
}
