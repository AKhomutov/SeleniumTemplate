package com.example.seleniumtemplate.core;

import com.example.seleniumtemplate.core.browser.BrowserOptions;
import com.example.seleniumtemplate.core.browser.BrowserSession;
import com.example.seleniumtemplate.core.browser.DefaultDriverFactory;
import com.example.seleniumtemplate.core.browser.DriverFactory;
import com.example.seleniumtemplate.core.ui.AlertActions;
import com.example.seleniumtemplate.core.ui.UiActions;
import com.example.seleniumtemplate.core.ui.UiWaits;
import com.example.seleniumtemplate.core.ui.WaitPolicy;
import com.example.seleniumtemplate.core.ui.WindowActions;
import java.util.Objects;
import org.openqa.selenium.WebDriver;

public final class ApplicationManager {

    private final BrowserOptions options;
    private final BrowserSession session;
    private final UiActions uiActions;
    private final UiWaits uiWaits;
    private final AlertActions alertActions;
    private final WindowActions windowActions;

    private ApplicationManager(BrowserOptions options, BrowserSession session, WaitPolicy waitPolicy) {
        this.options = options;
        this.session = session;
        this.uiActions = new UiActions(session, waitPolicy);
        this.uiWaits = new UiWaits(session, waitPolicy);
        this.alertActions = new AlertActions(session, waitPolicy);
        this.windowActions = new WindowActions(session, waitPolicy);
    }

    public static ApplicationManager start(BrowserOptions options) {
        return start(options, WaitPolicy.defaults(), new DefaultDriverFactory());
    }

    public static ApplicationManager start(BrowserOptions options, WaitPolicy waitPolicy) {
        return start(options, waitPolicy, new DefaultDriverFactory());
    }

    public static ApplicationManager start(
            BrowserOptions options,
            DriverFactory driverFactory) {
        return start(options, WaitPolicy.defaults(), driverFactory);
    }

    public static ApplicationManager start(
            BrowserOptions options,
            WaitPolicy waitPolicy,
            DriverFactory driverFactory) {
        Objects.requireNonNull(options, "options must not be null");
        Objects.requireNonNull(waitPolicy, "waitPolicy must not be null");
        Objects.requireNonNull(driverFactory, "driverFactory must not be null");

        WebDriver driver = driverFactory.create(options);
        return new ApplicationManager(options, new BrowserSession(driver), waitPolicy);
    }

    public void stop() {
        session.close();
    }

    public BrowserOptions options() {
        return options;
    }

    public BrowserSession session() {
        return session;
    }

    public WebDriver driver() {
        return session.driver();
    }

    public UiActions ui() {
        return uiActions;
    }

    public UiWaits waits() {
        return uiWaits;
    }

    public AlertActions alerts() {
        return alertActions;
    }

    public WindowActions windows() {
        return windowActions;
    }
}
