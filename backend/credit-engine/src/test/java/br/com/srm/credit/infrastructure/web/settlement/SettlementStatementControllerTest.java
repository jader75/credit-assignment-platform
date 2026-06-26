package br.com.srm.credit.infrastructure.web.settlement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.srm.credit.application.settlement.SettlementStatementApplicationService;
import br.com.srm.credit.application.settlement.SettlementStatementFilter;
import br.com.srm.credit.application.settlement.SettlementStatementItem;
import br.com.srm.credit.application.settlement.SettlementStatementPage;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SettlementStatementControllerTest {

    private SettlementStatementApplicationService settlementStatementApplicationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        settlementStatementApplicationService = mock(SettlementStatementApplicationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new SettlementStatementController(settlementStatementApplicationService))
                .build();
    }

    @Test
    void shouldReturnPagedSettlementStatement() throws Exception {
        when(settlementStatementApplicationService.search(any(SettlementStatementFilter.class)))
                .thenReturn(new SettlementStatementPage(
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
                        1));

        mockMvc.perform(get("/api/v1/settlements/statements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("startDate", "2026-06-26")
                        .param("endDate", "2026-06-26")
                        .param("assignorDocumentNumber", "12345678000199")
                        .param("paymentCurrencyCode", "BRL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].operationReference").value("OP-001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
