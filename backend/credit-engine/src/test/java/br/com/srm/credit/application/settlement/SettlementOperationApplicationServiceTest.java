package br.com.srm.credit.application.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.srm.credit.domain.settlement.SettlementBusinessException;
import br.com.srm.credit.domain.settlement.SettlementMessage;
import br.com.srm.credit.domain.settlement.SettlementValidationException;
import br.com.srm.credit.domain.shared.BatchStatus;
import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import br.com.srm.credit.infrastructure.persistence.entity.AssignorEntity;
import br.com.srm.credit.infrastructure.persistence.entity.CreditAssignmentEntity;
import br.com.srm.credit.infrastructure.persistence.entity.CreditBatchEntity;
import br.com.srm.credit.infrastructure.persistence.entity.ReceivableTypeEntity;
import br.com.srm.credit.infrastructure.persistence.repository.CreditAssignmentJpaRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SettlementOperationApplicationServiceTest {

    private SettlementOperationReadRepository settlementOperationReadRepository;
    private CreditAssignmentJpaRepository creditAssignmentJpaRepository;
    private SettlementOperationApplicationService applicationService;

    @BeforeEach
    void setUp() {
        settlementOperationReadRepository = mock(SettlementOperationReadRepository.class);
        creditAssignmentJpaRepository = mock(CreditAssignmentJpaRepository.class);
        applicationService = new SettlementOperationApplicationService(
                settlementOperationReadRepository, creditAssignmentJpaRepository, new SimpleMeterRegistry());
    }

    @Test
    void shouldLiquidateOperation() {
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

        when(creditAssignmentJpaRepository.findByOperationReference("OP-001")).thenReturn(Optional.of(assignment));
        when(creditAssignmentJpaRepository.save(any(CreditAssignmentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = applicationService.updateStatus(
                new SettlementOperationStatusCommand("OP-001", CreditAssignmentStatus.LIQUIDATED));

        assertThat(result.status()).isEqualTo(CreditAssignmentStatus.LIQUIDATED);
        assertThat(result.liquidatedAt()).isNotNull();
    }

    @Test
    void shouldRejectNullAndBlankCommands() {
        assertThatThrownBy(() -> applicationService.updateStatus(null))
                .isInstanceOf(SettlementValidationException.class)
                .hasMessage(SettlementMessage.OPERATION_REFERENCE_INVALID.message());

        assertThatThrownBy(() -> applicationService.updateStatus(
                        new SettlementOperationStatusCommand("   ", CreditAssignmentStatus.LIQUIDATED)))
                .isInstanceOf(SettlementValidationException.class)
                .hasMessage(SettlementMessage.OPERATION_REFERENCE_INVALID.message());

        assertThatThrownBy(() -> applicationService.updateStatus(new SettlementOperationStatusCommand("OP-001", null)))
                .isInstanceOf(SettlementValidationException.class)
                .hasMessage(SettlementMessage.TARGET_STATUS_INVALID.message());
    }

    @Test
    void shouldRejectTransitionFromLiquidatedToOtherStatus() {
        var assignment = new CreditAssignmentEntity(
                new CreditBatchEntity("BATCH-001", BatchStatus.PROCESSED),
                new AssignorEntity("12345678000199", "Alfa Comercio LTDA", "A"),
                new ReceivableTypeEntity(
                        "TRADE_RECEIVABLE", "Duplicata Mercantil", "TRADE_RECEIVABLE", new BigDecimal("0.0150"), true),
                "OP-002",
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
                CreditAssignmentStatus.LIQUIDATED);

        when(creditAssignmentJpaRepository.findByOperationReference("OP-002")).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> applicationService.updateStatus(
                        new SettlementOperationStatusCommand("OP-002", CreditAssignmentStatus.PRICED)))
                .isInstanceOf(SettlementBusinessException.class)
                .hasMessage(SettlementMessage.OPERATION_ALREADY_LIQUIDATED.message());

        verify(creditAssignmentJpaRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenOperationDoesNotExist() {
        when(creditAssignmentJpaRepository.findByOperationReference("OP-404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.updateStatus(
                        new SettlementOperationStatusCommand("OP-404", CreditAssignmentStatus.LIQUIDATED)))
                .isInstanceOf(SettlementBusinessException.class)
                .hasMessage(SettlementMessage.OPERATION_NOT_FOUND.message());
    }

    @Test
    void shouldSearchOpenOperationsByDefault() {
        when(settlementOperationReadRepository.search(any()))
                .thenReturn(new SettlementOperationPage(List.of(), 0, 20, 0, 0));

        var result = applicationService.search(null);

        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(20);
    }

    @Test
    void shouldDefaultStatusesWhenFilterHasEmptyList() {
        var captor = ArgumentCaptor.forClass(SettlementOperationFilter.class);
        when(settlementOperationReadRepository.search(any()))
                .thenReturn(new SettlementOperationPage(List.of(), 1, 10, 0, 0));

        applicationService.search(new SettlementOperationFilter(List.of(), "BATCH-001", "12345678000199", 1, 10));

        verify(settlementOperationReadRepository).search(captor.capture());
        assertThat(captor.getValue().statuses())
                .containsExactly(CreditAssignmentStatus.PENDING, CreditAssignmentStatus.PRICED);
        assertThat(captor.getValue().batchReference()).isEqualTo("BATCH-001");
        assertThat(captor.getValue().assignorDocumentNumber()).isEqualTo("12345678000199");
    }
}
