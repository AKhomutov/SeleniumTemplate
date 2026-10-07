package com.example.seleniumtemplate.demo.theinternet.pages;

import com.example.seleniumtemplate.core.browser.BrowserSession;
import com.example.seleniumtemplate.core.ui.UiActions;
import com.example.seleniumtemplate.core.ui.UiWaits;
import com.example.seleniumtemplate.demo.theinternet.TheInternetSettings;
import java.net.URI;
import java.util.Objects;
import org.openqa.selenium.By;
import org.jspecify.annotations.Nullable;

public final class DropdownPage {

    private static final By DROPDOWN = By.id("dropdown");

    private final BrowserSession session;
    private final UiActions ui;
    private final UiWaits waits;
    private final TheInternetSettings settings;

    public DropdownPage(
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
        URI target = settings.baseUrl().resolve("dropdown");
        session.driver().get(target.toString());
        waits.visible(DROPDOWN);
    }

    public boolean isOpen() {
        return "/dropdown".equals(currentPath()) && ui.count(DROPDOWN) > 0;
    }

    public void selectByText(String text) {
        Objects.requireNonNull(text, "text must not be null");
        ui.selectByText(DROPDOWN, text);
    }

    public void selectByIndex(int index) {
        ui.selectByIndex(DROPDOWN, index);
    }

    public String selectedText() {
        return ui.selectedText(DROPDOWN);
    }

    private String currentPath() {
        @Nullable String currentUrl = session.driver().getCurrentUrl();
        @Nullable String path = URI.create(Objects.requireNonNullElse(currentUrl, "")).getPath();
        return Objects.requireNonNullElse(path, "");
    }
}
