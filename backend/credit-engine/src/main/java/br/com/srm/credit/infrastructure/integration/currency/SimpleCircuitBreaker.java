package br.com.srm.credit.infrastructure.integration.currency;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

final class SimpleCircuitBreaker {

    private final int failureThreshold;
    private final Duration openDuration;
    private final AtomicInteger consecutiveFailures = new AtomicInteger();
    private final AtomicReference<Instant> openUntil = new AtomicReference<>(Instant.EPOCH);

    SimpleCircuitBreaker(int failureThreshold, Duration openDuration) {
        if (failureThreshold <= 0) {
            throw new IllegalArgumentException("failureThreshold must be greater than zero");
        }
        if (openDuration == null || openDuration.isNegative() || openDuration.isZero()) {
            throw new IllegalArgumentException("openDuration must be greater than zero");
        }
        this.failureThreshold = failureThreshold;
        this.openDuration = openDuration;
    }

    void assertCallAllowed() {
        var now = Instant.now();
        var until = openUntil.get();
        if (now.isBefore(until)) {
            throw new IllegalStateException("Circuit breaker aberto para a Frankfurter.");
        }
    }

    void onSuccess() {
        consecutiveFailures.set(0);
        openUntil.set(Instant.EPOCH);
    }

    void onFailure() {
        if (consecutiveFailures.incrementAndGet() >= failureThreshold) {
            openUntil.set(Instant.now().plus(openDuration));
        }
    }
}
