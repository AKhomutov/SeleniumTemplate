package com.example.seleniumtemplate.core.config;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

public record ApiConfig(URI baseUrl, Duration connectTimeout, Duration responseTimeout) {

    public ApiConfig {
        Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        Objects.requireNonNull(connectTimeout, "connectTimeout must not be null");
        Objects.requireNonNull(responseTimeout, "responseTimeout must not be null");

        if (!baseUrl.isAbsolute()) {
            throw new IllegalArgumentException(
                    "baseUrl must be an absolute URI, but was: " + baseUrl);
        }
        String scheme = baseUrl.getScheme();
        if (scheme == null
                || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new IllegalArgumentException(
                    "baseUrl must be an absolute http/https URI, but was: " + baseUrl);
        }
        if (baseUrl.getHost() == null) {
            throw new IllegalArgumentException(
                    "baseUrl must include a host, but was: " + baseUrl);
        }
        if (connectTimeout.isZero() || connectTimeout.isNegative()) {
            throw new IllegalArgumentException(
                    "connectTimeout must be > 0, but was: " + connectTimeout);
        }
        if (responseTimeout.isZero() || responseTimeout.isNegative()) {
            throw new IllegalArgumentException(
                    "responseTimeout must be > 0, but was: " + responseTimeout);
        }
    }
}
