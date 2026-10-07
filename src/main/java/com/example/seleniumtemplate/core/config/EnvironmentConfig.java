package com.example.seleniumtemplate.core.config;

import java.net.URI;
import java.util.Objects;

public record EnvironmentConfig(URI webBaseUrl, ApiConfig api, DatabaseConfig database) {

    public EnvironmentConfig {
        Objects.requireNonNull(webBaseUrl, "webBaseUrl must not be null");
        Objects.requireNonNull(api, "api must not be null");
        Objects.requireNonNull(database, "database must not be null");
        if (!webBaseUrl.isAbsolute()) {
            throw new IllegalArgumentException(
                    "webBaseUrl must be an absolute URI, but was: " + webBaseUrl);
        }
    }
}
