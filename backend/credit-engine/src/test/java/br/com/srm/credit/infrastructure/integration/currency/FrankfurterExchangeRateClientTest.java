package br.com.srm.credit.infrastructure.integration.currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class FrankfurterExchangeRateClientTest {

    @Test
    void shouldResolveSameCurrencyPairAndHandleRemoteResponses() {
        var builder = RestClient.builder().baseUrl("https://api.frankfurter.dev");
        var server = MockRestServiceServer.bindTo(builder).build();
        var restClient = builder.build();
        var client = new FrankfurterExchangeRateClient(restClient, 2, Duration.ofSeconds(1), 2, Duration.ZERO);

        var sameCurrency = client.fetch("brl", "BRL");
        assertThat(sameCurrency.rate()).isEqualByComparingTo("1");

        server.expect(requestTo("https://api.frankfurter.dev/v1/latest?base=USD&symbols=BRL"))
                .andRespond(withSuccess(
                        """
                        {"base":"usd","date":"2026-06-27","rates":{"BRL":5.20000000}}
                        """,
                        MediaType.APPLICATION_JSON));

        var response = client.fetch("usd", "brl");
        assertThat(response.fromCurrencyCode()).isEqualTo("USD");
        assertThat(response.toCurrencyCode()).isEqualTo("BRL");
        assertThat(response.rate()).isEqualByComparingTo("5.20000000");
        server.verify();
    }

    @Test
    void shouldRetryAndFailWhenRemotePayloadIsInvalid() {
        var builder = RestClient.builder().baseUrl("https://api.frankfurter.dev");
        var server = MockRestServiceServer.bindTo(builder).build();
        var restClient = builder.build();
        var client = new FrankfurterExchangeRateClient(restClient, 2, Duration.ofSeconds(1), 2, Duration.ZERO);

        server.expect(manyTimes(), requestTo("https://api.frankfurter.dev/v1/latest?base=USD&symbols=BRL"))
                .andRespond(withSuccess(
                        """
                        {"base":"usd","date":"2026-06-27","rates":{}}
                        """,
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetch("USD", "BRL"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Frankfurter");
        server.verify();
    }

    @Test
    void shouldRejectBlankCurrencyCodes() {
        var client = new FrankfurterExchangeRateClient(
                RestClient.builder().baseUrl("https://api.frankfurter.dev").build(),
                2,
                Duration.ofSeconds(1),
                2,
                Duration.ZERO);

        assertThatThrownBy(() -> client.fetch(" ", "BRL"))
                .isInstanceOf(br.com.srm.credit.domain.currency.CurrencyBusinessException.class);
        assertThatThrownBy(() -> client.fetch("USD", null))
                .isInstanceOf(br.com.srm.credit.domain.currency.CurrencyBusinessException.class);
    }
}
