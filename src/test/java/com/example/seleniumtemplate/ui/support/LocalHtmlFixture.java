package com.example.seleniumtemplate.ui.support;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jspecify.annotations.Nullable;

/**
 * Minimal HTML page served on the test host for deterministic browser smoke tests.
 * Binds to all interfaces so a REMOTE browser container can reach the host via
 * {@code host.docker.internal} (requires compose {@code extra_hosts} on Linux).
 */
public final class LocalHtmlFixture implements AutoCloseable {

    private static final String PAGE_PATH = "/remote-smoke.html";
    private static final String HTML = """
            <!DOCTYPE html>
            <html><head><title>Remote Smoke</title></head>
            <body><h1 id="marker">remote-smoke-ok</h1></body></html>
            """;

    private final HttpServer server;
    private final ExecutorService executor;
    private final URI hostLocalUri;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private LocalHtmlFixture(HttpServer server, ExecutorService executor, URI hostLocalUri) {
        this.server = server;
        this.executor = executor;
        this.hostLocalUri = hostLocalUri;
    }

    public static LocalHtmlFixture start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 0), 0);
            server.createContext(PAGE_PATH, LocalHtmlFixture::servePage);
            ExecutorService executor = Executors.newCachedThreadPool();
            server.setExecutor(executor);
            server.start();
            int port = server.getAddress().getPort();
            URI hostLocal = URI.create("http://127.0.0.1:" + port + PAGE_PATH);
            return new LocalHtmlFixture(server, executor, hostLocal);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to start local HTML fixture", ex);
        }
    }

    public URI hostLocalUri() {
        return hostLocalUri;
    }

    public int port() {
        return server.getAddress().getPort();
    }

    public String pagePath() {
        return PAGE_PATH;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            server.stop(0);
            executor.shutdownNow();
        }
    }

    private static void servePage(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }
        byte[] body = HTML.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    public static URI browserReachablePageUri(URI hostLocalUri, boolean remoteExecution) {
        Objects.requireNonNull(hostLocalUri, "hostLocalUri must not be null");
        if (!remoteExecution) {
            return hostLocalUri;
        }
        int port = hostLocalUri.getPort();
        if (port <= 0) {
            throw new IllegalArgumentException("hostLocalUri must include an explicit port: " + hostLocalUri);
        }
        @Nullable String rawPath = hostLocalUri.getRawPath();
        String path = (rawPath == null || rawPath.isBlank()) ? "/" : rawPath;
        return URI.create("http://host.docker.internal:" + port + path);
    }
}
