package com.example.seleniumtemplate.core.ui;

import com.example.seleniumtemplate.core.browser.BrowserSession;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class UiWaits {

    private static final String UNOBSTRUCTED_HIT_TEST = """
            const target = arguments[0];
            const rect = target.getBoundingClientRect();
            const x = rect.left + (rect.width / 2);
            const y = rect.top + (rect.height / 2);
            const hit = document.elementFromPoint(x, y);
            return hit === target || (hit !== null && target.contains(hit));
            """;

    private final WebDriver driver;
    private final WaitPolicy waitPolicy;

    public UiWaits(BrowserSession session, WaitPolicy waitPolicy) {
        Objects.requireNonNull(session, "session must not be null");
        this.driver = session.driver();
        this.waitPolicy = Objects.requireNonNull(waitPolicy, "waitPolicy must not be null");
    }

    public WebElement present(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        return Objects.requireNonNull(
                newWait().until(driver -> driver.findElement(locator)),
                "present wait completed without an element");
    }

    public WebElement visible(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        return Objects.requireNonNull(
                newWait().until(driver -> {
                    try {
                        WebElement element = driver.findElement(locator);
                        if (!element.isDisplayed()) {
                            return null;
                        }
                        return element;
                    } catch (StaleElementReferenceException ex) {
                        return null;
                    }
                }),
                "visible wait completed without an element");
    }

    public void absent(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        newWait().until(driver -> driver.findElements(locator).isEmpty());
    }

    public void textNotEqual(By locator, String text) {
        Objects.requireNonNull(locator, "locator must not be null");
        Objects.requireNonNull(text, "text must not be null");
        newWait().until(driver -> {
            try {
                WebElement element = driver.findElement(locator);
                return !text.equals(element.getText());
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        });
    }

    public void countIs(By locator, int expectedCount) {
        Objects.requireNonNull(locator, "locator must not be null");
        if (expectedCount < 0) {
            throw new IllegalArgumentException(
                    "expectedCount must not be negative, but was: " + expectedCount);
        }
        newWait().until(driver -> driver.findElements(locator).size() == expectedCount);
    }

    public WebElement clickable(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        return Objects.requireNonNull(
                newWait().until(driver -> {
                    try {
                        WebElement element = driver.findElement(locator);
                        if (!element.isDisplayed() || !element.isEnabled()) {
                            return null;
                        }
                        return element;
                    } catch (StaleElementReferenceException ex) {
                        return null;
                    }
                }),
                "clickable wait completed without an element");
    }

    public WebElement enabled(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        return Objects.requireNonNull(
                newWait().until(driver -> {
                    try {
                        WebElement element = driver.findElement(locator);
                        if (!element.isEnabled()) {
                            return null;
                        }
                        return element;
                    } catch (StaleElementReferenceException ex) {
                        return null;
                    }
                }),
                "enabled wait completed without an element");
    }

    public WebElement disabled(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        return Objects.requireNonNull(
                newWait().until(driver -> {
                    try {
                        WebElement element = driver.findElement(locator);
                        if (element.isEnabled()) {
                            return null;
                        }
                        return element;
                    } catch (StaleElementReferenceException ex) {
                        return null;
                    }
                }),
                "disabled wait completed without an element");
    }

    public WebElement steady(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        final @Nullable Rectangle[] previous = new Rectangle[1];

        return Objects.requireNonNull(
                newWait().until(driver -> {
                    try {
                        WebElement element = driver.findElement(locator);
                        if (!element.isDisplayed()) {
                            previous[0] = null;
                            return null;
                        }

                        Rectangle current = element.getRect();
                        Rectangle last = previous[0];
                        if (last == null || !sameGeometry(last, current)) {
                            previous[0] = current;
                            return null;
                        }

                        if (!isUnobstructed(element)) {
                            return null;
                        }

                        return element;
                    } catch (StaleElementReferenceException ex) {
                        previous[0] = null;
                        return null;
                    }
                }),
                "steady wait completed without an element");
    }

    private boolean isUnobstructed(WebElement element) {
        Object result = ((JavascriptExecutor) driver).executeScript(UNOBSTRUCTED_HIT_TEST, element);
        return Boolean.TRUE.equals(result);
    }

    private static boolean sameGeometry(Rectangle left, Rectangle right) {
        return left.getX() == right.getX()
                && left.getY() == right.getY()
                && left.getWidth() == right.getWidth()
                && left.getHeight() == right.getHeight();
    }

    private FluentWait<WebDriver> newWait() {
        return new WebDriverWait(driver, waitPolicy.timeout())
                .pollingEvery(waitPolicy.pollingInterval());
    }
}
