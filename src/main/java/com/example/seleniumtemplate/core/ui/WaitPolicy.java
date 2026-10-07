package com.example.seleniumtemplate.core.ui;

import java.time.Duration;
import java.util.Objects;

public record WaitPolicy(Duration timeout, Duration pollingInterval) {

    public WaitPolicy {
        Objects.requireNonNull(timeout, "timeout must not be null");
        Objects.requireNonNull(pollingInterval, "pollingInterval must not be null");
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive, but was: " + timeout);
        }
        if (pollingInterval.isZero() || pollingInterval.isNegative()) {
            throw new IllegalArgumentException(
                    "pollingInterval must be positive, but was: " + pollingInterval);
        }
    }

    public static WaitPolicy defaults() {
        return new WaitPolicy(Duration.ofSeconds(10), Duration.ofMillis(100));
    }
}
