package br.com.srm.credit.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.srm.credit.application.batch.BatchImportResult;
import br.com.srm.credit.application.settlement.SettlementOperationItem;
import br.com.srm.credit.application.settlement.SettlementOperationPage;
import br.com.srm.credit.application.settlement.SettlementStatementItem;
import br.com.srm.credit.application.settlement.SettlementStatementPage;
import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import br.com.srm.credit.domain.shared.ExchangeRateSource;
import br.com.srm.credit.infrastructure.web.auth.LoginRequest;
import br.com.srm.credit.infrastructure.web.auth.LoginResponse;
import br.com.srm.credit.infrastructure.web.batch.BatchImportResponse;
import br.com.srm.credit.infrastructure.web.currency.CurrencyResponse;
import br.com.srm.credit.infrastructure.web.currency.ExchangeRateRequest;
import br.com.srm.credit.infrastructure.web.currency.ExchangeRateResponse;
import br.com.srm.credit.infrastructure.web.pricing.PricingSimulationRequest;
import br.com.srm.credit.infrastructure.web.pricing.PricingSimulationResponse;
import br.com.srm.credit.infrastructure.web.settlement.SettlementOperationResponse;
import br.com.srm.credit.infrastructure.web.settlement.SettlementStatementResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApiDtoValueObjectTest {

    @Test
    void shouldExposeApiRequestAndResponseRecords() {
        var timestamp = OffsetDateTime.parse("2026-06-27T10:00:00Z");

        var loginRequest = new LoginRequest("operator", "secret");
        var loginResponse = new LoginResponse("Bearer", "token", "operator", List.of("OPERATOR"), timestamp);
        var currencyResponse = new CurrencyResponse("BRL", "Real brasileiro", "R$", timestamp);
        var exchangeRateRequest = new ExchangeRateRequest(
                "USD", "BRL", new BigDecimal("5.20000000"), timestamp, ExchangeRateSource.MANUAL);
        var exchangeRateResponse = new ExchangeRateResponse(
                1L, "USD", "BRL", new BigDecimal("5.20000000"), timestamp, ExchangeRateSource.MANUAL, timestamp);
        var pricingRequest = new PricingSimulationRequest(
                "OP-1", "TRADE_RECEIVABLE", "BRL", "USD", new BigDecimal("1000.00"), new BigDecimal("0.0200"), 30);
        var pricingResponse = new PricingSimulationResponse(
                "OP-1",
                "TRADE_RECEIVABLE",
                new BigDecimal("1000.00"),
                new BigDecimal("0.0200"),
                new BigDecimal("0.0150"),
                30,
                new BigDecimal("966.18"),
                new BigDecimal("5.20000000"),
                new BigDecimal("500.00"),
                true);
        var batchResponse =
                BatchImportResponse.from(new BatchImportResult("BATCH-1", "PROCESSED", 10, 1, 1, timestamp));

        var settlementItem = new SettlementOperationItem(
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
                timestamp,
                timestamp,
                CreditAssignmentStatus.LIQUIDATED);
        var settlementResponse =
                SettlementOperationResponse.from(new SettlementOperationPage(List.of(settlementItem), 0, 10, 1, 1));
        var settlementStatementResponse = SettlementStatementResponse.from(new SettlementStatementPage(
                List.of(new SettlementStatementItem(
                        "OP-1",
                        "BATCH-1",
                        "12345678000199",
                        "Cedente ABC Ltda",
                        "BRL",
                        new BigDecimal("1000.00"),
                        new BigDecimal("966.18"),
                        timestamp,
                        "LIQUIDATED")),
                0,
                10,
                1,
                1));

        assertThat(loginRequest.username()).isEqualTo("operator");
        assertThat(loginResponse.roles()).containsExactly("OPERATOR");
        assertThat(currencyResponse.code()).isEqualTo("BRL");
        assertThat(exchangeRateRequest.source()).isEqualTo(ExchangeRateSource.MANUAL);
        assertThat(exchangeRateResponse.id()).isEqualTo(1L);
        assertThat(pricingRequest.termDays()).isEqualTo(30);
        assertThat(pricingResponse.crossCurrency()).isTrue();
        assertThat(batchResponse.status()).isEqualTo("PROCESSED");
        assertThat(settlementResponse.items()).hasSize(1);
        assertThat(settlementStatementResponse.items()).hasSize(1);
    }

    @Test
    void shouldExposeApiErrorResponseRecord() {
        var response = new ApiErrorResponse(
                OffsetDateTime.parse("2026-06-27T10:00:00Z"),
                403,
                "Forbidden",
                "Acesso negado.",
                "/api/v1/exchange-rates/4",
                "corr-1");

        assertThat(response.status()).isEqualTo(403);
        assertThat(response.path()).isEqualTo("/api/v1/exchange-rates/4");
    }
}
