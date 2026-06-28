package br.com.srm.credit.infrastructure.integration.currency;

import br.com.srm.credit.application.currency.ExchangeRateCacheClient;
import br.com.srm.credit.application.currency.ExchangeRateSnapshot;
import br.com.srm.credit.domain.shared.ExchangeRateSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisExchangeRateCacheClient implements ExchangeRateCacheClient {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;
    private final String keyPrefix;

    public RedisExchangeRateCacheClient(
            StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper, Duration ttl, String keyPrefix) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = ttl;
        this.keyPrefix = keyPrefix;
    }

    @Override
    public Optional<ExchangeRateSnapshot> get(String fromCurrencyCode, String toCurrencyCode) {
        var key = key(fromCurrencyCode, toCurrencyCode);
        var payload = stringRedisTemplate.opsForValue().get(key);
        if (payload == null || payload.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(payload, ExchangeRateSnapshot.class));
        } catch (Exception exception) {
            stringRedisTemplate.delete(key);
            return Optional.empty();
        }
    }

    @Override
    public void put(
            String fromCurrencyCode,
            String toCurrencyCode,
            BigDecimal rate,
            OffsetDateTime quotedAt,
            ExchangeRateSource source) {
        try {
            var snapshot = new ExchangeRateSnapshot(
                    normalize(fromCurrencyCode), normalize(toCurrencyCode), rate, quotedAt, source);
            var key = key(fromCurrencyCode, toCurrencyCode);
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(snapshot), ttl);
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel gravar a taxa de cambio no Redis.", exception);
        }
    }

    private String key(String fromCurrencyCode, String toCurrencyCode) {
        return keyPrefix + ":" + normalize(fromCurrencyCode) + ":" + normalize(toCurrencyCode);
    }

    private static String normalize(String currencyCode) {
        return currencyCode == null ? "" : currencyCode.trim().toUpperCase(Locale.ROOT);
    }
}
