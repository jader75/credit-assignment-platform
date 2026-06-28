package br.com.srm.credit.infrastructure.configuration;

import br.com.srm.credit.application.currency.ExchangeRateQuoteClient;
import br.com.srm.credit.infrastructure.integration.currency.FrankfurterExchangeRateClient;
import br.com.srm.credit.infrastructure.integration.currency.RedisExchangeRateCacheClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.security.KeyStore;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@Configuration
@EnableScheduling
public class ExchangeRateIntegrationConfiguration {

    @Bean
    public ObjectMapper exchangeRateObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public RestClient frankfurterRestClient(
            @Value("${credit.exchange-rate.frankfurter.base-url:https://api.frankfurter.dev}") String baseUrl,
            @Value("${credit.exchange-rate.frankfurter.timeout-ms:3000}") int timeoutMs) {
        var requestFactory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .sslContext(buildSslContext())
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build());
        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public ExchangeRateQuoteClient exchangeRateQuoteClient(
            RestClient frankfurterRestClient,
            @Value("${credit.exchange-rate.frankfurter.retry.max-attempts:3}") int maxAttempts,
            @Value("${credit.exchange-rate.frankfurter.retry.backoff-ms:200}") long backoffMs,
            @Value("${credit.exchange-rate.frankfurter.circuit-breaker.failure-threshold:3}") int failureThreshold,
            @Value("${credit.exchange-rate.frankfurter.circuit-breaker.open-duration-ms:30000}") long openDurationMs) {
        return new FrankfurterExchangeRateClient(
                frankfurterRestClient,
                failureThreshold,
                Duration.ofMillis(openDurationMs),
                maxAttempts,
                Duration.ofMillis(backoffMs));
    }

    @Bean
    public RedisExchangeRateCacheClient redisExchangeRateCacheClient(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            @Value("${credit.exchange-rate.redis.ttl-seconds:90}") long ttlSeconds,
            @Value("${credit.exchange-rate.redis.key-prefix:credit:exchange-rate}") String keyPrefix) {
        return new RedisExchangeRateCacheClient(
                stringRedisTemplate, objectMapper, Duration.ofSeconds(ttlSeconds), keyPrefix);
    }

    private SSLContext buildSslContext() {
        try {
            var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            if (isWindows()) {
                var trustStore = KeyStore.getInstance("Windows-ROOT");
                trustStore.load(null, null);
                trustManagerFactory.init(trustStore);
            } else {
                trustManagerFactory.init((KeyStore) null);
            }
            var sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel configurar o trust store para a Frankfurter.", exception);
        }
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
