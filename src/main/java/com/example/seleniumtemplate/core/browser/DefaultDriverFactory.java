package com.example.seleniumtemplate.core.browser;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public final class DefaultDriverFactory implements DriverFactory {

    @Override
    public WebDriver create(BrowserOptions options) {
        Objects.requireNonNull(options, "options must not be null");

        return switch (options.browser()) {
            case CHROME -> createChrome(options);
        };
    }

    private WebDriver createChrome(BrowserOptions options) {
        ChromeOptions chromeOptions = toChromeOptions(options);

        WebDriver driver = switch (options.executionMode()) {
            case LOCAL -> new ChromeDriver(chromeOptions);
            case REMOTE -> new RemoteWebDriver(
                    toUrl(Objects.requireNonNull(
                            options.remoteUrl(),
                            "remoteUrl must not be null for REMOTE execution")),
                    chromeOptions);
        };

        try {
            WindowSize windowSize = options.windowSize();
            if (windowSize != null) {
                driver.manage().window().setSize(
                        new Dimension(windowSize.width(), windowSize.height()));
            }
            return driver;
        } catch (RuntimeException ex) {
            try {
                driver.quit();
            } catch (RuntimeException quitEx) {
                ex.addSuppressed(quitEx);
            }
            throw ex;
        }
    }

    private static URL toUrl(URI remoteUrl) {
        try {
            return Objects.requireNonNull(remoteUrl.toURL(), "remoteUrl.toURL() returned null");
        } catch (MalformedURLException | IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "remoteUrl cannot be converted to a URL: " + remoteUrl, ex);
        }
    }

    private static ChromeOptions toChromeOptions(BrowserOptions options) {
        ChromeOptions chromeOptions = new ChromeOptions();

        if (options.headless()) {
            chromeOptions.addArguments("--headless=new");
            chromeOptions.addArguments("--disable-dev-shm-usage");
        }

        WindowSize windowSize = options.windowSize();
        if (windowSize != null) {
            chromeOptions.addArguments(
                    "--window-size=" + windowSize.width() + "," + windowSize.height());
        }

        return chromeOptions;
    }
}
