package br.com.srm.credit.infrastructure.web.settlement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.srm.credit.application.settlement.SettlementOperationApplicationService;
import br.com.srm.credit.application.settlement.SettlementOperationItem;
import br.com.srm.credit.application.settlement.SettlementOperationPage;
import br.com.srm.credit.application.settlement.SettlementOperationStatusCommand;
import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import br.com.srm.credit.infrastructure.web.ApiExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SettlementOperationControllerTest {

    private SettlementOperationApplicationService settlementOperationApplicationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        settlementOperationApplicationService = mock(SettlementOperationApplicationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new SettlementOperationController(settlementOperationApplicationService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnOperationsPage() throws Exception {
        when(settlementOperationApplicationService.search(any()))
                .thenReturn(new SettlementOperationPage(
                        List.of(new SettlementOperationItem(
                                "OP-001",
                                "BATCH-001",
                                "12345678000199",
                                "Alfa Comercio LTDA",
                                "TRADE_RECEIVABLE",
                                "BRL",
                                "BRL",
                                new BigDecimal("1000.00"),
                                new BigDecimal("966.18"),
                                LocalDate.parse("2026-07-20"),
                                OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                                null,
                                CreditAssignmentStatus.PRICED)),
                        0,
                        20,
                        1,
                        1));

        mockMvc.perform(get("/api/v1/settlements/operations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].operationReference").value("OP-001"))
                .andExpect(jsonPath("$.items[0].status").value("PRICED"));
    }

    @Test
    void shouldLiquidateOperation() throws Exception {
        when(settlementOperationApplicationService.updateStatus(any(SettlementOperationStatusCommand.class)))
                .thenReturn(new SettlementOperationItem(
                        "OP-001",
                        "BATCH-001",
                        "12345678000199",
                        "Alfa Comercio LTDA",
                        "TRADE_RECEIVABLE",
                        "BRL",
                        "BRL",
                        new BigDecimal("1000.00"),
                        new BigDecimal("966.18"),
                        LocalDate.parse("2026-07-20"),
                        OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                        OffsetDateTime.parse("2026-06-27T11:00:00Z"),
                        CreditAssignmentStatus.LIQUIDATED));

        mockMvc.perform(
                        patch("/api/v1/settlements/operations/OP-001/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"targetStatus":"LIQUIDATED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIQUIDATED"))
                .andExpect(jsonPath("$.liquidatedAt").exists());
    }
}
