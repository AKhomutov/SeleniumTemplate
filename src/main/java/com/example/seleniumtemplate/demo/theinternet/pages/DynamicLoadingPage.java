package com.example.seleniumtemplate.demo.theinternet.pages;

import com.example.seleniumtemplate.core.browser.BrowserSession;
import com.example.seleniumtemplate.core.ui.UiActions;
import com.example.seleniumtemplate.core.ui.UiWaits;
import com.example.seleniumtemplate.demo.theinternet.TheInternetSettings;
import java.net.URI;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;

public final class DynamicLoadingPage {

    private static final By START = By.cssSelector("#start button");
    private static final By RESULT = By.id("finish");

    private final BrowserSession session;
    private final UiActions ui;
    private final UiWaits waits;
    private final TheInternetSettings settings;

    public DynamicLoadingPage(
            BrowserSession session,
            UiActions ui,
            UiWaits waits,
            TheInternetSettings settings) {
        this.session = Objects.requireNonNull(session, "session must not be null");
        this.ui = Objects.requireNonNull(ui, "ui must not be null");
        this.waits = Objects.requireNonNull(waits, "waits must not be null");
        this.settings = Objects.requireNonNull(settings, "settings must not be null");
    }

    public void openHiddenElementExample() {
        open("dynamic_loading/1");
    }

    public void openDeferredElementExample() {
        open("dynamic_loading/2");
    }

    public boolean isOpen() {
        String path = currentPath();
        return ("/dynamic_loading/1".equals(path) || "/dynamic_loading/2".equals(path))
                && ui.count(START) > 0;
    }

    public void startLoading() {
        ui.click(START);
    }

    public boolean isResultPresent() {
        return ui.count(RESULT) > 0;
    }

    public boolean isResultVisible() {
        try {
            return session.driver().findElement(RESULT).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException ex) {
            return false;
        }
    }

    public void waitUntilResultPresent() {
        waits.present(RESULT);
    }

    public void waitUntilResultVisible() {
        waits.visible(RESULT);
    }

    public String resultText() {
        return ui.text(RESULT).trim();
    }

    private void open(String relativePath) {
        URI target = settings.baseUrl().resolve(relativePath);
        session.driver().get(target.toString());
        waits.visible(START);
    }

    private String currentPath() {
        @Nullable String currentUrl = session.driver().getCurrentUrl();
        @Nullable String path = URI.create(Objects.requireNonNullElse(currentUrl, "")).getPath();
        return Objects.requireNonNullElse(path, "");
    }
}
