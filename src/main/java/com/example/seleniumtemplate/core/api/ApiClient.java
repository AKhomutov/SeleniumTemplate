package com.example.seleniumtemplate.core.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public final class ApiClient {

    private final ApiRequestFactory requestFactory;
    private final List<ApiRequestCustomizer> customizers;

    public ApiClient(ApiRequestFactory requestFactory, ApiRequestCustomizer... customizers) {
        this.requestFactory = Objects.requireNonNull(requestFactory, "requestFactory must not be null");
        Objects.requireNonNull(customizers, "customizers must not be null");
        this.customizers = List.of(customizers);
    }

    public Response get(String path) {
        return requireResponse(spec().get(requirePath(path)));
    }

    public Response get(String path, Map<String, ?> queryParams) {
        Objects.requireNonNull(queryParams, "queryParams must not be null");
        return requireResponse(spec().queryParams(queryParams).get(requirePath(path)));
    }

    public Response post(String path, @Nullable Object body) {
        return requireResponse(withBody(spec(), body).post(requirePath(path)));
    }

    public Response put(String path, @Nullable Object body) {
        return requireResponse(withBody(spec(), body).put(requirePath(path)));
    }

    public Response patch(String path, @Nullable Object body) {
        return requireResponse(withBody(spec(), body).patch(requirePath(path)));
    }

    public Response delete(String path) {
        return requireResponse(spec().delete(requirePath(path)));
    }

    public Response delete(String path, @Nullable Object body) {
        return requireResponse(withBody(spec(), body).delete(requirePath(path)));
    }

    private RequestSpecification spec() {
        RequestSpecification request = requestFactory.request();
        for (ApiRequestCustomizer customizer : customizers) {
            customizer.customize(request);
        }
        return request;
    }

    private static RequestSpecification withBody(RequestSpecification request, @Nullable Object body) {
        if (body == null) {
            return request;
        }
        return Objects.requireNonNull(request.body(body), "request.body returned null");
    }

    private static String requirePath(String path) {
        return Objects.requireNonNull(path, "path must not be null");
    }

    private static Response requireResponse(@Nullable Response response) {
        return Objects.requireNonNull(response, "RestAssured returned null Response");
    }
}
