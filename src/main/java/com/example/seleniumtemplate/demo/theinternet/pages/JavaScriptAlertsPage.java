package com.example.seleniumtemplate.demo.theinternet.pages;

import com.example.seleniumtemplate.core.browser.BrowserSession;
import com.example.seleniumtemplate.core.ui.UiActions;
import com.example.seleniumtemplate.core.ui.UiWaits;
import com.example.seleniumtemplate.demo.theinternet.TheInternetSettings;
import java.net.URI;
import java.util.Objects;
import org.openqa.selenium.By;
import org.jspecify.annotations.Nullable;

public final class JavaScriptAlertsPage {

    private static final By ALERT_BUTTON = By.cssSelector("button[onclick='jsAlert()']");
    private static final By CONFIRM_BUTTON = By.cssSelector("button[onclick='jsConfirm()']");
    private static final By PROMPT_BUTTON = By.cssSelector("button[onclick='jsPrompt()']");
    private static final By RESULT = By.id("result");

    private final BrowserSession session;
    private final UiActions ui;
    private final UiWaits waits;
    private final TheInternetSettings settings;

    public JavaScriptAlertsPage(
            BrowserSession session,
            UiActions ui,
            UiWaits waits,
            TheInternetSettings settings) {
        this.session = Objects.requireNonNull(session, "session must not be null");
        this.ui = Objects.requireNonNull(ui, "ui must not be null");
        this.waits = Objects.requireNonNull(waits, "waits must not be null");
        this.settings = Objects.requireNonNull(settings, "settings must not be null");
    }

    public void open() {
        URI target = settings.baseUrl().resolve("javascript_alerts");
        session.driver().get(target.toString());
        waits.visible(ALERT_BUTTON);
    }

    public boolean isOpen() {
        return "/javascript_alerts".equals(currentPath()) && ui.count(ALERT_BUTTON) > 0;
    }

    public void triggerAlert() {
        ui.click(ALERT_BUTTON);
    }

    public void triggerConfirm() {
        ui.click(CONFIRM_BUTTON);
    }

    public void triggerPrompt() {
        ui.click(PROMPT_BUTTON);
    }

    public String result() {
        return ui.text(RESULT);
    }

    private String currentPath() {
        @Nullable String currentUrl = session.driver().getCurrentUrl();
        @Nullable String path = URI.create(Objects.requireNonNullElse(currentUrl, "")).getPath();
        return Objects.requireNonNullElse(path, "");
    }
}
