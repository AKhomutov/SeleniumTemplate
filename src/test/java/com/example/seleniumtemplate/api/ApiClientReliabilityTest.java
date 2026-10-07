package com.example.seleniumtemplate.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.seleniumtemplate.api.support.LocalApiFixture;
import com.example.seleniumtemplate.core.api.ApiClient;
import com.example.seleniumtemplate.core.api.ApiExchangeFilter;
import com.example.seleniumtemplate.core.api.ApiRequestFactory;
import com.example.seleniumtemplate.core.config.ApiConfig;
import io.restassured.response.Response;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ApiClientReliabilityTest {

    private @Nullable LocalApiFixture fixture;
    private @Nullable ApiRequestFactory requestFactory;
    private @Nullable ApiClient api;

    @BeforeClass
    public void startFixture() {
        LocalApiFixture started = LocalApiFixture.start();
        fixture = started;
        ApiRequestFactory factory = new ApiRequestFactory(new ApiConfig(
                started.baseUri(),
                Duration.ofSeconds(3),
                Duration.ofSeconds(5)));
        requestFactory = factory;
        api = new ApiClient(factory);
    }

    @AfterClass(alwaysRun = true)
    public void stopFixture() {
        LocalApiFixture started = fixture;
        if (started != null) {
            started.close();
        }
    }

    @BeforeMethod
    public void clearExchange() {
        ApiExchangeFilter.clear();
    }

    @Test
    public void getHealthReturnsExpectedStatusAndBody() {
        Response response = api().get("/health");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("status")).isEqualTo("ok");
    }

    @Test
    public void getTransmitsQueryParams() {
        Response response = api().get("/echo-query", Map.of("name", "selenium"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("name")).isEqualTo("selenium");
    }

    @Test
    public void postSerializesRecordPayloadAsJson() {
        EchoPayload payload = new EchoPayload("alpha", 7);

        Response response = api().post("/echo", payload);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.asString()).isEqualTo("{\"name\":\"alpha\",\"count\":7}");
        assertThat(response.jsonPath().getString("name")).isEqualTo("alpha");
        assertThat(response.jsonPath().getInt("count")).isEqualTo(7);
    }

    @Test
    public void customizerTransmitsAuthorizationHeader() {
        ApiClient authorized = new ApiClient(
                requestFactory(),
                request -> request.header("Authorization", "Bearer secret-token"));

        Response response = authorized.get("/whoami");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("authorization"))
                .isEqualTo("Bearer secret-token");

        ApiExchangeFilter.Exchange exchange = Objects.requireNonNull(
                ApiExchangeFilter.lastExchange(),
                "expected captured API exchange");
        assertThat(exchange.requestHeaders().get("Authorization")).isEqualTo("***");
    }

    @Test
    public void deleteReturnsNoContent() {
        Response response = api().delete("/resource/123");

        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(response.asString()).isEmpty();
    }

    @Test
    public void baseUrlFromApiConfigIsUsed() {
        Response response = api().get("/health");
        ApiExchangeFilter.Exchange exchange = Objects.requireNonNull(
                ApiExchangeFilter.lastExchange(),
                "expected captured API exchange");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(exchange.uri()).startsWith(fixture().baseUri().toString());
    }

    @Test
    public void parallelRequestsDoNotLeakMutableState() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ConcurrentHashMap<String, String> results = new ConcurrentHashMap<>();

        try {
            executor.execute(() -> runEchoQuery(ready, start, results, "one"));
            executor.execute(() -> runEchoQuery(ready, start, results, "two"));

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            executor.shutdown();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdownNow();
        }

        assertThat(results).containsEntry("one", "one").containsEntry("two", "two");
    }

    private void runEchoQuery(
            CountDownLatch ready,
            CountDownLatch start,
            ConcurrentHashMap<String, String> results,
            String name) {
        try {
            ready.countDown();
            start.await(5, TimeUnit.SECONDS);
            Response response = api().get("/echo-query", Map.of("name", name));
            String echoed = Objects.requireNonNull(
                    response.jsonPath().getString("name"),
                    "echoed name");
            results.put(name, echoed);
        } catch (Exception ex) {
            throw new IllegalStateException("parallel request failed for " + name, ex);
        }
    }

    private LocalApiFixture fixture() {
        return Objects.requireNonNull(fixture, "fixture");
    }

    private ApiRequestFactory requestFactory() {
        return Objects.requireNonNull(requestFactory, "requestFactory");
    }

    private ApiClient api() {
        return Objects.requireNonNull(api, "api");
    }

    public record EchoPayload(String name, int count) {
    }
}
