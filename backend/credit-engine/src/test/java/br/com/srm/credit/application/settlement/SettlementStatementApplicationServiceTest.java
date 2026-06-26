package br.com.srm.credit.application.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SettlementStatementApplicationServiceTest {

    private final SettlementStatementReadRepository settlementStatementReadRepository =
            mock(SettlementStatementReadRepository.class);
    private final SettlementStatementApplicationService applicationService =
            new SettlementStatementApplicationService(settlementStatementReadRepository);

    @Test
    void shouldDelegateSearchToRepository() {
        var filter = new SettlementStatementFilter(null, null, null, null, 0, 20);
        var page = new SettlementStatementPage(
                List.of(new SettlementStatementItem(
                        "OP-001",
                        "BATCH-001",
                        "12345678000199",
                        "Cedente ABC Ltda",
                        "BRL",
                        new BigDecimal("1000.00"),
                        new BigDecimal("966.18"),
                        OffsetDateTime.parse("2026-06-26T10:30:00-03:00"),
                        "LIQUIDATED")),
                0,
                20,
                1,
                1);

        when(settlementStatementReadRepository.search(filter)).thenReturn(page);

        var result = applicationService.search(filter);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
    }
}
