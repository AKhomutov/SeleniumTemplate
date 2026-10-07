package com.example.seleniumtemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.seleniumtemplate.core.ApplicationContext;
import com.example.seleniumtemplate.core.ApplicationManager;
import com.example.seleniumtemplate.core.browser.BrowserOptions;
import com.example.seleniumtemplate.core.browser.DriverFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ApplicationContextBindTest {

    private static final DriverFactory STUB_DRIVER_FACTORY = _ -> new StubWebDriver();

    @AfterMethod(alwaysRun = true)
    public void clearContext() {
        ApplicationContext.clear();
    }

    @Test
    public void bindFailsFastWhenApplicationManagerAlreadyBoundOnThread() {
        ApplicationManager first = ApplicationManager.start(BrowserOptions.localChrome(), STUB_DRIVER_FACTORY);
        ApplicationManager second = ApplicationManager.start(BrowserOptions.localChrome(), STUB_DRIVER_FACTORY);

        try {
            ApplicationContext.bind(first);

            assertThatThrownBy(() -> ApplicationContext.bind(second))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("ApplicationManager is already bound to the current test thread");
        } finally {
            first.stop();
            second.stop();
        }
    }

    @NullUnmarked
    private static final class StubWebDriver implements WebDriver {

        @Override
        public void get(String url) {
        }

        @Override
        public @Nullable String getCurrentUrl() {
            return "about:blank";
        }

        @Override
        public @Nullable String getTitle() {
            return "";
        }

        @Override
        public List<WebElement> findElements(By by) {
            return List.of();
        }

        @Override
        public WebElement findElement(By by) {
            throw new UnsupportedOperationException("StubWebDriver does not support findElement");
        }

        @Override
        public @Nullable String getPageSource() {
            return "";
        }

        @Override
        public void close() {
        }

        @Override
        public void quit() {
        }

        @Override
        public Set<String> getWindowHandles() {
            return Collections.emptySet();
        }

        @Override
        public String getWindowHandle() {
            return "stub-window";
        }

        @Override
        public TargetLocator switchTo() {
            throw new UnsupportedOperationException("StubWebDriver does not support switchTo");
        }

        @Override
        public Navigation navigate() {
            throw new UnsupportedOperationException("StubWebDriver does not support navigate");
        }

        @Override
        public Options manage() {
            throw new UnsupportedOperationException("StubWebDriver does not support manage");
        }
    }
}
