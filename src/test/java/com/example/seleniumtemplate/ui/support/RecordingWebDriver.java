package com.example.seleniumtemplate.ui.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Deterministic WebDriver double: first resolution path is stale, subsequent paths succeed.
 * {@link NullUnmarked}: Selenium {@link WebDriver}/{@link WebElement} are unannotated; nullness
 * overrides on every method would only produce illegal-redefinition noise under {@code @NullMarked}.
 */
@NullUnmarked
public final class RecordingWebDriver implements WebDriver {

    private final AtomicInteger findElementCalls = new AtomicInteger();
    private final AtomicInteger findElementsCalls = new AtomicInteger();
    private final AtomicInteger clickCount = new AtomicInteger();
    private final StringBuilder typedValue = new StringBuilder();
    private final List<String> successfulTexts = List.of("alpha", "beta");

    public int findElementCalls() {
        return findElementCalls.get();
    }

    public int findElementsCalls() {
        return findElementsCalls.get();
    }

    public int clickCount() {
        return clickCount.get();
    }

    public String typedValue() {
        return typedValue.toString();
    }

    @Override
    public WebElement findElement(By by) {
        int call = findElementCalls.incrementAndGet();
        if (call == 1) {
            return new StaleOnInteractElement();
        }
        return new StableElement();
    }

    @Override
    public List<WebElement> findElements(By by) {
        int call = findElementsCalls.incrementAndGet();
        if (call == 1) {
            return List.of(new StableTextElement("alpha"), new StaleOnInteractElement());
        }
        List<WebElement> elements = new ArrayList<>(successfulTexts.size());
        for (String text : successfulTexts) {
            elements.add(new StableTextElement(text));
        }
        return elements;
    }

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
        return "recording-window";
    }

    @Override
    public TargetLocator switchTo() {
        throw new UnsupportedOperationException("switchTo is not supported by RecordingWebDriver");
    }

    @Override
    public Navigation navigate() {
        throw new UnsupportedOperationException("navigate is not supported by RecordingWebDriver");
    }

    @Override
    public Options manage() {
        throw new UnsupportedOperationException("manage is not supported by RecordingWebDriver");
    }

    private final class StaleOnInteractElement implements WebElement {

        @Override
        public void click() {
            throw new StaleElementReferenceException("stale on click");
        }

        @Override
        public void submit() {
            throw new StaleElementReferenceException("stale on submit");
        }

        @Override
        public void sendKeys(CharSequence... keysToSend) {
            throw new StaleElementReferenceException("stale on sendKeys");
        }

        @Override
        public void clear() {
            throw new StaleElementReferenceException("stale on clear");
        }

        @Override
        public String getTagName() {
            return "input";
        }

        @Override
        public @Nullable String getDomProperty(String name) {
            throw new StaleElementReferenceException("stale on getDomProperty");
        }

        @Override
        public @Nullable String getDomAttribute(String name) {
            throw new StaleElementReferenceException("stale on getDomAttribute");
        }

        @Override
        public @Nullable String getAttribute(String name) {
            throw new StaleElementReferenceException("stale on getAttribute");
        }

        @Override
        public boolean isSelected() {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public String getText() {
            throw new StaleElementReferenceException("stale on getText");
        }

        @Override
        public List<WebElement> findElements(By by) {
            return List.of();
        }

        @Override
        public WebElement findElement(By by) {
            throw new UnsupportedOperationException("nested findElement is not supported");
        }

        @Override
        public boolean isDisplayed() {
            return true;
        }

        @Override
        public Point getLocation() {
            return new Point(0, 0);
        }

        @Override
        public Dimension getSize() {
            return new Dimension(10, 10);
        }

        @Override
        public Rectangle getRect() {
            return new Rectangle(0, 0, 10, 10);
        }

        @Override
        public String getCssValue(String propertyName) {
            return "";
        }

        @Override
        public <X> X getScreenshotAs(OutputType<X> target) {
            throw new UnsupportedOperationException("screenshot is not supported");
        }
    }

    private final class StableElement implements WebElement {

        @Override
        public void click() {
            clickCount.incrementAndGet();
        }

        @Override
        public void submit() {
        }

        @Override
        public void sendKeys(CharSequence... keysToSend) {
            for (CharSequence keys : keysToSend) {
                typedValue.append(keys);
            }
        }

        @Override
        public void clear() {
            typedValue.setLength(0);
        }

        @Override
        public String getTagName() {
            return "input";
        }

        @Override
        public @Nullable String getDomProperty(String name) {
            if ("value".equals(name)) {
                return typedValue.toString();
            }
            return null;
        }

        @Override
        public @Nullable String getDomAttribute(String name) {
            return getDomProperty(name);
        }

        @Override
        public @Nullable String getAttribute(String name) {
            return getDomProperty(name);
        }

        @Override
        public boolean isSelected() {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public String getText() {
            return typedValue.toString();
        }

        @Override
        public List<WebElement> findElements(By by) {
            return List.of();
        }

        @Override
        public WebElement findElement(By by) {
            throw new UnsupportedOperationException("nested findElement is not supported");
        }

        @Override
        public boolean isDisplayed() {
            return true;
        }

        @Override
        public Point getLocation() {
            return new Point(0, 0);
        }

        @Override
        public Dimension getSize() {
            return new Dimension(10, 10);
        }

        @Override
        public Rectangle getRect() {
            return new Rectangle(0, 0, 10, 10);
        }

        @Override
        public String getCssValue(String propertyName) {
            return "";
        }

        @Override
        public <X> X getScreenshotAs(OutputType<X> target) {
            throw new UnsupportedOperationException("screenshot is not supported");
        }
    }

    private static final class StableTextElement implements WebElement {

        private final String text;

        private StableTextElement(String text) {
            this.text = text;
        }

        @Override
        public void click() {
        }

        @Override
        public void submit() {
        }

        @Override
        public void sendKeys(CharSequence... keysToSend) {
        }

        @Override
        public void clear() {
        }

        @Override
        public String getTagName() {
            return "li";
        }

        @Override
        public @Nullable String getDomProperty(String name) {
            return null;
        }

        @Override
        public @Nullable String getDomAttribute(String name) {
            return null;
        }

        @Override
        public @Nullable String getAttribute(String name) {
            return null;
        }

        @Override
        public boolean isSelected() {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public List<WebElement> findElements(By by) {
            return List.of();
        }

        @Override
        public WebElement findElement(By by) {
            throw new UnsupportedOperationException("nested findElement is not supported");
        }

        @Override
        public boolean isDisplayed() {
            return true;
        }

        @Override
        public Point getLocation() {
            return new Point(0, 0);
        }

        @Override
        public Dimension getSize() {
            return new Dimension(10, 10);
        }

        @Override
        public Rectangle getRect() {
            return new Rectangle(0, 0, 10, 10);
        }

        @Override
        public String getCssValue(String propertyName) {
            return "";
        }

        @Override
        public <X> X getScreenshotAs(OutputType<X> target) {
            throw new UnsupportedOperationException("screenshot is not supported");
        }
    }
}
