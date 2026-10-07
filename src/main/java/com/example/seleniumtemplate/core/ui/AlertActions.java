package com.example.seleniumtemplate.core.ui;

import com.example.seleniumtemplate.core.browser.BrowserSession;
import java.util.Objects;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class AlertActions {

    private final WebDriver driver;
    private final WaitPolicy waitPolicy;

    public AlertActions(BrowserSession session, WaitPolicy waitPolicy) {
        Objects.requireNonNull(session, "session must not be null");
        this.driver = session.driver();
        this.waitPolicy = Objects.requireNonNull(waitPolicy, "waitPolicy must not be null");
    }

    public boolean isPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException ex) {
            return false;
        }
    }

    public void waitUntilPresent() {
        newWait().until(driver -> {
            driver.switchTo().alert();
            return true;
        });
    }

    public String text() {
        return currentAlert().getText();
    }

    public void accept() {
        currentAlert().accept();
    }

    public void dismiss() {
        currentAlert().dismiss();
    }

    public void type(String text) {
        Objects.requireNonNull(text, "text must not be null");
        currentAlert().sendKeys(text);
    }

    private Alert currentAlert() {
        return driver.switchTo().alert();
    }

    private FluentWait<WebDriver> newWait() {
        return new WebDriverWait(driver, waitPolicy.timeout())
                .pollingEvery(waitPolicy.pollingInterval());
    }
}
