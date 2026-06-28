package br.com.srm.credit.infrastructure.integration.currency;

import br.com.srm.credit.application.currency.ExchangeRateQuote;
import br.com.srm.credit.application.currency.ExchangeRateQuoteClient;
import br.com.srm.credit.domain.currency.CurrencyBusinessException;
import br.com.srm.credit.domain.currency.CurrencyMessage;
import br.com.srm.credit.domain.shared.StructuredLog;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

public class FrankfurterExchangeRateClient implements ExchangeRateQuoteClient {

    private final RestClient restClient;
    private final SimpleCircuitBreaker circuitBreaker;
    private final int maxAttempts;
    private final Duration retryBackoff;

    public FrankfurterExchangeRateClient(
            RestClient restClient,
            int circuitBreakerFailureThreshold,
            Duration circuitBreakerOpenDuration,
            int maxAttempts,
            Duration retryBackoff) {
        this.restClient = restClient;
        this.circuitBreaker = new SimpleCircuitBreaker(circuitBreakerFailureThreshold, circuitBreakerOpenDuration);
        this.maxAttempts = maxAttempts;
        this.retryBackoff = retryBackoff;
    }

    @Override
    public ExchangeRateQuote fetch(String fromCurrencyCode, String toCurrencyCode) {
        var from = normalize(fromCurrencyCode);
        var to = normalize(toCurrencyCode);
        if (from.equals(to)) {
            return new ExchangeRateQuote(from, to, BigDecimal.ONE, OffsetDateTime.now(ZoneOffset.UTC));
        }

        RuntimeException lastFailure = null;
        for (var attempt = 1; attempt <= maxAttempts; attempt++) {
            circuitBreaker.assertCallAllowed();
            try {
                StructuredLog.info()
                        .step("exchange-rate-integration-attempt")
                        .append("provider", "frankfurter")
                        .append("fromCurrencyCode", from)
                        .append("toCurrencyCode", to)
                        .append("attempt", attempt)
                        .log();

                var response = restClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/v1/latest")
                                .queryParam("base", from)
                                .queryParam("symbols", to)
                                .build())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(FrankfurterLatestResponse.class);
                var quote = toQuote(response, from, to);
                circuitBreaker.onSuccess();
                StructuredLog.info()
                        .step("exchange-rate-integration-success")
                        .append("provider", "frankfurter")
                        .append("fromCurrencyCode", from)
                        .append("toCurrencyCode", to)
                        .append("attempt", attempt)
                        .append("rate", quote.rate())
                        .append("quotedAt", quote.quotedAt())
                        .log();
                return quote;
            } catch (RuntimeException exception) {
                lastFailure = exception;
                circuitBreaker.onFailure();
                StructuredLog.warn()
                        .step("exchange-rate-integration-failure")
                        .append("provider", "frankfurter")
                        .append("fromCurrencyCode", from)
                        .append("toCurrencyCode", to)
                        .append("attempt", attempt)
                        .append("reason", exception.getMessage())
                        .log();
                if (attempt == maxAttempts) {
                    throw new IllegalStateException("Nao foi possivel consultar a Frankfurter.", exception);
                }
                StructuredLog.warn()
                        .step("exchange-rate-integration-retry")
                        .append("provider", "frankfurter")
                        .append("fromCurrencyCode", from)
                        .append("toCurrencyCode", to)
                        .append("nextAttempt", attempt + 1)
                        .append("backoffMs", retryBackoff.toMillis())
                        .log();
                sleepBeforeRetry();
            }
        }

        throw new IllegalStateException("Nao foi possivel consultar a Frankfurter.", lastFailure);
    }

    private void sleepBeforeRetry() {
        if (retryBackoff.isZero() || retryBackoff.isNegative()) {
            return;
        }
        try {
            Thread.sleep(retryBackoff.toMillis());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Falha ao aguardar a nova tentativa da cotacao.", interruptedException);
        }
    }

    private static ExchangeRateQuote toQuote(FrankfurterLatestResponse response, String from, String to) {
        if (response == null) {
            throw new IllegalStateException("Resposta invalida da Frankfurter.");
        }

        var rate = response.rates() == null ? null : response.rates().get(to);
        if (rate == null || rate.signum() <= 0) {
            throw new IllegalStateException("Cotacao nao encontrada na resposta da Frankfurter.");
        }

        var quotedAt = response.date() == null
                ? OffsetDateTime.now(ZoneOffset.UTC)
                : response.date().atStartOfDay().atOffset(ZoneOffset.UTC);
        return new ExchangeRateQuote(
                response.base() == null ? from : response.base().toUpperCase(Locale.ROOT), to, rate, quotedAt);
    }

    private static String normalize(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new CurrencyBusinessException(CurrencyMessage.CURRENCY_CODE_INVALID);
        }
        return currencyCode.trim().toUpperCase(Locale.ROOT);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FrankfurterLatestResponse(String base, LocalDate date, Map<String, BigDecimal> rates) {}
}
