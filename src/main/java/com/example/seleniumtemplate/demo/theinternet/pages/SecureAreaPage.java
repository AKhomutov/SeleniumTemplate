package com.example.seleniumtemplate.demo.theinternet.pages;

import com.example.seleniumtemplate.core.browser.BrowserSession;
import com.example.seleniumtemplate.core.ui.UiActions;
import com.example.seleniumtemplate.core.ui.UiWaits;
import java.net.URI;
import java.util.Objects;
import org.openqa.selenium.By;
import org.jspecify.annotations.Nullable;

public final class SecureAreaPage {

    private static final By LOGOUT = By.cssSelector("a[href='/logout']");
    private static final By FLASH = By.id("flash");

    private final BrowserSession session;
    private final UiActions ui;
    private final UiWaits waits;

    public SecureAreaPage(
            BrowserSession session,
            UiActions ui,
            UiWaits waits) {
        this.session = Objects.requireNonNull(session, "session must not be null");
        this.ui = Objects.requireNonNull(ui, "ui must not be null");
        this.waits = Objects.requireNonNull(waits, "waits must not be null");
    }

    public boolean isOpen() {
        return "/secure".equals(currentPath()) && ui.count(LOGOUT) > 0;
    }

    public void logout() {
        ui.click(LOGOUT);
    }

    public String flashMessage() {
        return normalizeFlash(ui.text(FLASH));
    }

    public void waitUntilOpen() {
        waits.visible(LOGOUT);
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
