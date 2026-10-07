package com.example.seleniumtemplate.core.browser;

import java.net.URI;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record BrowserOptions(
        BrowserType browser,
        boolean headless,
        ExecutionMode executionMode,
        @Nullable URI remoteUrl,
        @Nullable WindowSize windowSize
) {

    public BrowserOptions {
        Objects.requireNonNull(browser, "browser must not be null");
        Objects.requireNonNull(executionMode, "executionMode must not be null");

        if (executionMode == ExecutionMode.REMOTE) {
            if (remoteUrl == null) {
                throw new IllegalArgumentException(
                        "remoteUrl is required when executionMode is REMOTE");
            }
            if (remoteUrl.getScheme() == null || remoteUrl.getHost() == null) {
                throw new IllegalArgumentException(
                        "remoteUrl must be an absolute URI with scheme and host, but was: " + remoteUrl);
            }
        } else if (remoteUrl != null) {
            throw new IllegalArgumentException(
                    "remoteUrl must be null when executionMode is LOCAL, but was: " + remoteUrl);
        }
    }

    public static BrowserOptions localChrome() {
        return builder()
                .browser(BrowserType.CHROME)
                .headless(true)
                .executionMode(ExecutionMode.LOCAL)
                .windowSize(new WindowSize(1280, 800))
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private BrowserType browser = BrowserType.CHROME;
        private boolean headless = true;
        private ExecutionMode executionMode = ExecutionMode.LOCAL;
        private @Nullable URI remoteUrl;
        private @Nullable WindowSize windowSize;

        public Builder browser(BrowserType browser) {
            this.browser = Objects.requireNonNull(browser, "browser must not be null");
            return this;
        }

        public Builder headless(boolean headless) {
            this.headless = headless;
            return this;
        }

        public Builder executionMode(ExecutionMode executionMode) {
            this.executionMode = Objects.requireNonNull(executionMode, "executionMode must not be null");
            return this;
        }

        public Builder remoteUrl(@Nullable URI remoteUrl) {
            this.remoteUrl = remoteUrl;
            return this;
        }

        public Builder windowSize(@Nullable WindowSize windowSize) {
            this.windowSize = windowSize;
            return this;
        }

        public BrowserOptions build() {
            return new BrowserOptions(browser, headless, executionMode, remoteUrl, windowSize);
        }
    }
}
