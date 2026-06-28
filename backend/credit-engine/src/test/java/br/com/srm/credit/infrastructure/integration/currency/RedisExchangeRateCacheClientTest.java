package br.com.srm.credit.infrastructure.integration.currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.srm.credit.application.currency.ExchangeRateSnapshot;
import br.com.srm.credit.domain.shared.ExchangeRateSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisExchangeRateCacheClientTest {

    @Test
    void shouldReadWriteAndDropMalformedPayloads() throws Exception {
        var redisTemplate = mock(StringRedisTemplate.class);
        var valueOperations = mock(ValueOperations.class);
        var objectMapper = new ObjectMapper().findAndRegisterModules();
        var client = new RedisExchangeRateCacheClient(
                redisTemplate, objectMapper, Duration.ofMinutes(5), "credit:exchange-rate");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        var quotedAt = OffsetDateTime.parse("2026-06-27T10:00:00Z");
        var stored = objectMapper.writeValueAsString(new ExchangeRateSnapshot(
                "USD", "BRL", new BigDecimal("5.20000000"), quotedAt, ExchangeRateSource.INTEGRATION));
        when(valueOperations.get("credit:exchange-rate:USD:BRL")).thenReturn(stored);

        var snapshot = client.get("usd", "brl");
        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().rate()).isEqualByComparingTo("5.20000000");
        client.put("usd", "brl", new BigDecimal("5.20000000"), quotedAt, ExchangeRateSource.INTEGRATION);

        when(valueOperations.get("credit:exchange-rate:USD:BRL")).thenReturn("bad-json");
        assertThat(client.get("usd", "brl")).isEmpty();
        verify(redisTemplate).delete("credit:exchange-rate:USD:BRL");
    }

    @Test
    void shouldReturnEmptyWhenPayloadMissingAndAllowNullCurrencyNormalizationOnWrite() {
        var redisTemplate = mock(StringRedisTemplate.class);
        var valueOperations = mock(ValueOperations.class);
        var objectMapper = new ObjectMapper().findAndRegisterModules();
        var client = new RedisExchangeRateCacheClient(
                redisTemplate, objectMapper, Duration.ofMinutes(5), "credit:exchange-rate");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get("credit:exchange-rate:USD:BRL")).thenReturn(null);

        assertThat(client.get("USD", "BRL")).isEmpty();
        client.put(null, "BRL", new BigDecimal("5.20000000"), OffsetDateTime.now(), ExchangeRateSource.MANUAL);
    }
}
