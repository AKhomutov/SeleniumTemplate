package com.example.seleniumtemplate.core.config;

import java.util.Objects;

public record TestConfig(FrameworkConfig framework, EnvironmentConfig environment) {

    public TestConfig {
        Objects.requireNonNull(framework, "framework must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
    }
}
