package com.example.seleniumtemplate.core.config;

import java.time.Duration;
import java.util.Objects;

public record DatabaseConfig(
        String jdbcUrl,
        String username,
        String password,
        int maximumPoolSize,
        Duration connectionTimeout) {

    public DatabaseConfig {
        Objects.requireNonNull(jdbcUrl, "jdbcUrl must not be null");
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
        Objects.requireNonNull(connectionTimeout, "connectionTimeout must not be null");

        String trimmedUrl = jdbcUrl.trim();
        if (trimmedUrl.isEmpty()) {
            throw new IllegalArgumentException("jdbcUrl must not be blank");
        }
        jdbcUrl = trimmedUrl;

        if (maximumPoolSize <= 0) {
            throw new IllegalArgumentException(
                    "maximumPoolSize must be > 0, but was: " + maximumPoolSize);
        }
        if (connectionTimeout.isZero() || connectionTimeout.isNegative()) {
            throw new IllegalArgumentException(
                    "connectionTimeout must be > 0, but was: " + connectionTimeout);
        }
    }

    @Override
    public String toString() {
        return "DatabaseConfig[jdbcUrl=" + jdbcUrl
                + ", username=" + username
                + ", password=***"
                + ", maximumPoolSize=" + maximumPoolSize
                + ", connectionTimeout=" + connectionTimeout
                + "]";
    }
}
