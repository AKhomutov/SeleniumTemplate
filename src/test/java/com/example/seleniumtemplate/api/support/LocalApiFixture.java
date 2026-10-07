package com.example.seleniumtemplate.api.support;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LocalApiFixture implements AutoCloseable {

    private final HttpServer server;
    private final ExecutorService executor;
    private final URI baseUri;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private LocalApiFixture(HttpServer server, ExecutorService executor) {
        this.server = server;
        this.executor = executor;
        this.baseUri = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
    }

    public static LocalApiFixture start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/health", LocalApiFixture::health);
            server.createContext("/echo-query", LocalApiFixture::echoQuery);
            server.createContext("/echo", LocalApiFixture::echo);
            server.createContext("/whoami", LocalApiFixture::whoami);
            server.createContext("/resource/", LocalApiFixture::resource);
            ExecutorService executor = Executors.newCachedThreadPool();
            server.setExecutor(executor);
            server.start();
            return new LocalApiFixture(server, executor);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to start local API fixture", ex);
        }
    }

    public URI baseUri() {
        return baseUri;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            server.stop(0);
            executor.shutdownNow();
        }
    }

    private static void health(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain", "Method Not Allowed");
            return;
        }
        send(exchange, 200, "application/json", "{\"status\":\"ok\"}");
    }

    private static void echoQuery(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain", "Method Not Allowed");
            return;
        }
        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
        String name = query.getOrDefault("name", "");
        String body = "{\"name\":\"" + escapeJson(name) + "\"}";
        send(exchange, 200, "application/json", body);
    }

    private static void echo(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain", "Method Not Allowed");
            return;
        }
        String requestBody = readBody(exchange);
        send(exchange, 200, "application/json", requestBody);
    }

    private static void whoami(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain", "Method Not Allowed");
            return;
        }
        Headers headers = exchange.getRequestHeaders();
        List<String> values = headers.get("Authorization");
        String authorization = values == null || values.isEmpty() ? "" : values.getFirst();
        String body = "{\"authorization\":\"" + escapeJson(authorization) + "\"}";
        send(exchange, 200, "application/json", body);
    }

    private static void resource(HttpExchange exchange) throws IOException {
        if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain", "Method Not Allowed");
            return;
        }
        send(exchange, 204, null, "");
    }

    private static void send(
            HttpExchange exchange,
            int status,
            String contentType,
            String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        if (contentType != null) {
            exchange.getResponseHeaders().set("Content-Type", contentType);
        }
        if (status == 204) {
            exchange.sendResponseHeaders(status, -1);
        } else {
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        }
        exchange.close();
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> values = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return values;
        }
        for (String pair : rawQuery.split("&")) {
            int separator = pair.indexOf('=');
            if (separator < 0) {
                values.put(decode(pair), "");
            } else {
                values.put(
                        decode(pair.substring(0, separator)),
                        decode(pair.substring(separator + 1)));
            }
        }
        return values;
    }

    private static String decode(String value) {
        return java.net.URLDecoder.decode(Objects.requireNonNullElse(value, ""), StandardCharsets.UTF_8);
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
