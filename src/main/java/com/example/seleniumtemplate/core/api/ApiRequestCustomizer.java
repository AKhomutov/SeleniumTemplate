package com.example.seleniumtemplate.core.api;

import io.restassured.specification.RequestSpecification;

@FunctionalInterface
public interface ApiRequestCustomizer {

    void customize(RequestSpecification request);
}
