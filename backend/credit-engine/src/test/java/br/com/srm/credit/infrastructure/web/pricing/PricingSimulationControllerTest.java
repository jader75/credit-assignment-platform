package br.com.srm.credit.infrastructure.web.pricing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.srm.credit.application.pricing.PricingSimulationApplicationService;
import br.com.srm.credit.application.pricing.PricingSimulationCommand;
import br.com.srm.credit.application.pricing.PricingSimulationResult;
import br.com.srm.credit.domain.pricing.PricingBusinessException;
import br.com.srm.credit.domain.pricing.PricingMessage;
import br.com.srm.credit.infrastructure.web.ApiExceptionHandler;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PricingSimulationControllerTest {

    private PricingSimulationApplicationService pricingSimulationApplicationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        pricingSimulationApplicationService = mock(PricingSimulationApplicationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PricingSimulationController(pricingSimulationApplicationService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnPricingSimulationResponse() throws Exception {
        when(pricingSimulationApplicationService.simulate(any(PricingSimulationCommand.class)))
                .thenReturn(new PricingSimulationResult(
                        "OP-001",
                        "TRADE_RECEIVABLE",
                        new BigDecimal("1000.00"),
                        new BigDecimal("0.0200"),
                        new BigDecimal("0.0150"),
                        30,
                        new BigDecimal("966.18"),
                        new BigDecimal("1.00000000"),
                        new BigDecimal("966.18"),
                        false));

        mockMvc.perform(
                        post("/api/v1/pricing/simulations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "operationReference": "OP-001",
                                  "receivableTypeCode": "UNKNOWN_RULE",
                                  "faceCurrencyCode": "BRL",
                                  "paymentCurrencyCode": "BRL",
                                  "faceAmount": 1000.00,
                                  "baseTaxRate": 0.0200,
                                  "termDays": 30
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationReference").value("OP-001"))
                .andExpect(jsonPath("$.netAmount").value(966.18));
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        mockMvc.perform(
                        post("/api/v1/pricing/simulations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "operationReference": "",
                                  "receivableTypeCode": "TRADE_RECEIVABLE",
                                  "faceCurrencyCode": "BRL",
                                  "paymentCurrencyCode": "BRL",
                                  "faceAmount": 1000.00,
                                  "baseTaxRate": 0.0200,
                                  "termDays": 30
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/pricing/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição inválido."));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenBusinessRuleFails() throws Exception {
        when(pricingSimulationApplicationService.simulate(any(PricingSimulationCommand.class)))
                .thenThrow(new PricingBusinessException(PricingMessage.PRICING_RULE_MISMATCH));

        mockMvc.perform(
                        post("/api/v1/pricing/simulations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "operationReference": "OP-001",
                                  "receivableTypeCode": "TRADE_RECEIVABLE",
                                  "faceCurrencyCode": "BRL",
                                  "paymentCurrencyCode": "BRL",
                                  "faceAmount": 1000.00,
                                  "baseTaxRate": 0.0200,
                                  "termDays": 30
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(PricingMessage.PRICING_RULE_MISMATCH.message()));
    }
}
