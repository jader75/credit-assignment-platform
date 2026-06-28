package br.com.srm.credit.infrastructure.integration.currency;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class SimpleCircuitBreakerTest {

    @Test
    void shouldRejectInvalidConstructorArguments() {
        assertThatThrownBy(() -> new SimpleCircuitBreaker(0, Duration.ofSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SimpleCircuitBreaker(1, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldOpenAfterConfiguredNumberOfFailuresAndResetAfterSuccess() {
        var breaker = new SimpleCircuitBreaker(2, Duration.ofMinutes(1));

        breaker.assertCallAllowed();
        breaker.onFailure();
        breaker.assertCallAllowed();
        breaker.onFailure();

        assertThatThrownBy(breaker::assertCallAllowed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Circuit breaker aberto para a Frankfurter.");

        breaker.onSuccess();
        breaker.assertCallAllowed();
    }
}
