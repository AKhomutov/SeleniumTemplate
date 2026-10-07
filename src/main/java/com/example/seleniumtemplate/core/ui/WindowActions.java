package com.example.seleniumtemplate.core.ui;

import com.example.seleniumtemplate.core.browser.BrowserSession;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class WindowActions {

    private final WebDriver driver;
    private final WaitPolicy waitPolicy;

    public WindowActions(BrowserSession session, WaitPolicy waitPolicy) {
        Objects.requireNonNull(session, "session must not be null");
        this.driver = session.driver();
        this.waitPolicy = Objects.requireNonNull(waitPolicy, "waitPolicy must not be null");
    }

    public String currentHandle() {
        return driver.getWindowHandle();
    }

    public Set<String> handles() {
        return Set.copyOf(driver.getWindowHandles());
    }

    public int count() {
        return handles().size();
    }

    public void waitForCount(int expectedCount) {
        if (expectedCount < 1) {
            throw new IllegalArgumentException(
                    "expectedCount must be >= 1, but was: " + expectedCount);
        }
        newWait().until(driver -> driver.getWindowHandles().size() == expectedCount);
    }

    public void switchTo(String handle) {
        Objects.requireNonNull(handle, "handle must not be null");
        driver.switchTo().window(handle);
    }

    public void switchToNewWindow(Set<String> previousHandles) {
        Objects.requireNonNull(previousHandles, "previousHandles must not be null");
        String newHandle = Objects.requireNonNull(
                newWait().until(driver -> findNewHandle(previousHandles)),
                "switchToNewWindow wait completed without a new handle");
        driver.switchTo().window(newHandle);
    }

    public void closeCurrent() {
        driver.close();
    }

    private @Nullable String findNewHandle(Set<String> previousHandles) {
        Set<String> current = new LinkedHashSet<>(driver.getWindowHandles());
        current.removeAll(previousHandles);
        if (current.isEmpty()) {
            return null;
        }
        return current.iterator().next();
    }

    private FluentWait<WebDriver> newWait() {
        return new WebDriverWait(driver, waitPolicy.timeout())
                .pollingEvery(waitPolicy.pollingInterval());
    }
}
