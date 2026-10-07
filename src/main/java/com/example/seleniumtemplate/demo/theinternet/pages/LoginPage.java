package com.example.seleniumtemplate.demo.theinternet.pages;

import com.example.seleniumtemplate.core.browser.BrowserSession;
import com.example.seleniumtemplate.core.ui.UiActions;
import com.example.seleniumtemplate.core.ui.UiWaits;
import com.example.seleniumtemplate.demo.theinternet.TheInternetSettings;
import java.net.URI;
import java.util.Objects;
import org.openqa.selenium.By;
import org.jspecify.annotations.Nullable;

public final class LoginPage {

    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    private static final By FLASH = By.id("flash");

    private final BrowserSession session;
    private final UiActions ui;
    private final UiWaits waits;
    private final TheInternetSettings settings;

    public LoginPage(
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
        URI target = settings.baseUrl().resolve("login");
        session.driver().get(target.toString());
    }

    public void login(String username, String password) {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
        ui.type(USERNAME, username);
        ui.type(PASSWORD, password);
        ui.click(LOGIN_BUTTON);
    }

    public boolean isOpen() {
        return "/login".equals(currentPath()) && ui.count(LOGIN_BUTTON) > 0;
    }

    public String flashMessage() {
        return normalizeFlash(ui.text(FLASH));
    }

    public void waitUntilOpen() {
        waits.visible(LOGIN_BUTTON);
    }

    public void waitForAuthenticationResult() {
        waits.visible(FLASH);
    }

    private String currentPath() {
        @Nullable String currentUrl = session.driver().getCurrentUrl();
        @Nullable String path = URI.create(Objects.requireNonNullElse(currentUrl, "")).getPath();
        return Objects.requireNonNullElse(path, "");
    }

    private static String normalizeFlash(String raw) {
        String normalized = raw.replace('\u00d7', ' ').replace('×', ' ');
        return normalized.replaceAll("\\s+", " ").trim();
    }
}
