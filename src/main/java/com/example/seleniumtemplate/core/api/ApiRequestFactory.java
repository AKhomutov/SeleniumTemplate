package com.example.seleniumtemplate.core.api;

import com.example.seleniumtemplate.core.config.ApiConfig;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.Objects;

public final class ApiRequestFactory {

    private static final String HTTP_CONNECTION_TIMEOUT = "http.connection.timeout";
    private static final String HTTP_SOCKET_TIMEOUT = "http.socket.timeout";

    private final ApiConfig config;
    private final RestAssuredConfig restAssuredConfig;
    private final ApiExchangeFilter exchangeFilter;

    public ApiRequestFactory(ApiConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.exchangeFilter = new ApiExchangeFilter();
        this.restAssuredConfig = buildRestAssuredConfig(config);
    }

    public RequestSpecification request() {
        RequestSpecification request = RestAssured.given();
        Objects.requireNonNull(request, "RestAssured.given() returned null RequestSpecification");
        return request.config(restAssuredConfig)
                .baseUri(config.baseUrl().toString())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filter(exchangeFilter);
    }

    private static RestAssuredConfig buildRestAssuredConfig(ApiConfig config) {
        int connectMs = toMillisInt(config.connectTimeout().toMillis(), "connectTimeout");
        int responseMs = toMillisInt(config.responseTimeout().toMillis(), "responseTimeout");

        // RestAssured 6 still expects its default AbstractHttpClient factory.
        // Timeouts are applied via HttpClient params (no custom CloseableHttpClient).
        HttpClientConfig httpClientConfig = HttpClientConfig.httpClientConfig()
                .setParam(HTTP_CONNECTION_TIMEOUT, connectMs)
                .setParam(HTTP_SOCKET_TIMEOUT, responseMs);

        RestAssuredConfig restAssuredConfig = RestAssuredConfig.config().httpClient(httpClientConfig);
        return Objects.requireNonNull(restAssuredConfig, "RestAssuredConfig must not be null");
    }

    private static int toMillisInt(long millis, String name) {
        if (millis > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(name + " is too large for HTTP client: " + millis);
        }
        return (int) millis;
    }
}
