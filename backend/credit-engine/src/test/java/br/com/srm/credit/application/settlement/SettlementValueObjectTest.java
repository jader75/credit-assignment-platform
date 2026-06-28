package br.com.srm.credit.application.settlement;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SettlementValueObjectTest {

    @Test
    void shouldExposeSettlementValueObjects() {
        var pricingAt = OffsetDateTime.parse("2026-06-27T10:00:00Z");
        var liquidatedAt = OffsetDateTime.parse("2026-06-27T11:00:00Z");
        var item = new SettlementOperationItem(
                "OP-1",
                "BATCH-1",
                "12345678000199",
                "Cedente ABC Ltda",
                "TRADE_RECEIVABLE",
                "BRL",
                "BRL",
                new BigDecimal("1000.00"),
                new BigDecimal("966.18"),
                LocalDate.parse("2026-07-20"),
                pricingAt,
                liquidatedAt,
                CreditAssignmentStatus.LIQUIDATED);
        var page = new SettlementOperationPage(List.of(item), 0, 10, 1, 1);
        var statementItem = new SettlementStatementItem(
                "OP-1",
                "BATCH-1",
                "12345678000199",
                "Cedente ABC Ltda",
                "BRL",
                new BigDecimal("1000.00"),
                new BigDecimal("966.18"),
                liquidatedAt,
                "LIQUIDATED");
        var statementPage = new SettlementStatementPage(List.of(statementItem), 0, 10, 1, 1);

        assertThat(page.items()).containsExactly(item);
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(statementPage.items()).containsExactly(statementItem);
        assertThat(statementItem.status()).isEqualTo("LIQUIDATED");
    }
}
