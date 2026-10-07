package com.example.seleniumtemplate.support.reporting;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.core.ApplicationManager;
import com.example.seleniumtemplate.core.browser.BrowserOptions;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UiFailureAttachments {

    private static final Logger log = LoggerFactory.getLogger(UiFailureAttachments.class);

    private UiFailureAttachments() {
    }

    static void attachIfPresent() {
        ApplicationManager app = ApplicationContext.currentOrNull();
        if (app == null) {
            return;
        }

        WebDriver driver = app.driver();
        attachScreenshot(driver);
        attachPageSource(driver);
        attachCurrentUrl(driver);
        attachBrowserMetadata(app, driver);
    }

    private static void attachScreenshot(WebDriver driver) {
        try {
            if (!(driver instanceof TakesScreenshot takesScreenshot)) {
                return;
            }
            byte @Nullable [] png = takesScreenshot.getScreenshotAs(OutputType.BYTES);
            if (png == null || png.length == 0) {
                return;
            }
            Allure.addAttachment(
                    "Screenshot",
                    "image/png",
                    new ByteArrayInputStream(png),
                    "png");
        } catch (RuntimeException ex) {
            log.warn("Screenshot attachment skipped: {}", ex.toString());
        }
    }

    private static void attachPageSource(WebDriver driver) {
        try {
            @Nullable String source = driver.getPageSource();
            if (source == null || source.isBlank()) {
                return;
            }
            Allure.addAttachment(
                    "Page source",
                    "text/html",
                    new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)),
                    "html");
        } catch (RuntimeException ex) {
            log.warn("Page source attachment skipped: {}", ex.toString());
        }
    }

    private static void attachCurrentUrl(WebDriver driver) {
        try {
            @Nullable String url = driver.getCurrentUrl();
            if (url == null) {
                return;
            }
            Allure.addAttachment("Current URL", "text/plain", url);
        } catch (RuntimeException ex) {
            log.warn("Current URL attachment skipped: {}", ex.toString());
        }
    }

    private static void attachBrowserMetadata(ApplicationManager app, WebDriver driver) {
        try {
            BrowserOptions options = app.options();
            Map<String, String> metadata = new LinkedHashMap<>();
            metadata.put("browser", options.browser().name());
            metadata.put("executionMode", options.executionMode().name());
            metadata.put("headless", Boolean.toString(options.headless()));
            @Nullable URI remoteUrl = options.remoteUrl();
            if (remoteUrl != null) {
                metadata.put("remoteUrl", remoteUrl.toString());
            }
            @Nullable String capturedSessionId = sessionId(driver);
            if (capturedSessionId != null) {
                metadata.put("sessionId", capturedSessionId);
            }
            @Nullable String capturedWindowHandle = windowHandle(driver);
            if (capturedWindowHandle != null) {
                metadata.put("windowHandle", capturedWindowHandle);
            }
            Allure.addAttachment("Browser metadata", "text/plain", metadata.toString());
        } catch (RuntimeException ex) {
            log.warn("Browser metadata attachment skipped: {}", ex.toString());
        }
    }

    private static @Nullable String sessionId(WebDriver driver) {
        try {
            if (driver instanceof RemoteWebDriver remote) {
                @Nullable SessionId id = remote.getSessionId();
                return id == null ? null : id.toString();
            }
        } catch (RuntimeException ex) {
            log.warn("Session id unavailable: {}", ex.toString());
        }
        return null;
    }

    private static @Nullable String windowHandle(WebDriver driver) {
        try {
            return driver.getWindowHandle();
        } catch (RuntimeException ex) {
            log.warn("Window handle unavailable: {}", ex.toString());
            return null;
        }
    }
}
