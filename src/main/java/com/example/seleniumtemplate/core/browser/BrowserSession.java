package com.example.seleniumtemplate.core.browser;

import java.util.Objects;
import org.openqa.selenium.WebDriver;

public final class BrowserSession {

    private final WebDriver driver;
    private boolean closed;

    public BrowserSession(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "driver must not be null");
    }

    public WebDriver driver() {
        return driver;
    }

    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        driver.quit();
    }
}
