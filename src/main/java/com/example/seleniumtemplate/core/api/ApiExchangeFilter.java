package com.example.seleniumtemplate.core.api;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Captures the latest request/response exchange per thread for failure diagnostics.
 * Sensitive headers are masked via {@link SensitiveHeaders}.
 */
public final class ApiExchangeFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ApiExchangeFilter.class);
    private static final ThreadLocal<@Nullable Exchange> LAST = new ThreadLocal<>();

    @Override
    public Response filter(
            @Nullable FilterableRequestSpecification requestSpec,
            @Nullable FilterableResponseSpecification responseSpec,
            @Nullable FilterContext ctx) {
        // RestAssured's Filter does not constrain parameter nullness; @Nullable keeps the
        // override legal under @NullMarked while we reject nulls at the boundary.
        FilterableRequestSpecification request = Objects.requireNonNull(
                requestSpec, "requestSpec must not be null");
        FilterContext context = Objects.requireNonNull(ctx, "ctx must not be null");

        Response response = Objects.requireNonNull(
                context.next(request, responseSpec),
                "RestAssured filter context returned null response");
        Exchange exchange = Exchange.from(request, response);
        LAST.set(exchange);

        if (response.statusCode() >= 500) {
            log.warn(
                    "API request failed with server error: {} {} -> {}",
                    exchange.method(),
                    exchange.uri(),
                    exchange.statusCode());
        }
        return response;
    }

    public static @Nullable Exchange lastExchange() {
        return LAST.get();
    }

    public static void clear() {
        LAST.remove();
    }

    public record Exchange(
            String method,
            String uri,
            Map<String, String> requestHeaders,
            String requestBody,
            int statusCode,
            Map<String, String> responseHeaders,
            String responseBody) {

        static Exchange from(FilterableRequestSpecification request, Response response) {
            return new Exchange(
                    Objects.requireNonNullElse(request.getMethod(), ""),
                    Objects.requireNonNull(request.getURI(), "request URI must not be null"),
                    maskHeaders(request.getHeaders()),
                    Objects.toString(request.getBody(), ""),
                    response.statusCode(),
                    maskHeaders(response.getHeaders()),
                    Objects.requireNonNullElse(response.asString(), ""));
        }

        public String summary() {
            return method + " " + uri + " -> " + statusCode
                    + " requestHeaders=" + requestHeaders
                    + " requestBody=" + requestBody
                    + " responseHeaders=" + responseHeaders
                    + " responseBody=" + responseBody;
        }

        public String render() {
            StringBuilder text = new StringBuilder();
            text.append(method).append(' ').append(uri).append('\n');
            text.append("status=").append(statusCode).append('\n');
            text.append("requestHeaders=").append(requestHeaders).append('\n');
            text.append("requestBody=").append(requestBody).append('\n');
            text.append("responseHeaders=").append(responseHeaders).append('\n');
            text.append("responseBody=").append(responseBody);
            return text.toString();
        }

        private static Map<String, String> maskHeaders(@Nullable Headers rawHeaders) {
            Map<String, String> headers = new LinkedHashMap<>();
            if (rawHeaders == null) {
                return Map.of();
            }
            List<Header> headerList = Objects.requireNonNull(
                    rawHeaders.asList(),
                    "headers list must not be null");
            for (Header header : headerList) {
                Objects.requireNonNull(header, "header must not be null");
                String name = Objects.requireNonNullElse(header.getName(), "");
                headers.put(name, SensitiveHeaders.maskedValue(header.getName(), header.getValue()));
            }
            return Map.copyOf(headers);
        }
    }
}
