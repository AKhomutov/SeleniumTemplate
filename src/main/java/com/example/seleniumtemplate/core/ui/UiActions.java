package com.example.seleniumtemplate.core.ui;

import com.example.seleniumtemplate.core.browser.BrowserSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class UiActions {

    private final WebDriver driver;
    private final WaitPolicy waitPolicy;

    public UiActions(BrowserSession session, WaitPolicy waitPolicy) {
        Objects.requireNonNull(session, "session must not be null");
        this.driver = session.driver();
        this.waitPolicy = Objects.requireNonNull(waitPolicy, "waitPolicy must not be null");
    }

    public void click(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        newWait().until(driver -> {
            try {
                WebElement element = driver.findElement(locator);
                if (!element.isDisplayed() || !element.isEnabled()) {
                    return false;
                }
                element.click();
                return true;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        });
    }

    public void type(By locator, String text) {
        Objects.requireNonNull(locator, "locator must not be null");
        Objects.requireNonNull(text, "text must not be null");
        newWait().until(driver -> {
            try {
                WebElement element = driver.findElement(locator);
                if (!element.isDisplayed() || !element.isEnabled()) {
                    return false;
                }
                element.clear();
                element.sendKeys(text);
                return true;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        });
    }

    public void clear(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        newWait().until(driver -> {
            try {
                WebElement element = driver.findElement(locator);
                if (!element.isDisplayed() || !element.isEnabled()) {
                    return false;
                }
                element.clear();
                return true;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        });
    }

    public void selectByText(By locator, String text) {
        Objects.requireNonNull(locator, "locator must not be null");
        Objects.requireNonNull(text, "text must not be null");
        newWait().until(driver -> {
            try {
                WebElement element = driver.findElement(locator);
                if (!element.isDisplayed() || !element.isEnabled()) {
                    return false;
                }
                new Select(element).selectByVisibleText(text);
                return true;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        });
    }

    public void selectByIndex(By locator, int index) {
        Objects.requireNonNull(locator, "locator must not be null");
        if (index < 0) {
            throw new IllegalArgumentException("index must not be negative, but was: " + index);
        }
        newWait().until(driver -> {
            try {
                WebElement element = driver.findElement(locator);
                if (!element.isDisplayed() || !element.isEnabled()) {
                    return false;
                }
                new Select(element).selectByIndex(index);
                return true;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        });
    }

    public String selectedText(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        return Objects.requireNonNull(
                newWait().until(driver -> {
                    try {
                        WebElement element = driver.findElement(locator);
                        if (!element.isDisplayed()) {
                            return null;
                        }
                        return new Select(element).getFirstSelectedOption().getText();
                    } catch (StaleElementReferenceException ex) {
                        return null;
                    }
                }),
                "selectedText wait completed without a value");
    }

    public void click(By locator, int index) {
        Objects.requireNonNull(locator, "locator must not be null");
        if (index < 0) {
            throw new IllegalArgumentException("index must not be negative, but was: " + index);
        }
        newWait().until(driver -> {
            try {
                List<WebElement> elements = driver.findElements(locator);
                if (elements.size() <= index) {
                    return false;
                }
                WebElement element = elements.get(index);
                if (!element.isDisplayed() || !element.isEnabled()) {
                    return false;
                }
                element.click();
                return true;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        });
    }

    public String text(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        return Objects.requireNonNull(
                newWait().until(driver -> {
                    try {
                        return driver.findElement(locator).getText();
                    } catch (StaleElementReferenceException ex) {
                        return null;
                    }
                }),
                "text wait completed without a value");
    }

    public String value(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        return Objects.requireNonNull(
                newWait().until(driver -> {
                    try {
                        String value = driver.findElement(locator).getDomProperty("value");
                        return value == null ? "" : value;
                    } catch (StaleElementReferenceException ex) {
                        return null;
                    }
                }),
                "value wait completed without a value");
    }

    public List<String> texts(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        return Objects.requireNonNull(
                newWait().until(driver -> {
                    try {
                        List<WebElement> elements = driver.findElements(locator);
                        List<String> values = new ArrayList<>(elements.size());
                        for (WebElement element : elements) {
                            values.add(element.getText());
                        }
                        return values;
                    } catch (StaleElementReferenceException ex) {
                        return null;
                    }
                }),
                "texts wait completed without a value");
    }

    public int count(By locator) {
        Objects.requireNonNull(locator, "locator must not be null");
        return driver.findElements(locator).size();
    }

    private FluentWait<WebDriver> newWait() {
        return new WebDriverWait(driver, waitPolicy.timeout())
                .pollingEvery(waitPolicy.pollingInterval());
    }
}
