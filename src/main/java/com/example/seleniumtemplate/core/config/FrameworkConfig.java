package com.example.seleniumtemplate.core.config;

import com.example.seleniumtemplate.core.browser.BrowserOptions;
import com.example.seleniumtemplate.core.ui.WaitPolicy;
import java.util.Objects;

public record FrameworkConfig(BrowserOptions browser, WaitPolicy waits) {

    public FrameworkConfig {
        Objects.requireNonNull(browser, "browser must not be null");
        Objects.requireNonNull(waits, "waits must not be null");
    }
}
